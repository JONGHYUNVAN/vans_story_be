package blog.vans_story_be.domain.auth.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 토큰을 처리하는 필터입니다.
 * 
 * <p>HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출하고 검증합니다.
 * 유효한 토큰이 있는 경우 SecurityContext에 인증 정보를 설정합니다.</p>
 * 
 * <h4>처리 흐름:</h4>
 * <ol>
 *   <li>Authorization 헤더에서 Bearer 토큰 추출</li>
 *   <li>토큰 유효성 검증</li>
 *   <li>유효한 토큰인 경우 SecurityContext에 인증 정보 설정</li>
 *   <li>다음 필터로 요청 전달</li>
 * </ol>
 * 
 * <h4>사용 예시:</h4>
 * <pre>
 * // SecurityConfig에서 필터 등록
 * http.addFilterBefore(
 *     JwtFilter(jwtProvider),
 *     UsernamePasswordAuthenticationFilter::class.java
 * )
 * </pre>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see JwtProvider
 * @see org.springframework.security.core.context.SecurityContextHolder
 */
class JwtFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private val logger = KotlinLogging.logger {}
    }

    /**
     * HTTP 요청을 필터링하여 JWT 토큰을 처리합니다.
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     * @throws jakarta.servlet.ServletException 서블릿 예외
     * @throws java.io.IOException IO 예외
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 로그아웃 경로는 토큰 검증 건너뛰기
        if (request.requestURI == "/api/v1/auth/logout") {
            filterChain.doFilter(request, response)
            return
        }

        try {
            resolveToken(request)?.let { token ->
                if (jwtProvider.validateToken(token)) {
                    val authentication = jwtProvider.getAuthentication(token)
                    SecurityContextHolder.getContext().authentication = authentication
                    logger.debug { "JWT 토큰 인증 성공: ${authentication.name}" }
                }
            }
        } catch (e: Exception) {
            logger.error("JWT 토큰 처리 중 오류 발생")
        }
        
        filterChain.doFilter(request, response)
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰을 추출합니다.
     * 
     * @param request HTTP 요청
     * @return 추출된 JWT 토큰 또는 null
     */
    private fun resolveToken(request: HttpServletRequest): String? =
        request.getHeader(AUTHORIZATION_HEADER)?.let { bearerToken ->
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
                bearerToken.substring(BEARER_PREFIX.length)
            } else null
        }
} 