package blog.vans_story_be.domain.auth.controller;

import blog.vans_story_be.domain.auth.dto.LoginRequest;
import blog.vans_story_be.domain.user.entity.Role;
import blog.vans_story_be.domain.user.entity.User;
import blog.vans_story_be.domain.user.repository.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
/**
 * AuthController 통합 테스트 클래스
 * 실제 데이터베이스와 서비스 계층을 사용하여 인증 관련 API를 테스트합니다.
 *
 * @SpringBootTest 전체 애플리케이션 컨텍스트를 로드하여 통합 테스트 수행
 * @AutoConfigureMockMvc MockMvc를 사용하여 HTTP 요청 테스트
 * @Transactional 각 테스트 메소드 실행 후 데이터베이스 롤백
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String testUserEmail;
    private String testUserName;
    /**
     * 각 테스트 실행 전 설정을 수행합니다.
     * 테스트용 사용자 데이터를 데이터베이스에 생성합니다.
     * 
     * @throws Exception 데이터베이스 작업 중 예외 발생 시
     */
    @BeforeEach
    void setUp() {
        // 각 테스트마다 고유한 사용자 생성
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        testUserEmail = "testuser_" + uniqueId + "@example.com";
        testUserName = "testuser_" + uniqueId;
        
        User testUser = User.builder()
                .email(testUserEmail)
                .password(passwordEncoder.encode("password"))
                .name(testUserName)
                .role(Role.USER)
                .build();
        userRepository.save(testUser);
    }
    /**
     * 로그인 API 성공 테스트
     * 인증 성공 시 토큰이 정상적으로 발급되는지 검증합니다.
     * 
     * @throws Exception MockMvc 실행 중 예외 발생 시
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
    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .email(testUserEmail)
                .password("password")
                .build();

        // when & then
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");
        assertThat(refreshTokenCookie).isNotNull();
    }
    /**
     * 토큰 갱신 API 성공 테스트
     * Refresh Token을 통한 Access Token 갱신이 정상적으로 동작하는지 검증합니다.
     * 
     * @throws Exception MockMvc 실행 중 예외 발생 시
     * @requires <pre>
     *    refreshToken: "valid.refresh.token"
     *    MockHttpServletResponse
     * </pre>
     * @returns <pre>
     *    Authorization 헤더: "Bearer new.access.token"
     *    refreshToken 쿠키: "new.refresh.token"
     * </pre>
     */
    @Test
    @DisplayName("토큰 갱신 성공 테스트")
    void refreshTokenSuccess() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .email(testUserEmail)
                .password("password")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");
        assertThat(refreshTokenCookie).isNotNull();
        @SuppressWarnings("null")
        String refreshToken = refreshTokenCookie.getValue();

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("refreshToken"));
    }
} 