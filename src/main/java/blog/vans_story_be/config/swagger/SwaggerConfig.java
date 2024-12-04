package blog.vans_story_be.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 설정을 위한 Configuration 클래스
 * API 문서화를 위한 기본 설정을 제공합니다.
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 설정을 구성합니다.
     * API 문서의 기본 정보를 설정합니다.
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vans Story API")
                        .description("Vans Story 블로그 API 문서")
                        .version("v1.0.0"));
    }
}