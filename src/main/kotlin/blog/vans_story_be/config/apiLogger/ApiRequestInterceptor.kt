package blog.vans_story_be.config.apiLogger

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration
import java.time.Instant

/**
 * HTTP 요청/응답에 대한 로깅을 수행하는 Spring MVC 인터셉터입니다.
 * 
 * <p>이 인터셉터는 모든 HTTP 요청의 시작과 완료 시점에 로그를 기록하여
 * API 호출 추적, 성능 모니터링, 디버깅을 지원합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>요청 시작 시점에 HTTP 메서드와 URI 로깅</li>
 *   <li>응답 완료 시점에 HTTP 상태 코드와 처리 시간 로깅</li>
 *   <li>예외 발생 시 상세 정보 로깅</li>
 *   <li>MDC를 활용한 요청 추적</li>
 * </ul>
 * 
 * <h3>로깅 출력 예시:</h3>
 * <pre>
 * {@code
 * // 요청 시작 시
 * INFO  [main] --- Request started: GET /api/v1/users [ID: 550e8400-e29b-41d4-a716-446655440000]
 * 
 * // 응답 완료 시 (성공)
 * INFO  [main] --- Request completed: GET /api/v1/users [ID: 550e8400-e29b-41d4-a716-446655440000] status: 200 duration: 123ms
 * 
 * // 응답 완료 시 (클라이언트 오류)
 * WARN  [main] --- Request warning: POST /api/v1/auth/login [ID: 550e8400-e29b-41d4-a716-446655440001] status: 401 duration: 45ms
 * 
 * // 응답 완료 시 (서버 오류)
 * ERROR [main] --- Request error: GET /api/v1/posts [ID: 550e8400-e29b-41d4-a716-446655440002] status: 500 duration: 234ms
 * 
 * // 예외 발생 시
 * ERROR [main] --- Request failed: GET /api/v1/users [ID: 550e8400-e29b-41d4-a716-446655440003] status: 500 duration: 156ms
 * java.lang.RuntimeException: Database connection failed
 *     at blog.vans_story_be.service.UserService.getUser(UserService.kt:42)
 *     ...
 * }
 * </pre>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see org.springframework.web.servlet.HandlerInterceptor
 * @see blog.vans_story_be.config.apiLogger.WebConfig
 */
@Component
class ApiRequestInterceptor : HandlerInterceptor {
    /**
     * 인터셉터의 공통 상수와 로거를 관리하는 companion object입니다.
     * 
     * <p>이 객체는 클래스의 모든 인스턴스가 공유하는 정적 멤버를 포함합니다.</p>
     * 
     * <h4>포함된 멤버:</h4>
     * <ul>
     *   <li>log: KotlinLogging 인스턴스</li>
     *   <li>REQUEST_START_TIME: 요청 시작 시간을 저장하는 속성 키</li>
     *   <li>REQUEST_ID: 요청 식별자를 저장하는 속성 키</li>
     * </ul>
     */
    companion object {
        /** KotlinLogging 인스턴스로, 로그 메시지 출력에 사용됩니다. */
        private val log = KotlinLogging.logger {}
        
        /** 요청 시작 시간을 저장하는 HttpServletRequest 속성의 키입니다. */
        private const val REQUEST_START_TIME = "requestStartTime"
        
        /** 요청 식별자를 저장하는 HttpServletRequest 속성의 키입니다. */
        private const val REQUEST_ID = "requestId"
    }

    /**
     * HTTP 요청의 시작을 로깅하는 확장 함수입니다.
     * 
     * <p>이 함수는 요청의 시작 시점에 호출되어 다음 정보를 로깅합니다:</p>
     * <ul>
     *   <li>요청 ID (UUID)</li>
     *   <li>요청 시작 시간</li>
     *   <li>HTTP 메서드</li>
     *   <li>요청 URI</li>
     * </ul>
     * 
     * <h4>로깅 예시:</h4>
     * <pre>
     * {@code
     * INFO  [http-nio-8080-exec-1] --- Request started: GET /api/v1/users [ID: 550e8400-e29b-41d4-a716-446655440000]
     * }
     * </pre>
     * 
     * @receiver 로깅할 HTTP 요청 객체
     */
    private fun HttpServletRequest.logRequest() {
        val requestId = java.util.UUID.randomUUID().toString()
        setAttribute(REQUEST_ID, requestId)
        setAttribute(REQUEST_START_TIME, Instant.now())
        log.info { "Request started: $method $requestURI [ID: $requestId]" }
    }

    /**
     * HTTP 요청의 응답을 로깅하는 확장 함수입니다.
     * 
     * <p>이 함수는 요청의 완료 시점에 호출되어 다음 정보를 로깅합니다:</p>
     * <ul>
     *   <li>요청 ID</li>
     *   <li>HTTP 메서드</li>
     *   <li>요청 URI</li>
     *   <li>HTTP 상태 코드</li>
     *   <li>처리 시간 (밀리초)</li>
     *   <li>예외 정보 (발생한 경우)</li>
     * </ul>
     * 
     * <h4>로그 레벨:</h4>
     * <ul>
     *   <li>ERROR: 예외 발생 또는 5xx 상태 코드</li>
     *   <li>WARN: 4xx 상태 코드</li>
     *   <li>INFO: 2xx, 3xx 상태 코드</li>
     * </ul>
     * 
     * @receiver 로깅할 HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param ex 요청 처리 중 발생한 예외 (없을 경우 null)
     */
    private fun HttpServletRequest.logResponse(response: HttpServletResponse, ex: Exception?) {
        val requestId = getAttribute(REQUEST_ID) as? String ?: "unknown"
        val startTime = getAttribute(REQUEST_START_TIME) as? Instant
        val duration = startTime?.let { Duration.between(it, Instant.now()) }?.toMillis() ?: 0L
        
        when {
            ex != null -> log.error(ex) { 
                "Request failed: $method $requestURI [ID: $requestId] status: ${response.status} duration: ${duration}ms" 
            }
            response.status >= 500 -> log.error { 
                "Request error: $method $requestURI [ID: $requestId] status: ${response.status} duration: ${duration}ms" 
            }
            response.status >= 400 -> log.warn { 
                "Request warning: $method $requestURI [ID: $requestId] status: ${response.status} duration: ${duration}ms" 
            }
            else -> log.info { 
                "Request completed: $method $requestURI [ID: $requestId] status: ${response.status} duration: ${duration}ms" 
            }
        }
    }

    /**
     * 요청 처리 전에 호출되는 메서드입니다.
     * 
     * <p>이 메서드는 컨트롤러 메서드가 실행되기 전에 호출되어
     * 요청의 시작을 로깅합니다.</p>
     * 
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler 요청을 처리할 핸들러
     * @return 항상 true를 반환하여 요청 처리를 계속 진행
     */
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean = request.logRequest().let { true }

    /**
     * 요청 처리 완료 후에 호출되는 메서드입니다.
     * 
     * <p>이 메서드는 컨트롤러 메서드 실행과 뷰 렌더링이 모두 완료된 후에
     * 호출되어 요청의 응답을 로깅합니다.</p>
     * 
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler 요청을 처리한 핸들러
     * @param ex 요청 처리 중 발생한 예외 (없을 경우 null)
     */
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) = request.logResponse(response, ex)
} 