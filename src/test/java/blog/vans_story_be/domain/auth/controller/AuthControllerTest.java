package blog.vans_story_be.domain.auth.controller;

import blog.vans_story_be.config.security.TestSecurityConfig;
import blog.vans_story_be.domain.auth.dto.LoginRequest;
import blog.vans_story_be.domain.auth.dto.TokenDto;
import blog.vans_story_be.domain.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 테스트 클래스
 * 인증 관련 API 엔드포인트들을 테스트합니다.
 */
@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    /**
     * 로그인 성공 테스트
     * 유효한 자격 증명으로 로그인 시 토큰이 정상적으로 발급되는지 검증합니다.
     */
    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        TokenDto tokenDto = TokenDto.builder()
                .accessToken("test.access.token")
                .refreshToken("test.refresh.token")
                .build();

        given(authService.login(any(LoginRequest.class))).willReturn(tokenDto);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("test.access.token"))
                .andExpect(cookie().exists("refreshToken"));
    }

    /**
     * 토큰 갱신 성공 테스트
     * 유효한 Refresh Token으로 Access Token을 갱신할 수 있는지 검증합니다.
     */
    @Test
    @DisplayName("토큰 갱신 성공 테스트")
    void refreshTokenSuccess() throws Exception {
        // given
        String refreshToken = "valid.refresh.token";
        TokenDto newTokenDto = TokenDto.builder()
                .accessToken("new.access.token")
                .refreshToken("new.refresh.token")
                .build();

        given(authService.refresh(refreshToken)).willReturn(newTokenDto);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new.access.token"));
    }
} 