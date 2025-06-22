package blog.vans_story_be.config.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DataSourceConfig {

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
            jdbcUrl = "jdbc:mariadb://$dbHost:$dbPort/$dbName?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
            username = dbUsername
            password = dbPassword
            
            // HikariCP 설정
            connectionInitSql = "SET NAMES utf8mb4"
            maximumPoolSize = 10
            minimumIdle = 5
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
        }
        
        return HikariDataSource(config)
    }
} 