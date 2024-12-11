package blog.vans_story_be.domain.auth.service;

import jakarta.servlet.http.Cookie;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import blog.vans_story_be.domain.auth.entity.RefreshToken;
import blog.vans_story_be.domain.auth.jwt.JwtProvider;
import blog.vans_story_be.global.exception.CustomException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import blog.vans_story_be.domain.auth.dto.LoginRequest;
import blog.vans_story_be.domain.auth.repository.RefreshTokenRepository;

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
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 사용자 로그인을 처리하고 JWT 토큰을 발급합니다.
     *
     * @param request 로그인 요청 정보 (사용자명, 비밀번호)
     * @param response HTTP 응답 객체
     * @throws BadCredentialsException 인증 실패 시 발생
     */
    public void login(LoginRequest request, HttpServletResponse response) {
        // 1. 인증 정보 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        // 4. RefreshToken 저장
        saveRefreshToken(authentication.getName(), refreshToken);

        // 5. 토큰을 응답 헤더와 쿠키에 설정
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

        // 3. 저장소에서 User ID를 기반으로 Refresh Token 값을 가져옴
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new CustomException("로그아웃 된 사용자입니다."));

        // 4. Refresh Token 일치하는지 검사
        if (!refreshTokenEntity.getToken().equals(refreshToken)) {
            throw new CustomException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        String newAccessToken = jwtProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtProvider.generateRefreshToken(authentication);

        // 6. 저장소 정보 업데이트
        refreshTokenEntity.updateToken(newRefreshToken,LocalDateTime.now().plusDays(7));

        // 7. 토큰을 응답 헤더와 쿠키에 설정
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        addRefreshTokenToCookie(response, newRefreshToken);
    }

    /**
     * Refresh Token을 저장소에 저장합니다.
     * 이미 존재하는 경우 토큰 값을 업데이트합니다.
     *
     * @param username 사용자명
     * @param refreshToken 저장할 Refresh Token
     */
    private void saveRefreshToken(String username, String refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUsername(username)
        .map(entity -> entity.updateToken(refreshToken, LocalDateTime.now().plusDays(7))) // 만료 날짜 추가
        .orElseGet(() -> new RefreshToken(username, refreshToken, LocalDateTime.now().plusDays(7)));

        refreshTokenRepository.save(refreshTokenEntity);
    }

    public void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
} 