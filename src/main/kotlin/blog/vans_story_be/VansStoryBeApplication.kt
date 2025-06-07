package blog.vans_story_be

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Vans Story 블로그 애플리케이션의 메인 클래스
 * Spring Boot 애플리케이션의 시작점입니다.
 */
@SpringBootApplication
class VansStoryBeApplication

fun main(args: Array<String>) {
    runApplication<VansStoryBeApplication>(*args )
} 