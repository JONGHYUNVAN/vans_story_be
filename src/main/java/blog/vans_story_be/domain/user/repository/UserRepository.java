package blog.vans_story_be.domain.user.repository;

import blog.vans_story_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스
 * Spring Data JPA를 사용하여 기본적인 CRUD 메서드를 제공합니다.
 * 추가적으로 사용자명과 이메일로 사용자 조회 메서드를 정의합니다.
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
} 