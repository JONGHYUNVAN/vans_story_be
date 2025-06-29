package blog.vans_story_be.domain.oauth.repository

import blog.vans_story_be.domain.oauth.entity.UserOAuth

/**
 * OAuth 연동 정보 데이터 접근을 위한 Repository 인터페이스
 *
 * 주요 기능:
 * - OAuth 연동 정보 CRUD 작업
 * - Provider별 조회 기능
 * - 사용자별 OAuth 계정 관리
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.01.09
 */
interface OAuthRepository {

    /**
     * OAuth 연동 정보를 저장합니다.
     *
     * @param userId 사용자 ID
     * @param provider OAuth 제공업체
     * @param providerId OAuth 제공업체 사용자 ID
     * @param providerEmail OAuth 제공업체 이메일 (선택적)
     * @return 저장된 OAuth 연동 정보
     */
    fun save(userId: Long, provider: String, providerId: String, providerEmail: String?): UserOAuth

    /**
     * Provider와 Provider ID로 OAuth 연동 정보를 조회합니다.
     *
     * @param provider OAuth 제공업체
     * @param providerId OAuth 제공업체 사용자 ID
     * @return OAuth 연동 정보 (없으면 null)
     */
    fun findByProviderAndProviderId(provider: String, providerId: String): UserOAuth?

    /**
     * 사용자 ID로 모든 OAuth 연동 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return OAuth 연동 정보 목록
     */
    fun findAllByUserId(userId: Long): List<UserOAuth>

    /**
     * 사용자 ID와 Provider로 OAuth 연동 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param provider OAuth 제공업체
     * @return OAuth 연동 정보 (없으면 null)
     */
    fun findByUserIdAndProvider(userId: Long, provider: String): UserOAuth?

    /**
     * OAuth 연동 정보를 삭제합니다.
     *
     * @param userId 사용자 ID
     * @param provider OAuth 제공업체
     * @return 삭제 성공 여부
     */
    fun deleteByUserIdAndProvider(userId: Long, provider: String): Boolean

    /**
     * Provider와 Provider ID로 OAuth 연동 정보가 존재하는지 확인합니다.
     *
     * @param provider OAuth 제공업체
     * @param providerId OAuth 제공업체 사용자 ID
     * @return 존재 여부
     */
    fun existsByProviderAndProviderId(provider: String, providerId: String): Boolean

    /**
     * 사용자 ID와 Provider로 OAuth 연동 정보가 존재하는지 확인합니다.
     *
     * @param userId 사용자 ID
     * @param provider OAuth 제공업체
     * @return 존재 여부
     */
    fun existsByUserIdAndProvider(userId: Long, provider: String): Boolean
} 