package blog.vans_story_be.domain.auth.service

import blog.vans_story_be.domain.auth.dto.LoginRequest
import blog.vans_story_be.domain.auth.dto.toLogString
import blog.vans_story_be.domain.auth.jwt.JwtProperties
import blog.vans_story_be.domain.auth.jwt.JwtProvider
import blog.vans_story_be.domain.auth.jwt.TokenType
import blog.vans_story_be.domain.auth.repository.RefreshTokenRepository
import blog.vans_story_be.domain.user.repository.UserRepository
import blog.vans_story_be.global.exception.CustomException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스.
 *
 * - 로그인: BCrypt 인증 → Access/Refresh Token 발급 → Refresh Token 해시 DB 저장
 * - 갱신: Refresh Token DB 존재 확인 → JWT 검증 → 토큰 회전(rotation)
 * - 로그아웃: DB에서 Refresh Token 삭제 → 쿠키 만료
 */
@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    @param:Qualifier("bcryptExecutor") private val bcryptExecutor: ExecutorService
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val REFRESH_TOKEN_COOKIE_NAME = "refreshToken"
        private const val BEARER_PREFIX = "Bearer "
        private const val REFRESH_COOKIE_PATH = "/api/v1/auth/refresh"
    }

    /**
     * 사용자 로그인 처리. BCrypt를 전용 스레드 풀에서 오프로드.
     */
    fun login(loginRequest: LoginRequest, response: HttpServletResponse) {
        runCatching {
            val authentication = CompletableFuture
                .supplyAsync({ authenticateUser(loginRequest) }, bcryptExecutor)
                .get()
            generateAndSetTokens(authentication, response)
        }.onFailure { e ->
            val cause = if (e is ExecutionException) e.cause ?: e else e
            throw when (cause) {
                is BadCredentialsException -> {
                    logger.warn { "인증 실패 - 잘못된 자격 증명: ${loginRequest.toLogString()}" }
                    CustomException("이메일 또는 비밀번호가 올바르지 않습니다.")
                }
                else -> {
                    logger.error(cause) { "로그인 처리 중 예상치 못한 오류: ${cause::class.simpleName}" }
                    CustomException("로그인 처리 중 오류가 발생했습니다.")
                }
            }
        }
    }

    /**
     * Refresh Token 검증 후 새 토큰 발급 (토큰 회전).
     * DB 존재 확인 → JWT 서명/만료 검증 → 기존 삭제 → 신규 저장.
     */
    fun refresh(refreshToken: String, response: HttpServletResponse) {
        runCatching {
            val tokenHash = hashToken(refreshToken)
            val userId = refreshTokenRepository.findUserIdByTokenHash(tokenHash)
                ?: throw CustomException("Refresh Token이 서버에 존재하지 않습니다.")

            val authentication = validateAndGetAuthentication(refreshToken)

            // 기존 토큰 삭제 후 신규 발급 (rotation)
            refreshTokenRepository.deleteByUserId(userId)
            generateAndSetTokens(authentication, response)

            logger.info { "토큰 갱신 성공: ${maskEmail(authentication.name)}" }
        }.onFailure { e ->
            logger.warn(e) { "토큰 갱신 실패" }
            throw CustomException("토큰 갱신에 실패했습니다.")
        }
    }

    /**
     * 로그아웃 처리. Refresh Token을 DB에서 삭제하고 쿠키를 만료시킵니다.
     */
    fun logout(refreshToken: String?, response: HttpServletResponse) {
        if (refreshToken != null) {
            runCatching {
                val tokenHash = hashToken(refreshToken)
                val userId = refreshTokenRepository.findUserIdByTokenHash(tokenHash)
                if (userId != null) {
                    refreshTokenRepository.deleteByUserId(userId)
                    logger.info { "Refresh Token DB 삭제 완료 (userId=$userId)" }
                }
            }.onFailure { e ->
                logger.warn(e) { "로그아웃 중 토큰 삭제 실패 (무시하고 계속)" }
            }
        }
        response.addHeader("Set-Cookie", createExpiredResponseCookie().toString())
        logger.info { "사용자 로그아웃 처리 완료" }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun authenticateUser(loginRequest: LoginRequest): Authentication {
        logger.info { "인증 시작: ${loginRequest.toLogString()}" }
        val token = UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password)
        return authenticationManager.authenticate(token)
    }

    /**
     * Refresh Token의 서명·만료·typ 검증.
     * typ 없는 토큰은 typ 도입 전 발급분으로 보고 허용한다 — 호출 전에 DB 해시 확인을 통과했으므로
     * Access Token이 섞여 들어올 수 없다. 배포 + Refresh 유효기간(7일)이 지나면 allowMissingType을 제거한다.
     */
    private fun validateAndGetAuthentication(refreshToken: String): Authentication {
        if (!jwtProvider.validateToken(refreshToken, TokenType.REFRESH, allowMissingType = true)) {
            throw CustomException("Refresh Token이 유효하지 않습니다.")
        }
        return jwtProvider.getAuthentication(refreshToken)
    }

    /**
     * Access/Refresh Token 생성 후 응답에 설정.
     * Refresh Token 해시를 DB에 저장 (기존 세션 무효화 후 저장).
     */
    private fun generateAndSetTokens(authentication: Authentication, response: HttpServletResponse) {
        val accessToken = jwtProvider.generateAccessToken(authentication)
        val refreshToken = jwtProvider.generateRefreshToken(authentication)

        // userId 조회 (JWT subject = email)
        // transaction{}은 UserRepositoryImpl 내부에서 처리됨
        val email = authentication.name
        val userId = userRepository.findByEmail(email)
            .map { it.id.value }
            .orElseThrow { CustomException("사용자를 찾을 수 없습니다.") }

        // 기존 세션 무효화 후 신규 토큰 저장
        val tokenHash = hashToken(refreshToken)
        val expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenValidityInSeconds)
        refreshTokenRepository.deleteByUserId(userId)
        refreshTokenRepository.save(userId, tokenHash, expiresAt)

        response.setHeader("Authorization", BEARER_PREFIX + accessToken)
        response.addHeader("Set-Cookie", createRefreshTokenResponseCookie(refreshToken).toString())
    }

    /**
     * Refresh Token 쿠키 생성 (ResponseCookie, SameSite=Strict).
     * path=/api/v1/auth/refresh 로 제한하여 다른 요청에 쿠키 미전송.
     */
    private fun createRefreshTokenResponseCookie(refreshToken: String): ResponseCookie =
        ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path(REFRESH_COOKIE_PATH)
            .maxAge(Duration.ofSeconds(jwtProperties.refreshTokenValidityInSeconds))
            .build()

    /**
     * 만료된 Refresh Token 쿠키 생성.
     */
    private fun createExpiredResponseCookie(): ResponseCookie =
        ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path(REFRESH_COOKIE_PATH)
            .maxAge(Duration.ZERO)
            .build()

    /**
     * SHA-256 해시 (64자 hex 문자열).
     */
    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(token.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * 이메일 마스킹: 앞 3자리 + *** + @도메인
     */
    private fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        if (atIndex <= 3) return "***@***"
        return email.take(3) + "***" + email.substring(atIndex)
    }
}
