package blog.vans_story_be.domain.user.repository;

import blog.vans_story_be.domain.user.entity.User;

import java.util.List;
import java.util.Optional;


public interface UserRepositoryCustom {
    
    /**
     * 모든 사용자를 조회합니다.
     * QueryDSL을 사용하여 구현됩니다.
         /**
     * 사용자명으로 사용자를 조회합니다.
     *
     * @return 조회된 사용자 (Optional)
     * @see blog.vans_story_be.domain.user.repository.UserRepositoryImpl#findAllUsers
     * @implNote 실행되는 쿼리:
     * 
     * <pre>
     * SELECT 
     *     user.id        
     *     user.username  
     *     user.email     
     *     user.password 
     *     user.role    
     *     user.created_at 
     *     user.updated_at 
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
     *     user.id      
     *     user.username  
     *     user.email  
     *     user.password  
     *     user.role     
     *     user.created_at 
     *     user.updated_at 
     * FROM 
     *     users user
     * </pre>
     */
    Optional<User> findUserById(Long id);
} 