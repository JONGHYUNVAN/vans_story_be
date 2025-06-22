package blog.vans_story_be.config.cors

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * CORS 설정 프로퍼티 클래스입니다.
 * 
 * <p>이 클래스는 환경변수에서 CORS 관련 설정을 관리하며,
 * CORS 정책을 유연하게 구성할 수 있게 합니다.</p>
 * 
 * <h3>환경변수 설정:</h3>
 * <ul>
 *   <li>CORS_ALLOWED_ORIGINS: 쉼표로 구분된 허용 출처 목록</li>
 * </ul>
 * 
 * <h3>설정 예시:</h3>
 * <pre>
 * CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,https://vans-story.com
 * </pre>
 * 
 * <h3>사용 시나리오:</h3>
 * <ul>
 *   <li>개발/운영 환경별 CORS 정책 분리</li>
 *   <li>동적 CORS 설정 변경</li>
 *   <li>환경 변수를 통한 CORS 설정 관리</li>
 * </ul>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
@Component
class CorsProperties {

    @Value("\${CORS_ALLOWED_ORIGINS:http://localhost:3000}")
    private lateinit var allowedOriginsString: String

    val allowedOrigins: List<String>
        get() = allowedOriginsString
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        
    val allowedMethods: List<String> = listOf(
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    )
    val allowedHeaders: List<String> = listOf(
        "Authorization",
        "Content-Type",
        "Accept",
        "Origin",
        "X-Requested-With"
    )
    val exposedHeaders: List<String> = listOf(
        "Authorization"  // 클라이언트에서 접근 가능한 헤더
    )
    val maxAge: Long = 3600L
    val allowCredentials: Boolean = true
} 