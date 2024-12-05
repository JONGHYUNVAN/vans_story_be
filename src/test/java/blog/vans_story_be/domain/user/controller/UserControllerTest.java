package blog.vans_story_be.domain.user.controller;

import blog.vans_story_be.config.security.TestSecurityConfig;
import blog.vans_story_be.domain.auth.jwt.JwtProvider;
import blog.vans_story_be.domain.user.dto.UserDto;
import blog.vans_story_be.domain.user.entity.Role;
import blog.vans_story_be.domain.user.service.UserService;
import blog.vans_story_be.global.exception.CustomException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserController 테스트 클래스
 * 사용자 관련 API 엔드포인트를 테스트합니다.
 * 
 * @see UserController
 */
@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtProvider jwtProvider;

    private UserDto.CreateRequest createRequest;
    private UserDto.UpdateRequest updateRequest;
    private UserDto.Response responseDto;

    private static final String TEST_JWT = "test.jwt.token";

    /**
     * 각 테스트 실행 전 필요한 데이터를 설정합니다.
     */
    @BeforeEach
    void setUp() {
        createRequest = UserDto.CreateRequest.builder()
                .username("testUser")
                .email("test@example.com")
                .password("Password1!")
                .build();

        updateRequest = UserDto.UpdateRequest.builder()
                .email("updated@example.com")
                .password("UpdatedPass1!")
                .build();

        responseDto = UserDto.Response.builder()
                .id(1L)
                .username("testUser")
                .email("updated@example.com")
                .role(Role.USER)
                .createdAt("2024-01-01T00:00:00")
                .updatedAt("2024-01-01T00:00:00")
                .build();

        // JWT 토큰 검증 모의 설정
        given(jwtProvider.validateToken(TEST_JWT)).willReturn(true);
        given(jwtProvider.getAuthentication(TEST_JWT)).willReturn(
            new UsernamePasswordAuthenticationToken(
                "testUser",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            )
        );
    }

    /**
     * 사용자 생성 API 테스트
     */
    @Nested
    @DisplayName("POST /api/v1/users 는")
    class CreateUser {

        /**
         * 올바른 요청으로 사용자를 생성할 수 있는지 테스트합니다.
         * 
         * @throws Exception MockMvc 실행 중 발생할 수 있는 예외
         */
        @Test
        @DisplayName("올바른 요청으로 사용자를 생성할 수 있다")
        void createUser_WithValidRequest_ShouldReturn201() throws Exception {
            // given
            given(userService.createUser(any(UserDto.CreateRequest.class)))
                    .willReturn(responseDto);

            // when & then
            mockMvc.perform(post("/api/v1/users")
                            .header("Authorization", "Bearer " + TEST_JWT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.username").value("testUser"));
        }

        /**
         * 잘못된 이메일 형식으로 요청 시 400 에러가 발생하는지 테스트합니다.
         * 
         * @throws Exception MockMvc 실행 중 발생할 수 있는 예외
         */
        @Test
        @DisplayName("잘못된 이메일 형식으로 요청시 400 에러가 발생한다")
        void createUser_WithInvalidEmail_ShouldReturn400() throws Exception {
            // given
            createRequest = UserDto.CreateRequest.builder()
                    .username("testUser")
                    .email("invalid-email")
                    .password("password")
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/users")
                            .header("Authorization", "Bearer " + TEST_JWT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    /**
     * 사용자 목록 조회 API 테스트
     */
    @Nested
    @DisplayName("GET /api/v1/users 는")
    class GetAllUsers {

        /**
         * 모든 사용자 목록을 조회할 수 있는지 테스트합니다.
         * 
         * @throws Exception MockMvc 실행 중 발생할 수 있는 예외
         */
        @Test
        @DisplayName("모든 사용자 목록을 조회할 수 있다")
        void getAllUsers_ShouldReturnUserList() throws Exception {
            // given
            List<UserDto.Response> users = Arrays.asList(responseDto);
            given(userService.getAllUsers()).willReturn(users);

            // when & then
            mockMvc.perform(get("/api/v1/users")
                            .header("Authorization", "Bearer " + TEST_JWT))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].id").value(1L));
        }
    }

    /**
     * 특정 사용자 조회 API 테스트
     */
    @Nested
    @DisplayName("GET /api/v1/users/{id} 는")
    class GetUser {

        /**
         * 존재하는 ID로 사용자를 조회할 수 있는지 테스트합니다.
         * 
         * @throws Exception MockMvc 실행 중 발생할 수 있는 예외
         */
        @Test
        @DisplayName("존재하는 ID로 사용자를 조회할 수 있다")
        void getUser_WithExistingId_ShouldReturnUser() throws Exception {
            // given
            given(userService.getUserById(1L)).willReturn(responseDto);

            // when & then
            mockMvc.perform(get("/api/v1/users/1")
                            .header("Authorization", "Bearer " + TEST_JWT))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1L));
        }

        /**
         * 존재하지 않는 ID로 조회 시 404 에러가 발생하는지 테스트합니다.
         * 
         * @throws Exception MockMvc 실행 중 발생할 수 있는 예외
         */
        @Test
        @DisplayName("존재하지 않는 ID로 조회시 404 에러가 발생한다")
        void getUser_WithNonExistingId_ShouldReturn404() throws Exception {
            // given
            given(userService.getUserById(99L))
                    .willThrow(new CustomException("User not found"));

            // when & then
            mockMvc.perform(get("/api/v1/users/99")
                            .header("Authorization", "Bearer " + TEST_JWT))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 사용자 정보 수정 API 테스트
     */
    @Nested
    @DisplayName("PATCH /api/v1/users/{id} 는")
    class UpdateUser {

        /**
         * 존재하는 사용자의 정보를 수정할 수 있는지 테스트합니다.
         * 
         * @throws Exception MockMvc 실행 중 발생할 수 있는 예외
         */
        @Test
        @DisplayName("존재하는 사용자의 정보를 수정할 수 있다")
        void updateUser_WithValidRequest_ShouldUpdateUser() throws Exception {
            // given
            given(userService.updateUser(eq(1L), any(UserDto.UpdateRequest.class)))
                    .willReturn(responseDto);

            // when & then
            mockMvc.perform(patch("/api/v1/users/1")
                            .header("Authorization", "Bearer " + TEST_JWT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1L));
        }

        /**
         * 존재하지 않는 사용자 수정 시 404 에러가 발생하는지 테스트합니다.
         * 
         * @throws Exception MockMvc 실행 중 발생할 수 있는 예외
         */
        @Test
        @DisplayName("존재하지 않는 사용자 수정 시 404 에러가 발생한다")
        void updateUser_WithNonExistingId_ShouldReturn404() throws Exception {
            // given
            given(userService.updateUser(eq(99L), any(UserDto.UpdateRequest.class)))
                    .willThrow(new CustomException("User not found"));

            // when & then
            mockMvc.perform(patch("/api/v1/users/99")
                            .header("Authorization", "Bearer " + TEST_JWT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 사용자 삭제 API 테스트
     */
    @Nested
    @DisplayName("DELETE /api/v1/users/{id} 는")
    class DeleteUser {

        /**
         * 존재하는 사용자를 삭제할 수 있는지 테스트합니다.
         * 
         * @throws Exception MockMvc 실행 중 발생할 수 있는 예외
         */
        @Test
        @DisplayName("존재하는 사용자를 삭제할 수 있다")
        void deleteUser_WithExistingId_ShouldDeleteUser() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/v1/users/1")
                            .header("Authorization", "Bearer " + TEST_JWT))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        /**
         * 존재하지 않는 사용자 삭제 시 404 에러가 발생하는지 테스트합니다.
         * 
         * @throws Exception MockMvc 실행 중 발생할 수 있는 예외
         */
        @Test
        @DisplayName("존재하지 않는 사용자 삭제 시 404 에러가 발생한다")
        void deleteUser_WithNonExistingId_ShouldReturn404() throws Exception {
            // given
            doThrow(new CustomException("User not found"))
                    .when(userService).deleteUser(99L);

            // when & then
            mockMvc.perform(delete("/api/v1/users/99")
                            .header("Authorization", "Bearer " + TEST_JWT))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
} 