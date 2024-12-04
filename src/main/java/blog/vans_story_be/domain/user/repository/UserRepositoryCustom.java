package blog.vans_story_be.domain.user.repository;

import blog.vans_story_be.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * User 엔티티에 대한 커스텀 쿼리 메서드를 정의하는 인터페이스
 * QueryDSL을 사용하여 구현합니다.
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
public interface UserRepositoryCustom {
    List<User> findAllUsers();
    Optional<User> findUserById(Long id);
} 