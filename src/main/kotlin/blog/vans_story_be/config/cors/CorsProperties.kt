package blog.vans_story_be.config.cors

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * CORS 설정 프로퍼티 클래스입니다.
 * 
 * <p>이 클래스는 application.yml에서 CORS 관련 설정을 관리하며,
 * 외부 설정 파일을 통해 CORS 정책을 유연하게 구성할 수 있게 합니다.</p>
 * 
 * <h3>설정 프로퍼티:</h3>
 * <ul>
 *   <li>allowedOrigins: 허용된 출처 목록</li>
 *   <li>allowedMethods: 허용된 HTTP 메서드 목록</li>
 *   <li>allowedHeaders: 허용된 헤더 목록</li>
 *   <li>exposedHeaders: 클라이언트에서 접근 가능한 헤더 목록</li>
 *   <li>maxAge: 프리플라이트 요청의 캐시 시간 (초)</li>
 *   <li>allowCredentials: 인증 정보 전송 허용 여부</li>
 * </ul>
 * 
 * <h3>설정 예시 (application.yml):</h3>
 * <pre>
 * {@code
 * cors:
 *   allowed-origins:
 *     - http://localhost:3000
 *     - http://localhost:5173
 *     - https://vans-story.com
 *   allowed-methods:
 *     - GET
 *     - POST
 *     - PUT
 *     - DELETE
 *   allowed-headers:
 *     - Authorization
 *     - Content-Type
 *     - Accept
 *     - Origin
 *     - X-Requested-With
 *   exposed-headers:
 *     - Authorization
 *   max-age: 3600
 *   allow-credentials: true
 * }
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
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 */
@ConfigurationProperties(prefix = "cors")
@Component
class CorsProperties {
    var allowedOrigins: List<String> = listOf(
        "http://localhost:3000",  // 개발 환경
        "https://vans-story.com"  // 프로덕션 환경
    )
    var allowedMethods: List<String> = listOf(
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    )
    var allowedHeaders: List<String> = listOf(
        "Authorization",
        "Content-Type",
        "Accept",
        "Origin",
        "X-Requested-With"
    )
    var exposedHeaders: List<String> = listOf(
        "Authorization"  // 클라이언트에서 접근 가능한 헤더
    )
    var maxAge: Long = 3600L
    var allowCredentials: Boolean = true
} 