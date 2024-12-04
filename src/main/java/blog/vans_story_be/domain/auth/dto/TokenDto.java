package blog.vans_story_be.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JWT 토큰 정보를 담는 DTO 클래스
 * Access Token과 Refresh Token 정보를 전달하는데 사용됩니다.
 *
 * @author van
 * @since 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {
    /**
     * Access Token
     * API 요청 시 인증에 사용되는 JWT 토큰입니다.
     */
    private String accessToken;

    /**
     * Refresh Token
     * Access Token 갱신에 사용되는 JWT 토큰입니다.
     */
    private String refreshToken;
} 