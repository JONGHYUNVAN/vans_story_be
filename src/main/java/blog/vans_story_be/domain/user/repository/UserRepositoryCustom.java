package blog.vans_story_be.domain.user.repository;

import blog.vans_story_be.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * User 엔티티에 대한 커스텀 쿼리 메서드를 정의하는 인터페이스입니다.
 * Spring Data JPA의 커스텀 레포지토리 패턴을 사용합니다.
 * 
 * <p>이 인터페이스는 다음과 같은 구조로 사용됩니다:</p>
 * <pre>
 * 1. UserRepositoryCustom (이 인터페이스) - 커스텀 메서드 정의
 * 2. UserRepositoryImpl - QueryDSL을 사용한 구현체
 * 3. UserRepository - JpaRepository와 이 인터페이스를 함께 상속
 * </pre>
 * 
 * <p>네이밍 규칙:</p>
 * <ul>
 *   <li>인터페이스: [Repository이름]Custom</li>
 *   <li>구현체: [Repository이름]Impl</li>
 * </ul>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 * @see blog.vans_story_be.domain.user.repository.UserRepositoryImpl
 * @see blog.vans_story_be.domain.user.repository.UserRepository
 */
public interface UserRepositoryCustom {
    
    /**
     * 모든 사용자를 조회합니다.
     * QueryDSL을 사용하여 구현됩니다.
         /**
     * 사용자명으로 사용자를 조회합니다.
     * 
     * @param username 조회할 사용자명
     * @return 조회된 사용자 (Optional)
     * @see blog.vans_story_be.domain.user.repository.UserRepositoryImpl#findUserById(String)
     * @implNote 실행되는 쿼리:
     * 
     * <pre>
     * SELECT 
     *     user.id         AS user_id,
     *     user.username   AS user_username,
     *     user.email      AS user_email,
     *     user.password   AS user_password,
     *     user.role       AS user_role,
     *     user.created_at AS user_created_at,
     *     user.updated_at AS user_updated_at
     * FROM 
     *     users user
     * </pre>
     *
     */
    List<User> findAllUsers();

    /**
     * ID로 사용자를 조회합니다.
     * QueryDSL을 사용하여 구현됩니다.
     * 
     * @param id 조회할 사용자 ID
     * @return 조회된 사용자 (Optional)
     * @see blog.vans_story_be.domain.user.repository.UserRepositoryImpl#findUserById(Long)
     * @implNote 실행되는 쿼리:
     * <pre>
     * SELECT 
     *     user.id         AS user_id,
     *     user.username   AS user_username,
     *     user.email      AS user_email,
     *     user.password   AS user_password,
     *     user.role       AS user_role,
     *     user.created_at AS user_created_at,
     *     user.updated_at AS user_updated_at
     * FROM 
     *     users user
     * </pre>
     */
    Optional<User> findUserById(Long id);
} 