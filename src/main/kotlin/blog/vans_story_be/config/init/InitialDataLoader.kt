package blog.vans_story_be.config.init

import blog.vans_story_be.domain.user.dto.UserDto
import blog.vans_story_be.domain.user.service.UserService
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

/**
 * 초기 데이터 로더 클래스입니다.
 * 
 * <p>애플리케이션 시작 시 기본 사용자 데이터를 데이터베이스에 로드합니다.
 * CommandLineRunner 인터페이스를 구현하여 애플리케이션이 시작될 때 실행됩니다.
 * 환경 변수를 통해 초기 계정 정보를 설정할 수 있습니다.</p>
 * 
 * <h3>환경 변수 설정:</h3>
 * <ul>
 *   <li>ADMIN_USERNAME: 관리자 계정 아이디 (기본값: "admin")</li>
 *   <li>ADMIN_PASSWORD: 관리자 계정 비밀번호 (기본값: "admin1234!")</li>
 *   <li>ADMIN_NICKNAME: 관리자 닉네임 (기본값: "관리자")</li>
 *   <li>ADMIN_EMAIL: 관리자 이메일 (기본값: "admin@vans-story.com")</li>
 *   <li>TEST_USERNAME: 테스트 계정 아이디 (기본값: "testuser")</li>
 *   <li>TEST_PASSWORD: 테스트 계정 비밀번호 (기본값: "Test1234!")</li>
 *   <li>TEST_NICKNAME: 테스트 계정 닉네임 (기본값: "테스트 사용자")</li>
 *   <li>TEST_EMAIL: 테스트 계정 이메일 (기본값: "test@vans-story.com")</li>
 * </ul>
 * 
 * <h3>초기화 프로세스:</h3>
 * <ol>
 *   <li>애플리케이션 시작 시 자동으로 실행</li>
 *   <li>관리자 계정 존재 여부 확인</li>
 *   <li>테스트 계정 존재 여부 확인</li>
 *   <li>존재하지 않는 계정 생성</li>
 * </ol>
 * 
 * <h3>오류 처리:</h3>
 * <ul>
 *   <li>테이블이 없는 경우: 경고 로그 출력 후 다음 실행 시 초기화 예정</li>
 *   <li>계정 생성 실패: 상세 오류 로그 출력 후 예외 발생</li>
 * </ul>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see org.springframework.boot.CommandLineRunner
 * @see blog.vans_story_be.domain.user.service.UserService
 */
@Component
class InitialDataLoader(
    private val userService: UserService,
    private val dataSource: DataSource
) : CommandLineRunner {
    companion object {
        private val log = KotlinLogging.logger {}
        private const val ADMIN_USERNAME = "admin"
        private const val TEST_USERNAME = "testuser"
    }

    /**
     * 초기 계정 설정을 위한 데이터 클래스입니다.
     * 
     * @property username 계정 아이디
     * @property password 계정 비밀번호
     * @property nickname 계정 닉네임
     * @property email 계정 이메일
     */
    private data class AccountConfig(
        val username: String,
        val password: String,
        val nickname: String,
        val email: String
    )

    @Value("\${VANS_BLOG_ADMIN_USERNAME}")
    private lateinit var adminUsername: String
    
    @Value("\${VANS_BLOG_ADMIN_PASSWORD}")
    private lateinit var adminPassword: String
    
    @Value("관리자")
    private lateinit var adminNickname: String
    
    @Value("\${VANS_BLOG_ADMIN_EMAIL}")
    private lateinit var adminEmail: String
    
    @Value("\${VANS_BLOG_TEST_USERNAME}")
    private lateinit var testUsername: String
    
    @Value("\${VANS_BLOG_TEST_PASSWORD:Test1234!}")
    private lateinit var testPassword: String
    
    @Value("테스트 사용자")
    private lateinit var testNickname: String
    
    @Value("\${VANS_BLOG_TEST_EMAIL}")
    private lateinit var testEmail: String

    /**
     * 애플리케이션 시작 시 호출되는 메서드입니다.
     * 
     * <p>초기 데이터를 로드하고, 오류 발생 시 적절한 로그를 남깁니다.
     * 테이블이 존재하지 않을 경우 경고 로그를 남기고, 
     * 다음 실행 시 데이터가 초기화될 것임을 알립니다.</p>
     * 
     * @param args 커맨드라인 인수
     * @throws RuntimeException 초기 데이터 로드 실패 시
     */
    override fun run(vararg args: String) {
        runCatching {
            // Exposed Database 연결 설정
            Database.connect(dataSource)
            initializeData()
        }.onFailure { e ->
            log.error(e) { "초기 데이터 로드 중 상세 오류: ${e.message}" }
            when {
                e.message?.contains("Table") == true && e.message?.contains("not found") == true -> {
                    log.warn { "테이블이 아직 생성되지 않았습니다. 다음 실행시 데이터가 초기화됩니다." }
                }
                else -> {
                    log.error(e) { "초기 데이터 로드 중 예상치 못한 오류 발생" }
                    // 애플리케이션 시작을 막지 않도록 예외를 다시 던지지 않음
                }
            }
        }
    }

    /**
     * 초기 데이터 설정 메서드입니다.
     * 
     * <p>관리자 및 테스트 사용자 계정이 존재하지 않을 경우 생성합니다.</p>
     * 
     * @throws Exception 계정 생성 중 오류 발생 시
     */
    private fun initializeData() = transaction {
        
        val adminConfig = AccountConfig(adminUsername, adminPassword, adminNickname, adminEmail)
        val testConfig = AccountConfig(testUsername, testPassword, testNickname, testEmail)

        createAccountIfNotExists(adminConfig, ::createAdminAccount)
        createAccountIfNotExists(testConfig, ::createTestAccount)
        
        log.info { "초기 데이터 설정 완료" }
    }

    /**
     * 계정이 존재하지 않을 경우 생성하는 메서드입니다.
     * 
     * @param config 계정 설정 정보
     * @param creator 계정 생성 함수
     */
    private fun createAccountIfNotExists(
        config: AccountConfig,
        creator: (UserDto.CreateRequest) -> Unit
    ) {
        if (!userService.existsByEmail(config.email)) {
            runCatching {
                val request = UserDto.CreateRequest(
                    password = config.password,
                    nickname = config.nickname,
                    email = config.email
                )
                creator(request)
            }.onFailure { e ->
                log.error(e) { "${config.nickname} 계정 생성 중 오류 발생" }
                throw e
            }
        }
    }

    /**
     * 관리자 계정을 생성하는 함수입니다.
     * 
     * @param request 계정 생성 요청 객체
     */
    private fun createAdminAccount(request: UserDto.CreateRequest) = 
        userService.createAdmin(request)

    /**
     * 테스트 계정을 생성하는 함수입니다.
     * 
     * @param request 계정 생성 요청 객체
     */
    private fun createTestAccount(request: UserDto.CreateRequest) = 
        userService.createUser(request)
} 