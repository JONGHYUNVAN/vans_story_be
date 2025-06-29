package blog.vans_story_be.domain.user.repository

import blog.vans_story_be.domain.user.entity.User
import blog.vans_story_be.domain.user.entity.Users
import blog.vans_story_be.domain.user.entity.Role
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * 사용자 정보에 대한 데이터 액세스를 제공하는 리포지토리 인터페이스
 *
 * 리포지토리 구조:
 * - [UserRepository]: JpaRepository와 커스텀 인터페이스를 함께 상속
 * - [UserRepositoryCustom]: 커스텀 메서드 정의
 * - [UserRepositoryImpl]: QueryDSL을 사용한 구현체
 *
 * 주요 기능:
 * - 기본 CRUD 작업 (JpaRepository)
 * - 사용자명/이메일로 조회
 * - 사용자명/이메일 존재 여부 확인
 * - 커스텀 쿼리 (UserRepositoryCustom)
 *
 * 사용 예시:
 * ```kotlin
 * @Service
 * class UserService(
 *     private val userRepository: UserRepository
 * ) {
 *     fun findUserByEmail(email: String): User? =
 *         userRepository.findByEmail(email).orElse(null)
 *
 *     fun isEmailExists(email: String): Boolean =
 *         userRepository.existsByEmail(email)
 * }
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 */
@Repository
interface UserRepository {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun save(user: User): User
    fun delete(user: User)
    fun findAllUsers(): List<User>
    fun findUserById(id: Long): Optional<User>
    fun existsByNickname(nickname: String): Boolean
    fun findAll(): List<User>
    fun findById(id: Long): User?
} 