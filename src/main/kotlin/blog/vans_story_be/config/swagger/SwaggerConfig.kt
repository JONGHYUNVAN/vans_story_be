package blog.vans_story_be.config.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger(OpenAPI) 설정 클래스입니다.
 * 
 * <p>API 문서화를 위한 OpenAPI 3.0 설정을 제공합니다.
 * Swagger UI를 통해 API 문서를 확인하고 테스트할 수 있습니다.</p>
 * 
 * <h4>주요 기능:</h4>
 * <ul>
 *   <li>API 기본 정보 설정 (제목, 버전, 설명)</li>
 *   <li>JWT 기반 인증 설정</li>
 *   <li>API Key 기반 인증 설정</li>
 *   <li>보안 스키마 구성</li>
 * </ul>
 * 
 * <h4>접근 방법:</h4>
 * <ul>
 *   <li>Swagger UI: http://localhost:8080/swagger-ui/index.html</li>
 *   <li>OpenAPI 명세: http://localhost:8080/v3/api-docs</li>
 * </ul>
 * 
 * <h4>인증 방식:</h4>
 * <ol>
 *   <li>JWT 인증: Authorization 헤더에 "Bearer {token}" 형식으로 전달</li>
 *   <li>API Key 인증: X-API-KEY 헤더에 API 키 전달</li>
 * </ol>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see io.swagger.v3.oas.models.OpenAPI
 * @see io.swagger.v3.oas.models.security.SecurityScheme
 */
@Configuration
class SwaggerConfig {

    /**
     * OpenAPI 설정을 구성합니다.
     * 
     * <p>API 문서의 기본 정보와 보안 설정을 정의합니다.
     * JWT와 API Key 두 가지 인증 방식을 지원합니다.</p>
     * 
     * <h4>설정 내용:</h4>
     * <ul>
     *   <li>API 제목: "Vans Story API"</li>
     *   <li>API 버전: "1.0"</li>
     *   <li>JWT 인증: Bearer 토큰 방식</li>
     *   <li>API Key 인증: X-API-KEY 헤더 사용</li>
     * </ul>
     * 
     * @return OpenAPI 설정 객체
     * @see io.swagger.v3.oas.models.OpenAPI
     */
    @Bean
    fun customOpenAPI(): OpenAPI {
        // API Key 보안 스키마 설정
        val apiKeyScheme = SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .`in`(SecurityScheme.In.HEADER)
            .name("X-API-KEY")
            .description("vansdevbloguserinternalapikey1234567890!")

        // JWT 보안 스키마 설정
        val bearerScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")

        return OpenAPI()
            .info(
                Info()
                    .title("Vans Story API")
                    .version("1.0")
                    .description("사용자 관리 및 인증 API")
            )
            .addSecurityItem(SecurityRequirement().addList("BearerAuth"))
            .components(
                Components()
                    .addSecuritySchemes("BearerAuth", bearerScheme)
                    .addSecuritySchemes("ApiKeyAuth", apiKeyScheme)
            )
    }
} 