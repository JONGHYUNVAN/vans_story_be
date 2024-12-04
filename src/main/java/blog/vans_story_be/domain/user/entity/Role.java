package blog.vans_story_be.domain.user.entity;

import lombok.Getter;

/**
 * 사용자 권한을 정의하는 열거형
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
@Getter
public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");
    
    private final String value;
    
    Role(String value) {
        this.value = value;
    }
} 