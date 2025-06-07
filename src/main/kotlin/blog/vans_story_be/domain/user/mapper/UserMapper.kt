package blog.vans_story_be.domain.user.mapper

import blog.vans_story_be.domain.user.dto.UserDto
import blog.vans_story_be.domain.user.entity.Role
import blog.vans_story_be.domain.user.entity.User
import blog.vans_story_be.global.mapper.GenericMapper
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget

/**
 * User 엔티티와 DTO 간의 변환을 처리하는 매퍼 인터페이스
 *
 * MapStruct를 사용하여 엔티티와 DTO 간의 변환 로직을 자동으로 생성합니다.
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
@Mapper(componentModel = "spring")
interface UserMapper : GenericMapper<UserDto.Response, User> {

    override
    @Mapping(target = "password", ignore = true)
    fun updateEntity(dto: UserDto.Response, @MappingTarget entity: User)

    override
    @Mapping(target = "password", ignore = true)
    fun toEntity(dto: UserDto.Response): User

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "role", constant = "USER")
    fun toEntity(createRequest: UserDto.CreateRequest): User

    override
    fun toDto(user: User): UserDto.Response
} 