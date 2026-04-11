package blog.vans_story_be.domain.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 로그인 요청 정보를 담는 데이터 클래스입니다.
 * 
 * <p>사용자 인증을 위해 필요한 로그인 정보를 전달하는데 사용됩니다.
 * 이메일과 비밀번호를 통해 사용자를 인증합니다.</p>
 * 
 * <h4>필드 검증:</h4>
 * <ul>
 *   <li>email: 이메일 형식의 로그인 아이디 (필수, 공백 불가)</li>
 *   <li>password: 사용자 비밀번호 (필수, 공백 불가, 8자 이상, 영문/숫자/특수문자 조합)</li>
 * </ul>
 * 
 * <h4>사용 예시:</h4>
 * <pre>
 * // 기본 생성
 * val loginRequest = LoginRequest(
 *     email = "user@example.com",
 *     password = "Password1!"
 * )
 * 
 * // 컨트롤러에서 사용
 * @PostMapping("/login")
 * fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
 *     return authService.login(request)
 * }
 * </pre>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see blog.vans_story_be.domain.auth.service.AuthService
 */
@Schema(description = "로그인 요청 DTO")
data class LoginRequest(
    /**
     * 이메일 형식의 로그인 아이디입니다.
     * 
     * <p>사용자의 고유 식별자로 사용되며, 이메일 형식을 따라야 합니다.</p>
     * 
     * <h4>검증 규칙:</h4>
     * <ul>
     *   <li>필수 입력</li>
     *   <li>공백 불가</li>
     *   <li>이메일 형식 준수</li>
     * </ul>
     * 
     * @example user@example.com
     */
    @field:Schema(
        description = "이메일 형식의 로그인 아이디",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "유효한 이메일 형식이어야 합니다.")
    val email: String,
    
    /**
     * 사용자 비밀번호입니다.
     * 
     * <p>계정 보안을 위한 비밀번호로, 복잡한 조합이 필요합니다.</p>
     * 
     * <h4>검증 규칙:</h4>
     * <ul>
     *   <li>필수 입력</li>
     *   <li>공백 불가</li>
     *   <li>8자 이상</li>
     *   <li>영문/숫자/특수문자 조합</li>
     * </ul>
     * 
     * @example Password1!
     */
    @field:Schema(
        description = "사용자 비밀번호 (8자 이상, 영문/숫자/특수문자 조합)",
        example = "Password1!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    val password: String
)

/**
 * LoginRequest의 확장 함수들입니다.
 */
fun LoginRequest.toMap(): Map<String, String> = mapOf(
    "email" to email,
    "password" to "********" // 비밀번호는 마스킹 처리
)

/**
 * 로그인 요청을 안전하게 로깅하기 위한 확장 함수입니다.
 */
fun LoginRequest.toLogString(): String = 
    "LoginRequest(email=${email.take(3)}..., password=********)" 