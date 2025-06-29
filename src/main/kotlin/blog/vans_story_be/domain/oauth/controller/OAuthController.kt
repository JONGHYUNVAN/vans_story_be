package blog.vans_story_be.domain.oauth.controller

import blog.vans_story_be.domain.oauth.dto.OAuthDto
import blog.vans_story_be.domain.oauth.service.OAuthService
import blog.vans_story_be.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

/**
 * OAuth 관련 요청을 처리하는 컨트롤러
 *
 * 주요 기능:
 * - OAuth 소셜 로그인
 * - OAuth 계정 연결/해제
 * - 연결된 OAuth 계정 조회
 *
 * API 엔드포인트:
 * - POST /api/v1/oauth/login: OAuth 로그인
 * - POST /api/v1/oauth/link: OAuth 계정 연결
 * - DELETE /api/v1/oauth/unlink: OAuth 계정 해제
 * - GET /api/v1/oauth/linked: 연결된 계정 조회
 *
 * @property oauthService OAuth 관련 비즈니스 로직을 처리하는 서비스
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.01.09
 */
@RestController
@RequestMapping("/api/v1/oauth")
@Tag(name = "OAuth", description = "OAuth 소셜 로그인 API")
class OAuthController(
    private val oauthService: OAuthService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * OAuth 소셜 로그인을 처리합니다.
     *
     * 처리 과정:
     * 1. 중간 서버에서 OAuth 인증 후 전달받은 정보 검증
     * 2. 기존 OAuth 연동 계정 확인
     * 3. 연동 계정이 있으면 해당 사용자로 로그인
     * 4. 연동 계정이 없으면 새 사용자 생성 후 OAuth 연동
     * 5. JWT 토큰 발급 및 응답 설정
     *
     * @param loginRequest OAuth 로그인 요청 정보
     * @param response HTTP 응답 객체 (토큰 설정에 사용)
     * @return OAuth 로그인 성공 응답
     * @throws CustomException OAuth 로그인 실패 시
     *
     * 사용 예시:
     * ```json
     * POST /api/v1/oauth/login
     * {
     *   "provider": "google",
     *   "providerId": "google_user_12345"
     * }
     * ```
     */
    @Operation(
        summary = "OAuth 소셜 로그인",
        description = "OAuth 제공업체를 통해 로그인하고 JWT 토큰을 발급합니다."
    )
    @PostMapping("/login")
    fun oauthLogin(
        @Valid @RequestBody loginRequest: OAuthDto.LoginRequest
    ): ResponseEntity<ApiResponse<OAuthDto.CodeResponse>> = withLogging("OAuth 임시 코드 발급") {
        val codeResponse = oauthService.oauthLogin(loginRequest)
        ResponseEntity.ok(ApiResponse.success(codeResponse))
    }

    /**
     * 임시 코드를 JWT 토큰으로 교환합니다.
     *
     * 처리 과정:
     * 1. 임시 코드 검증 및 만료 확인
     * 2. 코드에 포함된 OAuth 정보로 사용자 확인/생성
     * 3. JWT 토큰 발급 및 응답 설정
     * 4. 사용된 코드 즉시 삭제 (일회용)
     *
     * @param exchangeRequest 임시 코드 교환 요청 정보
     * @param response HTTP 응답 객체 (토큰 설정에 사용)
     * @return JWT 토큰 발급 성공 응답
     * @throws CustomException 코드 검증 실패 또는 만료 시
     *
     * 사용 예시:
     * ```json
     * POST /api/v1/oauth/exchange
     * {
     *   "code": "oauth_temp_abc123def456"
     * }
     * ```
     */
    @Operation(
        summary = "임시 코드를 JWT 토큰으로 교환",
        description = "OAuth 로그인에서 발급받은 임시 코드를 실제 JWT 토큰으로 교환합니다."
    )
    @PostMapping("/exchange")
    fun exchangeCodeForToken(
        @Valid @RequestBody exchangeRequest: OAuthDto.ExchangeRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Nothing?>> = withLogging("OAuth 코드 교환") {
        oauthService.exchangeCodeForToken(exchangeRequest, response)
        ResponseEntity.ok(ApiResponse.success<Nothing?>(null))
    }

    /**
     * 기존 계정에 OAuth 계정을 연결합니다.
     *
     * 처리 과정:
     * 1. 현재 로그인된 사용자 확인
     * 2. OAuth 계정 중복 연결 여부 확인
     * 3. OAuth 계정 연결 정보 저장
     *
     * @param linkRequest OAuth 계정 연결 요청 정보
     * @param userDetails 현재 로그인된 사용자 정보
     * @return OAuth 계정 연결 성공 응답
     * @throws CustomException OAuth 계정 연결 실패 시
     *
     * 사용 예시:
     * ```json
     * POST /api/v1/oauth/link
     * Authorization: Bearer <access_token>
     * {
     *   "provider": "kakao",
     *   "providerId": "kakao_user_67890"
     * }
     * ```
     */
    @Operation(
        summary = "OAuth 계정 연결",
        description = "현재 로그인된 계정에 OAuth 계정을 연결합니다."
    )
    @PostMapping("/link")
    fun linkOAuthAccount(
        @Valid @RequestBody linkRequest: OAuthDto.LinkRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<OAuthDto.Response>> = withLogging("OAuth 계정 연결") {
        val userId = extractUserId(userDetails)
        val linkedOAuth = oauthService.linkOAuthAccount(userId, linkRequest)
        ResponseEntity.ok(ApiResponse.success(linkedOAuth))
    }

    /**
     * OAuth 계정 연결을 해제합니다.
     *
     * 처리 과정:
     * 1. 현재 로그인된 사용자 확인
     * 2. 연결된 OAuth 계정 확인
     * 3. OAuth 계정 연결 해제
     *
     * @param unlinkRequest OAuth 계정 연결 해제 요청 정보
     * @param userDetails 현재 로그인된 사용자 정보
     * @return OAuth 계정 연결 해제 성공 응답
     * @throws CustomException OAuth 계정 연결 해제 실패 시
     *
     * 사용 예시:
     * ```json
     * DELETE /api/v1/oauth/unlink
     * Authorization: Bearer <access_token>
     * {
     *   "provider": "google"
     * }
     * ```
     */
    @Operation(
        summary = "OAuth 계정 연결 해제",
        description = "현재 로그인된 계정에서 OAuth 계정 연결을 해제합니다."
    )
    @DeleteMapping("/unlink")
    fun unlinkOAuthAccount(
        @Valid @RequestBody unlinkRequest: OAuthDto.UnlinkRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<Nothing?>> = withLogging("OAuth 계정 연결 해제") {
        val userId = extractUserId(userDetails)
        oauthService.unlinkOAuthAccount(userId, unlinkRequest)
        ResponseEntity.ok(ApiResponse.success<Nothing?>(null))
    }

    /**
     * 현재 사용자에게 연결된 OAuth 계정 목록을 조회합니다.
     *
     * @param userDetails 현재 로그인된 사용자 정보
     * @return 연결된 OAuth 계정 목록
     *
     * 사용 예시:
     * ```json
     * GET /api/v1/oauth/linked
     * Authorization: Bearer <access_token>
     * ```
     */
    @Operation(
        summary = "연결된 OAuth 계정 조회",
        description = "현재 로그인된 사용자에게 연결된 OAuth 계정 목록을 조회합니다."
    )
    @GetMapping("/linked")
    fun getLinkedAccounts(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<OAuthDto.LinkedAccountsResponse>> = withLogging("연결된 OAuth 계정 조회") {
        val userId = extractUserId(userDetails)
        val linkedAccounts = oauthService.getLinkedAccounts(userId)
        ResponseEntity.ok(ApiResponse.success(linkedAccounts))
    }

    /**
     * UserDetails에서 사용자 ID를 추출합니다.
     *
     * @param userDetails 사용자 인증 정보
     * @return 사용자 ID
     */
    private fun extractUserId(userDetails: UserDetails): Long {
        // UserDetails의 username이 사용자 ID인 경우
        return userDetails.username.toLongOrNull()
            ?: throw IllegalArgumentException("유효하지 않은 사용자 정보입니다.")
    }

    /**
     * 로깅을 포함한 컨트롤러 메서드 실행을 위한 확장 함수
     *
     * @param operation 수행할 작업의 이름
     * @param block 실행할 코드 블록
     * @return API 응답
     */
    private inline fun <T> withLogging(
        operation: String,
        block: () -> ResponseEntity<ApiResponse<T>>
    ): ResponseEntity<ApiResponse<T>> = try {
        logger.info { "[$operation] 시작" }
        val result = block()
        logger.info { "[$operation] 성공" }
        result
    } catch (e: Exception) {
        logger.error(e) { "[$operation] 실패" }
        throw e
    }
} 