package blog.vans_story_be.config.cors

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * CORS(Cross-Origin Resource Sharing) 설정을 관리하는 설정 클래스입니다.
 * 
 * 이 클래스는 웹 애플리케이션의 CORS 정책을 정의하며,
 * CorsProperties를 통해 외부 설정 파일에서 CORS 정책을 관리할 수 있습니다.
 * 
 * 주요 기능:
 * - 허용된 출처(Origin) 설정
 * - 허용된 HTTP 메서드 설정
 * - 허용된 헤더 설정
 * - 인증 정보 전송 설정
 * - 프리플라이트 요청 캐시 시간 설정
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see CorsProperties
 * @see org.springframework.web.cors.CorsConfiguration
 */
@Configuration
class CorsConfig(
    private val corsProperties: CorsProperties
) {
    /**
     * CORS 설정을 구성하는 빈을 생성합니다.
     * 
     * 이 메서드는 Spring의 CORS 필터에서 사용할 CORS 설정을 정의합니다.
     * 모든 경로에 대해 동일한 CORS 정책을 적용하며,
     * 정책은 CorsProperties를 통해 외부 설정에서 관리됩니다.
     * 
     * @return UrlBasedCorsConfigurationSource CORS 설정이 적용된 설정 소스
     * @see CorsProperties
     * @see org.springframework.web.cors.UrlBasedCorsConfigurationSource
     * @see org.springframework.web.cors.CorsConfiguration
     */
    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource = 
        UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", CorsConfiguration().apply {
                allowedOrigins = corsProperties.allowedOrigins
                allowedMethods = corsProperties.allowedMethods
                allowedHeaders = corsProperties.allowedHeaders
                exposedHeaders = corsProperties.exposedHeaders
                maxAge = corsProperties.maxAge
                allowCredentials = corsProperties.allowCredentials
            })
        }
} 