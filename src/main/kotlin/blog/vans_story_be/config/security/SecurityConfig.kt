package blog.vans_story_be.config.security

import blog.vans_story_be.domain.auth.jwt.JwtFilter
import blog.vans_story_be.domain.auth.jwt.JwtProvider
import blog.vans_story_be.domain.user.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
 * Spring Security 설정 클래스입니다.
 * 
 * 애플리케이션의 보안 설정을 담당하며, 인증/인가, CORS, 세션 관리 등을 구성합니다.
 * JWT 기반의 인증을 사용하며, RESTful API에 적합한 보안 설정을 제공합니다.
 * 
 * 주요 기능:
 * - JWT 기반 인증 처리
 * - 인증이 필요없는 엔드포인트 설정
 * - CORS 설정
 * - 세션 관리 (STATELESS)
 * - CSRF 보호 비활성화 (REST API 특성상)
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see blog.vans_story_be.domain.auth.jwt.JwtFilter
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val corsConfigurationSource: CorsConfigurationSource
) {
    /**
     * 비밀번호 인코더를 생성합니다.
     * 
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * 인증 관리자를 생성합니다.
     * 
     * @param config 인증 설정 객체
     * @return AuthenticationManager 인스턴스
     * @throws Exception 인증 관리자 생성 실패 시
     */
    @Bean
    @Throws(Exception::class)
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager = 
        config.authenticationManager

    /**
     * 보안 필터 체인을 구성합니다.
     * 
     * @param http HTTP 보안 설정 객체
     * @return SecurityFilterChain 인스턴스
     * @throws Exception 보안 설정 실패 시
     */
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors { it.configurationSource(corsConfigurationSource) }
        .csrf { it.disable() }
        .authorizeHttpRequests { auth ->
            auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                .requestMatchers("/api/v1/users/register").permitAll()
                .requestMatchers("/api/v1/users/email/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
        }
        .addFilterBefore(
            JwtFilter(jwtProvider),
            UsernamePasswordAuthenticationFilter::class.java
        )
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .build()


} 