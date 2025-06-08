package blog.vans_story_be.domain.user.controller

import blog.vans_story_be.domain.user.dto.UserDto
import blog.vans_story_be.domain.user.service.UserService
import blog.vans_story_be.global.response.ApiResponse
import blog.vans_story_be.global.response.noContent
import blog.vans_story_be.global.response.withData
import blog.vans_story_be.global.response.withLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * 사용자 관련 요청을 처리하는 컨트롤러
 *
 * 주요 기능:
 * - 사용자 정보 조회
 * - 사용자 정보 수정
 * - 사용자 삭제
 * - 사용자 목록 조회
 *
 * API 엔드포인트:
 * - GET /api/v1/users: 사용자 목록 조회
 * - GET /api/v1/users/{id}: 특정 사용자 정보 조회
 * - PUT /api/v1/users/{id}: 사용자 정보 수정
 * - DELETE /api/v1/users/{id}: 사용자 삭제
 *
 * @property userService 사용자 관련 비즈니스 로직을 처리하는 서비스
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "사용자 관리 API")
class UserController(
    private val userService: UserService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 새로운 사용자를 생성합니다.
     * 관리자만 접근 가능합니다.
     *
     * @param request 사용자 생성 요청 데이터
     * @param response HTTP 응답 객체
     * @return 생성된 사용자 정보
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createUser(
        @Valid @RequestBody request: UserDto.CreateRequest,
    ): ResponseEntity<ApiResponse<Unit>> = withLogging("사용자 생성") {
        userService.createUser(request)
        ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Unit))
    }

    /**
     * 모든 사용자 목록을 조회합니다.
     *
     * @return 사용자 목록
     *
     * 사용 예시:
     * ```kotlin
     * // GET /api/v1/users
     * // 응답:
     * // {
     * //   "success": true,
     * //   "data": [
     * //     {
     * //       "id": 1,
     * //       "email": "user1@example.com",
     * //       "name": "User 1",
     * //       "role": "USER"
     * //     },
     * //     ...
     * //   ],
     * //   "message": null
     * // }
     * ```
     */
    @Operation(
        summary = "사용자 목록 조회",
        description = "모든 사용자의 목록을 조회합니다."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers() = withLogging("사용자 목록 조회") {
        userService.getAllUsers()
    }.withData(userService.getAllUsers())

    /**
     * 특정 ID의 사용자 정보를 조회합니다.
     *
     * @param id 조회할 사용자의 ID
     * @return 사용자 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우
     *
     * 사용 예시:
     * ```kotlin
     * // GET /api/v1/users/1
     * // 응답:
     * // {
     * //   "success": true,
     * //   "data": {
     * //     "id": 1,
     * //     "email": "user1@example.com",
     * //     "name": "User 1",
     * //     "role": "USER"
     * //   },
     * //   "message": null
     * // }
     * ```
     */
    @Operation(
        summary = "사용자 정보 조회",
        description = "특정 ID의 사용자 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    fun getUser(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable id: Long
    ) = withLogging("사용자 정보 조회") {
        userService.getUserById(id)
    }.withData(userService.getUserById(id))

    /**
     * 이메일로 사용자의 닉네임을 조회합니다.
     *
     * @param email 조회할 사용자의 이메일
     * @return 사용자의 닉네임
     * @throws CustomException 사용자를 찾을 수 없는 경우
     */
    @Operation(
        summary = "이메일로 닉네임 조회",
        description = "특정 이메일의 사용자 닉네임을 조회합니다."
    )
    @GetMapping("/email/{email}")
    fun getNicknameByEmail(
        @Parameter(description = "사용자 이메일", required = true)
        @PathVariable email: String
    ) = withLogging("이메일로 닉네임 조회") {
        userService.getNicknameByEmail(email)
    }.withData(userService.getNicknameByEmail(email))

    /**
     * 사용자 정보를 수정합니다.
     * 본인 또는 관리자만 수정 가능합니다.
     *
     * @param id 사용자 ID
     * @param request 수정할 사용자 정보
     * @return 수정된 사용자 정보
     */
    @Operation(
        summary = "사용자 정보 수정",
        description = "특정 ID의 사용자 정보를 수정합니다."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    fun updateUser(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable id: Long,
        @Valid @RequestBody updateRequest: UserDto.UpdateRequest
    ) = withLogging("사용자 정보 수정") {
        userService.updateUser(id, updateRequest)
    }.withData(userService.updateUser(id, updateRequest))

    /**
     * 사용자를 삭제합니다.
     * 본인 또는 관리자만 삭제 가능합니다.
     *
     * @param id 사용자 ID
     * @param response HTTP 응답 객체
     * @return 삭제 성공 응답
     */
    @Operation(
        summary = "사용자 삭제",
        description = "특정 ID의 사용자를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    fun deleteUser(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable id: Long,
        response: HttpServletResponse
    ) = withLogging("사용자 삭제") {
        userService.deleteUser(id)
        response.noContent()
    }

    /**
     * 로깅을 포함한 컨트롤러 메서드 실행을 위한 확장 함수
     *
     * @param operation 수행할 작업의 이름
     * @param block 실행할 코드 블록
     * @return API 응답
     */
    private inline fun <T> withLogging(
        operation: String,
        block: () -> T
    ): ResponseEntity<ApiResponse<T>> = try {
        logger.info { "[$operation] 시작" }
        val result = block()
        logger.info { "[$operation] 완료" }
        ResponseEntity.ok(ApiResponse.success(result))
    } catch (e: Exception) {
        logger.error(e) { "[$operation] 실패" }
        throw e
    }
} 