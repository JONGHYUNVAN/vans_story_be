package blog.vans_story_be.domain.auth.service

import blog.vans_story_be.domain.auth.dto.LoginRequest
import blog.vans_story_be.domain.auth.dto.TokenPair
import blog.vans_story_be.domain.auth.jwt.JwtProvider
import blog.vans_story_be.global.exception.CustomException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스입니다.
 * 
 * <p>로그인, 토큰 갱신, Refresh Token 관리 등의 인증 관련 기능을 제공합니다.
 * JWT 기반의 인증을 사용하며, 액세스 토큰과 리프레시 토큰을 모두 관리합니다.</p>
 * 
 * <h4>주요 기능:</h4>
 * <ul>
 *   <li>사용자 로그인 및 토큰 발급</li>
 *   <li>토큰 갱신 (Refresh Token 사용)</li>
 *   <li>로그아웃 처리</li>
 * </ul>
 * 
 * <h4>사용 예시:</h4>
 * <pre>
 * // 로그인 처리
 * authService.login(loginRequest, response)
 * 
 * // 토큰 갱신
 * authService.refresh(refreshToken, response)
 * 
 * // 로그아웃
 * authService.logout(response)
 * </pre>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see JwtProvider
 * @see LoginRequest
 */
@Service
@Transactional
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtProvider: JwtProvider
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val REFRESH_TOKEN_COOKIE_NAME = "refreshToken"
        private const val BEARER_PREFIX = "Bearer "
    }

    /**
     * 사용자 로그인을 처리하고 JWT 토큰을 발급합니다.
     * 
     * <p>사용자 인증 후 액세스 토큰과 리프레시 토큰을 발급하며,
     * 액세스 토큰은 Authorization 헤더에, 리프레시 토큰은 쿠키에 저장됩니다.</p>
     * 
     * @param loginRequest 로그인 요청 정보 (이메일, 비밀번호)
     * @param response HTTP 응답 객체
     * @throws BadCredentialsException 인증 실패 시
     */
    fun login(loginRequest: LoginRequest, response: HttpServletResponse) {
        logger.info { "로그인 시도: ${loginRequest.email}" }
        runCatching {
            val authentication = authenticateUser(loginRequest)
            generateAndSetTokens(authentication, response)
            logger.info { "사용자 로그인 성공: ${authentication.name}" }
        }.onFailure { e ->
            logger.error(e) { "로그인 실패: ${loginRequest.email}, 오류: ${e.message}" }
            throw when (e) {
                is BadCredentialsException -> {
                    logger.error { "인증 실패 - 잘못된 자격 증명: ${loginRequest.email}" }
                    CustomException("이메일 또는 비밀번호가 올바르지 않습니다.")
                }
                else -> {
                    logger.error(e) { "로그인 처리 중 예상치 못한 오류: ${e::class.simpleName}" }
                    CustomException("로그인 처리 중 오류가 발생했습니다.")
                }
            }
        }
    }

    /**
     * Refresh Token을 검증하고 새로운 토큰을 발급합니다.
     * 
     * <p>기존 Refresh Token의 유효성을 검증한 후,
     * 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.</p>
     * 
     * @param refreshToken 갱신에 사용할 Refresh Token
     * @param response HTTP 응답 객체
     * @throws CustomException 토큰이 유효하지 않거나 사용자 정보가 일치하지 않는 경우
     */
    fun refresh(refreshToken: String, response: HttpServletResponse) {
        runCatching {
            val authentication = validateAndGetAuthentication(refreshToken)
            generateAndSetTokens(authentication, response)
            logger.info { "토큰 갱신 성공: ${authentication.name}" }
        }.onFailure { e ->
            logger.error(e) { "토큰 갱신 실패" }
            throw CustomException("토큰 갱신에 실패했습니다.")
        }
    }

    /**
     * 로그아웃을 처리합니다.
     * 
     * <p>Refresh Token 쿠키를 만료시켜 로그아웃을 처리합니다.</p>
     * 
     * @param response HTTP 응답 객체
     */
    fun logout(response: HttpServletResponse) {
        response.addCookie(createExpiredCookie())
        logger.info { "사용자 로그아웃 처리 완료" }
    }

    // Private helper functions

    /**
     * 사용자 인증을 수행합니다.
     *
     * 처리 과정:
     * 1. 이메일과 비밀번호로 인증 토큰 생성
     * 2. AuthenticationManager를 통해 실제 인증 수행
     *
     * @param loginRequest 로그인 요청 정보
     * @return 인증된 사용자의 Authentication 객체
     * @throws BadCredentialsException 인증 실패 시
     */
    private fun authenticateUser(loginRequest: LoginRequest): Authentication {
        logger.info { "인증 토큰 생성: ${loginRequest.email}" }
        val authenticationToken = UsernamePasswordAuthenticationToken(
            loginRequest.email,
            loginRequest.password
        )
        logger.info { "AuthenticationManager를 통한 인증 시작" }
        return authenticationManager.authenticate(authenticationToken)
    }

    /**
     * Refresh Token을 검증하고 인증 정보를 추출합니다.
     *
     * 처리 과정:
     * 1. Refresh Token 유효성 검증
     * 2. 토큰에서 인증 정보 추출
     *
     * @param refreshToken 검증할 Refresh Token
     * @return 인증된 사용자의 Authentication 객체
     * @throws CustomException 토큰이 유효하지 않은 경우
     */
    private fun validateAndGetAuthentication(refreshToken: String): Authentication {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw CustomException("Refresh Token이 유효하지 않습니다.")
        }
        return jwtProvider.getAuthentication(refreshToken)
    }

    /**
     * 새로운 토큰을 생성하고 HTTP 응답에 설정합니다.
     *
     * 처리 과정:
     * 1. Access Token과 Refresh Token 생성
     * 2. Access Token을 Authorization 헤더에 설정
     * 3. Refresh Token을 쿠키에 설정
     *
     * @param authentication 인증된 사용자 정보
     * @param response HTTP 응답 객체
     */
    private fun generateAndSetTokens(authentication: Authentication, response: HttpServletResponse) {
        val accessToken = jwtProvider.generateAccessToken(authentication)
        val refreshToken = jwtProvider.generateRefreshToken(authentication)
        
        response.setHeader("Authorization", BEARER_PREFIX + accessToken)
        response.addCookie(createRefreshTokenCookie(refreshToken))
    }

    /**
     * Refresh Token을 저장할 쿠키를 생성합니다.
     *
     * 보안 설정:
     * - HttpOnly: JavaScript에서 쿠키 접근 불가
     * - Secure: HTTPS에서만 전송
     * - Path: 루트 경로에서만 접근 가능
     *
     * @param refreshToken 저장할 Refresh Token
     * @return 설정된 쿠키 객체
     */
    private fun createRefreshTokenCookie(refreshToken: String): Cookie =
        Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken).apply {
            isHttpOnly = true
            secure = true
            path = "/"
        }

    /**
     * 만료된 쿠키를 생성하여 로그아웃 처리를 합니다.
     *
     * 처리 과정:
     * 1. 빈 값의 쿠키 생성
     * 2. 보안 설정 적용
     * 3. MaxAge를 0으로 설정하여 즉시 만료
     *
     * @return 만료 설정된 쿠키 객체
     */
    private fun createExpiredCookie(): Cookie =
        Cookie(REFRESH_TOKEN_COOKIE_NAME, "").apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 0
        }
}

/**
 * HttpServletResponse의 확장 함수들입니다.
 */
private fun HttpServletResponse.addCookie(cookie: Cookie) {
    this.addCookie(cookie)
}

/**
 * 인증 결과를 나타내는 시일드 클래스입니다.
 */
sealed class AuthResult {
    data class Success(val tokenPair: TokenPair) : AuthResult()
    data class Failure(val exception: Exception) : AuthResult()
} 