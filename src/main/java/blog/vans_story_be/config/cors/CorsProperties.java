package blog.vans_story_be.config.cors;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * CORS 설정 프로퍼티 클래스
 * application.yml에서 CORS 관련 설정을 관리합니다.
 *
 * @author van
 * @since 1.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "cors")
@Component
public class CorsProperties {
    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private Long maxAge;
    private Boolean allowCredentials;
} 