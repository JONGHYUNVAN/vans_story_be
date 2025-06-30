package blog.vans_story_be.domain.oauth.entity

import blog.vans_story_be.domain.user.entity.Users
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * 사용자 OAuth 연동 정보를 관리하는 테이블 정의
 */
object UserOAuths : LongIdTable("user_oauths") {
    val userId = reference("user_id", Users)
    val provider = varchar("provider", 50)
    val providerId = varchar("provider_id", 100)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    
    init {
        // 복합 유니크 인덱스: 같은 provider에서 같은 providerId는 한 번만 등록 가능
        uniqueIndex(provider, providerId)
    }
}

/**
 * 사용자 OAuth 연동 정보를 관리하는 엔티티 클래스
 *
 * 주요 기능:
 * - OAuth 계정과 일반 사용자 계정 연결
 * - 다중 OAuth 계정 지원 (Google, Kakao, Naver 등)
 * - OAuth 계정 정보 관리
 *
 * 필드 설명:
 * - [id]: 자동 생성된 OAuth 연동 ID
 * - [userId]: 연결된 사용자 ID (Users 테이블 참조)
 * - [provider]: OAuth 제공업체 (google, kakao, naver 등)
 * - [providerId]: OAuth 제공업체에서 제공하는 사용자 고유 ID

 * - [createdAt]: 연동 생성 시간
 * - [updatedAt]: 연동 정보 수정 시간
 *
 * 사용 예시:
 * ```kotlin
 * // OAuth 연동 정보 생성
 * val userOAuth = UserOAuth.new {
 *     userId = EntityID(1, Users)
 *     provider = "google"
 *     providerId = "google_user_12345"

 * }
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.01.09
 */
class UserOAuth(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserOAuth>(UserOAuths)

    var userId by UserOAuths.userId
    var provider by UserOAuths.provider
    var providerId by UserOAuths.providerId
    var createdAt by UserOAuths.createdAt
    var updatedAt by UserOAuths.updatedAt

    // 연결된 User 엔티티 참조
    val user by blog.vans_story_be.domain.user.entity.User referencedOn UserOAuths.userId

    override fun toString(): String =
        "UserOAuth(id=$id, userId=$userId, provider='$provider', providerId='$providerId')"
} 