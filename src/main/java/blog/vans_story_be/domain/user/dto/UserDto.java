package blog.vans_story_be.domain.user.dto;

import blog.vans_story_be.domain.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 사용자 정보 전송을 위한 DTO 클래스
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
public class UserDto {

    /**
     * 사용자 생성 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "사용자명은 필수입니다")
        @Size(min = 3, max = 50, message = "사용자명은 3자 이상 50자 이하여야 합니다")
        private String username;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다")
        private String password;

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;
    }

    /**
     * 사용자 수정 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다")
        private String password;

        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;
    }

    /**
     * 사용자 정보 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String username;
        private String email;
        private Role role;
        private String createdAt;
        private String updatedAt;
    }
} 