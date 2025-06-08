package auth.service

import auth.support.TestConstants
import auth.support.TestDataBuilder
import blog.vans_story_be.domain.auth.dto.LoginRequest
import blog.vans_story_be.domain.auth.jwt.JwtProvider
import blog.vans_story_be.domain.auth.service.AuthService
import blog.vans_story_be.domain.user.entity.User
import blog.vans_story_be.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication

/**
 * AuthService 단위 테스트
 * 
 * 모킹을 사용하여 외부 의존성을 격리하고 서비스 로직만 테스트합니다.
 */
class AuthServiceTest : DescribeSpec({
    
    // 테스트에 필요한 모의 객체들
    val mockAuthManager = mockk<AuthenticationManager>()
    val mockJwtProvider = mockk<JwtProvider>()
    val mockResponse = mockk<HttpServletResponse>(relaxed = true)
    
    // 테스트할 서비스 인스턴스
    val authService = AuthService(mockAuthManager, mockJwtProvider)
    
    describe("login 메서드는") {
        context("유효한 로그인 요청이 주어지면") {
            val loginRequest = TestDataBuilder.createLoginRequest()
            val mockAuth = mockk<Authentication>()
            val mockUser = mockk<User>()
            
            beforeEach {
                // 인증 성공 시나리오 설정
                every { mockAuthManager.authenticate(any<UsernamePasswordAuthenticationToken>()) } returns mockAuth
                every { mockAuth.principal } returns mockUser
                every { mockJwtProvider.generateAccessToken(mockAuth) } returns "test.access.token"
                every { mockJwtProvider.generateRefreshToken(mockAuth) } returns "test.refresh.token"
                every { mockResponse.addHeader(any(), any()) } returns Unit
                every { mockResponse.addCookie(any()) } returns Unit
            }
            
            it("액세스 토큰을 헤더에, 리프레시 토큰을 쿠키에 설정해야 한다") {
                // when
                authService.login(loginRequest, mockResponse)
                
                // verify
                verify { 
                    mockAuthManager.authenticate(any<UsernamePasswordAuthenticationToken>())
                    mockJwtProvider.generateAccessToken(mockAuth)
                    mockJwtProvider.generateRefreshToken(mockAuth)
                    mockResponse.setHeader("Authorization", "Bearer test.access.token")
                    mockResponse.addCookie(match { cookie ->
                        cookie.name == "refreshToken" && 
                        cookie.value == "test.refresh.token" &&
                        cookie.isHttpOnly &&
                        cookie.secure
                    })
                }
            }
        }
        
        context("잘못된 로그인 요청이 주어지면") {
            val loginRequest = TestDataBuilder.createLoginRequest(password = "wrong-password")
            
            beforeEach {
                // 인증 실패 시나리오 설정
                every { mockAuthManager.authenticate(any<UsernamePasswordAuthenticationToken>()) } throws 
                    BadCredentialsException(TestConstants.INVALID_CREDENTIALS)
            }
            
            it("CustomException을 던져야 한다") {
                // when & then
                val exception = shouldThrow<CustomException> {
                    authService.login(loginRequest, mockResponse)
                }
                
                exception.message shouldBe "이메일 또는 비밀번호가 올바르지 않습니다."
                
                // verify
                verify { mockAuthManager.authenticate(any<UsernamePasswordAuthenticationToken>()) }
            }
        }
    }
    
    describe("refresh 메서드는") {
        context("유효한 리프레시 토큰이 주어지면") {
            val refreshToken = "valid.refresh.token"
            val mockAuth = mockk<Authentication>()
            
            beforeEach {
                // 토큰 갱신 성공 시나리오 설정
                every { mockJwtProvider.validateToken(refreshToken) } returns true
                every { mockJwtProvider.getAuthentication(refreshToken) } returns mockAuth
                every { mockJwtProvider.generateAccessToken(mockAuth) } returns "new.access.token"
                every { mockJwtProvider.generateRefreshToken(mockAuth) } returns "new.refresh.token"
                every { mockResponse.addHeader(any(), any()) } returns Unit
                every { mockResponse.addCookie(any()) } returns Unit
            }
            
            it("새로운 액세스 토큰을 헤더에, 리프레시 토큰을 쿠키에 설정해야 한다") {
                // when
                authService.refresh(refreshToken, mockResponse)
                
                // verify
                verify { 
                    mockJwtProvider.validateToken(refreshToken)
                    mockJwtProvider.getAuthentication(refreshToken)
                    mockJwtProvider.generateAccessToken(mockAuth)
                    mockJwtProvider.generateRefreshToken(mockAuth)
                    mockResponse.setHeader("Authorization", "Bearer new.access.token")
                    mockResponse.addCookie(match { cookie ->
                        cookie.name == "refreshToken" && 
                        cookie.value == "new.refresh.token" &&
                        cookie.isHttpOnly &&
                        cookie.secure
                    })
                }
            }
        }
        
        context("유효하지 않은 리프레시 토큰이 주어지면") {
            val refreshToken = "invalid.refresh.token"
            
            beforeEach {
                // 토큰 검증 실패 시나리오 설정
                every { mockJwtProvider.validateToken(refreshToken) } returns false
            }
            
            it("CustomException을 던져야 한다") {
                // when & then
                val exception = shouldThrow<CustomException> {
                    authService.refresh(refreshToken, mockResponse)
                }
                
                exception.message shouldBe "토큰 갱신에 실패했습니다."
                
                // verify
                verify { mockJwtProvider.validateToken(refreshToken) }
            }
        }
    }
}) 