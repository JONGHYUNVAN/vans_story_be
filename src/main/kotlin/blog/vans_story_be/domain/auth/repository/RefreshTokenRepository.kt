package blog.vans_story_be.domain.auth.repository

import blog.vans_story_be.domain.auth.entity.RefreshTokens
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Refresh Token DB 저장소.
 * 모든 메서드는 Exposed transaction 내에서 실행됩니다.
 */
@Repository
class RefreshTokenRepository {

    private val logger = KotlinLogging.logger {}

    /**
     * Refresh Token 해시를 저장합니다.
     */
    fun save(userId: Long, tokenHash: String, expiresAt: LocalDateTime) {
        transaction {
            RefreshTokens.insert {
                it[RefreshTokens.userId] = userId
                it[RefreshTokens.tokenHash] = tokenHash
                it[RefreshTokens.expiresAt] = expiresAt
            }
        }
    }

    /**
     * tokenHash로 userId를 조회합니다.
     * 존재하지 않으면 null 반환.
     */
    fun findUserIdByTokenHash(tokenHash: String): Long? {
        return transaction {
            RefreshTokens.selectAll()
                .where { RefreshTokens.tokenHash eq tokenHash }
                .map { it[RefreshTokens.userId] }
                .firstOrNull()
        }
    }

    /**
     * 특정 사용자의 모든 Refresh Token을 삭제합니다 (로그아웃 / 토큰 회전 시 사용).
     */
    fun deleteByUserId(userId: Long) {
        transaction {
            RefreshTokens.deleteWhere { RefreshTokens.userId eq userId }
        }
    }

    /**
     * 만료된 Refresh Token을 정리합니다.
     */
    fun deleteExpired() {
        transaction {
            val now = LocalDateTime.now()
            val deleted = RefreshTokens.deleteWhere { RefreshTokens.expiresAt less now }
            if (deleted > 0) logger.debug { "만료된 Refresh Token ${deleted}개 정리 완료" }
        }
    }
}
