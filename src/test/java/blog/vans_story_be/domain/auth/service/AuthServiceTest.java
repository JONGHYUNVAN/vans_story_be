package blog.vans_story_be.domain.auth.service;

import blog.vans_story_be.domain.auth.dto.LoginRequest;
import blog.vans_story_be.domain.auth.dto.TokenDto;
import blog.vans_story_be.domain.auth.entity.RefreshToken;
import blog.vans_story_be.domain.auth.jwt.JwtProvider;
import blog.vans_story_be.domain.auth.repository.RefreshTokenRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * AuthService 테스트 클래스
 * 인증 관련 비즈니스 로직을 테스트합니다.
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
     */
    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "password");
        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(jwtProvider.generateAccessToken(authentication)).willReturn("test.access.token");
        given(jwtProvider.generateRefreshToken(authentication)).willReturn("test.refresh.token");

        // when
        TokenDto result = authService.login(request);

        // then
        assertThat(result.getAccessToken()).isEqualTo("test.access.token");
        assertThat(result.getRefreshToken()).isEqualTo("test.refresh.token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    /**
     * 토큰 갱신 성공 테스트
     * Refresh Token을 통한 Access Token 갱신이 정상적으로 동작하는지 검증합니다.
     */
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

        // when
        TokenDto result = authService.refresh(refreshToken);

        // then
        assertThat(result.getAccessToken()).isEqualTo("new.access.token");
        assertThat(result.getRefreshToken()).isEqualTo("new.refresh.token");
    }
} 