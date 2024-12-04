package blog.vans_story_be.domain.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT 설정 프로퍼티 클래스
 * application.yml에서 JWT 관련 설정을 관리합니다.
 *
 * @author van
 * @since 1.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
@Component
public class JwtProperties {
    private String secretKey;
    private long accessTokenValidityInSeconds;
    private long refreshTokenValidityInSeconds;
} 