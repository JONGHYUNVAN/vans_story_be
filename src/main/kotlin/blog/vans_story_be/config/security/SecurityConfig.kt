package blog.vans_story_be.config.security

import blog.vans_story_be.domain.auth.jwt.JwtFilter
import blog.vans_story_be.domain.auth.jwt.JwtProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

/**
 * Spring Security 보안 정책 설정.
 *
 * - JWT 인증 필터 등록
 * - Rate Limit 필터 등록 (/login, /signup)
 * - 이메일 확인 API 인증 필요로 변경 (User Enumeration 방지)
 * - Swagger: prod 프로파일에서 비활성화
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtProvider: JwtProvider,
    private val corsConfigurationSource: CorsConfigurationSource,
    private val loginRateLimiter: LoginRateLimiter,
    private val environment: Environment
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val isProd = environment.activeProfiles.contains("prod")

        return http
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/v1/auth/login",
                        "/api/v1/auth/signup",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/logout"
                    ).permitAll()
                    .requestMatchers("/api/v1/oauth/login", "/api/v1/oauth/exchange").permitAll()
                    .requestMatchers("/error").permitAll()
                if (!isProd) {
                    auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                }
                auth.anyRequest().authenticated()
            }
            .addFilterBefore(
                RateLimitFilter(loginRateLimiter),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .addFilterBefore(
                JwtFilter(jwtProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .build()
    }
}
