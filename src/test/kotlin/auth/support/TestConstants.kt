package auth.support

/**
 * 테스트에 사용되는 상수 정의
 */
object TestConstants {
    // API 엔드포인트
    const val AUTH_API_BASE = "/api/auth"
    const val LOGIN_ENDPOINT = "$AUTH_API_BASE/login"
    const val SIGNUP_ENDPOINT = "$AUTH_API_BASE/signup"
    const val REFRESH_TOKEN_ENDPOINT = "$AUTH_API_BASE/refresh"
    
    // 테스트용 JWT 토큰
    const val TEST_JWT_SECRET = "test-jwt-secret-key-for-testing-only-do-not-use-in-production-vans-story-blog-2024"
    const val TEST_ACCESS_TOKEN_VALIDITY = 1800L    // 30분
    const val TEST_REFRESH_TOKEN_VALIDITY = 604800L // 7일
    
    // 테스트용 헤더
    const val AUTHORIZATION_HEADER = "Authorization"
    const val BEARER_PREFIX = "Bearer "
    
    // 테스트용 에러 메시지
    const val INVALID_CREDENTIALS = "잘못된 이메일 또는 비밀번호입니다"
    const val USER_NOT_FOUND = "사용자를 찾을 수 없습니다"
    const val INVALID_TOKEN = "유효하지 않은 토큰입니다"
    const val EXPIRED_TOKEN = "만료된 토큰입니다"
    const val INVALID_REFRESH_TOKEN = "유효하지 않은 리프레시 토큰입니다"
} 