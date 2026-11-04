package blog.vans_story_be.config.security

import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.authorization.AuthorizationResult
import org.springframework.security.core.Authentication
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import java.security.MessageDigest
import java.util.function.Supplier

/**
 * `X-API-KEY` 헤더가 내부 서비스 키와 같을 때만 허용합니다.
 *
 * 설정된 키가 비어 있으면 어떤 요청도 허용하지 않습니다.
 */
class InternalApiKeyAuthorizationManager(
    private val internalApiKey: String
) : AuthorizationManager<RequestAuthorizationContext> {

    override fun authorize(
        authentication: Supplier<out Authentication?>,
        context: RequestAuthorizationContext
    ): AuthorizationResult {
        val provided = context.request.getHeader(HEADER_NAME).orEmpty()
        val granted = internalApiKey.isNotEmpty() && MessageDigest.isEqual(
            internalApiKey.toByteArray(Charsets.UTF_8),
            provided.toByteArray(Charsets.UTF_8)
        )
        return AuthorizationDecision(granted)
    }

    companion object {
        const val HEADER_NAME = "X-API-KEY"
    }
}
