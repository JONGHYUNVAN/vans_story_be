package blog.vans_story_be.config.init;

import blog.vans_story_be.domain.user.dto.UserDto;
import blog.vans_story_be.domain.user.entity.Role;
import blog.vans_story_be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) {
        createAdminIfNotExists();
    }

    private void createAdminIfNotExists() {
        UserDto.CreateRequest adminRequest = UserDto.CreateRequest.builder()
                .username("admin")
                .password("admin1234")
                .email("admin@vans-story.com")
                .role(Role.ADMIN)
                .build();

        try {
            userService.createUser(adminRequest);
        } catch (Exception e) {
            // 이미 존재하는 경우 무시
        }
    }
} 