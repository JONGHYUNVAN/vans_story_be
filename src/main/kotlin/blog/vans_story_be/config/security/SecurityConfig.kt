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
 * Spring Security의 전반적인 보안 정책을 설정하는 @Configuration 클래스입니다.
 *
 * ## 주요 기능
 * - JWT 기반 인증 및 인가 처리
 * - 인증이 필요 없는 엔드포인트(로그인, 회원가입, Swagger 등) 화이트리스트 처리
 * - CORS 정책 연동
 * - 세션 관리 정책(STATELESS)
 * - CSRF 보호 비활성화(REST API 특성상)
 *
 * @property jwtProvider JWT 토큰 발급 및 검증을 담당하는 Provider
 * @property userRepository 사용자 정보 조회용 Repository (추후 커스텀 인증 로직에 활용 가능)
 * @property corsConfigurationSource CORS 정책을 제공하는 Bean
 * @constructor JWT Provider, UserRepository, CORS Source를 주입받아 생성
 * @author vans
 * @since 2025.06.07
 * @version 1.0.0
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see blog.vans_story_be.domain.auth.jwt.JwtFilter
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    /**
     * JWT 토큰 발급 및 검증을 담당하는 Provider
     */
    private val jwtProvider: JwtProvider,
    /**
     * 사용자 정보 조회용 Repository (추후 커스텀 인증 로직에 활용 가능)
     */
    private val userRepository: UserRepository,
    /**
     * CORS 정책을 제공하는 Bean
     */
    private val corsConfigurationSource: CorsConfigurationSource
) {
    /**
     * 비밀번호 암호화를 위한 PasswordEncoder 빈을 생성합니다.
     *
     * @return [PasswordEncoder] BCrypt 해시 기반 인코더
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * 인증 관리자를 생성합니다.
     *
     * @param config 인증 설정 객체([AuthenticationConfiguration])
     * @return [AuthenticationManager] 인증 관리자 인스턴스
     * @throws Exception 인증 관리자 생성 실패 시 예외 발생
     */
    @Bean
    @Throws(Exception::class)
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    /**
     * Spring Security의 보안 필터 체인을 구성합니다.
     *
     * - JWT 인증 필터([JwtFilter])를 UsernamePasswordAuthenticationFilter 앞에 추가
     * - 인증이 필요 없는 엔드포인트는 permitAll로 허용
     * - 나머지 모든 요청은 인증 필요
     * - 세션은 STATELESS로 관리
     * - CORS, CSRF 정책 적용
     *
     * @param http [HttpSecurity] 보안 설정 객체
     * @return [SecurityFilterChain] 보안 필터 체인 인스턴스
     * @throws Exception 보안 설정 실패 시 예외 발생
     * @see JwtFilter
     * @see UsernamePasswordAuthenticationFilter
     * @see SessionCreationPolicy
     */
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors { it.configurationSource(corsConfigurationSource) }
        .csrf { it.disable() }
        .authorizeHttpRequests { auth ->
            auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/signup", "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                .requestMatchers("/api/v1/oauth/login", "/api/v1/oauth/exchange").permitAll()
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