package blog.vans_story_be.domain.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtProvider 테스트 클래스
 * JWT 토큰 생성 및 검증 로직을 테스트합니다.
 */
@SpringBootTest
class JwtProviderTest {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(jwtProperties);
        jwtProvider.init();
    }

    /**
     * Access Token 생성 테스트
     * 인증 정보로부터 유효한 Access Token이 생성되는지 검증합니다.
     */
    @Test
    @DisplayName("Access Token 생성 테스트")
    void generateAccessTokenTest() {
        // given
        User user = new User("testuser", "", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // when
        String token = jwtProvider.generateAccessToken(authentication);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    /**
     * 토큰 검증 테스트
     * 생성된 토큰이 올바르게 검증되는지 확인합니다.
     */
    @Test
    @DisplayName("토큰 검증 테스트")
    void validateTokenTest() {
        // given
        User user = new User("testuser", "", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        String token = jwtProvider.generateAccessToken(authentication);

        // when
        boolean isValid = jwtProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    /**
     * 토큰에서 인증 정보 추출 테스트
     * 토큰으로부터 원래의 인증 정보가 올바르게 추출되는지 검증합니다.
     */
    @Test
    @DisplayName("토큰에서 인증 정보 추출 테스트")
    void getAuthenticationTest() {
        // given
        User user = new User("testuser", "", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication originalAuth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        String token = jwtProvider.generateAccessToken(originalAuth);

        // when
        Authentication extractedAuth = jwtProvider.getAuthentication(token);

        // then
        assertThat(extractedAuth.getName()).isEqualTo(user.getUsername());
        assertThat(extractedAuth.getAuthorities().iterator().next())
                .isEqualTo(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    @DisplayName("시크릿 키 초기화 테스트")
    void initKeyTest() {
        // given
        String secretKey = jwtProperties.getSecretKey();
        System.out.println("Loaded Secret Key: " + secretKey);
        System.out.println("Secret Key Length: " + secretKey.length());
        System.out.println("Secret Key Bytes Length: " + secretKey.getBytes().length);

        assertThat(jwtProperties.getSecretKey()).isNotNull();

        // when
        jwtProvider.init();

        // then
        String token = jwtProvider.generateAccessToken(
            new UsernamePasswordAuthenticationToken("testuser", null, Collections.emptyList())
        );
        assertThat(token).isNotNull();
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }
} 