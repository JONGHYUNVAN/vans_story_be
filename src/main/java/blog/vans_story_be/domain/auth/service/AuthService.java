package blog.vans_story_be.domain.auth.service;

import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import blog.vans_story_be.domain.auth.jwt.JwtProvider;
import blog.vans_story_be.global.exception.CustomException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import blog.vans_story_be.domain.auth.dto.LoginRequest;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스
 * 로그인, 토큰 갱신, Refresh Token 관리 등을 담당합니다.
 * 
 * @author van
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    /**
     * 사용자 로그인을 처리하고 JWT 토큰을 발급합니다.
     * 
     * @param loginRequest 로그인 요청 정보 (사용자명, 비밀번호)
     * @param response HTTP 응답 객체
     * @throws BadCredentialsException 인증 실패 시 발생
     */
    public void login(LoginRequest loginRequest, HttpServletResponse response) {
        // 1. 인증 정보 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        // 4. 토큰을 응답 헤더와 쿠키에 설정
        response.setHeader("Authorization", "Bearer " + accessToken);
        addRefreshTokenToCookie(response, refreshToken);
    }

    /**
     * Refresh Token을 검증하고 새로운 토큰을 발급합니다.
     * 
     * @param refreshToken 갱신에 사용할 Refresh Token
     * @param response 새로 발급된 Access Token과 Refresh Token
     * @throws CustomException 토큰이 유효하지 않거나 사용자 정보가 일치하지 않는 경우 발생
     */
    public void refresh(String refreshToken, HttpServletResponse response) {
        // 1. Refresh Token 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new CustomException("Refresh Token이 유효하지 않습니다.");
        }

        // 2. Access Token에서 User 정보를 가져옴
        Authentication authentication = jwtProvider.getAuthentication(refreshToken);

        // 3. 새로운 토큰 생성
        String newAccessToken = jwtProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtProvider.generateRefreshToken(authentication);

        // 4. 토큰을 응답 헤더와 쿠키에 설정
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        addRefreshTokenToCookie(response, newRefreshToken);
    }

    public void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * MaxAge 0인 쿠키를 담은 응답을 반환합니다.
     *
     * @param response HTTP 응답 객체
     */
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}