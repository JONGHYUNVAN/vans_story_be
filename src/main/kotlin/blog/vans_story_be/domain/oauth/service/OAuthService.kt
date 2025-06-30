package blog.vans_story_be.domain.oauth.service

import blog.vans_story_be.domain.auth.jwt.JwtProvider
import blog.vans_story_be.domain.oauth.dto.OAuthDto
import blog.vans_story_be.domain.oauth.mapper.OAuthMapper
import blog.vans_story_be.domain.oauth.repository.OAuthRepository
import blog.vans_story_be.domain.user.entity.User
import blog.vans_story_be.global.exception.CustomException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * OAuth 관련 비즈니스 로직을 처리하는 서비스
 *
 * 주요 기능:
 * - OAuth 로그인 처리 (기존 연동된 계정만 가능)
 * - OAuth 계정 연결/해제
 * - 연결된 계정 조회
 *
 * 참고: 
 * - OAuth 로그인은 사전에 link를 통해 연동을 완료한 계정만 가능합니다.
 * - 새로운 OAuth 계정으로 자동 가입은 지원하지 않습니다.
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.01.09
 */
@Service
@Transactional
class OAuthService(
    private val oauthRepository: OAuthRepository,
    private val oauthMapper: OAuthMapper,
    private val jwtProvider: JwtProvider
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val REFRESH_TOKEN_COOKIE_NAME = "refreshToken"
        private const val BEARER_PREFIX = "Bearer "
        private const val TEMP_CODE_EXPIRY_MINUTES = 5L
    }

    // 임시 코드 저장소 (실제 운영환경에서는 Redis 등 외부 저장소 사용 권장)
    private val tempCodeStorage = ConcurrentHashMap<String, TempCodeData>()

    /**
     * 임시 코드 데이터 클래스
     */
    private data class TempCodeData(
        val provider: String,
        val providerId: String,
        val expiryTime: LocalDateTime
    )

    /**
     * OAuth 로그인 시 임시 코드를 발급합니다.
     */
    fun oauthLogin(loginRequest: OAuthDto.LoginRequest): OAuthDto.CodeResponse {
        logger.info { "OAuth 임시 코드 발급 시작 - provider: ${loginRequest.provider}, providerId: ${loginRequest.providerId}" }

        // 임시 코드 생성
        val tempCode = generateTempCode()
        
        // 임시 코드 저장 (5분 후 만료)
        val expiryTime = LocalDateTime.now().plusMinutes(TEMP_CODE_EXPIRY_MINUTES)
        tempCodeStorage[tempCode] = TempCodeData(
            provider = loginRequest.provider,
            providerId = loginRequest.providerId,
            expiryTime = expiryTime
        )

        // 만료된 코드 정리
        cleanupExpiredCodes()

        logger.info { "OAuth 임시 코드 발급 완료 - code: ${tempCode.take(8)}..." }
        return OAuthDto.CodeResponse(tempCode)
    }

    /**
     * 임시 코드를 JWT 토큰으로 교환합니다.
     */
    fun exchangeCodeForToken(exchangeRequest: OAuthDto.ExchangeRequest, response: HttpServletResponse) {
        logger.info { "OAuth 코드 교환 시작 - code: ${exchangeRequest.code.take(8)}..." }

        // 임시 코드 검증
        val tempCodeData = tempCodeStorage[exchangeRequest.code]
            ?: throw CustomException("유효하지 않은 인증 코드입니다.")

        // 코드 만료 확인
        if (LocalDateTime.now().isAfter(tempCodeData.expiryTime)) {
            tempCodeStorage.remove(exchangeRequest.code)
            throw CustomException("만료된 인증 코드입니다.")
        }

        // 코드 사용 후 즉시 삭제 (일회용)
        tempCodeStorage.remove(exchangeRequest.code)

        // 기존 OAuth 연동 정보 확인
        val existingOAuth = oauthRepository.findByProviderAndProviderId(
            tempCodeData.provider, 
            tempCodeData.providerId
        ) ?: throw CustomException("연동되지 않은 OAuth 계정입니다. 먼저 기존 계정에 OAuth 연동을 설정해주세요.")

        // 기존 OAuth 계정으로 로그인
        logger.info { "기존 OAuth 계정으로 로그인 - userId: ${existingOAuth.userId.value}" }
        val user = existingOAuth.user

        // JWT 토큰 발급
        generateAndSetTokens(user, response)

        logger.info { "OAuth 코드 교환 완료 - userId: ${user.id}" }
    }

    /**
     * 기존 사용자에게 OAuth 계정을 연결합니다.
     */
    fun linkOAuthAccount(userId: Long, linkRequest: OAuthDto.LinkRequest): OAuthDto.Response {
        logger.info { "OAuth 계정 연결 시작 - userId: $userId, provider: ${linkRequest.provider}" }

        // 이미 다른 계정에 연결되어 있는지 확인
        if (oauthRepository.existsByProviderAndProviderId(linkRequest.provider, linkRequest.providerId)) {
            throw CustomException("이미 다른 계정에 연결된 OAuth 계정입니다.")
        }

        // 해당 사용자가 이미 같은 provider로 연결되어 있는지 확인
        if (oauthRepository.existsByUserIdAndProvider(userId, linkRequest.provider)) {
            throw CustomException("이미 ${linkRequest.provider} 계정이 연결되어 있습니다.")
        }

        // OAuth 계정 연결
        val userOAuth = oauthRepository.save(
            userId = userId,
            provider = linkRequest.provider,
            providerId = linkRequest.providerId
        )

        logger.info { "OAuth 계정 연결 완료 - oauthId: ${userOAuth.id}" }
        return oauthMapper.toDto(userOAuth)
    }

    /**
     * OAuth 계정 연결을 해제합니다.
     */
    fun unlinkOAuthAccount(userId: Long, unlinkRequest: OAuthDto.UnlinkRequest) {
        logger.info { "OAuth 계정 연결 해제 시작 - userId: $userId, provider: ${unlinkRequest.provider}" }

        // 연결된 OAuth 계정 확인
        val existingOAuth = oauthRepository.findByUserIdAndProvider(userId, unlinkRequest.provider)
            ?: throw CustomException("연결된 ${unlinkRequest.provider} 계정이 없습니다.")

        // 연결 해제
        val isDeleted = oauthRepository.deleteByUserIdAndProvider(userId, unlinkRequest.provider)
        
        if (!isDeleted) {
            throw CustomException("OAuth 계정 연결 해제에 실패했습니다.")
        }

        logger.info { "OAuth 계정 연결 해제 완료 - userId: $userId, provider: ${unlinkRequest.provider}" }
    }

    /**
     * 사용자의 연결된 OAuth 계정 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getLinkedAccounts(userId: Long): OAuthDto.LinkedAccountsResponse {
        logger.debug { "연결된 OAuth 계정 조회 - userId: $userId" }

        val oauthAccounts = oauthRepository.findAllByUserId(userId)
        return oauthMapper.toLinkedAccountsResponse(oauthAccounts)
    }



    /**
     * JWT 토큰을 생성하고 HTTP 응답에 설정합니다.
     */
    private fun generateAndSetTokens(user: User, response: HttpServletResponse) {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        val authentication = UsernamePasswordAuthenticationToken(
            user.email,
            null,
            authorities
        )

        val accessToken = jwtProvider.generateAccessToken(authentication)
        val refreshToken = jwtProvider.generateRefreshToken(authentication)

        response.setHeader("Authorization", BEARER_PREFIX + accessToken)
        response.addCookie(createRefreshTokenCookie(refreshToken))
    }

    /**
     * Refresh Token 쿠키를 생성합니다.
     */
    private fun createRefreshTokenCookie(refreshToken: String): Cookie =
        Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken).apply {
            isHttpOnly = true
            secure = true
            path = "/"
        }



    /**
     * 임시 인증 코드를 생성합니다.
     */
    private fun generateTempCode(): String {
        return "oauth_temp_${UUID.randomUUID().toString().replace("-", "")}"
    }

    /**
     * 만료된 임시 코드들을 정리합니다.
     */
    private fun cleanupExpiredCodes() {
        val now = LocalDateTime.now()
        val expiredCodes = tempCodeStorage.filterValues { it.expiryTime.isBefore(now) }.keys
        expiredCodes.forEach { tempCodeStorage.remove(it) }
        
        if (expiredCodes.isNotEmpty()) {
            logger.debug { "만료된 임시 코드 ${expiredCodes.size}개 정리 완료" }
        }
    }
} 