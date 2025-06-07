package blog.vans_story_be.domain.auth.dto

/**
 * JWT 토큰 쌍을 나타내는 데이터 클래스입니다.
 * 
 * <p>액세스 토큰과 리프레시 토큰을 함께 관리합니다.</p>
 * 
 * @property accessToken 액세스 토큰
 * @property refreshToken 리프레시 토큰
 */
data class TokenPair(
    val accessToken: String,
    val refreshToken: String
) 