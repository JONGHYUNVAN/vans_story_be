package blog.vans_story_be.config.init;

import blog.vans_story_be.domain.user.dto.UserDto;
import blog.vans_story_be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 초기 데이터 로더 클래스
 * <p>
 * 애플리케이션 시작 시 기본 사용자 데이터를 데이터베이스에 로드합니다.
 * CommandLineRunner 인터페이스를 구현하여 애플리케이션이 시작될 때 실행됩니다.
 * 환경 변수를 통해 초기 계정 정보를 설정할 수 있습니다.
 * </p>
 * 
 * <h3>환경 변수 설정:</h3>
 * <ul>
 *   <li>ADMIN_USERNAME: 관리자 계정 아이디 (기본값: "admin")</li>
 *   <li>ADMIN_PASSWORD: 관리자 계정 비밀번호 (기본값: "admin1234!")</li>
 *   <li>ADMIN_NICKNAME: 관리자 닉네임 (기본값: "관리자")</li>
 *   <li>ADMIN_EMAIL: 관리자 이메일 (기본값: "admin@vans-story.com")</li>
 *   <li>TEST_USERNAME: 테스트 계정 아이디 (기본값: "testuser")</li>
 *   <li>TEST_PASSWORD: 테스트 계정 비밀번호 (기본값: "Test1234!")</li>
 *   <li>TEST_NICKNAME: 테스트 계정 닉네임 (기본값: "테스트 사용자")</li>
 *   <li>TEST_EMAIL: 테스트 계정 이메일 (기본값: "test@vans-story.com")</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataLoader implements CommandLineRunner {

    private final UserService userService;

    @Value("${app.init.admin.username:admin}")
    private String adminUsername;

    @Value("${app.init.admin.password:admin1234!}")
    private String adminPassword;

    @Value("${app.init.admin.nickname:관리자}")
    private String adminNickname;

    @Value("${app.init.admin.email:admin@vans-story.com}")
    private String adminEmail;

    @Value("${app.init.test.username:testuser}")
    private String testUsername;

    @Value("${app.init.test.password:Test1234!}")
    private String testPassword;

    @Value("${app.init.test.nickname:테스트 사용자}")
    private String testNickname;

    @Value("${app.init.test.email:test@vans-story.com}")
    private String testEmail;

    /**
     * 애플리케이션 시작 시 호출되는 메서드
     * <p>
     * 초기 데이터를 로드하고, 오류 발생 시 적절한 로그를 남깁니다.
     * 테이블이 존재하지 않을 경우 경고 로그를 남기고, 다음 실행 시 데이터가 초기화될 것임을 알립니다.
     * </p>
     *
     * @param args 커맨드라인 인수
     */
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

    /**
     * 초기 데이터 설정 메서드
     * <p>
     * 관리자 및 테스트 사용자 계정이 존재하지 않을 경우 생성합니다.
     * </p>
     */
    private void initializeData() {
        if (!userService.existsByName("admin")) {
            createAdminIfNotExists();
        }
        if (!userService.existsByName("testuser1!")) {
            createTestUserIfNotExists();
        }
    }

    /**
     * 관리자 계정이 존재하지 않을 경우 생성하는 메서드
     * <p>
     * 관리자 계정을 생성하고, 생성 완료 로그를 남깁니다.
     * </p>
     */
    private void createAdminIfNotExists() {
        try {
            UserDto.CreateRequest adminRequest = UserDto.CreateRequest.builder()
                    .name(adminUsername)
                    .password(adminPassword)
                    .nickname(adminNickname)
                    .email(adminEmail)
                    .build();

            userService.createAdmin(adminRequest);
            log.info("관리자 계정 생성 완료");
        } catch (Exception e) {
            log.error("관리자 계정 생성 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * 테스트 사용자 계정이 존재하지 않을 경우 생성하는 메서드
     * <p>
     * 테스트 계정을 생성하고, 생성 완료 로그를 남깁니다.
     * </p>
     */
    private void createTestUserIfNotExists() {
        try {
            UserDto.CreateRequest userRequest = UserDto.CreateRequest.builder()
                    .name(testUsername)
                    .password(testPassword)
                    .nickname(testNickname)
                    .email(testEmail)
                    .build();

            userService.createUser(userRequest);
            log.info("테스트 계정 생성 완료");
        } catch (Exception e) {
            log.error("테스트 계정 생성 중 오류 발생", e);
            throw e;
        }
    }
}