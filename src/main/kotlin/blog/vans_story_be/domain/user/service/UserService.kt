package blog.vans_story_be.domain.user.service

import blog.vans_story_be.domain.user.dto.UserDto
import blog.vans_story_be.domain.user.entity.Role
import blog.vans_story_be.domain.user.entity.User
import blog.vans_story_be.domain.user.mapper.UserMapper
import blog.vans_story_be.domain.user.repository.UserRepository
import blog.vans_story_be.global.exception.CustomException
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * 주요 기능:
 * - 사용자 계정 생성 (일반 사용자/관리자)
 * - 사용자 정보 조회 (전체/개별)
 * - 사용자 정보 수정
 * - 사용자 계정 삭제
 * - 사용자 정보 검증
 *
 * 사용 예시:
 * ```kotlin
 * @RestController
 * class UserController(
 *     private val userService: UserService
 * ) {
 *     @PostMapping("/users")
 *     fun createUser(@RequestBody request: UserDto.CreateRequest): UserDto.Response =
 *         userService.createUser(request)
 *
 *     @GetMapping("/users/{id}")
 *     fun getUser(@PathVariable id: Long): UserDto.Response =
 *         userService.getUserById(id)
 * }
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    /**
     * 일반 사용자 계정을 생성합니다.
     *
     * @param request 사용자 생성 요청 정보
     * @return 생성된 사용자 정보
     * @throws CustomException 사용자명 또는 이메일이 이미 존재하는 경우
     *
     * 사용 예시:
     * ```kotlin
     * val userRequest = UserDto.CreateRequest(
     *     name = "user",
     *     email = "user@example.com",
     *     password = "user123!",
     *     nickname = "일반사용자"
     * )
     * val user = userService.createUser(userRequest)
     * 
     * // 관리자로 변경하려면
     * userService.updateRole(user.id, Role.ADMIN)
     * ```
     */
    fun createUser(request: UserDto.CreateRequest): UserDto.Response {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email)) {
            throw CustomException("이미 존재하는 이메일입니다")
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.nickname)) {
            throw CustomException("이미 존재하는 닉네임입니다")
        }

        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(request.password)

        // 엔티티 생성 및 저장
        val user = userMapper.toEntity(request, encodedPassword)
        userRepository.save(user)

        log.info { "사용자 계정 생성 완료: id=${user.id}, name=${user.name}" }
        return userMapper.toResponseDto(user)
    }

    /**
     * 관리자 계정을 생성합니다.
     *
     * @param request 사용자 생성 요청 정보
     * @return 생성된 관리자 정보
     *
     * 사용 예시:
     * ```kotlin
     * val adminRequest = UserDto.CreateRequest(
     *     name = "admin",
     *     email = "admin@example.com",
     *     password = "admin123!",
     *     nickname = "관리자"
     * )
     * val admin = userService.createAdmin(adminRequest)
     * ```
     */
    fun createAdmin(request: UserDto.CreateRequest): UserDto.Response {
        // 일반 사용자로 먼저 생성
        val user = createUser(request)
        
        // 관리자 역할로 변경
        updateRole(user.id, Role.ADMIN)
        
        // 변경된 사용자 정보 반환
        return getUserById(user.id)
    }

    /**
     * 모든 사용자 정보를 조회합니다.
     *
     * @return 사용자 목록
     *
     * 사용 예시:
     * ```kotlin
     * val users = userService.getAllUsers()
     * users.forEach { user ->
     *     println("사용자: ${user.name}, 이메일: ${user.email}")
     * }
     * ```
     */
    fun getAllUsers(): List<UserDto.Response> =
        userRepository.findAllUsers()
            .map(userMapper::toResponseDto)
            .also { log.debug { "전체 사용자 조회 완료: ${it.size}명" } }

    /**
     * ID로 사용자를 조회합니다.
     *
     * @param id 사용자 ID
     * @return 조회된 사용자 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우
     *
     * 사용 예시:
     * ```kotlin
     * val user = userService.getUserById(1L)
     * println("조회된 사용자: ${user.name}")
     * ```
     */
    fun getUserById(id: Long): UserDto.Response =
        userRepository.findUserById(id)
            .map(userMapper::toResponseDto)
            .orElseThrow { CustomException("사용자를 찾을 수 없습니다") }
            .also { log.debug { "사용자 조회 완료: id=$id" } }

    /**
     * 사용자 정보를 수정합니다.
     *
     * @param id 수정할 사용자 ID
     * @param request 수정할 사용자 정보
     * @return 수정된 사용자 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우
     *
     * 사용 예시:
     * ```kotlin
     * val updateRequest = UserDto.UpdateRequest(
     *     email = "new@example.com",
     *     password = "newPassword123!"
     * )
     * val updatedUser = userService.updateUser(1L, updateRequest)
     * ```
     */
    fun updateUser(id: Long, request: UserDto.UpdateRequest): UserDto.Response {
        val user = userRepository.findUserById(id)
            .orElseThrow { CustomException("사용자를 찾을 수 없습니다") }

        // 이메일 중복 체크 (다른 사용자의 이메일과 중복되는 경우)
        request.email?.let { email ->
            if (email != user.email && userRepository.existsByEmail(email)) {
                throw CustomException("이미 존재하는 이메일입니다")
            }
        }

        // 닉네임 중복 체크 (다른 사용자의 닉네임과 중복되는 경우)
        request.nickname?.let { nickname ->
            if (nickname != user.nickname && userRepository.existsByNickname(nickname)) {
                throw CustomException("이미 존재하는 닉네임입니다")
            }
        }

        val updateDto = userMapper.toUpdateDto(
            entity = user,
            email = request.email,
            password = request.password?.let { passwordEncoder.encode(it) },
            nickname = request.nickname
        )
        userMapper.updateEntity(updateDto, user)
        userRepository.save(user)

        log.info { "사용자 정보 수정 완료: id=$id" }
        return userMapper.toResponseDto(user)
    }

    /**
     * 사용자를 삭제합니다.
     *
     * @param id 삭제할 사용자 ID
     * @throws CustomException 사용자를 찾을 수 없는 경우
     *
     * 사용 예시:
     * ```kotlin
     * userService.deleteUser(1L)
     * ```
     */
    fun deleteUser(id: Long) {
        val user = userRepository.findUserById(id)
            .orElseThrow { CustomException("사용자를 찾을 수 없습니다") }
        userRepository.delete(user)
        log.info { "사용자 삭제 완료: id=$id" }
    }

    /**
     * 사용자명 존재 여부를 확인합니다.
     *
     * @param name 확인할 사용자명
     * @return 사용자명 존재 여부
     *
     * 사용 예시:
     * ```kotlin
     * if (userService.existsByName("홍길동")) {
     *     println("이미 존재하는 사용자명입니다")
     * }
     * ```
     */
    fun existsByName(name: String): Boolean =
        userRepository.existsByName(name)
            .also { log.debug { "사용자명 존재 여부 확인: name=$name, exists=$it" } }

    /**
     * 이메일로 사용자의 닉네임을 조회합니다.
     *
     * @param email 조회할 사용자의 이메일
     * @return 사용자의 닉네임
     * @throws CustomException 사용자를 찾을 수 없는 경우
     *
     * 사용 예시:
     * ```kotlin
     * val nickname = userService.getNicknameByEmail("user@example.com")
     * println("사용자 닉네임: $nickname")
     * ```
     */
    fun getNicknameByEmail(email: String): String =
        userRepository.findByEmail(email)
            .map(User::nickname)
            .orElseThrow { CustomException("사용자를 찾을 수 없습니다") }
            .also { log.debug { "닉네임 조회 완료: email=$email, nickname=$it" } }

    /**
     * 사용자의 비밀번호를 업데이트합니다.
     *
     * @param id 사용자 ID
     * @param newPassword 새로운 비밀번호 (평문)
     * @throws CustomException 사용자를 찾을 수 없는 경우
     * @throws IllegalArgumentException 비밀번호가 비어있는 경우
     */
    fun updatePassword(id: Long, newPassword: String) {
        require(newPassword.isNotBlank()) { "비밀번호는 비어있을 수 없습니다" }
        
        val user = userRepository.findUserById(id)
            .orElseThrow { CustomException("사용자를 찾을 수 없습니다") }
        
        val updateDto = userMapper.toUpdateDto(
            entity = user,
            password = passwordEncoder.encode(newPassword)
        )
        userMapper.updateEntity(updateDto, user)
        userRepository.save(user)
        
        log.info { "사용자 비밀번호 업데이트: id=$id" }
    }

    /**
     * 사용자의 역할을 업데이트합니다.
     *
     * @param id 사용자 ID
     * @param newRole 새로운 역할
     * @throws CustomException 사용자를 찾을 수 없는 경우
     */
    fun updateRole(id: Long, newRole: Role) {
        val user = userRepository.findUserById(id)
            .orElseThrow { CustomException("사용자를 찾을 수 없습니다") }
        
        val updateDto = userMapper.toUpdateDto(
            entity = user,
            role = newRole
        )
        userMapper.updateEntity(updateDto, user)
        userRepository.save(user)
        
        log.info { "사용자 역할 업데이트: id=$id, role=$newRole" }
    }
} 