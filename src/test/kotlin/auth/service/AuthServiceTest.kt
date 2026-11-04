package auth.service

import auth.support.TestConstants
import auth.support.TestDataBuilder
import blog.vans_story_be.domain.auth.jwt.JwtProperties
import blog.vans_story_be.domain.auth.jwt.JwtProvider
import blog.vans_story_be.domain.auth.jwt.TokenType
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
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import java.util.Optional
import java.util.concurrent.Executors

/**
 * AuthService 단위 테스트
 *
 * 모킹을 사용하여 외부 의존성을 격리하고 서비스 로직만 테스트합니다.
 * - RefreshTokenRepository: DB 저장 mock
 * - UserRepository: 이메일 → userId 조회 mock
 * - JwtProperties: 토큰 유효기간 mock
 */
class AuthServiceTest : DescribeSpec({

    val mockAuthManager = mockk<AuthenticationManager>()
    val mockJwtProvider = mockk<JwtProvider>()
    val mockJwtProperties = mockk<JwtProperties>()
    val mockRefreshTokenRepository = mockk<RefreshTokenRepository>()
    val mockUserRepository = mockk<UserRepository>()
    val mockResponse = mockk<HttpServletResponse>(relaxed = true)

    val authService = AuthService(
        mockAuthManager,
        mockJwtProvider,
        mockJwtProperties,
        mockRefreshTokenRepository,
        mockUserRepository,
        Executors.newSingleThreadExecutor()
    )

    // 공통 User mock 설정 헬퍼
    fun setupUserMock(): User {
        val mockUser = mockk<User>(relaxed = true)
        val mockEntityId = mockk<EntityID<Long>>()
        every { mockUser.id } returns mockEntityId
        every { mockEntityId.value } returns 1L
        every { mockJwtProperties.refreshTokenValidityInSeconds } returns 604800L
        every { mockUserRepository.findByEmail(any()) } returns Optional.of(mockUser)
        every { mockRefreshTokenRepository.deleteByUserId(any()) } just runs
        every { mockRefreshTokenRepository.save(any(), any(), any()) } just runs
        return mockUser
    }

    describe("login 메서드는") {

        context("유효한 로그인 요청이 주어지면") {
            val loginRequest = TestDataBuilder.createLoginRequest()
            val mockAuth = mockk<Authentication>()

            beforeEach {
                setupUserMock()
                every { mockAuthManager.authenticate(any<UsernamePasswordAuthenticationToken>()) } returns mockAuth
                every { mockAuth.name } returns "test@example.com"
                every { mockJwtProvider.generateAccessToken(mockAuth) } returns "test.access.token"
                every { mockJwtProvider.generateRefreshToken(mockAuth) } returns "test.refresh.token"
            }

            it("액세스 토큰을 헤더에, 리프레시 토큰 쿠키를 Set-Cookie 헤더에 설정해야 한다") {
                authService.login(loginRequest, mockResponse)

                verify {
                    mockAuthManager.authenticate(any<UsernamePasswordAuthenticationToken>())
                    mockJwtProvider.generateAccessToken(mockAuth)
                    mockJwtProvider.generateRefreshToken(mockAuth)
                    mockResponse.setHeader("Authorization", "Bearer test.access.token")
                    mockResponse.addHeader("Set-Cookie", any())
                }
            }

            it("Refresh Token 해시를 DB에 저장해야 한다") {
                authService.login(loginRequest, mockResponse)

                verify {
                    mockRefreshTokenRepository.deleteByUserId(1L)
                    mockRefreshTokenRepository.save(eq(1L), any(), any())
                }
            }
        }

        context("잘못된 자격 증명이 주어지면") {
            val loginRequest = TestDataBuilder.createLoginRequest(password = "wrong-password")

            beforeEach {
                every { mockAuthManager.authenticate(any<UsernamePasswordAuthenticationToken>()) } throws
                    BadCredentialsException(TestConstants.INVALID_CREDENTIALS)
            }

            it("CustomException을 던져야 한다") {
                val exception = shouldThrow<CustomException> {
                    authService.login(loginRequest, mockResponse)
                }

                exception.message shouldBe "이메일 또는 비밀번호가 올바르지 않습니다."
                verify { mockAuthManager.authenticate(any<UsernamePasswordAuthenticationToken>()) }
            }
        }
    }

    describe("refresh 메서드는") {

        context("유효하고 DB에 존재하는 리프레시 토큰이 주어지면") {
            val refreshToken = "valid.refresh.token"
            val mockAuth = mockk<Authentication>()

            beforeEach {
                setupUserMock()
                every { mockRefreshTokenRepository.findUserIdByTokenHash(any()) } returns 1L
                every { mockJwtProvider.validateToken(refreshToken, TokenType.REFRESH, true) } returns true
                every { mockJwtProvider.getAuthentication(refreshToken) } returns mockAuth
                every { mockAuth.name } returns "test@example.com"
                every { mockJwtProvider.generateAccessToken(mockAuth) } returns "new.access.token"
                every { mockJwtProvider.generateRefreshToken(mockAuth) } returns "new.refresh.token"
            }

            it("새로운 액세스 토큰을 헤더에 설정하고 쿠키를 갱신해야 한다") {
                authService.refresh(refreshToken, mockResponse)

                verify {
                    mockJwtProvider.validateToken(refreshToken, TokenType.REFRESH, true)
                    mockJwtProvider.getAuthentication(refreshToken)
                    mockJwtProvider.generateAccessToken(mockAuth)
                    mockJwtProvider.generateRefreshToken(mockAuth)
                    mockResponse.setHeader("Authorization", "Bearer new.access.token")
                    mockResponse.addHeader("Set-Cookie", any())
                }
            }

            it("기존 토큰을 삭제하고 새 토큰을 저장해야 한다 (토큰 회전)") {
                authService.refresh(refreshToken, mockResponse)

                verify {
                    mockRefreshTokenRepository.deleteByUserId(1L)
                    mockRefreshTokenRepository.save(eq(1L), any(), any())
                }
            }
        }

        context("DB에 존재하지 않는 리프레시 토큰이 주어지면 (로그아웃 후 재사용 시도)") {
            val refreshToken = "revoked.refresh.token"

            beforeEach {
                every { mockRefreshTokenRepository.findUserIdByTokenHash(any()) } returns null
            }

            it("CustomException을 던져야 한다") {
                val exception = shouldThrow<CustomException> {
                    authService.refresh(refreshToken, mockResponse)
                }

                exception.message shouldBe "토큰 갱신에 실패했습니다."
            }
        }

        context("DB에는 존재하지만 JWT 서명이 유효하지 않은 토큰이 주어지면") {
            val refreshToken = "invalid.refresh.token"

            beforeEach {
                every { mockRefreshTokenRepository.findUserIdByTokenHash(any()) } returns 1L
                every { mockJwtProvider.validateToken(refreshToken, TokenType.REFRESH, true) } returns false
            }

            it("CustomException을 던져야 한다") {
                val exception = shouldThrow<CustomException> {
                    authService.refresh(refreshToken, mockResponse)
                }

                exception.message shouldBe "토큰 갱신에 실패했습니다."
                verify { mockJwtProvider.validateToken(refreshToken, TokenType.REFRESH, true) }
            }
        }
    }

    describe("logout 메서드는") {

        beforeEach { clearMocks(mockRefreshTokenRepository, mockResponse) }

        context("유효한 리프레시 토큰 쿠키가 있으면") {
            val refreshToken = "valid.refresh.token"

            beforeEach {
                every { mockRefreshTokenRepository.findUserIdByTokenHash(any()) } returns 1L
                every { mockRefreshTokenRepository.deleteByUserId(any()) } just runs
            }

            it("DB에서 토큰을 삭제하고 만료 쿠키를 설정해야 한다") {
                authService.logout(refreshToken, mockResponse)

                verify {
                    mockRefreshTokenRepository.deleteByUserId(1L)
                    mockResponse.addHeader("Set-Cookie", any())
                }
            }
        }

        context("리프레시 토큰 쿠키가 없으면 (이미 만료된 경우)") {
            it("DB 삭제 없이 만료 쿠키만 설정해야 한다") {
                authService.logout(null, mockResponse)

                verify(exactly = 0) { mockRefreshTokenRepository.deleteByUserId(any()) }
                verify { mockResponse.addHeader("Set-Cookie", any()) }
            }
        }
    }
})
