package auth.support

import blog.vans_story_be.domain.auth.jwt.JwtProperties
import blog.vans_story_be.domain.auth.jwt.JwtProvider
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.Base64
import java.util.Date

/**
 * 실제 서명·검증을 하는 JwtProvider와 테스트 토큰을 만드는 헬퍼
 */
object JwtTestSupport {
    // HS512에 충분한 64바이트 키 (Base64)
    val SECRET_KEY: String = Base64.getEncoder().encodeToString(ByteArray(64) { it.toByte() })
    const val ACCESS_VALIDITY = 18000L    // 5시간
    const val REFRESH_VALIDITY = 604800L  // 7일

    private val signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY))

    fun jwtProperties() = JwtProperties().apply {
        secretKey = SECRET_KEY
        accessTokenValidityInSeconds = ACCESS_VALIDITY
        refreshTokenValidityInSeconds = REFRESH_VALIDITY
    }

    /**
     * @PostConstruct init()까지 호출한 JwtProvider
     */
    fun jwtProvider(): JwtProvider = JwtProvider(jwtProperties()).also { provider ->
        JwtProvider::class.java.getDeclaredMethod("init")
            .apply { isAccessible = true }
            .invoke(provider)
    }

    fun authentication(
        email: String = TestDataBuilder.TEST_EMAIL,
        vararg authorities: String = arrayOf("ROLE_USER")
    ): Authentication =
        UsernamePasswordAuthenticationToken(email, null, authorities.map { SimpleGrantedAuthority(it) })

    fun parseClaims(token: String): Claims =
        Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).payload

    /**
     * typ 도입 전 JwtProvider와 같은 형식(sub, auth, exp)의 토큰. typ 값을 주면 그 값을 넣는다.
     */
    fun tokenWithType(
        typ: String?,
        email: String = TestDataBuilder.TEST_EMAIL,
        validityInSeconds: Long = REFRESH_VALIDITY
    ): String = Jwts.builder()
        .subject(email)
        .claim("auth", "ROLE_USER")
        .apply { if (typ != null) claim("typ", typ) }
        .signWith(signingKey)
        .expiration(Date(System.currentTimeMillis() + validityInSeconds * 1000))
        .compact()
}
