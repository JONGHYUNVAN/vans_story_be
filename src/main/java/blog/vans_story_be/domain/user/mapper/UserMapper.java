package blog.vans_story_be.domain.user.mapper;

import blog.vans_story_be.domain.user.dto.UserDto;
import blog.vans_story_be.domain.user.entity.User;
import blog.vans_story_be.global.mapper.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * User 엔티티와 DTO 간의 변환을 처리하는 매퍼 인터페이스
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
@Mapper(componentModel = "spring")
public interface UserMapper extends GenericMapper<UserDto.Response, User> {

    @Override
    @Mapping(target = "password", ignore = true)
    void updateEntity(UserDto.Response dto, @MappingTarget User entity);

    @Override
    @Mapping(target = "password", ignore = true)
    User toEntity(UserDto.Response dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", expression = "java(blog.vans_story_be.domain.user.entity.Role.USER)")
    User toEntity(UserDto.CreateRequest createRequest);

    @Override
    UserDto.Response toDto(User user);
}