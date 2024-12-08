package blog.vans_story_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Vans Story 블로그 애플리케이션의 메인 클래스
 * Spring Boot 애플리케이션의 시작점입니다.
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
@SpringBootApplication
public class VansStoryBeApplication {

    /**
     * 애플리케이션의 메인 메서드
     * Spring Boot 애플리케이션을 실행합니다.
     *
     * @start f5
     */
    public static void main(String[] args) {
        SpringApplication.run(VansStoryBeApplication.class, args);
    }
} 