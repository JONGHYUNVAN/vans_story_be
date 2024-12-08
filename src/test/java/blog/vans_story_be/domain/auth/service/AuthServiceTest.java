package blog.vans_story_be.domain.auth.service;

import blog.vans_story_be.domain.auth.dto.LoginRequest;
import blog.vans_story_be.domain.auth.entity.RefreshToken;
import blog.vans_story_be.domain.auth.jwt.JwtProvider;
import blog.vans_story_be.domain.auth.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * AuthService 테스트 클래스
 * 인증 관련 비즈니스 로직을 테스트합니다.
 * 
 * @ExtendWith(MockitoExtension.class) Mockito를 사용하여 목 객체를 주입하고 관리
 * @InjectMocks 테스트 대상인 AuthService에 목 객체들을 자동 주입
 * @Mock 각 의존성을 목 객체로 대체
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    /**
     * 로그인 성공 테스트
     * 인증 성공 시 토큰이 정상적으로 발급되는지 검증합니다.
     * 
     * @throws Exception 인증 처리 중 예외 발생 시
     * @requires <pre>
     *    LoginRequest(email: "testuser@test.com", password: "password")
     *    MockHttpServletResponse
     * </pre>
     * @returns <pre>
     *    Authorization 헤더: "Bearer test.access.token"
     *    refreshToken 쿠키: "test.refresh.token"
     *    RefreshToken 엔티티가 저장소에 저장됨
     * </pre>
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("testuser@test.com")
                .password("password")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "password");
        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(jwtProvider.generateAccessToken(authentication)).willReturn("test.access.token");
        given(jwtProvider.generateRefreshToken(authentication)).willReturn("test.refresh.token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        authService.login(request, response);

        // then
        assertThat(response.getHeader("Authorization")).isEqualTo("Bearer test.access.token");
        assertThat(response.getCookie("refreshToken").getValue()).isEqualTo("test.refresh.token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    /**
     * 토큰 갱신 성공 테스트
     * Refresh Token을 통한 Access Token 갱신이 정상적으로 동작하는지 검증합니다.
     * 
     * @throws Exception 토큰 갱신 중 예외 발생 시
     * @requires <pre>
     *    refreshToken: "valid.refresh.token"
     *    MockHttpServletResponse
     * </pre>
     * @returns <pre>
     *    Authorization 헤더: "Bearer new.access.token"
     *    refreshToken 쿠키: "new.refresh.token"
     * </pre>
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("토큰 갱신 성공 테스트")
    void refreshTokenSuccess() {
        // given
        String refreshToken = "valid.refresh.token";
        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", null);
        
        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getAuthentication(refreshToken)).willReturn(authentication);
        given(refreshTokenRepository.findByUsername("testuser"))
                .willReturn(Optional.of(new RefreshToken("testuser", refreshToken)));
        given(jwtProvider.generateAccessToken(authentication)).willReturn("new.access.token");
        given(jwtProvider.generateRefreshToken(authentication)).willReturn("new.refresh.token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        authService.refresh(refreshToken, response);

        // then
        assertThat(response.getHeader("Authorization")).isEqualTo("Bearer new.access.token");
        Cookie refreshTokenCookie = response.getCookie("refreshToken");
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(refreshTokenCookie.getValue()).isEqualTo("new.refresh.token");
    }
} 