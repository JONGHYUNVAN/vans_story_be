package blog.vans_story_be.domain.user.repository;

import blog.vans_story_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User 엔티티에 대한 데이터 접근을 처리하는 메인 리포지토리 인터페이스입니다.
 * 
 * <ul>
 *   <li>JpaRepository: 기본 CRUD 작업</li>
 *   <li>UserRepositoryCustom: QueryDSL을 사용한 커스텀 쿼리</li>
 *   <li>추가 메서드: 사용자명, 이메일 관련 쿼리</li>
 * </ul>
 * 
 * <p>레포지토리 구조:</p>
 * <pre>
 * - UserRepository (이 인터페이스)
 *   - JpaRepository: 기본 CRUD
 *   - UserRepositoryCustom: 커스텀 쿼리
 *   - 추가 쿼리 메서드
 * </pre>
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 * @see blog.vans_story_be.domain.user.repository.UserRepositoryCustom
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    
    /**
     * 사용자명으로 사용자를 조회합니다.
     * 
     * @param name 조회할 사용자명
     * @return 조회된 사용자 (Optional)
     */
    Optional<User> findByName(String name);
    
    /**
     * 이메일로 사용자를 조회합니다.
     * 
     * @param email 조회할 이메일
     * @return 조회된 사용자 (Optional)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 사용자명 존재 여부를 확인합니다.
     * 
     * @param name 확인할 사용자명
     * @return 존재 여부
     */
    boolean existsByName(String name);
    
    /**
     * 이메일 존재 여부를 확인합니다.
     * 
     * @param email 확인할 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);
} 