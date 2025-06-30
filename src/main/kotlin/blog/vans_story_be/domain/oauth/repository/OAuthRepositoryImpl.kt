package blog.vans_story_be.domain.oauth.repository

import blog.vans_story_be.domain.oauth.entity.UserOAuth
import blog.vans_story_be.domain.oauth.entity.UserOAuths
import blog.vans_story_be.domain.user.entity.Users
import mu.KotlinLogging
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * OAuth 연동 정보 데이터 접근을 위한 Repository 구현체
 *
 * Exposed ORM을 사용하여 데이터베이스 작업을 처리합니다.
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.01.09
 */
@Repository
class OAuthRepositoryImpl : OAuthRepository {

    private val logger = KotlinLogging.logger {}

    override fun save(userId: Long, provider: String, providerId: String): UserOAuth {
        return transaction {
            logger.info { "OAuth 연동 정보 저장 시작 - userId: $userId, provider: $provider, providerId: $providerId" }
            
            val userOAuth = UserOAuth.new {
                this.userId = EntityID(userId, Users)
                this.provider = provider
                this.providerId = providerId
                this.updatedAt = LocalDateTime.now()
            }
            
            logger.info { "OAuth 연동 정보 저장 완료 - id: ${userOAuth.id}" }
            userOAuth
        }
    }

    override fun findByProviderAndProviderId(provider: String, providerId: String): UserOAuth? {
        return transaction {
            logger.debug { "OAuth 연동 정보 조회 - provider: $provider, providerId: $providerId" }
            
            UserOAuth.find {
                (UserOAuths.provider eq provider) and (UserOAuths.providerId eq providerId)
            }.singleOrNull()
        }
    }

    override fun findAllByUserId(userId: Long): List<UserOAuth> {
        return transaction {
            logger.debug { "사용자 OAuth 연동 정보 목록 조회 - userId: $userId" }
            
            UserOAuth.find {
                UserOAuths.userId eq userId
            }.toList()
        }
    }

    override fun findByUserIdAndProvider(userId: Long, provider: String): UserOAuth? {
        return transaction {
            logger.debug { "사용자 OAuth 연동 정보 조회 - userId: $userId, provider: $provider" }
            
            UserOAuth.find {
                (UserOAuths.userId eq userId) and (UserOAuths.provider eq provider)
            }.singleOrNull()
        }
    }

    override fun deleteByUserIdAndProvider(userId: Long, provider: String): Boolean {
        return transaction {
            logger.info { "OAuth 연동 정보 삭제 시작 - userId: $userId, provider: $provider" }
            
            val deletedCount = UserOAuths.deleteWhere {
                (UserOAuths.userId eq userId) and (UserOAuths.provider eq provider)
            }
            
            val isDeleted = deletedCount > 0
            logger.info { "OAuth 연동 정보 삭제 완료 - userId: $userId, provider: $provider, 삭제된 개수: $deletedCount" }
            
            isDeleted
        }
    }

    override fun existsByProviderAndProviderId(provider: String, providerId: String): Boolean {
        return transaction {
            logger.debug { "OAuth 연동 정보 존재 여부 확인 - provider: $provider, providerId: $providerId" }
            
            UserOAuth.find {
                (UserOAuths.provider eq provider) and (UserOAuths.providerId eq providerId)
            }.count() > 0
        }
    }

    override fun existsByUserIdAndProvider(userId: Long, provider: String): Boolean {
        return transaction {
            logger.debug { "사용자 OAuth 연동 정보 존재 여부 확인 - userId: $userId, provider: $provider" }
            
            UserOAuth.find {
                (UserOAuths.userId eq userId) and (UserOAuths.provider eq provider)
            }.count() > 0
        }
    }
} 