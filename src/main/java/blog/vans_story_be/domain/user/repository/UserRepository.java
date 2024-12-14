package blog.vans_story_be.domain.user.repository;

import blog.vans_story_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 정보에 대한 데이터 액세스를 제공하는 리포지토리 인터페이스입니다.</p>
 * Spring Data JPA의 커스텀 리포지토리 패턴을 구현합니다.
 * 
 * <p>레포지토리 구조:</p>
 * <ul>
 *   <li>{@code UserRepository} - JpaRepository와 커스텀 인터페이스를 함께 상속</li>
 *   <li>{@code UserRepositoryCustom} - 커스텀 메서드 정의</li>
 *   <li>{@code UserRepositoryImpl} - QueryDSL을 사용한 구현체</li>
 * </ul>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 * @see blog.vans_story_be.domain.user.repository.UserRepositoryImpl
 * @see blog.vans_story_be.domain.user.repository.UserRepositoryCustom
 */
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    
    /**
     * name으로 User를 조회합니다.
     * 
     * @param name 조회할 사용자명
     * @return 조회된 User (Optional)
     */
    Optional<User> findByName(String name);
    
    /**
     * email로 User를 조회합니다.
     * 
     * @param email 조회할 이메일
     * @return 조회된 User (Optional)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 해당 name의 User가 존재하는지 확인합니다.
     * 
     * @param name 확인할 name
     * @return 존재 여부
     */
    boolean existsByName(String name);
    
    /**
     * 해당 email의 User가 존재하는지 확인합니다.
     * 
     * @param email 확인할 email
     * @return 존재 여부
     */
    boolean existsByEmail(String email);
} 