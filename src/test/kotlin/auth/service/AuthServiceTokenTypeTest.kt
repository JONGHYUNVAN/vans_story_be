package auth.service

import auth.support.JwtTestSupport
import blog.vans_story_be.domain.auth.repository.RefreshTokenRepository
import blog.vans_story_be.domain.auth.service.AuthService
import blog.vans_story_be.domain.user.entity.User
import blog.vans_story_be.domain.user.repository.UserRepository
import blog.vans_story_be.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.springframework.security.authentication.AuthenticationManager
import java.util.Optional
import java.util.concurrent.Executors

/**
 * AuthService.refresh의 토큰 타입 검증 테스트.
 *
 * AuthServiceTest와 달리 실제 JwtProvider를 써서 typ 클레임 판정이 refresh 경로에 연결되어 있는지 확인한다.
 * DB(RefreshTokenRepository)는 mock이며, 모든 케이스에서 "해시가 DB에 있다"고 응답해 JWT 검증 단계까지 가게 한다.
 */
class AuthServiceTokenTypeTest : DescribeSpec({

    val jwtProvider = JwtTestSupport.jwtProvider()
    val mockRefreshTokenRepository = mockk<RefreshTokenRepository>()
    val mockUserRepository = mockk<UserRepository>()
    val mockResponse = mockk<HttpServletResponse>(relaxed = true)

    val authService = AuthService(
        mockk<AuthenticationManager>(),
        jwtProvider,
        JwtTestSupport.jwtProperties(),
        mockRefreshTokenRepository,
        mockUserRepository,
        Executors.newSingleThreadExecutor()
    )

    beforeEach {
        clearMocks(mockRefreshTokenRepository, mockUserRepository, mockResponse)
        val mockUser = mockk<User>(relaxed = true)
        val mockEntityId = mockk<EntityID<Long>>()
        every { mockUser.id } returns mockEntityId
        every { mockEntityId.value } returns 1L
        every { mockUserRepository.findByEmail(any()) } returns Optional.of(mockUser)
        every { mockRefreshTokenRepository.findUserIdByTokenHash(any()) } returns 1L
        every { mockRefreshTokenRepository.deleteByUserId(any()) } just runs
        every { mockRefreshTokenRepository.save(any(), any(), any()) } just runs
    }

    /**
     * 응답 Authorization 헤더로 나간 새 Access 토큰의 typ 값
     */
    fun issuedAccessTokenType(): Any? {
        val header = slot<String>()
        verify { mockResponse.setHeader("Authorization", capture(header)) }
        return JwtTestSupport.parseClaims(header.captured.removePrefix("Bearer "))["typ"]
    }

    describe("refresh 메서드는") {

        it("typ=refresh 토큰이면 typ이 붙은 새 토큰으로 회전한다") {
            val refreshToken = jwtProvider.generateRefreshToken(JwtTestSupport.authentication())

            authService.refresh(refreshToken, mockResponse)

            verify { mockRefreshTokenRepository.save(eq(1L), any(), any()) }
            issuedAccessTokenType() shouldBe "access"
        }

        it("Access 토큰이면 거부하고 토큰을 회전하지 않는다") {
            val accessToken = jwtProvider.generateAccessToken(JwtTestSupport.authentication())

            shouldThrow<CustomException> {
                authService.refresh(accessToken, mockResponse)
            }.message shouldBe "토큰 갱신에 실패했습니다."

            verify(exactly = 0) { mockRefreshTokenRepository.deleteByUserId(any()) }
            verify(exactly = 0) { mockRefreshTokenRepository.save(any(), any(), any()) }
        }

        it("typ 없는 레거시 Refresh 토큰은 전환기 동안 허용하고 typ이 붙은 토큰으로 회전한다") {
            val legacyToken = JwtTestSupport.tokenWithType(null)

            authService.refresh(legacyToken, mockResponse)

            verify { mockRefreshTokenRepository.save(eq(1L), any(), any()) }
            issuedAccessTokenType() shouldBe "access"
        }
    }
})
