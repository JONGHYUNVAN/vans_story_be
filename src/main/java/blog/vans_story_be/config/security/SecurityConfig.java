package blog.vans_story_be.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import blog.vans_story_be.domain.auth.jwt.JwtFilter;
import blog.vans_story_be.domain.auth.jwt.JwtProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;


/**
 * Spring Security 설정을 위한 Configuration 클래스
 * 보안 필터 체인과 인증/인가 규칙을 정의합니다.
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 비밀번호 암호화를 위한 BCrypt 인코더를 빈으로 등록합니다.
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT 필터를 생성합니다.
     *
     * @return JwtFilter 인스턴스
     */
    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtProvider);
    }

    /**
     * Security Filter Chain을 구성합니다.
     * CSRF 보호 비활성화, 세션 관리 정책 설정, 요청 권한 설정을 포함합니다.
     *
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 보안 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().permitAll())
            .authenticationProvider(authenticationProvider(userDetailsService, passwordEncoder()))
            .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    /**
     * AuthenticationManager 빈을 설정합니다.
     *
     * @param authenticationConfiguration 인증 설정
     * @return AuthenticationManager 인스턴스
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
} 