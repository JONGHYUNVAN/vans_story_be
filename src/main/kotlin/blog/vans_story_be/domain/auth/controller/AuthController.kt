package blog.vans_story_be.domain.auth.controller

import blog.vans_story_be.domain.auth.dto.LoginRequest
import blog.vans_story_be.domain.auth.service.AuthService
import blog.vans_story_be.global.response.ApiResponse
import blog.vans_story_be.global.response.noContent
import blog.vans_story_be.global.response.ok
import blog.vans_story_be.global.response.withLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 인증 관련 요청을 처리하는 컨트롤러
 *
 * 주요 기능:
 * - 사용자 로그인 및 토큰 발급
 * - 토큰 갱신
 * - 로그아웃 처리
 *
 * API 엔드포인트:
 * - POST /api/v1/auth/login: 로그인
 * - POST /api/v1/auth/refresh: 토큰 갱신
 * - POST /api/v1/auth/logout: 로그아웃
 *
 * @property authService 인증 관련 비즈니스 로직을 처리하는 서비스
 *
 * @author van
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "인증 API")
class AuthController(
    private val authService: AuthService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 사용자 로그인을 처리합니다.
     *
     * 처리 과정:
     * 1. 로그인 요청 정보 검증
     * 2. 인증 서비스를 통한 로그인 처리
     * 3. 토큰 발급 및 응답 설정
     *    - Access Token: Authorization 헤더
     *    - Refresh Token: HTTP Only 쿠키
     *
     * @param loginRequest 로그인 요청 정보 (이메일, 비밀번호)
     * @param response HTTP 응답 객체 (토큰 설정에 사용)
     * @return 로그인 성공 응답
     * @throws CustomException 인증 실패 시
     *
     * 사용 예시:
     * ```kotlin
     * // 클라이언트 측 요청 예시
     * val loginRequest = LoginRequest(
     *     email = "user@example.com",
     *     password = "Password1!"
     * )
     * 
     * // 서버 응답 예시
     * // HTTP/1.1 200 OK
     * // Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
     * // Set-Cookie: refreshToken=eyJhbGciOiJIUzI1NiIs...; HttpOnly; Secure; Path=/
     * ```
     */
    @Operation(
        summary = "로그인",
        description = "사용자 로그인을 처리하고 JWT 토큰을 발급합니다."
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Nothing?>> = withLogging("로그인") {
        authService.login(loginRequest, response)
        ResponseEntity.ok(ApiResponse.success<Nothing?>(null))
    }

    /**
     * Access Token을 갱신합니다.
     *
     * 처리 과정:
     * 1. 쿠키에서 Refresh Token 추출
     * 2. Refresh Token 검증
     * 3. 새로운 토큰 발급
     *    - Access Token: Authorization 헤더
     *    - Refresh Token: HTTP Only 쿠키
     *
     * @param refreshToken 쿠키에서 추출한 Refresh Token
     * @param response HTTP 응답 객체 (토큰 설정에 사용)
     * @return 토큰 갱신 성공 응답
     * @throws CustomException 토큰이 유효하지 않거나 만료된 경우
     *
     * 사용 예시:
     * ```kotlin
     * // 클라이언트 측 요청 예시
     * // Cookie: refreshToken=eyJhbGciOiJIUzI1NiIs...
     * 
     * // 서버 응답 예시
     * // HTTP/1.1 200 OK
     * // Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
     * // Set-Cookie: refreshToken=eyJhbGciOiJIUzI1NiIs...; HttpOnly; Secure; Path=/
     * ```
     */
    @Operation(
        summary = "토큰 갱신",
        description = "Refresh Token을 사용하여 새로운 Access Token을 발급합니다."
    )
    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(name = "refreshToken") refreshToken: String,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Nothing?>> = withLogging("토큰 갱신") {
        authService.refresh(refreshToken, response)
        ResponseEntity.ok(ApiResponse.success<Nothing?>(null))
    }

    /**
     * 사용자 로그아웃을 처리합니다.
     *
     * 처리 과정:
     * 1. Refresh Token 쿠키 만료 처리
     * 2. 로그아웃 이벤트 로깅
     *
     * @param response HTTP 응답 객체 (쿠키 만료에 사용)
     * @return 로그아웃 성공 응답
     *
     * 사용 예시:
     * ```kotlin
     * // 클라이언트 측 요청 예시
     * // POST /api/v1/auth/logout
     * 
     * // 서버 응답 예시
     * // HTTP/1.1 200 OK
     * // Set-Cookie: refreshToken=; Max-Age=0; HttpOnly; Secure; Path=/
     * ```
     */
    @Operation(
        summary = "로그아웃",
        description = "사용자 로그아웃을 처리하고 Refresh Token을 만료시킵니다."
    )
    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<ApiResponse<Nothing?>> = withLogging("로그아웃") {
        authService.logout(response)
        ResponseEntity.noContent().build()
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
        block()
    } catch (e: Exception) {
        logger.error(e) { "[$operation] 실패" }
        throw e
    }
} 