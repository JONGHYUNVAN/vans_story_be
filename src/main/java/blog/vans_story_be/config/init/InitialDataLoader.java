package blog.vans_story_be.config.init;

import blog.vans_story_be.domain.user.dto.UserDto;
import blog.vans_story_be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataLoader implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) {
        try {
            initializeData();
            log.info("초기 데이터 로드 완료");
        } catch (Exception e) {
            // 테이블이 없는 경우는 무시 (첫 실행시 Hibernate가 테이블 생성)
            if (e.getMessage() != null && e.getMessage().contains("Table \"USERS\" not found")) {
                log.warn("테이블이 아직 생성되지 않았습니다. 다음 실행시 데이터가 초기화됩니다.");
                return;
            }
            log.error("초기 데이터 로드 중 오류 발생", e);
            throw new RuntimeException("필수 초기 데이터 로드 실패", e);
        }
    }

    private void initializeData() {
        if (!userService.existsByUsername("admin")) {
            createAdminIfNotExists();
        }
        if (!userService.existsByUsername("testuser")) {
            createTestUserIfNotExists();
        }
    }

    private void createAdminIfNotExists() {
        try {
            UserDto.CreateRequest adminRequest = UserDto.CreateRequest.builder()
                    .username("admin")
                    .password("admin1234")
                    .email("admin@vans-story.com")
                    .build();

            userService.createAdmin(adminRequest);
            log.info("관리자 계정 생성 완료");
        } catch (Exception e) {
            log.error("관리자 계정 생성 중 오류 발생", e);
            throw e;
        }
    }

    private void createTestUserIfNotExists() {
        try {
            UserDto.CreateRequest userRequest = UserDto.CreateRequest.builder()
                    .username("testuser")
                    .password("Test1234!")
                    .email("test@vans-story.com")
                    .build();

            userService.createUser(userRequest);
            log.info("테스트 계정 생성 완료");
        } catch (Exception e) {
            log.error("테스트 계정 생성 중 오류 발생", e);
            throw e;
        }
    }
} 