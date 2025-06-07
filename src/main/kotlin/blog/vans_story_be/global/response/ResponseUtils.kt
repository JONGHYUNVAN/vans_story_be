package blog.vans_story_be.global.response

import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.BodyBuilder

/**
 * HTTP 응답 생성을 위한 확장 함수들
 *
 * 주요 기능:
 * - 성공 응답 생성 (200 OK)
 * - 생성 성공 응답 (201 Created)
 * - 삭제 성공 응답 (204 No Content)
 * - 데이터와 함께하는 성공 응답
 * - 컨트롤러 메서드 실행 로깅
 *
 * 사용 예시:
 * ```kotlin
 * @PostMapping
 * fun createUser(
 *     @RequestBody request: CreateRequest,
 *     response: HttpServletResponse
 * ): ResponseEntity<ApiResponse<Nothing?>> = withLogging("사용자 생성") {
 *     userService.createUser(request)
 *     response.created()
 * }
 *
 * @GetMapping("/{id}")
 * fun getUser(@PathVariable id: Long): ResponseEntity<ApiResponse<UserDto>> = withLogging("사용자 조회") {
 *     userService.findUserById(id)
 * }.withData(userService.findUserById(id))
 * ```
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 */

/**
 * 로깅을 포함한 컨트롤러 메서드 실행을 위한 확장 함수
 *
 * @param operation 수행할 작업의 이름
 * @param block 실행할 코드 블록
 * @return API 응답
 */
inline fun <T> withLogging(
    operation: String,
    block: () -> T
): ResponseEntity<ApiResponse<T>> = try {
    val log = KotlinLogging.logger {}
    log.info { "[$operation] 시작" }
    val result = block()
    log.info { "[$operation] 완료" }
    ResponseEntity.ok(ApiResponse.success(result))
} catch (e: Exception) {
    val log = KotlinLogging.logger {}
    log.error(e) { "[$operation] 실패" }
    throw e
}

/**
 * 200 OK 응답을 생성합니다.
 * 주로 조회, 수정 등의 작업 성공 시 사용합니다.
 *
 * @return 성공 응답 (데이터 없음)
 */
fun HttpServletResponse.ok() = ResponseEntity
    .status(HttpStatus.OK)
    .body(ApiResponse.success<Nothing?>(null))

/**
 * 201 Created 응답을 생성합니다.
 * 주로 리소스 생성 성공 시 사용합니다.
 *
 * @return 생성 성공 응답 (데이터 없음)
 */
fun HttpServletResponse.created() = ResponseEntity
    .status(HttpStatus.CREATED)
    .body(ApiResponse.success<Nothing?>(null))

/**
 * 204 No Content 응답을 생성합니다.
 * 주로 삭제 작업 성공 시 사용합니다.
 *
 * @return 삭제 성공 응답 (데이터 없음)
 */
fun HttpServletResponse.noContent() = ResponseEntity
    .status(HttpStatus.NO_CONTENT)
    .body(ApiResponse.success<Nothing?>(null))

/**
 * 데이터와 함께하는 200 OK 응답을 생성합니다.
 *
 * @param data 응답에 포함할 데이터
 * @return 성공 응답 (데이터 포함)
 */
fun <T> ResponseEntity<ApiResponse<T>>.withData(data: T) = ResponseEntity
    .status(HttpStatus.OK)
    .body(ApiResponse.success(data))

/**
 * 에러 응답을 생성하는 확장 함수
 *
 * @param message 에러 메시지
 * @return 에러 응답 객체
 */
fun BodyBuilder.withError(message: String): ResponseEntity<ApiResponse<Unit>> =
    this.body(ApiResponse.error<Unit>(message)) 