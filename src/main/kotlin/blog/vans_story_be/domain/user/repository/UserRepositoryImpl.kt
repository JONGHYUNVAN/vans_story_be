package blog.vans_story_be.domain.user.repository

import blog.vans_story_be.domain.user.entity.QUser
import blog.vans_story_be.domain.user.entity.User
import com.querydsl.jpa.impl.JPAQueryFactory
import mu.KotlinLogging
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * [UserRepositoryCustom] 인터페이스의 구현체
 * QueryDSL을 사용하여 사용자 쿼리를 구현합니다.
 *
 * 주요 기능:
 * - QueryDSL을 사용한 동적 쿼리 구현
 * - 모든 사용자 조회
 * - ID로 사용자 조회
 *
 * 사용 예시:
 * ```kotlin
 * @Service
 * class UserService(
 *     private val userRepository: UserRepository  // UserRepositoryImpl이 주입됨
 * ) {
 *     fun getAllUsers(): List<User> =
 *         userRepository.findAllUsers()
 *             .also { log.info { "조회된 사용자 수: ${it.size}" } }
 *
 *     fun getUserById(id: Long): User =
 *         userRepository.findUserById(id)
 *             .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
 *             .also { log.info { "조회된 사용자: ${it.username}" } }
 * }
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
@Repository
class UserRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : UserRepositoryCustom {

    companion object {
        private val log = KotlinLogging.logger {}
        private val user = QUser.user
    }

    /**
     * 모든 사용자 정보를 조회합니다.
     *
     * @return 전체 사용자 목록
     *
     * QueryDSL 동작:
     * ```sql
     * 1. selectFrom(user)
     *    SELECT id, name, email, password, role, created_at, updated_at
     *    FROM users user
     *
     * 2. fetch()
     *    결과를 List<User>로 변환하여 반환
     * ```
     *
     * 사용 예시:
     * ```kotlin
     * val users = userRepository.findAllUsers()
     * users.forEach { user ->
     *     log.info { "사용자: ${user.username}, 이메일: ${user.email}" }
     * }
     * ```
     */
    override fun findAllUsers(): List<User> =
        queryFactory
            .selectFrom(user)
            .fetch()
            .also { log.debug { "모든 사용자 조회 완료: ${it.size}명" } }

    /**
     * 사용자 ID로 사용자를 조회합니다.
     *
     * @param id 조회할 사용자 ID
     * @return 조회된 사용자 (존재하지 않을 경우 빈 Optional)
     *
     * QueryDSL 동작:
     * ```sql
     * 1. selectFrom(user)
     *    SELECT id, name, email, password, role, created_at, updated_at
     *    FROM users user
     *
     * 2. where(user.id.eq(id))
     *    WHERE user.id = :id
     *
     * 3. fetchOne()
     *    결과를 단일 User 객체로 변환하여 반환
     * ```
     *
     * 사용 예시:
     * ```kotlin
     * val user = userRepository.findUserById(1L)
     *     .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
     *     .also { log.info { "조회된 사용자: ${it.username}" } }
     * ```
     */
    override fun findUserById(id: Long): Optional<User> =
        queryFactory
            .selectFrom(user)
            .where(user.id.eq(id))
            .fetchOne()
            .let { Optional.ofNullable(it) }
            .also { log.debug { "사용자 조회 완료: id=$id, exists=${it.isPresent}" } }
} 