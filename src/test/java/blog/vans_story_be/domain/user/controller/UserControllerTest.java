package blog.vans_story_be.domain.user.controller;

import blog.vans_story_be.domain.auth.jwt.JwtProvider;
import blog.vans_story_be.domain.user.dto.UserDto;
import blog.vans_story_be.domain.user.entity.Role;
import blog.vans_story_be.domain.user.entity.User;
import blog.vans_story_be.domain.user.repository.UserRepository;
import blog.vans_story_be.domain.user.service.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * User API 통합 테스트
 * 실제 애플리케이션 컨텍스트를 로드하여 사용자 관련 API를 테스트합니다.
 * 
 * @see UserController
 * @see UserService
 * @see JwtProvider
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;
    private User testUser;

    /**
     * 테스트 데이터 초기화
     * 테스트용 사용자를 생성하고 JWT 토큰을 발급받습니다.
     * 
     * @throws Exception 데이터 설정 중 예외 발생 시
     */
    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = userRepository.save(User.builder()
                .name("testUser")
                .email("test@example.com")
                .password(passwordEncoder.encode("Password1!"))
                .role(Role.USER)
                .build());

        // 실제 JWT 토큰 발급
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser.getName(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        accessToken = jwtProvider.generateAccessToken(auth);
    }

    @Nested
    @DisplayName("사용자 생성 API")
    class CreateUser {
        /**
         * 사용자 생성 API 성공 테스트
         * 
         * @throws Exception API 호출 중 예외 발생 시
         * @requires <pre>
         *    POST /api/v1/users
         *    Authorization: Bearer {validAccessToken}
         *    Content-Type: application/json
         *    Body: {
         *      "name": "newUser",
         *      "email": "new@example.com",
         *      "password": "NewPass1!"
         *    }
         * </pre>
         * @returns <pre>
         *    Status: 200 OK
         *    Body: {
         *      "success": true,
         *      "data": {
         *        "id": {id},
         *        "username": "newUser",
         *        "email": "new@example.com",
         *        ...
         *      }
         *    }
         * </pre>
         */
        @Test
        @DisplayName("올바른 요청으로 사용자를 생성할 수 있다")
        void createUser_Success() throws Exception {
            // given
            UserDto.CreateRequest request = UserDto.CreateRequest.builder()
                    .name("newUser")
                    .email("new@example.com")
                    .password("NewPass1!")
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/users")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value("newUser"));

            // DB 저장 확인
            assertThat(userRepository.findByEmail("new@example.com")).isPresent();
        }

        /**
         * 사용자 생성 API 실패 테스트 - 잘못된 이메일 형식
         * 
         * @throws Exception API 호출 중 예외 발생 시
         * @requires <pre>
         *    POST /api/v1/users
         *    Authorization: Bearer {validAccessToken}
         *    Content-Type: application/json
         *    Body: {
         *      "name": "newUser",
         *      "email": "invalid-email",
         *      "password": "NewPass1!"
         *    }
         * </pre>
         * @returns <pre>
         *    Status: 400 Bad Request
         *    Body: {
         *      "success": false,
         *      "message": "잘못된 이메일 형식입니다"
         *    }
         * </pre>
         */
        @Test
        @DisplayName("잘못된 이메일 형식으로 요청시 400 에러가 발생한다")
        void createUser_InvalidEmail() throws Exception {
            // given
            UserDto.CreateRequest request = UserDto.CreateRequest.builder()
                    .name("newUser")
                    .email("invalid-email")
                    .password("NewPass1!")
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/users")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("사용자 조회 API")
    class GetUser {
        /**
         * 단일 사용자 조회 API 성공 테스트
         * 
         * @throws Exception API 호출 중 예외 발생 시
         * @requires <pre>
         *    GET /api/v1/users/{userId}
         *    Authorization: Bearer {validAccessToken}
         * </pre>
         * @returns <pre>
         *    Status: 200 OK
         *    Body: {
         *      "success": true,
         *      "data": {
         *        "id": {userId},
         *        "username": "testUser",
         *        "email": "test@example.com"
         *      }
         *    }
         * </pre>
         */
        @Test
        @DisplayName("존재하는 사용자 ID로 조회시 사용자 정보를 반환한다")
        void getUser_Success() throws Exception {
            // when & then
            mockMvc.perform(get("/api/v1/users/" + testUser.getId())
                    .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value(testUser.getName()))
                    .andExpect(jsonPath("$.data.email").value(testUser.getEmail()));
        }

        /**
         * 단일 사용자 조회 API 실패 테스트 - 존재하지 않는 사용자
         * 
         * @throws Exception API 호출 중 예외 발생 시
         * @requires <pre>
         *    GET /api/v1/users/99999
         *    Authorization: Bearer {validAccessToken}
         * </pre>
         * @returns <pre>
         *    Status: 404 Not Found
         *    Body: {
         *      "success": false,
         *      "message": "사용자를 찾을 수 없습니다"
         *    }
         * </pre>
         */
        @Test
        @DisplayName("존재하지 않는 ID로 조회시 404 에러가 발생한다")
        void getUser_NotFound() throws Exception {
            // when & then
            mockMvc.perform(get("/api/v1/users/99999")
                    .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("사용자 정보 수정 API")
    class UpdateUser {
        /**
         * 사용자 정보 수정 API 성공 테스트
         * 
         * @throws Exception API 호출 중 예외 발생 시
         * @requires <pre>
         *    PUT /api/v1/users/{userId}
         *    Authorization: Bearer {validAccessToken}
         *    Content-Type: application/json
         *    Body: {
         *      "email": "updated@example.com",
         *      "password": "UpdatedPass1!"  // 비밀번호도 함께 수정
         *    }
         * </pre>
         * @returns <pre>
         *    Status: 200 OK
         *    Body: {
         *      "success": true,
         *      "data": {
         *        "id": {userId},
         *        "name": "testUser",
         *        "email": "updated@example.com"
         *      }
         *    }
         * </pre>
         */
        @Test
        @DisplayName("올바른 요청으로 사용자 정보를 수정할 수 있다")
        void updateUser_Success() throws Exception {
            // given
            UserDto.UpdateRequest request = UserDto.UpdateRequest.builder()
                    .email("updated@example.com")
                    .password("UpdatedPass1!")  
                    .build();

            // when & then
            mockMvc.perform(patch("/api/v1/users/" + testUser.getId())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value(testUser.getName()))
                    .andExpect(jsonPath("$.data.email").value("updated@example.com"));

            // DB 업데이트 확인
            User updatedUser = userRepository.findById(testUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            assertThat(updatedUser.getName()).isEqualTo(testUser.getName());
            assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
            assertThat(passwordEncoder.matches("UpdatedPass1!", updatedUser.getPassword())).isTrue();
        }
    }

    @Nested
    @DisplayName("사용자 삭제 API")
    class DeleteUser {
        /**
         * 사용자 삭제 API 성공 테스트
         * 
         * @throws Exception API 호출 중 예외 발생 시
         * @requires <pre>
         *    DELETE /api/v1/users/{userId}
         *    Authorization: Bearer {validAccessToken}
         * </pre>
         * @returns <pre>
         *    Status: 200 OK
         *    Body: {
         *      "success": true,
         *      "message": "사용자가 성공적으로 삭제되었습니다"
         *    }
         * </pre>
         */
        @Test
        @DisplayName("존재하는 사용자를 삭제할 수 있다")
        void deleteUser_Success() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/v1/users/" + testUser.getId())
                    .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // DB 삭제 확인
            assertThat(userRepository.findById(testUser.getId())).isEmpty();
        }
    }
} 