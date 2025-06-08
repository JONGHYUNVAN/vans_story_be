package blog.vans_story_be.domain.user.dto

import blog.vans_story_be.domain.user.entity.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

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
    /**
     * 사용자 생성 요청 DTO
     *
     * @property name 사용자 이름 (필수, 2-50자)
     * @property password 비밀번호 (필수, 8자 이상, 영문/숫자/특수문자 조합)
     * @property email 이메일 주소 (필수, 유효한 이메일 형식)
     * @property nickname 닉네임 (필수, 2-50자)
     */
    data class CreateRequest(
        @field:NotBlank(message = "이름은 필수입니다")
        @field:Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
        val name: String,

        @field:NotBlank(message = "이메일은 필수입니다")
        @field:Email(message = "올바른 이메일 형식이 아닙니다")
        val email: String,

        @field:NotBlank(message = "비밀번호는 필수입니다")
        @field:Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다"
        )
        val password: String,

        @field:NotBlank(message = "닉네임은 필수입니다")
        @field:Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다")
        val nickname: String
    )

    /**
     * 사용자 정보 수정 요청 DTO
     *
     * @property password 새로운 비밀번호 (선택, 8자 이상, 영문/숫자/특수문자 조합)
     * @property email 새로운 이메일 주소 (선택)
     * @property nickname 새로운 닉네임 (선택, 2-50자)
     */
    data class UpdateRequest(
        @field:Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다"
        )
        val password: String? = null,

        @field:Email(message = "올바른 이메일 형식이 아닙니다")
        val email: String = "",

        @field:Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다")
        val nickname: String = "",

        val role: Role? = null
    )

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
        val id: Long = 0L,
        val name: String = "",
        val email: String = "",
        val nickname: String = "",
        val role: Role = Role.USER,
        val createdAt: String = "",
        val updatedAt: String = ""
    )
} 