package blog.vans_story_be.config.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
   
/**
 * 테스트용 Security Filter Chain 구성
 * 모든 요청에 대해 인증을 허용하고 기본적인 보안 설정을 비활성화합니다.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보호 비활성화 (테스트 환경)
            .csrf(csrf -> csrf.disable())
            
            // 세션 관리 설정
            // JWT를 사용하므로 세션은 STATELESS로 설정
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 요청 권한 설정
            // 테스트를 위해 모든 요청 허용
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll())
            
            // H2 콘솔 접근을 위한 프레임 옵션 비활성화
            .headers(headers -> headers.frameOptions(
                frameOptions -> frameOptions.disable()
            ));
            
        return http.build();
    }
} 