package blog.vans_story_be.domain.user.entity;

import blog.vans_story_be.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

/**
 * 사용자 정보를 관리하는 엔티티 클래스
 *  <pre>
 * Jakarta Bean Validation
 * JPA에 의해 데이터베이스에 저장하기 전 검사:
 * - id: 자동 생성된 사용자 ID
 * - name: 사용자명 (필수, 3자 이상 50자 이하)
 * - password: 비밀번호 (필수, 8자 이상, 영문자, 숫자, 특수문자 포함)
 * - email: 이메일 (필수, 유효한 이메일 형식)
 * - role: 사용자 역할 (필수)
 * </pre>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 *
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class User extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "User의 사용자명의 값이 없습니다")
    @Column(nullable = false, unique = true)
    private String name;
    
    @NotBlank(message = "User의 비밀번호의 값이 없습니다")
    @Column(nullable = false)
    private String password;
    
    @NotBlank(message = "User의 이메일의 값이 없습니다")
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank(message = "User의 닉네임의 값이 없습니다")
    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
} 