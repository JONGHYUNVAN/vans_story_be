package blog.vans_story_be.config.jpa

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * JPA 설정 클래스입니다.
 * 
 * <p>JPA Auditing과 QueryDSL을 사용하기 위한 기본 설정을 제공합니다.
 * 엔티티의 생성/수정 시간 자동 기록과 타입 세이프한 쿼리 작성을 지원합니다.</p>
 * 
 * <h4>주요 기능:</h4>
 * <ul>
 *   <li>JPA Auditing 활성화 (@CreatedDate, @LastModifiedDate 등)</li>
 *   <li>QueryDSL JPAQueryFactory 설정</li>
 *   <li>타입 세이프한 쿼리 작성 지원</li>
 * </ul>
 * 
 * <h4>사용 예시:</h4>
 * <pre>
 * // JPA Auditing 사용
 * @Entity
 * class User(
 *     @CreatedDate
 *     val createdAt: LocalDateTime,
 *     @LastModifiedDate
 *     val updatedAt: LocalDateTime
 * )
 * 
 * // QueryDSL 사용
 * @Repository
 * class UserRepositoryImpl(
 *     private val queryFactory: JPAQueryFactory
 * ) {
 *     fun findByUsername(username: String): User? {
 *         return queryFactory
 *             .selectFrom(QUser.user)
 *             .where(QUser.user.username.eq(username))
 *             .fetchOne()
 *     }
 * }
 * </pre>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see org.springframework.data.jpa.repository.config.EnableJpaAuditing
 * @see com.querydsl.jpa.impl.JPAQueryFactory
 */
@Configuration
@EnableJpaAuditing
class JpaConfig {
    
    /**
     * JPA EntityManager 인스턴스입니다.
     * 
     * <p>영속성 컨텍스트에 접근하기 위한 EntityManager를 주입받습니다.
     * QueryDSL의 JPAQueryFactory 생성에 사용됩니다.</p>
     */
    @PersistenceContext
    private lateinit var entityManager: EntityManager
    
    /**
     * QueryDSL을 사용하기 위한 JPAQueryFactory를 생성합니다.
     * 
     * <p>타입 세이프한 쿼리 작성을 지원하는 QueryDSL의 핵심 컴포넌트입니다.
     * 빈으로 등록되어 필요한 곳에서 주입받아 사용할 수 있습니다.</p>
     * 
     * <h4>주요 특징:</h4>
     * <ul>
     *   <li>타입 세이프한 쿼리 작성</li>
     *   <li>메타데이터 기반 쿼리 생성</li>
     *   <li>동적 쿼리 작성 지원</li>
     * </ul>
     * 
     * @return JPAQueryFactory 인스턴스
     * @see com.querydsl.jpa.impl.JPAQueryFactory
     */
    @Bean
    fun jpaQueryFactory(): JPAQueryFactory = JPAQueryFactory(entityManager)
} 