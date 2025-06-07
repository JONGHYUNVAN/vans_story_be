package blog.vans_story_be.domain.auth.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * JWT 토큰의 생성, 검증, 파싱을 담당하는 클래스입니다.
 * 
 * <p>JWT 토큰의 생명주기를 관리하며, 토큰의 생성, 검증, 파싱 기능을 제공합니다.
 * 액세스 토큰과 리프레시 토큰을 모두 지원합니다.</p>
 * 
 * <h4>주요 기능:</h4>
 * <ul>
 *   <li>액세스 토큰 생성 (기본 5시간)</li>
 *   <li>리프레시 토큰 생성 (기본 7일)</li>
 *   <li>토큰 유효성 검증</li>
 *   <li>토큰에서 인증 정보 추출</li>
 * </ul>
 * 
 * <h4>사용 예시:</h4>
 * <pre>
 * // 토큰 생성
 * val accessToken = jwtProvider.generateAccessToken(authentication)
 * val refreshToken = jwtProvider.generateRefreshToken(authentication)
 * 
 * // 토큰 검증
 * if (jwtProvider.validateToken(token)) {
 *     val authentication = jwtProvider.getAuthentication(token)
 *     // 인증 처리
 * }
 * </pre>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see JwtProperties
 * @see org.springframework.security.core.Authentication
 */
@Component
class JwtProvider(
    private val jwtProperties: JwtProperties
) {
    private lateinit var key: Key
    private val logger = KotlinLogging.logger {}

    /**
     * JWT 서명에 사용할 키를 초기화합니다.
     * 
     * <p>application.yml에 설정된 secretKey를 디코딩하여 HMAC-SHA 키를 생성합니다.</p>
     */
    @PostConstruct
    protected fun init() {
        val keyBytes = Base64.getDecoder().decode(jwtProperties.secretKey)
        key = Keys.hmacShaKeyFor(keyBytes)
    }

    /**
     * 액세스 토큰을 생성합니다.
     * 
     * @param authentication 인증 정보
     * @return 생성된 액세스 토큰
     */
    fun generateAccessToken(authentication: Authentication): String =
        generateToken(authentication, jwtProperties.accessTokenValidityInSeconds)

    /**
     * 리프레시 토큰을 생성합니다.
     * 
     * @param authentication 인증 정보
     * @return 생성된 리프레시 토큰
     */
    fun generateRefreshToken(authentication: Authentication): String =
        generateToken(authentication, jwtProperties.refreshTokenValidityInSeconds)

    /**
     * JWT 토큰을 생성합니다.
     * 
     * @param authentication 인증 정보
     * @param validityInSeconds 토큰 유효 기간(초)
     * @return 생성된 JWT 토큰
     */
    private fun generateToken(authentication: Authentication, validityInSeconds: Long): String {
        val authorities = authentication.authorities.joinToString(",") { it.authority }
        val now = Date()
        val validity = Date(now.time + validityInSeconds * 1000)

        return Jwts.builder()
            .setSubject(authentication.name)
            .claim("auth", authorities)
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact()
    }

    /**
     * JWT 토큰에서 인증 정보를 추출합니다.
     * 
     * @param token JWT 토큰
     * @return 인증 정보
     * @throws JwtException 토큰 파싱 실패 시
     */
    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val authorities = claims["auth"]
            .toString()
            .split(",")
            .map { SimpleGrantedAuthority(it) }

        val principal = User(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     * 
     * @param token 검증할 JWT 토큰
     * @return 토큰 유효성 여부
     */
    fun validateToken(token: String): Boolean = try {
        parseClaims(token)
        true
    } catch (e: SecurityException) {
        logger.info { "잘못된 JWT 서명입니다." }
        false
    } catch (e: MalformedJwtException) {
        logger.info { "잘못된 JWT 토큰입니다." }
        false
    } catch (e: ExpiredJwtException) {
        logger.info { "만료된 JWT 토큰입니다." }
        false
    } catch (e: UnsupportedJwtException) {
        logger.info { "지원되지 않는 JWT 토큰입니다." }
        false
    } catch (e: IllegalArgumentException) {
        logger.info { "JWT 토큰이 잘못되었습니다." }
        false
    }

    /**
     * JWT 토큰을 파싱하여 클레임을 추출합니다.
     * 
     * @param token 파싱할 JWT 토큰
     * @return 토큰의 클레임
     * @throws JwtException 토큰 파싱 실패 시
     */
    private fun parseClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
} 