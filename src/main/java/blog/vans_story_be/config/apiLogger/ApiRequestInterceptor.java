package blog.vans_story_be.config.apiLogger;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.extern.slf4j.Slf4j;

/**
 * API 요청 및 응답을 로깅하는 인터셉터 클래스입니다.
 * <p>
 * 이 클래스는 {@link HandlerInterceptor}를 구현하여
 * HTTP 요청과 응답에 대한 정보를 로깅합니다.
 * </p>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
@Slf4j
public class ApiRequestInterceptor implements HandlerInterceptor {
      
    /**
     * 요청을 처리하기 전에 호출됩니다.
     * 
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler  처리할 핸들러 객체
     * @return true를 반환하면 다음 인터셉터 또는 핸들러로 진행, false를 반환하면 요청 처리를 중단
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
     * 요청 처리가 완료된 후 호출됩니다.
     * 
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler  처리한 핸들러 객체
     * @param ex      발생한 예외 (있을 경우)
     * @throws Exception 예외가 발생할 경우
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
