package blog.vans_story_be.domain.user.entity

import blog.vans_story_be.domain.base.BaseEntity
import blog.vans_story_be.domain.base.BaseTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import java.time.LocalDateTime

/**
 * 사용자 정보를 관리하는 테이블 정의
 */
object Users : LongIdTable("users") {
    val name = varchar("name", 50).uniqueIndex()
    val password = varchar("password", 100)
    val email = varchar("email", 100).uniqueIndex()
    val nickname = varchar("nickname", 50).uniqueIndex()
    val role = enumerationByName("role", 20, Role::class)
    val createdAt = datetime("created_at").default(java.time.LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(java.time.LocalDateTime.now())
}

/**
 * 사용자 정보를 관리하는 엔티티 클래스
 *
 * 주요 기능:
 * - 사용자 정보 저장 및 관리
 * - Exposed를 통한 데이터베이스 매핑
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
 * @since 2025.06.07
 */
class User(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<User>(Users)

    var name: String by Users.name
    var password: String by Users.password
    var email: String by Users.email
    var nickname: String by Users.nickname
    var role: Role by Users.role
    var createdAt: LocalDateTime by Users.createdAt
    var updatedAt: LocalDateTime by Users.updatedAt

    override fun toString(): String =
        "User(id=$id, name='$name', email='$email', nickname='$nickname', role=$role)"
} 