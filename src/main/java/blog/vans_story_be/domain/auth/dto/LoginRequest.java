package blog.vans_story_be.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그인 요청 정보를 담는 DTO 클래스
 * 사용자로부터 받은 로그인 정보를 전달하는데 사용됩니다.
 * 
 * <pre>
 * Field Validation:
 * - email: 이메일 형식의 로그인 아이디 (필수, 공백 불가)
 * - password: 사용자 비밀번호 (필수, 공백 불가, 8자 이상, 영문/숫자/특수문자 조합)
 * </pre>
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.12.14
 *
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청 DTO")
public class LoginRequest {
    /**
     * @validation 공백 불가
     * @format example@domain.com
     */
    @Schema(
        description = "이메일 형식의 로그인 아이디",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "사용자 이름은 필수입니다.")
    private String email;
    
    /**
     * @validation 공백 불가
     * @format 8자 이상, 영문/숫자/특수문자 조합
     */
    @Schema(
        description = "사용자 비밀번호 (8자 이상, 영문/숫자/특수문자 조합)",
        example = "Password1!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
} 