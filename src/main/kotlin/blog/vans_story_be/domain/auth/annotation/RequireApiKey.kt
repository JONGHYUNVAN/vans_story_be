package blog.vans_story_be.domain.auth.annotation

/**
 * API Key 인증이 필요한 메서드를 표시하는 어노테이션입니다.
 * 
 * <p>이 어노테이션이 적용된 메서드는 API Key 인증이 필요하며,
 * 요청 헤더에 유효한 X-API-KEY가 포함되어야 합니다.</p>
 * 
 * <h4>인증 처리:</h4>
 * <ul>
 *   <li>요청 헤더에서 X-API-KEY 값을 검증</li>
 *   <li>유효하지 않은 API Key인 경우 401 Unauthorized 응답</li>
 *   <li>API Key가 없는 경우 403 Forbidden 응답</li>
 * </ul>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see blog.vans_story_be.domain.auth.aspect.ApiKeyAspect
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireApiKey 