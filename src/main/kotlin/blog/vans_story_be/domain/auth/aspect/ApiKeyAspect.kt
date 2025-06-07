package blog.vans_story_be.domain.auth.aspect

import blog.vans_story_be.domain.auth.annotation.RequireApiKey
import blog.vans_story_be.global.exception.CustomException
import mu.KotlinLogging
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * API Key 인증을 처리하는 Aspect 클래스입니다.
 * 
 * <p>@RequireApiKey 어노테이션이 적용된 메서드에 대해 API Key 검증을 수행합니다.
 * 요청 헤더의 X-API-KEY 값을 검증하여 내부 API 접근을 제어합니다.</p>
 * 
 * <h4>주요 기능:</h4>
 * <ul>
 *   <li>API Key 존재 여부 검증</li>
 *   <li>API Key 유효성 검증</li>
 *   <li>요청 컨텍스트 검증</li>
 * </ul>
 * 
 * <h4>동작 방식:</h4>
 * <ol>
 *   <li>@RequireApiKey 어노테이션이 적용된 메서드 호출 시 실행</li>
 *   <li>요청 헤더에서 X-API-KEY 값 추출</li>
 *   <li>설정된 API Key와 비교하여 검증</li>
 *   <li>검증 실패 시 CustomException 발생</li>
 * </ol>
 * 
 * <h4>예외 처리:</h4>
 * <ul>
 *   <li>요청 컨텍스트 없음: "요청 컨텍스트를 찾을 수 없습니다."</li>
 *   <li>API Key 불일치: "유효하지 않은 API 키입니다."</li>
 * </ul>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see blog.vans_story_be.domain.auth.annotation.RequireApiKey
 * @see blog.vans_story_be.global.exception.CustomException
 */
@Aspect
@Component
class ApiKeyAspect {
    
    private val log = KotlinLogging.logger {}
    
    @Value("\${VANS_BLOG_INTERNAL_API_KEY}")
    private lateinit var validApiKey: String

    /**
     * API Key를 검증하는 메서드입니다.
     * 
     * <p>@RequireApiKey 어노테이션이 적용된 메서드 실행 전에 호출되며,
     * 요청 헤더의 X-API-KEY 값을 검증합니다.</p>
     * 
     * <h4>검증 과정:</h4>
     * <ol>
     *   <li>요청 컨텍스트 확인</li>
     *   <li>X-API-KEY 헤더 값 추출</li>
     *   <li>API Key 유효성 검증</li>
     * </ol>
     * 
     * @throws CustomException 요청 컨텍스트를 찾을 수 없거나 API Key가 유효하지 않은 경우
     * @see blog.vans_story_be.domain.auth.annotation.RequireApiKey
     */
    @Before("@annotation(blog.vans_story_be.domain.auth.annotation.RequireApiKey)")
    fun validateApiKey() {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw CustomException("요청 컨텍스트를 찾을 수 없습니다.")
        
        val apiKey = attributes.request.getHeader("X-API-KEY")
        if (validApiKey != apiKey) {
            log.warn { "유효하지 않은 API Key 접근 시도: ${apiKey?.take(5)}..." }
            throw CustomException("유효하지 않은 API 키입니다.")
        }
        
        log.debug { "API Key 검증 성공" }
    }
} 