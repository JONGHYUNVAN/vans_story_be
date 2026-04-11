package blog.vans_story_be.domain.auth.entity

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.datetime

/**
 * Refresh Token 서버측 저장 테이블.
 * 로그아웃 후 토큰 재사용 방지 및 토큰 회전(Rotation) 지원.
 */
object RefreshTokens : LongIdTable("refresh_tokens") {
    val userId = long("user_id").index()
    val tokenHash = varchar("token_hash", 64).uniqueIndex()
    val expiresAt = datetime("expires_at")
}
