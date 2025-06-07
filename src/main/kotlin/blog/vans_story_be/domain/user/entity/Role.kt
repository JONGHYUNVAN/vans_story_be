package blog.vans_story_be.domain.user.entity

/**
 * 사용자 권한을 정의하는 열거형
 *
 * 권한 종류:
 * - [USER]: 일반 사용자 (ROLE_USER)
 * - [ADMIN]: 관리자 (ROLE_ADMIN)
 *
 * 사용 예시:
 * ```kotlin
 * // 권한 확인
 * val userRole = Role.USER
 * println(userRole.value)  // "ROLE_USER" 출력
 *
 * // 권한 비교
 * if (user.role == Role.ADMIN) {
 *     println("관리자 권한이 있습니다")
 * }
 *
 * // 권한 목록 조회
 * Role.values().forEach { role ->
 *     println("${role.name}: ${role.value}")
 * }
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
enum class Role(val value: String) {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    companion object {
        /**
         * 문자열로부터 Role enum을 찾습니다.
         *
         * @param value 찾을 권한 값
         * @return 찾은 Role enum 또는 null
         *
         * 사용 예시:
         * ```kotlin
         * val role = Role.fromValue("ROLE_USER")  // Role.USER 반환
         * val invalidRole = Role.fromValue("INVALID")  // null 반환
         * ```
         */
        fun fromValue(value: String): Role? =
            values().find { it.value == value }

        /**
         * 문자열이 유효한 권한 값인지 확인합니다.
         *
         * @param value 확인할 권한 값
         * @return 유효한 권한 값 여부
         *
         * 사용 예시:
         * ```kotlin
         * if (Role.isValid("ROLE_USER")) {
         *     println("유효한 권한입니다")
         * }
         * ```
         */
        fun isValid(value: String): Boolean =
            values().any { it.value == value }
    }
} 