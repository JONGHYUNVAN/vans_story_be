package blog.vans_story_be.config.security

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * 로그인/회원가입 엔드포인트 Rate Limiter.
 * IP당 분당 5회 제한. in-memory (ConcurrentHashMap + Bucket4j).
 */
@Component
class LoginRateLimiter {

    private val buckets = ConcurrentHashMap<String, Bucket>()

    /**
     * 요청 소비 시도. true면 허용, false면 차단.
     */
    fun tryConsume(ip: String): Boolean =
        buckets.computeIfAbsent(ip) { createBucket() }.tryConsume(1)

    private fun createBucket(): Bucket {
        val bandwidth = Bandwidth.builder()
            .capacity(5)
            .refillGreedy(5, Duration.ofMinutes(1))
            .build()
        return Bucket.builder()
            .addLimit(bandwidth)
            .build()
    }
}
