package blog.vans_story_be.global.exception

/**
 * 사용자 정의 예외 클래스
 *
 * 애플리케이션에서 발생하는 비즈니스 예외를 처리합니다.
 * 주로 다음과 같은 상황에서 사용됩니다:
 * - 비즈니스 로직 검증 실패
 * - 리소스 조회 실패
 * - 권한 부족
 * - 잘못된 요청
 *
 * 사용 예시:
 * ```kotlin
 * // 리소스 조회 실패
 * throw CustomException("사용자를 찾을 수 없습니다")
 *
 * // 비즈니스 로직 검증 실패
 * if (userRepository.existsByEmail(email)) {
 *     throw CustomException("이미 존재하는 이메일입니다")
 * }
 *
 * // 권한 부족
 * if (!user.hasPermission()) {
 *     throw CustomException("접근 권한이 없습니다")
 * }
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 */
class CustomException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    companion object {
        /**
         * 리소스를 찾을 수 없는 경우의 예외를 생성합니다.
         *
         * @param resourceName 찾을 수 없는 리소스의 이름
         * @return CustomException 인스턴스
         *
         * 사용 예시:
         * ```kotlin
         * throw CustomException.notFound("사용자")
         * // 결과: "사용자를 찾을 수 없습니다" 메시지의 예외
         * ```
         */
        fun notFound(resourceName: String): CustomException =
            CustomException("${resourceName}을(를) 찾을 수 없습니다")

        /**
         * 이미 존재하는 리소스에 대한 예외를 생성합니다.
         *
         * @param resourceName 이미 존재하는 리소스의 이름
         * @return CustomException 인스턴스
         *
         * 사용 예시:
         * ```kotlin
         * throw CustomException.alreadyExists("이메일")
         * // 결과: "이미 존재하는 이메일입니다" 메시지의 예외
         * ```
         */
        fun alreadyExists(resourceName: String): CustomException =
            CustomException("이미 존재하는 ${resourceName}입니다")

        /**
         * 권한 부족에 대한 예외를 생성합니다.
         *
         * @param action 수행하려는 동작
         * @return CustomException 인스턴스
         *
         * 사용 예시:
         * ```kotlin
         * throw CustomException.unauthorized("게시글 수정")
         * // 결과: "게시글 수정 권한이 없습니다" 메시지의 예외
         * ```
         */
        fun unauthorized(action: String): CustomException =
            CustomException("${action} 권한이 없습니다")
    }
} 