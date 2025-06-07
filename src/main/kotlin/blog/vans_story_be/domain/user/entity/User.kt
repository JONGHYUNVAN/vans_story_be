package blog.vans_story_be.domain.user.entity

import blog.vans_story_be.domain.base.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 사용자 정보를 관리하는 엔티티 클래스
 *
 * 주요 기능:
 * - 사용자 정보 저장 및 관리
 * - Jakarta Bean Validation을 통한 데이터 검증
 * - JPA를 통한 데이터베이스 매핑
 *
 * 필드 설명:
 * - [id]: 자동 생성된 사용자 ID
 * - [name]: 사용자명 (필수, 3자 이상 50자 이하)
 * - [password]: 비밀번호 (필수, 8자 이상, 영문자, 숫자, 특수문자 포함)
 * - [email]: 이메일 (필수, 유효한 이메일 형식)
 * - [nickname]: 닉네임 (필수, 고유값)
 * - [role]: 사용자 역할 (필수)
 *
 * 사용 예시:
 * ```kotlin
 * // 사용자 생성
 * val user = User(
 *     name = "홍길동",
 *     password = "encryptedPassword",
 *     email = "user@example.com",
 *     nickname = "길동이",
 *     role = Role.USER
 * )
 *
 * // 사용자 정보 수정
 * user.updateNickname("새로운닉네임")
 * user.updatePassword("newEncryptedPassword")
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "사용자명은 필수입니다")
    @field:Size(min = 3, max = 50, message = "사용자 이름은 3자 이상 50자 이하여야 합니다")
    @Column(nullable = false, unique = true)
    var name: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @Column(nullable = false)
    var password: String,

    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "유효한 이메일 형식이어야 합니다")
    @Column(nullable = false, unique = true)
    var email: String,

    @field:NotBlank(message = "닉네임은 필수입니다")
    @Column(nullable = false, unique = true)
    var nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER
) : BaseEntity() {



    override fun toString(): String =
        "User(id=$id, name='$name', email='$email', nickname='$nickname', role=$role)"
} 