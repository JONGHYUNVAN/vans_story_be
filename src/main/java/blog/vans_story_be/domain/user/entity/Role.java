package blog.vans_story_be.domain.user.entity;

import lombok.Getter;

/**
 * 사용자 권한을 정의하는 열거형
 *  
 * <pre>
 * Roles:   
 * - USER: 일반 사용자
 * - ADMIN: 관리자
 * </pre>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
@Getter
public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");
    
    /**
     * 생성자
     * @param value 역할 값을 나타내는 String
     */
    private final String value;
    
    Role(String value) {
        this.value = value;
    }
} 