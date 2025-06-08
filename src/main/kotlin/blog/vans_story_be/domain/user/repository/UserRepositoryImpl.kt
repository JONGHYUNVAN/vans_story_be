package blog.vans_story_be.domain.user.repository

import blog.vans_story_be.domain.user.entity.Role
import blog.vans_story_be.domain.user.entity.User
import blog.vans_story_be.domain.user.entity.Users
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class UserRepositoryImpl : UserRepository {
    override fun findByName(name: String): Optional<User> = transaction {
        Optional.ofNullable(User.find { Users.name.eq(name) }.firstOrNull())
    }

    override fun findByEmail(email: String): Optional<User> = transaction {
        Optional.ofNullable(User.find { Users.email.eq(email) }.firstOrNull())
    }

    override fun existsByName(name: String): Boolean = transaction {
        return@transaction Users.select { Users.name eq name }.count() > 0L
    }

    override fun existsByEmail(email: String): Boolean = transaction {
        return@transaction Users.select { Users.email eq email }.count() > 0L
    }

    override fun existsByNickname(nickname: String): Boolean = transaction {
        return@transaction Users.select { Users.nickname eq nickname }.count() > 0L
    }

    override fun save(user: User): User = transaction {
        user.flush()
        user
    }

    override fun delete(user: User) = transaction {
        user.delete()
    }

    override fun findAllUsers(): List<User> = transaction {
        User.all().toList()
    }

    override fun findUserById(id: Long): Optional<User> = transaction {
        Optional.ofNullable(User.findById(id))
    }

    override fun findAll(): List<User> = transaction {
        User.all().toList()
    }

    override fun findById(id: Long): User? = transaction {
        User.findById(id)
    }
} 