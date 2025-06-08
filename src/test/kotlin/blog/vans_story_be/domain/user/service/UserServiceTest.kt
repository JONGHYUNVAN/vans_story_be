package blog.vans_story_be.domain.user.service

import auth.support.TestDataBuilder
import blog.vans_story_be.domain.user.dto.UserDto
import blog.vans_story_be.domain.user.entity.Role
import blog.vans_story_be.domain.user.entity.User
import blog.vans_story_be.domain.user.mapper.UserMapper
import blog.vans_story_be.domain.user.repository.UserRepository
import blog.vans_story_be.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class UserServiceTest : DescribeSpec({
    
    // 테스트에 필요한 모의 객체들
    val mockUserRepository = mockk<UserRepository>()
    val mockUserMapper = mockk<UserMapper>()
    val mockPasswordEncoder = mockk<PasswordEncoder>()
    
    // 테스트할 서비스 인스턴스
    val userService = UserService(
        userRepository = mockUserRepository,
        userMapper = mockUserMapper,
        passwordEncoder = mockPasswordEncoder
    )
    
    describe("createUser 메서드는") {
        context("유효한 사용자 생성 요청이 주어지면") {
            val createRequest = TestDataBuilder.createSignupRequest()
            val encodedPassword = "encoded_password"
            val mockUser = mockk<User>()
            val mockResponseDto = mockk<UserDto.Response>()
            
            beforeEach {
                // 중복 체크 실패 시나리오 설정
                every { mockUserRepository.existsByEmail(any()) } returns false
                every { mockUserRepository.existsByNickname(any()) } returns false
                every { mockPasswordEncoder.encode(any()) } returns encodedPassword
                every { mockUserMapper.toEntity(createRequest, encodedPassword) } returns mockUser
                every { mockUserRepository.save(mockUser) } returns mockUser
                every { mockUserMapper.toResponseDto(mockUser) } returns mockResponseDto
            }
            
            it("새로운 사용자를 생성하고 응답 DTO를 반환해야 한다") {
                // when
                val result = userService.createUser(createRequest)
                
                // then
                result shouldBe mockResponseDto
                
                // verify
                verify {
                    mockUserRepository.existsByEmail(createRequest.email)
                    mockUserRepository.existsByNickname(createRequest.nickname)
                    mockPasswordEncoder.encode(createRequest.password)
                    mockUserMapper.toEntity(createRequest, encodedPassword)
                    mockUserRepository.save(mockUser)
                    mockUserMapper.toResponseDto(mockUser)
                }
            }
        }
        
        context("이미 존재하는 이메일로 요청이 주어지면") {
            val createRequest = TestDataBuilder.createSignupRequest()
            
            beforeEach {
                clearAllMocks()
                every { mockUserRepository.existsByEmail(any()) } returns false
                every { mockUserRepository.existsByEmail(createRequest.email) } returns true
                every { mockUserRepository.existsByNickname(any()) } returns false
            }
            
            it("CustomException을 던져야 한다") {
                // when & then
                val exception = shouldThrow<CustomException> {
                    userService.createUser(createRequest)
                }
                
                exception.message shouldBe "이미 존재하는 이메일입니다"
                
                verify(exactly = 1) { mockUserRepository.existsByEmail(createRequest.email) }
                verify(exactly = 0) { mockUserRepository.save(any()) }
            }
        }
    }
    
    describe("getUserById 메서드는") {
        context("존재하는 사용자 ID가 주어지면") {
            val userId = 1L
            val mockUser = mockk<User>()
            val mockResponseDto = mockk<UserDto.Response>()
            
            beforeEach {
                every { mockUserRepository.findUserById(userId) } returns Optional.of(mockUser)
                every { mockUserMapper.toResponseDto(mockUser) } returns mockResponseDto
            }
            
            it("해당 사용자의 정보를 반환해야 한다") {
                // when
                val result = userService.getUserById(userId)
                
                // then
                result shouldBe mockResponseDto
                
                // verify
                verify {
                    mockUserRepository.findUserById(userId)
                    mockUserMapper.toResponseDto(mockUser)
                }
            }
        }
        
        context("존재하지 않는 사용자 ID가 주어지면") {
            val userId = 999L
            
            beforeEach {
                every { mockUserRepository.findUserById(userId) } returns Optional.empty()
            }
            
            it("CustomException을 던져야 한다") {
                // when & then
                val exception = shouldThrow<CustomException> {
                    userService.getUserById(userId)
                }
                
                exception.message shouldBe "사용자를 찾을 수 없습니다"
                
                verify(exactly = 1) { mockUserRepository.findUserById(userId) }
            }
        }
    }
    
    describe("updateUser 메서드는") {
        context("유효한 업데이트 요청이 주어지면") {
            val userId = 1L
            val updateRequest = UserDto.UpdateRequest(
                email = "new@example.com",
                password = "newPassword123!",
                nickname = "새로운닉네임"
            )
            val mockUser = mockk<User>()
            val mockResponseDto = mockk<UserDto.Response>()
            val encodedPassword = "encoded_new_password"
            val mockUpdateDto = mockk<UserDto.UpdateRequest>()
            
            beforeEach {
                every { mockUserRepository.findUserById(userId) } returns Optional.of(mockUser)
                every { mockUser.email } returns "old@example.com"
                every { mockUser.nickname } returns "기존닉네임"
                every { mockUserRepository.existsByEmail(any()) } returns false
                every { mockUserRepository.existsByNickname(any()) } returns false
                every { mockPasswordEncoder.encode(any()) } returns encodedPassword
                every { mockUserMapper.toUpdateDto(mockUser, updateRequest.email, encodedPassword, updateRequest.nickname) } returns mockUpdateDto
                every { mockUserMapper.updateEntity(mockUpdateDto, mockUser) } just Runs
                every { mockUserRepository.save(mockUser) } returns mockUser
                every { mockUserMapper.toResponseDto(mockUser) } returns mockResponseDto
            }
            
            it("사용자 정보를 업데이트하고 응답 DTO를 반환해야 한다") {
                // when
                val result = userService.updateUser(userId, updateRequest)
                
                // then
                result shouldBe mockResponseDto
                
                // verify
                verify {
                    mockUserRepository.findUserById(userId)
                    mockUserRepository.existsByEmail(updateRequest.email!!)
                    mockUserRepository.existsByNickname(updateRequest.nickname!!)
                    mockPasswordEncoder.encode(updateRequest.password!!)
                    mockUserMapper.toUpdateDto(mockUser, updateRequest.email, encodedPassword, updateRequest.nickname)
                    mockUserMapper.updateEntity(mockUpdateDto, mockUser)
                    mockUserRepository.save(mockUser)
                    mockUserMapper.toResponseDto(mockUser)
                }
            }
        }
    }
    
    describe("deleteUser 메서드는") {
        context("존재하는 사용자 ID가 주어지면") {
            val userId = 1L
            val mockUser = mockk<User>()
            
            beforeEach {
                every { mockUserRepository.findUserById(userId) } returns Optional.of(mockUser)
                every { mockUserRepository.delete(mockUser) } just Runs
            }
            
            it("해당 사용자를 삭제해야 한다") {
                // when
                userService.deleteUser(userId)
                
                // verify
                verify {
                    mockUserRepository.findUserById(userId)
                    mockUserRepository.delete(mockUser)
                }
            }
        }
    }
    
    describe("updateRole 메서드는") {
        context("유효한 역할 업데이트 요청이 주어지면") {
            val userId = 1L
            val newRole = Role.ADMIN
            val mockUser = mockk<User>()
            val mockUpdateDto = mockk<UserDto.UpdateRequest>()
            
            beforeEach {
                every { mockUserRepository.findUserById(userId) } returns Optional.of(mockUser)
                every { mockUserMapper.toUpdateDto(mockUser, role = newRole) } returns mockUpdateDto
                every { mockUserMapper.updateEntity(mockUpdateDto, mockUser) } just Runs
                every { mockUserRepository.save(mockUser) } returns mockUser
            }
            
            it("사용자의 역할을 업데이트해야 한다") {
                // when
                userService.updateRole(userId, newRole)
                
                // verify
                verify {
                    mockUserRepository.findUserById(userId)
                    mockUserMapper.toUpdateDto(mockUser, role = newRole)
                    mockUserMapper.updateEntity(mockUpdateDto, mockUser)
                    mockUserRepository.save(mockUser)
                }
            }
        }
    }
}) 