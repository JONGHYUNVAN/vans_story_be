package blog.vans_story_be.config.apiLogger;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.extern.slf4j.Slf4j;

/**
 * HTTP 요청/응답에 대한 로깅을 수행하는 Spring MVC 인터셉터입니다.
 * 
 * <p>이 인터셉터는 모든 HTTP 요청의 시작과 완료 시점에 로그를 기록하여
 * API 호출 추적, 성능 모니터링, 디버깅을 지원합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>요청 시작 시점에 HTTP 메서드와 URI 로깅</li>
 *   <li>응답 완료 시점에 HTTP 상태 코드 로깅</li>
 *   <li>예외 발생 시에도 응답 상태 추적</li>
 * </ul>
 * 
 * <h3>로깅 출력 예시:</h3>
 * <pre>
 * {@code
 * // 요청 시작 시
 * INFO  [main] --- <<< Api Request GET /api/v1/users
 * INFO  [main] --- <<< Api Request POST /api/v1/auth/login
 * 
 * // 응답 완료 시
 * INFO  [main] --- >>> Api Response GET /api/v1/users status: 200
 * INFO  [main] --- >>> Api Response POST /api/v1/auth/login status: 401
 * }
 * </pre>
 * 
 * <h3>설정 방법:</h3>
 * <p>이 인터셉터는 {@link blog.vans_story_be.config.apiLogger.WebConfig}에서
 * Spring MVC 인터셉터 체인에 자동으로 등록됩니다.</p>
 * 
 * <h3>사용 시나리오:</h3>
 * <ul>
 *   <li>API 호출 패턴 분석</li>
 *   <li>응답 시간 측정을 위한 기초 데이터 수집</li>
 *   <li>운영 환경에서의 API 트래픽 모니터링</li>
 *   <li>개발 환경에서의 디버깅 지원</li>
 * </ul>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 * @see org.springframework.web.servlet.HandlerInterceptor
 * @see blog.vans_story_be.config.apiLogger.WebConfig
 */
@Slf4j
public class ApiRequestInterceptor implements HandlerInterceptor {
      
    /**
     * HTTP 요청 처리 전에 호출되어 요청 정보를 로깅합니다.
     * 
     * <p>이 메서드는 Spring MVC의 {@link HandlerInterceptor#preHandle} 라이프사이클에서
     * 실제 컨트롤러 메서드가 실행되기 전에 호출됩니다.</p>
     * 
     * <h4>로깅 정보:</h4>
     * <ul>
     *   <li>HTTP 메서드 (GET, POST, PUT, DELETE 등)</li>
     *   <li>요청 URI (쿼리 파라미터 제외)</li>
     *   <li>로그 레벨: INFO</li>
     * </ul>
     * 
     * <h4>로깅 예시:</h4>
     * <pre>
     * {@code
     * INFO  [http-nio-8080-exec-1] --- <<< Api Request GET /api/v1/users/123
     * INFO  [http-nio-8080-exec-2] --- <<< Api Request POST /api/v1/auth/login
     * INFO  [http-nio-8080-exec-3] --- <<< Api Request DELETE /api/v1/posts/456
     * }
     * </pre>
     * 
     * @param request  클라이언트로부터 받은 HTTP 요청 객체
     * @param response 클라이언트로 보낼 HTTP 응답 객체
     * @param handler  요청을 처리할 컨트롤러 메서드나 핸들러 객체
     * @return {@code true} - 항상 true를 반환하여 요청 처리를 계속 진행
     * @implNote 이 메서드는 예외를 발생시키지 않으며, 로깅 실패 시에도 요청 처리에 영향을 주지 않습니다.
     */
    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request, 
        @NonNull HttpServletResponse response, 
        @NonNull Object handler
    ) {
        log.info("<<< Api Request {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    /**
     * HTTP 요청 처리 완료 후에 호출되어 응답 정보를 로깅합니다.
     * 
     * <p>이 메서드는 컨트롤러 메서드 실행과 뷰 렌더링이 모두 완료된 후에 호출되며,
     * 예외가 발생한 경우에도 실행되어 최종 응답 상태를 기록합니다.</p>
     * 
     * <h4>로깅 정보:</h4>
     * <ul>
     *   <li>HTTP 메서드</li>
     *   <li>요청 URI</li>
     *   <li>HTTP 응답 상태 코드 (200, 404, 500 등)</li>
     *   <li>로그 레벨: INFO</li>
     * </ul>
     * 
     * <h4>로깅 예시:</h4>
     * <pre>
     * {@code
     * 성공적인 응답
     * INFO  [http-nio-8080-exec-1] --- >>> Api Response GET /api/v1/users/123 status: 200
     * INFO  [http-nio-8080-exec-2] --- >>> Api Response POST /api/v1/posts status: 201
     * 
     * 클라이언트 오류
     * INFO  [http-nio-8080-exec-3] --- >>> Api Response GET /api/v1/users/999 status: 404
     * INFO  [http-nio-8080-exec-4] --- >>> Api Response POST /api/v1/auth/login status: 401
     * 
     * 서버 오류
     * INFO  [http-nio-8080-exec-5] --- >>> Api Response GET /api/v1/posts status: 500
     * }
     * </pre>
     * 
     * @param request  클라이언트로부터 받은 HTTP 요청 객체
     * @param response 클라이언트로 보낸 HTTP 응답 객체
     * @param handler  요청을 처리한 컨트롤러 메서드나 핸들러 객체
     * @param ex       요청 처리 중 발생한 예외 (없을 경우 null)
     * @throws Exception 로깅 중 예외가 발생할 수 있지만, 실제로는 발생하지 않음
     * @implNote 예외 파라미터가 null이 아닌 경우에도 응답 상태 코드는 정상적으로 로깅됩니다.
     */
    @Override
    public void afterCompletion(
        @NonNull HttpServletRequest request, 
        @NonNull HttpServletResponse response, 
        @NonNull Object handler,
        @Nullable Exception ex
    ) throws Exception {
        log.info(">>> Api Response {} {} status: {}", request.getMethod(), request.getRequestURI(), response.getStatus());
    }
}
