package blog.vans_story_be.domain.auth.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 정보를 담는 DTO 클래스
 * 사용자로부터 받은 로그인 정보를 전달하는데 사용됩니다.
 *
 * @author van
 * @since 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    /**
     * 사용자 이름
     * 로그인에 사용되는 고유한 사용자 식별자입니다.
     */
    @NotBlank(message = "사용자 이름은 필수입니다.")
    private String email;
    
    /**
     * 비밀번호
     * 사용자 인증에 사용되는 비밀번호입니다.
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
} 