package blog.vans_story_be.domain.oauth.mapper

import blog.vans_story_be.domain.oauth.dto.OAuthDto
import blog.vans_story_be.domain.oauth.entity.UserOAuth
import blog.vans_story_be.global.mapper.GenericMapper
import org.springframework.stereotype.Component

/**
 * OAuth 엔티티와 DTO 간의 변환을 담당하는 Mapper
 *
 * 주요 기능:
 * - UserOAuth 엔티티 → OAuthDto.Response 변환
 * - OAuth 계정 목록 → LinkedAccountsResponse 변환
 * - 필요한 필드만 선택적으로 매핑
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.01.09
 */
@Component
class OAuthMapper {

    /**
     * UserOAuth 엔티티를 OAuthDto.Response로 변환합니다.
     *
     * @param entity 변환할 UserOAuth 엔티티
     * @return 변환된 OAuthDto.Response
     */
    fun toDto(entity: UserOAuth): OAuthDto.Response {
        return OAuthDto.Response(
            id = entity.id.value,
            userId = entity.userId.value,
            provider = entity.provider,
            providerId = entity.providerId,
            providerEmail = entity.providerEmail,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    /**
     * UserOAuth 엔티티 목록을 OAuthDto.Response 목록으로 변환합니다.
     *
     * @param entities 변환할 UserOAuth 엔티티 목록
     * @return 변환된 OAuthDto.Response 목록
     */
    fun toDtoList(entities: List<UserOAuth>): List<OAuthDto.Response> {
        return entities.map { toDto(it) }
    }

    /**
     * UserOAuth 엔티티를 LinkedAccount로 변환합니다.
     *
     * @param entity 변환할 UserOAuth 엔티티
     * @return 변환된 LinkedAccount
     */
    fun toLinkedAccount(entity: UserOAuth): OAuthDto.LinkedAccountsResponse.LinkedAccount {
        return OAuthDto.LinkedAccountsResponse.LinkedAccount(
            provider = entity.provider,
            providerEmail = entity.providerEmail,
            createdAt = entity.createdAt
        )
    }

    /**
     * UserOAuth 엔티티 목록을 LinkedAccountsResponse로 변환합니다.
     *
     * @param entities 변환할 UserOAuth 엔티티 목록
     * @return 변환된 LinkedAccountsResponse
     */
    fun toLinkedAccountsResponse(entities: List<UserOAuth>): OAuthDto.LinkedAccountsResponse {
        val linkedAccounts = entities.map { toLinkedAccount(it) }
        return OAuthDto.LinkedAccountsResponse(linkedAccounts)
    }

    /**
     * 단일 OAuth 계정을 LinkedAccountsResponse로 변환합니다.
     *
     * @param entity 변환할 UserOAuth 엔티티
     * @return 변환된 LinkedAccountsResponse (단일 계정)
     */
    fun toLinkedAccountsResponse(entity: UserOAuth): OAuthDto.LinkedAccountsResponse {
        return toLinkedAccountsResponse(listOf(entity))
    }
} 