package blog.vans_story_be.domain.user.service;

import blog.vans_story_be.domain.user.dto.UserDto;
import blog.vans_story_be.domain.user.entity.Role;
import blog.vans_story_be.domain.user.entity.User;
import blog.vans_story_be.domain.user.mapper.UserMapper;
import blog.vans_story_be.domain.user.repository.UserRepository;
import blog.vans_story_be.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * UserService 테스트 클래스
 * 사용자 관련 비즈니스 로직을 테스트합니다.
 *
 * @see UserService
 * @see MockitoExtension
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    /**
     * 테스트 대상이 되는 UserService
     * Mockito가 Mock 객체들을 자동으로 주입합니다.
     */
    @InjectMocks
    private UserService userService;

    /**
     * UserRepository의 Mock 객체
     * 사용자 데이터 조작에 대한 동작을 모의합니다.
     */
    @Mock
    private UserRepository userRepository;

    /**
     * UserMapper의 Mock 객체
     * DTO와 엔티티 간의 변환 동작을 모의합니다.
     */
    @Mock
    private UserMapper userMapper;

    private User testUser;
    private UserDto.CreateRequest createRequest;
    private UserDto.UpdateRequest updateRequest;
    private UserDto.Response responseDto;

    /**
     * 각 테스트 실행 전 필요한 데이터를 설정합니다.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);

        createRequest = UserDto.CreateRequest.builder()
                .username("newUser")
                .email("new@example.com")
                .password("newPassword")
                .role(Role.USER)
                .build();

        updateRequest = UserDto.UpdateRequest.builder()
                .email("updated@example.com")
                .password("updatedPassword")
                .build();

        responseDto = UserDto.Response.builder()
                .id(1L)
                .username("testUser")
                .email("test@example.com")
                .role(Role.USER)
                .build();
    }

    /**
     * 사용자 생성 관련 테스트 클래스
     * 사용자 생성 비즈니스 로직에 대한 다양한 시나리오를 테스트합니다.
     *
     * @see UserService#createUser(UserDto.CreateRequest)
     */
    @Nested
    @DisplayName("createUser 메소드는")
    class CreateUser {

        /**
         * 유효한 요청으로 사용자를 성공적으로 생성할 수 있는지 테스트합니다.
         * 생성된 사용자의 ID와 사용자명이 예상값과 일치하는지 확인합니다.
         *
         * @throws Exception 테스트 실행 중 발생할 수 있는 예외
         * 
         * @see UserDto.CreateRequest
         * @see UserDto.Response
         */
        @Test
        @DisplayName("올바른 요청으로 사용자를 생성할 수 있다")
        void createUser_WithValidRequest_ShouldCreateUser() throws Exception {
            // given
            given(userMapper.toEntity(createRequest)).willReturn(testUser);
            given(userRepository.save(any(User.class))).willReturn(testUser);
            given(userMapper.toDto(testUser)).willReturn(responseDto);

            // when
            UserDto.Response result = userService.createUser(createRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testUser.getId());
            assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
            verify(userRepository).save(any(User.class));
        }
    }

    /**
     * 사용자 조회 관련 테스트 클래스
     * 사용자 조회 비즈니스 로직에 대한 다양한 시나리오를 테스트합니다.
     *
     * @see UserService#getUserById(Long)
     */
    @Nested
    @DisplayName("getUserById 메소드는")
    class GetUserById {

        /**
         * 존재하는 ID로 사용자를 성공적으로 조회할 수 있는지 테스트합니다.
         * 조회된 사용자의 ID가 예상값과 일치하는지 확인합니다.
         *
         * @throws Exception 테스트 실행 중 발생할 수 있는 예외
         * 
         * @see UserDto.Response
         */
        @Test
        @DisplayName("존재하는 ID로 사용자를 조회할 수 있다")
        void getUserById_WithExistingId_ShouldReturnUser() throws Exception {
            // given
            given(userRepository.findUserById(1L)).willReturn(Optional.of(testUser));
            given(userMapper.toDto(testUser)).willReturn(responseDto);

            // when
            UserDto.Response result = userService.getUserById(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testUser.getId());
        }

        /**
         * 존재하지 않는 ID로 사용자 조회 시 예외가 발생하는지 테스트합니다.
         * CustomException이 발생하고 적절한 에러 메시지가 포함되어 있는지 확인합니다.
         *
         * @throws Exception 테스트 실행 중 발생할 수 있는 예외
         * 
         * @see CustomException
         */
        @Test
        @DisplayName("존재하지 않는 ID로 조회시 예외가 발생한다")
        void getUserById_WithNonExistingId_ShouldThrowException() throws Exception {
            // given
            given(userRepository.findUserById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("User not found");
        }
    }

    /**
     * 사용자 목록 조회 관련 테스트 클래스
     * 사용자 목록 조회 비즈니스 로직에 대한 다양한 시나리오를 테스트합니다.
     *
     * @see UserService#getAllUsers()
     */
    @Nested
    @DisplayName("getAllUsers 메소드는")
    class GetAllUsers {

        /**
         * 모든 사용자 목록을 성공적으로 조회할 수 있는지 테스트합니다.
         * 조회된 사용자 목록의 크기와 각 사용자의 ID가 예상값과 일치하는지 확인합니다.
         *
         * @throws Exception 테스트 실행 중 발생할 수 있는 예외
         * 
         * @see UserDto.Response
         */
        @Test
        @DisplayName("모든 사용자 목록을 조회할 수 있다")
        void getAllUsers_ShouldReturnAllUsers() throws Exception {
            // given
            List<User> users = Arrays.asList(testUser);
            given(userRepository.findAllUsers()).willReturn(users);
            given(userMapper.toDto(testUser)).willReturn(responseDto);

            // when
            List<UserDto.Response> result = userService.getAllUsers();

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(testUser.getId());
        }
    }

    /**
     * 사용자 정보 수정 관련 테스트 클래스
     * 사용자 정보 수정 비즈니스 로직에 대한 다양한 시나리오를 테스트합니다.
     *
     * @see UserService#updateUser(Long, UserDto.UpdateRequest)
     */
    @Nested
    @DisplayName("updateUser 메소드는")
    class UpdateUser {

        /**
         * 존재하는 사용자의 정보를 성공적으로 수정할 수 있는지 테스트합니다.
         * 수정된 사용자의 정보가 예상값과 일치하는지 확인합니다.
         *
         * @throws Exception 테스트 실행 중 발생할 수 있는 예외
         * 
         * @see UserDto.UpdateRequest
         * @see UserDto.Response
         */
        @Test
        @DisplayName("존재하는 사용자의 정보를 수정할 수 있다")
        void updateUser_WithValidRequest_ShouldUpdateUser() throws Exception {
            // given
            given(userRepository.findUserById(1L)).willReturn(Optional.of(testUser));
            given(userMapper.toDto(testUser)).willReturn(responseDto);

            // when
            UserDto.Response result = userService.updateUser(1L, updateRequest);

            // then
            assertThat(result).isNotNull();
            verify(userRepository).findUserById(1L);
        }

        /**
         * 존재하지 않는 사용자 수정 시 예외가 발생하는지 테스트합니다.
         * CustomException이 발생하고 적절한 에러 메시지가 포함되어 있는지 확인합니다.
         *
         * @throws Exception 테스트 실행 중 발생할 수 있는 예외
         * 
         * @see CustomException
         */
        @Test
        @DisplayName("존재하지 않는 사용자 수정 시 예외가 발생한다")
        void updateUser_WithNonExistingId_ShouldThrowException() throws Exception {
            // given
            given(userRepository.findUserById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateUser(99L, updateRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("User not found");
        }
    }

    /**
     * 사용자 삭제 관련 테스트 클래스
     * 사용자 삭제 비즈니스 로직에 대한 다양한 시나리오를 테스트합니다.
     *
     * @see UserService#deleteUser(Long)
     */
    @Nested
    @DisplayName("deleteUser 메소드는")
    class DeleteUser {

        /**
         * 존재하는 사용자를 성공적으로 삭제할 수 있는지 테스트합니다.
         * 삭제된 사용자의 데이터가 정상적으로 삭제되었는지 확인합니다.
         *
         * @throws Exception 테스트 실행 중 발생할 수 있는 예외
         * 
         * @see UserRepository#delete(User)
         */
        @Test
        @DisplayName("존재하는 사용자를 삭제할 수 있다")
        void deleteUser_WithExistingId_ShouldDeleteUser() throws Exception {
            // given
            given(userRepository.findUserById(1L)).willReturn(Optional.of(testUser));

            // when
            userService.deleteUser(1L);

            // then
            verify(userRepository).delete(testUser);
        }

        /**
         * 존재하지 않는 사용자 삭제 시 예외가 발생하는지 테스트합니다.
         * CustomException이 발생하고 적절한 에러 메시지가 포함되어 있는지 확인합니다.
         *
         * @throws Exception 테스트 실행 중 발생할 수 있는 예외
         * 
         * @see CustomException
         */
        @Test
        @DisplayName("존재하지 않는 사용자 삭제 시 예외가 발생한다")
        void deleteUser_WithNonExistingId_ShouldThrowException() throws Exception {
            // given
            given(userRepository.findUserById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("User not found");
        }
    }
} 