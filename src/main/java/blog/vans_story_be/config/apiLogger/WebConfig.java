package blog.vans_story_be.config.apiLogger;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 애플리케이션의 설정을 위한 구성 클래스입니다.
 * 
 * <p>이 클래스는 {@link WebMvcConfigurer}를 구현하여
 * Spring MVC의 웹 설정을 커스터마이징합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>API 요청/응답 로깅을 위한 인터셉터 등록</li>
 *   <li>Spring MVC의 기본 설정 확장</li>
 * </ul>
 * 
 * <h3>설정 방법:</h3>
 * <p>이 클래스는 {@code @Configuration} 어노테이션이 적용되어 있어
 * Spring Boot 애플리케이션 시작 시 자동으로 로드됩니다.</p>
 * 
 * <h3>사용 시나리오:</h3>
 * <ul>
 *   <li>API 요청/응답 로깅 설정</li>
 *   <li>Spring MVC 인터셉터 등록</li>
 *   <li>웹 애플리케이션의 전역 설정 관리</li>
 * </ul>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.12.14
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 * @see blog.vans_story_be.config.apiLogger.ApiRequestInterceptor
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Spring MVC 인터셉터를 등록합니다.
     * 
     * <p>이 메서드는 {@link WebMvcConfigurer#addInterceptors}를 오버라이드하여
     * API 요청/응답 로깅을 위한 인터셉터를 등록합니다.</p>
     * 
     * <h4>등록되는 인터셉터:</h4>
     * <ul>
     *   <li>{@link ApiRequestInterceptor} - API 요청/응답 로깅</li>
     * </ul>
     * 
     * <h4>설정 예시:</h4>
     * <pre>
     * {@code
     * @Override
     * public void addInterceptors(InterceptorRegistry registry) {
     *     registry.addInterceptor(new ApiRequestInterceptor());
     * }
     * }
     * </pre>
     * 
     * @param registry 인터셉터 등록을 위한 {@link InterceptorRegistry} 객체
     * @implNote 이 메서드는 Spring Boot 애플리케이션 시작 시 자동으로 호출됩니다.
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new ApiRequestInterceptor());
    }
}
