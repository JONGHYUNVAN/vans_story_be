package auth.support

import blog.vans_story_be.domain.auth.dto.LoginRequest
import blog.vans_story_be.domain.user.dto.UserDto
import blog.vans_story_be.domain.user.entity.Role
import blog.vans_story_be.domain.user.entity.User
import java.time.LocalDateTime

/**
 * 테스트 데이터 생성을 위한 빌더 클래스
 */
object TestDataBuilder {
    // 테스트용 기본 사용자 정보
    const val TEST_NAME = "테스트사용자"
    const val TEST_EMAIL = "test@vans-story.com"
    const val TEST_PASSWORD = "Test1234!"
    const val TEST_NICKNAME = "테스트닉네임"
    
    // 관리자 계정 정보
    const val ADMIN_NAME = "관리자"
    const val ADMIN_EMAIL = "admin@vans-story.com"
    const val ADMIN_PASSWORD = "Admin1234!"
    const val ADMIN_NICKNAME = "관리자닉네임"

    /**
     * 기본 로그인 요청 생성
     */
    fun createLoginRequest(
        email: String = TEST_EMAIL,
        password: String = TEST_PASSWORD
    ) = LoginRequest(
        email = email,
        password = password
    )

    /**
     * 기본 회원가입 요청 생성
     */
    fun createSignupRequest(
        name: String = TEST_NAME,
        email: String = TEST_EMAIL,
        password: String = TEST_PASSWORD,
        nickname: String = TEST_NICKNAME
    ) = UserDto.CreateRequest(
        name = name,
        email = email,
        password = password,
        nickname = nickname
    )

    /**
     * 기본 사용자 엔티티 생성
     */
    fun createUser(
        id: Long? = null,
        name: String = TEST_NAME,
        email: String = TEST_EMAIL,
        password: String = TEST_PASSWORD,
        nickname: String = TEST_NICKNAME,
        role: Role = Role.USER
    ) = User.new {
        this.name = name
        this.email = email
        this.password = password
        this.nickname = nickname
        this.role = role
    }

    /**
     * 관리자 사용자 엔티티 생성
     */
    fun createAdminUser(
        id: Long? = null,
        name: String = ADMIN_NAME,
        email: String = ADMIN_EMAIL,
        password: String = ADMIN_PASSWORD,
        nickname: String = ADMIN_NICKNAME
    ) = User.new {
        this.name = name
        this.email = email
        this.password = password
        this.nickname = nickname
        this.role = Role.ADMIN
    }
} 