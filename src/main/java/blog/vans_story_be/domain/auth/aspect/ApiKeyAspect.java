package blog.vans_story_be.domain.auth.aspect;

import blog.vans_story_be.global.exception.CustomException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 내부 API 요청에 대한 API 키 검증을 수행하는 Aspect
 * 
 * <p>이 Aspect는 {@link RequireApiKey} 어노테이션이 적용된 메서드에 대해
 * 요청 헤더의 API 키를 검증합니다.</p>
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
@Aspect
@Component
public class ApiKeyAspect {
    
    @Value("${internal.api.key}")
    private String validApiKey;

    /**
     * API 키를 검증하는 메서드
     * 
     * <p>요청 헤더에서 'X-API-KEY' 값을 추출하여 설정된 API 키와 비교합니다.
     * API 키가 일치하지 않거나 요청 컨텍스트를 찾을 수 없는 경우 예외가 발생합니다.</p>
     *
     * @throws CustomException 요청 컨텍스트를 찾을 수 없거나 API 키가 유효하지 않은 경우
     */
    @Before("@annotation(blog.vans_story_be.domain.auth.annotation.RequireApiKey)")
    public void validateApiKey() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new CustomException("요청 컨텍스트를 찾을 수 없습니다.");
        }
        
        String apiKey = attributes.getRequest().getHeader("X-API-KEY");
        if (!validApiKey.equals(apiKey)) {
            throw new CustomException("유효하지 않은 API 키입니다.");
        }
    }
} 