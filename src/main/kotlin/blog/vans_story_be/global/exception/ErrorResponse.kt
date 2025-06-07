package blog.vans_story_be.global.exception

/**
 * API 오류 응답을 위한 데이터 클래스
 *
 * 오류 발생 시 클라이언트에게 반환되는 응답 형식을 정의합니다.
 * 주로 [GlobalExceptionHandler]에서 사용됩니다.
 *
 * 필드 설명:
 * - [message]: 오류 메시지 (사용자에게 보여질 메시지)
 * - [details]: 상세 오류 정보 (디버깅용)
 *
 * 사용 예시:
 * ```kotlin
 * // 기본 오류 응답
 * val error = ErrorResponse(
 *     message = "사용자를 찾을 수 없습니다",
 *     details = "User with id 1 not found"
 * )
 *
 * // 커스텀 예외로부터 오류 응답 생성
 * val customException = CustomException("잘못된 요청입니다")
 * val error = ErrorResponse.from(customException)
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 */
data class ErrorResponse(
    val message: String,
    val details: String? = null
) {
    companion object {
        /**
         * [CustomException]으로부터 [ErrorResponse]를 생성합니다.
         *
         * @param exception 변환할 예외
         * @return 생성된 ErrorResponse
         *
         * 사용 예시:
         * ```kotlin
         * try {
         *     // 비즈니스 로직
         * } catch (e: CustomException) {
         *     val error = ErrorResponse.from(e)
         *     return ResponseEntity.badRequest().body(error)
         * }
         * ```
         */
        fun from(exception: CustomException): ErrorResponse =
            ErrorResponse(
                message = exception.message ?: "알 수 없는 오류가 발생했습니다",
                details = exception.cause?.message
            )

        /**
         * 일반 [Exception]으로부터 [ErrorResponse]를 생성합니다.
         *
         * @param exception 변환할 예외
         * @return 생성된 ErrorResponse
         *
         * 사용 예시:
         * ```kotlin
         * try {
         *     // 비즈니스 로직
         * } catch (e: Exception) {
         *     val error = ErrorResponse.from(e)
         *     return ResponseEntity.internalServerError().body(error)
         * }
         * ```
         */
        fun from(exception: Exception): ErrorResponse =
            ErrorResponse(
                message = "서버 오류가 발생했습니다",
                details = exception.message
            )
    }
} 