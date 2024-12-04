package blog.vans_story_be.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import blog.vans_story_be.domain.auth.dto.TokenDto;
import blog.vans_story_be.domain.auth.dto.LoginRequest;
import blog.vans_story_be.domain.auth.service.AuthService;
import blog.vans_story_be.global.exception.CustomException;
import blog.vans_story_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 인증 관련 요청을 처리하는 컨트롤러
 * 로그인, 토큰 갱신 등의 인증 관련 엔드포인트를 제공합니다.
 *
 * @author van
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {
    private final AuthService authService;

    /**
     * 사용자 로그인을 처리합니다.
     * Access Token은 응답 본문에, Refresh Token은 HTTP Only 쿠키에 포함하여 반환합니다.
     *
     * @param request 로그인 요청 정보 (사용자명, 비밀번호)
     * @param response HTTP 응답 객체 (쿠키 설정에 사용)
     * @return 로그인 성공 시 Access Token이 포함된 응답
     * @throws CustomException 인증 실패 시 발생
     */
    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDto>> login(
            @Valid @RequestBody LoginRequest request, 
            HttpServletResponse response) {
        TokenDto tokenDto = authService.login(request);
        addRefreshTokenToCookie(response, tokenDto.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(tokenDto));
    }

    /**
     * Access Token을 갱신합니다.
     * 쿠키에 저장된 Refresh Token을 검증하여 새로운 Access Token을 발급합니다.
     *
     * @param refreshToken 쿠키에서 추출한 Refresh Token
     * @return 새로 발급된 Access Token이 포함된 응답
     * @throws CustomException 토큰이 유효하지 않거나 만료된 경우 발생
     */
    @Operation(summary = "토큰 갱신", description = "Access Token을 갱신합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenDto>> refresh(
            @CookieValue(name = "refreshToken") String refreshToken) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(refreshToken)));
    }

    /**
     * Refresh Token을 HTTP Only 쿠키에 추가합니다.
     *
     * @param response HTTP 응답 객체
     * @param refreshToken 저장할 Refresh Token
     */
    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);  // HTTPS에서만 전송
        cookie.setPath("/");
        response.addCookie(cookie);
    }
} 