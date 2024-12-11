package blog.vans_story_be.domain.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh Token 정보를 저장하는 엔티티 클래스
 * 사용자별 Refresh Token을 데이터베이스에 저장하고 관리합니다.
 *
 * @author van
 * @since 1.0
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    /**
     * 사용자 이름
     * Refresh Token의 소유자를 식별하는 고유 식별자입니다.
     */
    @Id
    private String username;

    /**
     * Refresh Token 값
     * 실제 JWT Refresh Token 문자열입니다.
     */
    private String token;

    /**
     * Refresh Token 만료 날짜
     * 토큰이 만료되는 날짜와 시간입니다.
     */
    private LocalDateTime expiresAt;

    /**
     * Refresh Token 엔티티를 생성합니다.
     *
     * @param username 사용자 이름
     * @param token Refresh Token 값
     * @param expiresAt 만료 날짜
     */
    public RefreshToken(String username, String token, LocalDateTime expiresAt) {
        this.username = username;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    /**
     * Refresh Token 값을 업데이트합니다.
     *
     * @param token 새로운 Refresh Token 값
     * @param expiresAt 새로운 만료 날짜
     * @return 업데이트된 RefreshToken 엔티티
     */
    public RefreshToken updateToken(String token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
        return this;
    }

    /**
     * 토큰이 만료되었는지 확인합니다.
     *
     * @return 만료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}