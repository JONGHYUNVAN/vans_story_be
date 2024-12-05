package blog.vans_story_be.config.security;

import blog.vans_story_be.domain.user.entity.User;
import blog.vans_story_be.domain.user.repository.UserRepository;
import blog.vans_story_be.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Spring Security의 UserDetailsService 구현체
 * 사용자 인증에 필요한 정보를 로드합니다.
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 이메일로 사용자의 상세 정보를 조회합니다.
     *
     * @param email 조회할 이메일
     * @return UserDetails 사용자 상세 정보
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(this::createUserDetails)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));
    }

    /**
     * User 엔티티를 UserDetails 객체로 변환합니다.
     *
     * @param user User 엔티티
     * @return UserDetails 객체
     */
    private UserDetails createUserDetails(User user) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singleton(authority))
                .build();
    }
} 