package blog.vans_story_be.global.exception

import blog.vans_story_be.global.response.ApiResponse
import blog.vans_story_be.global.response.withError
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리를 위한 핸들러 클래스
 *
 * 애플리케이션에서 발생하는 모든 예외를 일관된 형식으로 처리합니다.
 * 주요 처리 예외:
 * - [CustomException]: 비즈니스 로직 예외
 * - [MethodArgumentNotValidException]: 요청 데이터 검증 실패
 * - [HttpMessageNotReadableException]: JSON 파싱 실패
 * - [BadCredentialsException]: 인증 실패
 * - [Exception]: 기타 예외
 *
 * 사용 예시:
 * ```kotlin
 * // 컨트롤러에서 예외 발생
 * @GetMapping("/users/{id}")
 * fun getUser(@PathVariable id: Long): UserDto.Response {
 *     val user = userRepository.findById(id)
 *         ?: throw CustomException("사용자를 찾을 수 없습니다")
 *     return userMapper.toDto(user)
 * }
 *
 * // GlobalExceptionHandler가 자동으로 처리
 * // 응답:
 * // {
 * //     "code": "ERROR",
 * //     "message": "사용자를 찾을 수 없습니다",
 * //     "data": null
 * // }
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    /**
     * 모든 예외를 처리하는 핸들러 메서드
     *
     * @param e 발생한 예외
     * @return ApiResponse 형식의 에러 응답
     *
     * 처리되는 예외:
     * - [Exception] 및 그 하위 클래스
     * - [RuntimeException] 및 그 하위 클래스
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Unit>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error<Unit>(e.message ?: "알 수 없는 오류가 발생했습니다"))
            .also { log.error(e) { "처리되지 않은 예외 발생: ${e.message}" } }

    /**
     * JSON 파싱/타입 변환 예외를 처리하는 핸들러
     *
     * @param e 발생한 예외
     * @return ApiResponse 형식의 에러 응답
     *
     * 처리되는 예외:
     * - [HttpMessageNotReadableException]
     * - JSON 파싱 실패
     * - 타입 변환 실패
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Unit>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error<Unit>("잘못된 요청 형식입니다. 요청 본문을 확인해주세요."))
            .also { log.warn { "잘못된 요청 형식: ${e.message}" } }

    /**
     * CustomException 처리를 위한 핸들러
     *
     * @param e 발생한 예외
     * @return ApiResponse 형식의 에러 응답
     *
     * 처리되는 예외:
     * - [CustomException]
     * - 비즈니스 로직 예외
     * - 리소스 조회 실패
     * - 권한 부족
     */
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ApiResponse<Unit>> =
        when {
            e.message?.contains("not found", ignoreCase = true) == true ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error<Unit>(e.message ?: "리소스를 찾을 수 없습니다"))
            else ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error<Unit>(e.message ?: "잘못된 요청입니다"))
        }.also { log.warn { "비즈니스 예외 발생: ${e.message}" } }

    /**
     * 검증 실패 예외를 처리하는 핸들러
     *
     * @param ex 발생한 예외
     * @return ApiResponse 형식의 에러 응답
     *
     * 처리되는 예외:
     * - [MethodArgumentNotValidException]
     * - @Valid 검증 실패
     * - 필드 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> =
        ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
            .let { errorMessage ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error<Unit>(errorMessage))
            }
            .also { log.warn { "검증 실패: ${ex.bindingResult.fieldErrors}" } }

    /**
     * BadCredentialsException 처리를 위한 핸들러
     *
     * @param e 발생한 예외
     * @return ApiResponse 형식의 에러 응답
     *
     * 처리되는 예외:
     * - [BadCredentialsException]
     * - 로그인 실패
     * - 인증 실패
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(e: BadCredentialsException): ResponseEntity<ApiResponse<Unit>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error<Unit>("잘못된 사용자명 또는 비밀번호입니다."))
            .also { log.warn { "인증 실패: ${e.message}" } }
} 