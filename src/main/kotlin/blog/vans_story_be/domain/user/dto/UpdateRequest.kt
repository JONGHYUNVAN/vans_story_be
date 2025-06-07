package blog.vans_story_be.domain.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

/**
 * 사용자 수정 요청 DTO
 */
data class UpdateRequest(
    @field:Size(min = 2, max = 50, message = "이름은 2-50자 사이여야 합니다")
    val name: String?,
    
    @field:Size(min = 2, max = 50, message = "닉네임은 2-50자 사이여야 합니다")
    val nickname: String?,
    
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String?
) 