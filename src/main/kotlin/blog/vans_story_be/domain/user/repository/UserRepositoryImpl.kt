package blog.vans_story_be.domain.user.repository

import blog.vans_story_be.domain.user.entity.Role
import blog.vans_story_be.domain.user.entity.User
import blog.vans_story_be.domain.user.entity.Users
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * UserRepository 인터페이스의 구현체
 * Exposed를 사용하여 데이터베이스 작업을 수행합니다.
 * 
 * @property UserRepository 인터페이스를 구현하여 사용자 관련 데이터베이스 작업을 처리
 * @sample 사용 예시
 * ```kotlin
 * val userRepository: UserRepository = UserRepositoryImpl()
 * val user = userRepository.findByName("홍길동")
 * ```
 */
@Repository
class UserRepositoryImpl : UserRepository {
    /**
     * 이름으로 사용자 조회
     * @param name 사용자 이름
     * @return Optional<User>
     * @sample SQL
     * SELECT * FROM users WHERE name = ? LIMIT 1;
     */
    override fun findByName(name: String): Optional<User> = transaction {
        Optional.ofNullable(User.find { Users.name.eq(name) }.firstOrNull())
    }

    /**
     * 이메일로 사용자 조회
     * @param email 사용자 이메일
     * @return Optional<User>
     * @sample SQL
     * SELECT * FROM users WHERE email = ? LIMIT 1;
     */
    override fun findByEmail(email: String): Optional<User> = transaction {
        Optional.ofNullable(User.find { Users.email.eq(email) }.firstOrNull())
    }

    /**
     * 이름으로 사용자 존재 여부 확인
     * @param name 사용자 이름
     * @return Boolean
     * @sample SQL
     * SELECT EXISTS (SELECT 1 FROM users WHERE name = ? LIMIT 1);
     */
    override fun existsByName(name: String): Boolean = transaction {
        Users.select { Users.name eq name }
            .any()
    }

    /**
     * 이메일로 사용자 존재 여부 확인
     * @param email 사용자 이메일
     * @return Boolean
     * @sample SQL
     * SELECT EXISTS (SELECT 1 FROM users WHERE email = ? LIMIT 1);
     */
    override fun existsByEmail(email: String): Boolean = transaction {
        Users.select { Users.email eq email }
            .any()
    }

    /**
     * 닉네임으로 사용자 존재 여부 확인
     * @param nickname 사용자 닉네임
     * @return Boolean
     * @sample SQL
     * SELECT EXISTS (SELECT 1 FROM users WHERE nickname = ? LIMIT 1);
     */
    override fun existsByNickname(nickname: String): Boolean = transaction {
        Users.select { Users.nickname eq nickname }
            .any()
    }

    /**
     * 사용자 저장(업데이트)
     * @param user 저장할 User 엔티티
     * @return 저장된 User 엔티티
     * @sample SQL
     * UPDATE users SET ... WHERE id = ?;
     * (User 엔티티의 변경사항이 있을 때 flush 시점에 UPDATE 쿼리 발생)
     */
    override fun save(user: User): User = transaction {
        user.flush()
        user
    }

    /**
     * 사용자 삭제
     * @param user 삭제할 User 엔티티
     * @sample SQL
     * DELETE FROM users WHERE id = ?;
     */
    override fun delete(user: User) = transaction {
        user.delete()
    }

    /**
     * 모든 사용자 조회
     * @return List<User>
     * @sample SQL
     * SELECT * FROM users;
     */
    override fun findAllUsers(): List<User> = transaction {
        User.all().toList()
    }

    /**
     * ID로 사용자 조회 (Optional)
     * @param id 사용자 ID
     * @return Optional<User>
     * @sample SQL
     * SELECT * FROM users WHERE id = ? LIMIT 1;
     */
    override fun findUserById(id: Long): Optional<User> = transaction {
        Optional.ofNullable(User.findById(id))
    }

    /**
     * 모든 사용자 조회 (중복 구현)
     * @return List<User>
     * @sample SQL
     * SELECT * FROM users;
     */
    override fun findAll(): List<User> = transaction {
        User.all().toList()
    }

    /**
     * ID로 사용자 조회 (nullable)
     * @param id 사용자 ID
     * @return User?
     * @sample SQL
     * SELECT * FROM users WHERE id = ? LIMIT 1;
     */
    override fun findById(id: Long): User? = transaction {
        User.findById(id)
    }
} 