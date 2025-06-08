package blog.vans_story_be.domain.user.mapper

import blog.vans_story_be.domain.user.dto.UserDto
import blog.vans_story_be.domain.user.entity.Role
import blog.vans_story_be.domain.user.entity.User
import blog.vans_story_be.global.mapper.GenericMapper
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * User 엔티티와 DTO 간의 변환을 처리하는 매퍼 클래스
 *
 * 주요 기능:
 * - 엔티티를 DTO로 변환
 * - DTO를 엔티티로 변환
 * - 엔티티 업데이트
 * - 생성 요청을 엔티티로 변환
 *
 * 사용 예시:
 * ```kotlin
 * @Autowired
 * private lateinit var userMapper: UserMapper
 *
 * // 엔티티를 DTO로 변환
 * val userDto = userMapper.toDto(user)
 *
 * // DTO로 엔티티 업데이트
 * userMapper.updateEntity(userDto, user)
 *
 * // 생성 요청을 엔티티로 변환
 * val newUser = userMapper.toEntity(createRequest)
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 */
@Component
class UserMapper {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    fun updateEntity(dto: UserDto.UpdateRequest, entity: User) {
        GenericMapper.toEntity(dto, entity).apply {
            // password는 null이 아닐 때만 업데이트
            dto.password?.let { this.password = it }
        }
    }

    // 특수한 경우 처리
    fun toEntity(createRequest: UserDto.CreateRequest, encodedPassword: String): User = 
        User.new {
            GenericMapper.toEntity(createRequest, this)
            this.password = encodedPassword
            this.role = Role.USER
        }

    fun toResponseDto(entity: User): UserDto.Response = 
        GenericMapper.toDto(entity, UserDto.Response(
            id = entity.id.value,
            createdAt = formatDateTime(entity.createdAt),
            updatedAt = formatDateTime(entity.updatedAt)
        ))

    fun toUpdateDto(
        entity: User, 
        email: String? = null, 
        password: String? = null, 
        nickname: String? = null,
        role: Role? = null
    ): UserDto.UpdateRequest = 
        GenericMapper.toDto(entity, UserDto.UpdateRequest(
            email = email ?: entity.email,
            password = password,
            nickname = nickname ?: entity.nickname,
            role = role ?: entity.role
        ))

    private fun formatDateTime(dateTime: LocalDateTime): String =
        dateTime.format(formatter)
} 