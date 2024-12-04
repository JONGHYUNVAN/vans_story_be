package blog.vans_story_be.config.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정을 위한 Configuration 클래스
 * JPA Auditing 기능과 QueryDSL을 위한 JPAQueryFactory를 설정합니다.
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    
    /** JPA EntityManager */
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * QueryDSL을 사용하기 위한 JPAQueryFactory 빈을 생성합니다.
     *
     * @return JPAQueryFactory 인스턴스
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
} 