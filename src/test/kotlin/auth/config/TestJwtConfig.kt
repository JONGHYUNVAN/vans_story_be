package auth.config

import blog.vans_story_be.domain.auth.jwt.JwtProperties
import blog.vans_story_be.domain.auth.jwt.JwtProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.TestPropertySource

/**
 * JWT 테스트 설정
 * 
 * 테스트 환경에서 사용할 JWT 관련 설정을 제공합니다.
 * 실제 운영 환경과 분리된 테스트용 설정을 사용합니다.
 */
@TestConfiguration
@TestPropertySource(properties = [
    "spring.security.jwt.secret=test-jwt-secret-key-for-testing-only-do-not-use-in-production-vans-story-blog-2024",
    "spring.security.jwt.access-token-validity=1800",
    "spring.security.jwt.refresh-token-validity=604800"
])
class TestJwtConfig {
    
    @Bean
    @Primary
    fun jwtProperties(): JwtProperties {
        val properties = JwtProperties()
        properties.secretKey = "test-secret-key-that-is-long-enough-for-hmac-sha-512-algorithm"
        properties.accessTokenValidityInSeconds = 18000L  // 5시간
        properties.refreshTokenValidityInSeconds = 604800L  // 7일
        return properties
    }

    @Bean
    @Primary
    fun jwtProvider(jwtProperties: JwtProperties): JwtProvider {
        return JwtProvider(jwtProperties)
    }
} 