package blog.vans_story_be.config.apiLogger;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 애플리케이션의 설정을 위한 구성 클래스입니다.
 * <p>
 * {@link WebMvcConfigurer}를 구현하여
 * API 요청에 대한 인터셉터를 등록합니다.
 * </p>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.12.14
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 인터셉터를 등록합니다.
     * 
     * @param registry 인터셉터 등록을 위한 {@link InterceptorRegistry} 객체
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new ApiRequestInterceptor());
    }
}
