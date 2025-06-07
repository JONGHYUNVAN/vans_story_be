package blog.vans_story_be.domain.auth.jwt

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * JWT 설정 프로퍼티 클래스입니다.
 * 
 * <p>환경변수에서 JWT 관련 설정을 관리합니다.
 * 토큰 생성 및 검증에 필요한 설정값을 제공합니다.</p>
 * 
 * <h4>환경변수 설정:</h4>
 * <pre>
 * VANS_BLOG_JWT_SECRET_KEY=your-secret-key-here
 * VANS_BLOG_JWT_ACCESS_TOKEN_VALIDITY=18000    # 5시간
 * VANS_BLOG_JWT_REFRESH_TOKEN_VALIDITY=604800  # 7일
 * </pre>
 * 
 * <h4>사용 예시:</h4>
 * <pre>
 * @Autowired
 * private lateinit var jwtProperties: JwtProperties
 * 
 * // 토큰 생성 시
 * val token = generateToken(
 *     secretKey = jwtProperties.secretKey,
 *     expiration = jwtProperties.accessTokenValidityInSeconds
 * )
 * </pre>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 */
@Component
class JwtProperties {
    /**
     * JWT 토큰 서명에 사용되는 비밀 키입니다.
     */
    @Value("\${VANS_BLOG_JWT_SECRET_KEY}")
    lateinit var secretKey: String
    
    /**
     * 액세스 토큰의 유효 기간(초)입니다.
     */
    @Value("\${VANS_BLOG_JWT_ACCESS_TOKEN_VALIDITY}")
    var accessTokenValidityInSeconds: Long = 0L
    
    /**
     * 리프레시 토큰의 유효 기간(초)입니다.
     */
    @Value("\${VANS_BLOG_JWT_REFRESH_TOKEN_VALIDITY}")
    var refreshTokenValidityInSeconds: Long = 0L
} 