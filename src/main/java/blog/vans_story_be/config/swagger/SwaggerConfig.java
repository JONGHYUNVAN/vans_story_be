package blog.vans_story_be.config.swagger;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
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
    public OpenAPI customOpenAPI() {
        // API Key 보안 스키마 설정
        SecurityScheme apiKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-KEY")
                .description("vansdevbloguserinternalapikey1234567890!"); 

        // JWT 보안 스키마 설정 (기존)
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("Vans Story API")
                        .version("1.0")
                        .description("사용자 관리 및 인증 API")
                )
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", bearerScheme)
                        .addSecuritySchemes("ApiKeyAuth", apiKeyScheme)  // API Key 스키마 추가
                );
    }
}