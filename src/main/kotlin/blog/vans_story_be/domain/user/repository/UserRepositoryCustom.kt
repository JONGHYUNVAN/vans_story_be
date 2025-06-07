package blog.vans_story_be.domain.user.repository

import blog.vans_story_be.domain.user.entity.User
import java.util.Optional

/**
 * 사용자 정보에 대한 커스텀 쿼리를 정의하는 인터페이스
 *
 * 이 인터페이스는 QueryDSL을 사용하여 구현되며, [UserRepositoryImpl]에서 실제 구현을 제공합니다.
 * 기본 JPA 쿼리로는 구현하기 어려운 복잡한 쿼리나 동적 쿼리를 처리합니다.
 *
 * 주요 기능:
 * - 모든 사용자 조회 (QueryDSL)
 * - ID로 사용자 조회 (QueryDSL)
 *
 * 사용 예시:
 * ```kotlin
 * @Service
 * class UserService(
 *     private val userRepository: UserRepository  // UserRepositoryCustom을 상속
 * ) {
 *     fun getAllUsers(): List<User> =
 *         userRepository.findAllUsers()
 *
 *     fun getUserById(id: Long): User =
 *         userRepository.findUserById(id)
 *             .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
 * }
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
interface UserRepositoryCustom {
    /**
     * 모든 사용자를 조회합니다.
     * QueryDSL을 사용하여 구현됩니다.
     *
     * @return 조회된 사용자 목록
     * @see UserRepositoryImpl.findAllUsers
     *
     * 실행되는 쿼리:
     * ```sql
     * SELECT 
     *     user.id,        
     *     user.username,  
     *     user.email,     
     *     user.password, 
     *     user.role,    
     *     user.created_at, 
     *     user.updated_at 
     * FROM 
     *     users user
     * ```
     *
     * 사용 예시:
     * ```kotlin
     * val users = userRepository.findAllUsers()
     * users.forEach { user ->
     *     println("사용자: ${user.username}, 이메일: ${user.email}")
     * }
     * ```
     */
    fun findAllUsers(): List<User>

    /**
     * ID로 사용자를 조회합니다.
     * QueryDSL을 사용하여 구현됩니다.
     *
     * @param id 조회할 사용자 ID
     * @return 조회된 사용자 (Optional)
     * @see UserRepositoryImpl.findUserById
     *
     * 실행되는 쿼리:
     * ```sql
     * SELECT 
     *     user.id,      
     *     user.username,  
     *     user.email,  
     *     user.password,  
     *     user.role,     
     *     user.created_at, 
     *     user.updated_at 
     * FROM 
     *     users user
     * WHERE
     *     user.id = :id
     * ```
     *
     * 사용 예시:
     * ```kotlin
     * val user = userRepository.findUserById(1L)
     *     .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
     * println("조회된 사용자: ${user.username}")
     * ```
     */
    fun findUserById(id: Long): Optional<User>
} 