package blog.vans_story_be.domain.user.dto

import blog.vans_story_be.domain.user.entity.Role
import blog.vans_story_be.domain.user.entity.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 사용자 정보 전송을 위한 DTO 클래스들
 *
 * 주요 DTO:
 * - [CreateRequest]: 사용자 생성 요청
 * - [UpdateRequest]: 사용자 정보 수정 요청
 * - [Response]: 사용자 정보 응답
 *
 * 사용 예시:
 * ```kotlin
 * // 사용자 생성 요청
 * val createRequest = UserDto.CreateRequest(
 *     name = "홍길동",
 *     email = "user@example.com",
 *     password = "Password1!",
 *     nickname = "길동이"
 * )
 *
 * // 사용자 정보 수정 요청
 * val updateRequest = UserDto.UpdateRequest(
 *     nickname = "새로운닉네임"
 * )
 *
 * // 사용자 정보 응답
 * val response = UserDto.Response(
 *     id = 1L,
 *     name = "홍길동",
 *     email = "user@example.com",
 *     nickname = "길동이",
 *     role = Role.USER,
 *     createdAt = "2024-03-19 10:00:00",
 *     updatedAt = "2024-03-19 10:00:00"
 * )
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
sealed class UserDto {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        /**
         * LocalDateTime을 문자열로 변환합니다.
         *
         * @param dateTime 변환할 LocalDateTime
         * @return "yyyy-MM-dd HH:mm:ss" 형식의 문자열
         * @throws IllegalStateException dateTime이 null인 경우
         */
        fun formatDateTime(dateTime: LocalDateTime?): String =
            dateTime?.format(formatter) ?: throw IllegalStateException("날짜/시간이 null입니다")
    }

    /**
     * 사용자 생성 요청 DTO
     *
     * @property name 사용자 이름 (필수, 3-50자)
     * @property password 비밀번호 (필수, 8자 이상, 영문/숫자/특수문자 조합)
     * @property email 이메일 주소 (필수, 유효한 이메일 형식)
     * @property nickname 닉네임 (필수, 3-50자)
     */
    data class CreateRequest(
        @field:NotBlank(message = "사용자명은 필수입니다")
        @field:Size(min = 3, max = 50, message = "사용자 이름은 3자 이상 50자 이하여야 합니다")
        val name: String,

        @field:NotBlank(message = "비밀번호는 필수입니다")
        @field:Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다"
        )
        val password: String,

        @field:NotBlank(message = "이메일은 필수입니다")
        @field:Email(message = "유효한 이메일 형식이어야 합니다")
        val email: String,

        @field:NotBlank(message = "닉네임은 필수입니다")
        @field:Size(min = 3, max = 50, message = "닉네임은 3자 이상 50자 이하여야 합니다")
        val nickname: String
    ) {
        /**
         * DTO를 User 엔티티로 변환합니다.
         *
         * @param passwordEncoder 비밀번호 암호화에 사용할 인코더
         * @return 생성된 User 엔티티
         */
        fun toEntity(passwordEncoder: PasswordEncoder): User =
            User(
                name = name,
                password = passwordEncoder.encode(password),
                email = email,
                nickname = nickname
            )
    }

    /**
     * 사용자 정보 수정 요청 DTO
     *
     * @property password 새로운 비밀번호 (선택, 8자 이상, 영문/숫자/특수문자 조합)
     * @property email 새로운 이메일 주소 (선택)
     * @property nickname 새로운 닉네임 (선택, 3-50자)
     */
    data class UpdateRequest(
        @field:Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다"
        )
        val password: String? = null,

        @field:Email(message = "올바른 이메일 형식이 아닙니다")
        val email: String? = null,

        @field:Size(min = 3, max = 50, message = "닉네임은 3자 이상 50자 이하여야 합니다")
        val nickname: String? = null
    ) {
        /**
         * DTO의 유효성을 검사합니다.
         *
         * @throws IllegalArgumentException 유효성 검사 실패 시
         */
        fun validate() {
            password?.let {
                require(it.isNotBlank()) { "비밀번호는 비어있을 수 없습니다" }
            }
            email?.let {
                require(it.isNotBlank()) { "이메일은 비어있을 수 없습니다" }
            }
            nickname?.let {
                require(it.isNotBlank()) { "닉네임은 비어있을 수 없습니다" }
            }
        }

        /**
         * DTO가 비어있는지 확인합니다.
         *
         * @return 모든 필드가 null이면 true, 아니면 false
         */
        fun isEmpty() = password == null && email == null && nickname == null
    }

    /**
     * 사용자 정보 응답 DTO
     *
     * @property id 사용자 고유 식별자
     * @property name 사용자 이름
     * @property email 이메일 주소
     * @property nickname 사용자 닉네임
     * @property role 사용자 권한
     * @property createdAt 생성 시간 (yyyy-MM-dd HH:mm:ss 형식)
     * @property updatedAt 수정 시간 (yyyy-MM-dd HH:mm:ss 형식)
     */
    data class Response(
        val id: Long,
        val name: String,
        val email: String,
        val nickname: String,
        val role: Role,
        val createdAt: String,
        val updatedAt: String
    ) {
        companion object {
            /**
             * User 엔티티로부터 Response DTO를 생성합니다.
             *
             * @param user 변환할 User 엔티티
             * @return 생성된 Response DTO
             */
            fun from(user: blog.vans_story_be.domain.user.entity.User) = Response(
                id = user.id ?: throw IllegalStateException("User ID가 null입니다"),
                name = user.name,
                email = user.email,
                nickname = user.nickname,
                role = user.role,
                createdAt = formatDateTime(user.createdAt),
                updatedAt = formatDateTime(user.updatedAt)
            )
        }
    }
} 