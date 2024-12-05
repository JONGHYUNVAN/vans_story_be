package blog.vans_story_be.domain.auth.controller;

import blog.vans_story_be.config.security.TestSecurityConfig;
import blog.vans_story_be.domain.auth.dto.LoginRequest;
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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.ArgumentMatchers.anyString;
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
     * 유효한 자격 증명으로 로그인 시 토큰이 정상적으로 발급되는지 증합니다.
     */
    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("testuser@example.com")
                .password("password")
                .build();

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setHeader("Authorization", "Bearer test.access.token");
            Cookie cookie = new Cookie("refreshToken", "test.refresh.token");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            response.addCookie(cookie);
            return null;
        }).when(authService).login(any(LoginRequest.class), any(HttpServletResponse.class));

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer test.access.token"))
                .andExpect(cookie().value("refreshToken", "test.refresh.token"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true));
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
        Cookie cookie = new Cookie("refreshToken", refreshToken);

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setHeader("Authorization", "Bearer new.access.token");
            Cookie newCookie = new Cookie("refreshToken", "new.refresh.token");
            newCookie.setHttpOnly(true);
            newCookie.setSecure(true);
            response.addCookie(newCookie);
            return null;
        }).when(authService).refresh(anyString(), any(HttpServletResponse.class));

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(cookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer new.access.token"))
                .andExpect(cookie().value("refreshToken", "new.refresh.token"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true));
    }
} 