package auth.jwt

import auth.support.JwtTestSupport
import auth.support.TestDataBuilder
import blog.vans_story_be.domain.auth.jwt.JwtFilter
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

/**
 * JwtFilter 단위 테스트 — Authorization 헤더의 토큰 타입에 따라 인증 여부가 갈리는지 확인한다.
 */
class JwtFilterTest : DescribeSpec({

    val jwtProvider = JwtTestSupport.jwtProvider()
    val authentication = JwtTestSupport.authentication()

    afterEach { SecurityContextHolder.clearContext() }

    /**
     * 주어진 Bearer 토큰으로 필터를 한 번 통과시키고 다음 필터가 호출됐는지 반환한다.
     */
    fun runFilter(bearerToken: String?): Boolean {
        val request = MockHttpServletRequest("GET", "/api/v1/users/me")
        if (bearerToken != null) request.addHeader("Authorization", "Bearer $bearerToken")
        val chain = MockFilterChain()
        JwtFilter(jwtProvider).doFilter(request, MockHttpServletResponse(), chain)
        return chain.request != null
    }

    describe("JwtFilter는") {

        it("Access 토큰이면 SecurityContext에 인증을 설정한다") {
            runFilter(jwtProvider.generateAccessToken(authentication)) shouldBe true

            val auth = SecurityContextHolder.getContext().authentication
            auth.shouldNotBeNull()
            auth.name shouldBe TestDataBuilder.TEST_EMAIL
        }

        it("Refresh 토큰이면 인증하지 않고 다음 필터로 넘긴다") {
            runFilter(jwtProvider.generateRefreshToken(authentication)) shouldBe true

            SecurityContextHolder.getContext().authentication.shouldBeNull()
        }

        it("typ 없는 레거시 토큰이면 인증하지 않는다") {
            runFilter(JwtTestSupport.tokenWithType(null)) shouldBe true

            SecurityContextHolder.getContext().authentication.shouldBeNull()
        }

        it("Authorization 헤더가 없으면 인증하지 않는다") {
            runFilter(null) shouldBe true

            SecurityContextHolder.getContext().authentication.shouldBeNull()
        }
    }
})
