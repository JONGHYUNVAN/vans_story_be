package blog.vans_story_be.domain.auth.jwt

/**
 * JWT의 용도를 나타내는 `typ` 클레임 값입니다.
 *
 * <p>Access/Refresh 토큰은 같은 키로 서명되므로, 이 클레임이 없으면 7일짜리 Refresh 토큰이
 * Access 토큰으로도 통과합니다. 토큰을 검증하는 모든 서비스(이 서버, NestJS 게시글 서비스)는
 * 기대하는 타입과 일치하는지 확인해야 합니다.</p>
 *
 * @property claimValue JWT 페이로드에 들어가는 문자열 값
 */
enum class TokenType(val claimValue: String) {
    ACCESS("access"),
    REFRESH("refresh");

    companion object {
        /** 토큰 타입을 담는 페이로드 클레임 이름 */
        const val CLAIM = "typ"

        /**
         * 클레임 값으로 토큰 타입을 찾습니다.
         *
         * @param value `typ` 클레임 값
         * @return 일치하는 타입, 없거나 알 수 없는 값이면 null
         */
        fun fromClaim(value: Any?): TokenType? = entries.firstOrNull { it.claimValue == value }
    }
}
