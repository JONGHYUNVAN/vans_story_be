package blog.vans_story_be.config.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.sql.DataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import blog.vans_story_be.domain.user.entity.Users
import blog.vans_story_be.domain.oauth.entity.UserOAuths
import blog.vans_story_be.domain.auth.entity.RefreshTokens
import io.github.oshai.kotlinlogging.KotlinLogging

@Configuration
class DataSourceConfig {

    private val log = KotlinLogging.logger {}

    @Value("\${VANS_BLOG_DB_HOST:localhost}")
    private lateinit var dbHost: String

    @Value("\${VANS_BLOG_DB_PORT:3306}")
    private lateinit var dbPort: String

    @Value("\${VANS_BLOG_DB_NAME:devblog}")
    private lateinit var dbName: String

    @Value("\${VANS_BLOG_DB_USERNAME:root}")
    private lateinit var dbUsername: String

    @Value("\${VANS_BLOG_DB_PASSWORD:qwe123}")
    private lateinit var dbPassword: String

    @Bean
    fun dataSource(): DataSource {
        val config = HikariConfig().apply {
            driverClassName = "org.mariadb.jdbc.Driver"
            jdbcUrl = "jdbc:mariadb://$dbHost:$dbPort/$dbName?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
            username = dbUsername
            password = dbPassword
            
            // HikariCP 설정
            connectionInitSql = "SET NAMES utf8mb4"
            maximumPoolSize = 20
            minimumIdle = 10
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
            initializationFailTimeout = -1
        }
        
        return HikariDataSource(config)
    }

    /**
     * BCrypt 해시 연산 전용 플랫폼 스레드 풀.
     *
     * Virtual Thread 환경에서 CPU bound 작업(BCrypt ~100-300ms)을
     * carrier thread로부터 분리하여 다른 VT 스케줄링을 방해하지 않도록 합니다.
     * 풀 크기는 가용 CPU 코어 수 기준으로 설정하며, 최소 4개를 보장합니다.
     */
    @Bean(name = ["bcryptExecutor"], destroyMethod = "shutdown")
    fun bcryptExecutor(): ExecutorService =
        Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors().coerceAtLeast(4)
        )

    @EventListener(ApplicationReadyEvent::class)
    fun createTables() {
        runCatching {
            Database.connect(dataSource())
            transaction {
                log.info { "데이터베이스 테이블 생성 시작..." }
                SchemaUtils.create(Users, UserOAuths, RefreshTokens)
                log.info { "데이터베이스 테이블 생성 완료!" }
            }
        }.onFailure { e ->
            log.error(e) { "테이블 생성 중 오류 발생: ${e.message}" }
        }
    }
} 