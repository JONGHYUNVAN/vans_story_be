package blog.vans_story_be.domain.user.repository

import blog.vans_story_be.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
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
 * @since 2024.03.19
 */
@Repository
interface UserRepository : JpaRepository<User, Long>, UserRepositoryCustom {
    /**
     * 사용자명으로 사용자를 조회합니다.
     *
     * @param name 조회할 사용자명
     * @return 조회된 사용자 (Optional)
     *
     * 사용 예시:
     * ```kotlin
     * val user = userRepository.findByName("홍길동")
     *     .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
     * ```
     */
    fun findByName(name: String): Optional<User>

    /**
     * 이메일로 사용자를 조회합니다.
     *
     * @param email 조회할 이메일
     * @return 조회된 사용자 (Optional)
     *
     * 사용 예시:
     * ```kotlin
     * val user = userRepository.findByEmail("user@example.com")
     *     .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
     * ```
     */
    fun findByEmail(email: String): Optional<User>

    /**
     * 사용자명이 이미 존재하는지 확인합니다.
     *
     * @param name 확인할 사용자명
     * @return 존재 여부
     *
     * 사용 예시:
     * ```kotlin
     * if (userRepository.existsByName("홍길동")) {
     *     throw IllegalArgumentException("이미 존재하는 사용자명입니다")
     * }
     * ```
     */
    fun existsByName(name: String): Boolean

    /**
     * 이메일이 이미 존재하는지 확인합니다.
     *
     * @param email 확인할 이메일
     * @return 존재 여부
     *
     * 사용 예시:
     * ```kotlin
     * if (userRepository.existsByEmail("user@example.com")) {
     *     throw IllegalArgumentException("이미 존재하는 이메일입니다")
     * }
     * ```
     */
    fun existsByEmail(email: String): Boolean
} 