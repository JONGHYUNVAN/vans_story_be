package blog.vans_story_be.global.response

/**
 * API 응답을 위한 공통 응답 객체
 * 모든 API 응답은 이 클래스의 형식을 따릅니다.
 *
 * 주요 기능:
 * - 성공/실패 여부 표시
 * - 제네릭 타입의 응답 데이터 포함
 * - 응답 메시지 관리
 *
 * 사용 예시:
 * ```kotlin
 * // 성공 응답
 * val successResponse = ApiResponse.success(userDto)
 * // 결과: ApiResponse(success=true, data=UserDto(...), message=null)
 *
 * // 에러 응답
 * val errorResponse = ApiResponse.error("사용자를 찾을 수 없습니다")
 * // 결과: ApiResponse(success=false, data=null, message="사용자를 찾을 수 없습니다")
 *
 * // 컨트롤러에서 사용
 * @GetMapping("/users/{id}")
 * fun getUser(@PathVariable id: Long): ApiResponse<UserDto> {
 *     return userService.findUser(id)
 *         ?.let { ApiResponse.success(it) }
 *         ?: ApiResponse.error("사용자를 찾을 수 없습니다")
 * }
 * ```
 *
 * @param T 응답 데이터의 타입
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 */
data class ApiResponse<T>(
    /** API 호출 성공 여부 */
    val success: Boolean,
    /** 응답 데이터 */
    val data: T?,
    /** 응답 메시지 (주로 에러 메시지) */
    val message: String?
) {
    companion object {
        /**
         * 성공 응답을 생성하는 팩토리 메서드
         *
         * @param data 응답 데이터
         * @return 성공 응답 객체
         */
        fun <T> success(data: T) = ApiResponse(
            success = true,
            data = data,
            message = null
        )

        /**
         * 에러 응답을 생성하는 팩토리 메서드
         *
         * @param message 에러 메시지
         * @return 에러 응답 객체
         */
        fun <T> error(message: String) = ApiResponse<T>(
            success = false,
            data = null,
            message = message
        )
    }
} 