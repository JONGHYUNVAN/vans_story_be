package blog.vans_story_be.config.security

import blog.vans_story_be.domain.auth.jwt.JwtProvider
import io.mockk.mockk
import jakarta.servlet.Filter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.mock.env.MockEnvironment
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.EnableWebMvc

/**
 * 실제 SecurityConfig 의 필터 체인으로 닉네임 조회 경로의 접근 규칙을 확인합니다.
 */
@SpringJUnitWebConfig(NicknameLookupSecurityTest.Config::class)
@TestPropertySource(properties = ["VANS_BLOG_INTERNAL_API_KEY=$INTERNAL_KEY"])
class NicknameLookupSecurityTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var springSecurityFilterChain: Filter

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilters<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(springSecurityFilterChain)
            .build()
    }

    @Test
    fun `내부 키가 맞으면 닉네임 조회를 허용한다`() {
        mockMvc.get(LOOKUP_PATH) { header(InternalApiKeyAuthorizationManager.HEADER_NAME, INTERNAL_KEY) }
            .andExpect { status { isOk() } }
    }

    @Test
    fun `내부 키가 틀리면 거절한다`() {
        mockMvc.get(LOOKUP_PATH) { header(InternalApiKeyAuthorizationManager.HEADER_NAME, "wrong-key") }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `내부 키가 없으면 거절한다`() {
        mockMvc.get(LOOKUP_PATH).andExpect { status { isForbidden() } }
    }

    @Test
    fun `내부 키는 다른 사용자 경로를 열지 않는다`() {
        mockMvc.get("/api/v1/users") { header(InternalApiKeyAuthorizationManager.HEADER_NAME, INTERNAL_KEY) }
            .andExpect { status { isForbidden() } }
    }

    @RestController
    class StubController {
        @GetMapping(LOOKUP_PATH)
        fun lookup(): String = "nickname"

        @GetMapping("/api/v1/users")
        fun users(): String = "users"
    }

    @Configuration
    @EnableWebMvc
    @Import(SecurityConfig::class, StubController::class)
    class Config {
        @Bean
        fun jwtProvider(): JwtProvider = mockk(relaxed = true)

        @Bean
        fun corsConfigurationSource(): CorsConfigurationSource = UrlBasedCorsConfigurationSource()

        @Bean
        fun loginRateLimiter(): LoginRateLimiter = LoginRateLimiter()

        @Bean
        fun environment(): Environment = MockEnvironment()
    }

    companion object {
        const val LOOKUP_PATH = "/api/v1/users/email/claude@vans-story.com"
    }
}

private const val INTERNAL_KEY = "test-internal-key"
