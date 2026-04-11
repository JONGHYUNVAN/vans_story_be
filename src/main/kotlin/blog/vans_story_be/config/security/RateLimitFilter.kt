package blog.vans_story_be.config.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.filter.OncePerRequestFilter

/**
 * /login, /signup 엔드포인트에 Rate Limiting 적용 필터.
 * IP 기준 분당 5회 초과 시 429 응답 반환.
 */
class RateLimitFilter(
    private val loginRateLimiter: LoginRateLimiter
) : OncePerRequestFilter() {

    private val rateLimitedPaths = setOf(
        "/api/v1/auth/login",
        "/api/v1/auth/signup"
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.requestURI in rateLimitedPaths) {
            val ip = resolveClientIp(request)
            if (!loginRateLimiter.tryConsume(ip)) {
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.contentType = "application/json;charset=UTF-8"
                response.writer.write(
                    """{"code":"ERROR","message":"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.","data":null}"""
                )
                return
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveClientIp(request: HttpServletRequest): String =
        request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?: request.remoteAddr
}
