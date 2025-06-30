package blog.vans_story_be.domain.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * OAuth 관련 데이터 전송 객체(DTO) 모음
 *
 * @author vans
 * @version 1.0.0
 * @since 2025.01.09
 */
class OAuthDto {

    /**
     * OAuth 로그인 요청 DTO
     *
     * 중간 서버에서 OAuth 인증 후 전달받는 정보
     */
    @Schema(description = "OAuth 로그인 요청")
    data class LoginRequest(
        @field:NotBlank(message = "OAuth 제공업체는 필수입니다")
        @field:Size(max = 50, message = "OAuth 제공업체는 50자 이하여야 합니다")
        @Schema(description = "OAuth 제공업체", example = "google", required = true)
        @JsonProperty("provider")
        val provider: String,

        @field:NotBlank(message = "OAuth 제공업체 사용자 ID는 필수입니다")
        @field:Size(max = 100, message = "OAuth 제공업체 사용자 ID는 100자 이하여야 합니다")
        @Schema(description = "OAuth 제공업체 사용자 ID", example = "google_user_12345", required = true)
        @JsonProperty("providerId")
        val providerId: String
    )

    /**
     * OAuth 계정 연결 요청 DTO
     *
     * 기존 로그인된 사용자가 OAuth 계정을 연결할 때 사용
     */
    @Schema(description = "OAuth 계정 연결 요청")
    data class LinkRequest(
        @field:NotBlank(message = "OAuth 제공업체는 필수입니다")
        @field:Size(max = 50, message = "OAuth 제공업체는 50자 이하여야 합니다")
        @Schema(description = "OAuth 제공업체", example = "kakao", required = true)
        @JsonProperty("provider")
        val provider: String,

        @field:NotBlank(message = "OAuth 제공업체 사용자 ID는 필수입니다")
        @field:Size(max = 100, message = "OAuth 제공업체 사용자 ID는 100자 이하여야 합니다")
        @Schema(description = "OAuth 제공업체 사용자 ID", example = "kakao_user_67890", required = true)
        @JsonProperty("providerId")
        val providerId: String
    )

    /**
     * OAuth 계정 연결 해제 요청 DTO
     */
    @Schema(description = "OAuth 계정 연결 해제 요청")
    data class UnlinkRequest(
        @field:NotBlank(message = "OAuth 제공업체는 필수입니다")
        @field:Size(max = 50, message = "OAuth 제공업체는 50자 이하여야 합니다")
        @Schema(description = "OAuth 제공업체", example = "google", required = true)
        @JsonProperty("provider")
        val provider: String
    )

    /**
     * OAuth 로그인 임시 코드 응답 DTO
     */
    @Schema(description = "OAuth 로그인 임시 코드 응답")
    data class CodeResponse(
        @Schema(description = "임시 인증 코드 (5분 후 만료)", example = "temp_oauth_code_12345")
        @JsonProperty("code")
        val code: String
    )

    /**
     * 임시 코드로 JWT 토큰 교환 요청 DTO
     */
    @Schema(description = "임시 코드로 JWT 토큰 교환 요청")
    data class ExchangeRequest(
        @field:NotBlank(message = "임시 인증 코드는 필수입니다")
        @Schema(description = "임시 인증 코드", example = "temp_oauth_code_12345", required = true)
        @JsonProperty("code")
        val code: String
    )

    /**
     * OAuth 연동 정보 응답 DTO
     */
    @Schema(description = "OAuth 연동 정보 응답")
    data class Response(
        @Schema(description = "OAuth 연동 ID", example = "1")
        @JsonProperty("id")
        val id: Long,

        @Schema(description = "사용자 ID", example = "1")
        @JsonProperty("userId")
        val userId: Long,

        @Schema(description = "OAuth 제공업체", example = "google")
        @JsonProperty("provider")
        val provider: String,

        @Schema(description = "OAuth 제공업체 사용자 ID", example = "google_user_12345")
        @JsonProperty("providerId")
        val providerId: String,

        @Schema(description = "연동 생성 시간", example = "2025-01-09T10:00:00")
        @JsonProperty("createdAt")
        val createdAt: LocalDateTime,

        @Schema(description = "연동 정보 수정 시간", example = "2025-01-09T10:00:00")
        @JsonProperty("updatedAt")
        val updatedAt: LocalDateTime
    )

    /**
     * 연결된 OAuth 계정 목록 응답 DTO
     */
    @Schema(description = "연결된 OAuth 계정 목록 응답")
    data class LinkedAccountsResponse(
        @Schema(description = "연결된 OAuth 계정 목록")
        @JsonProperty("linkedAccounts")
        val linkedAccounts: List<LinkedAccount>
    ) {
        @Schema(description = "연결된 OAuth 계정 정보")
        data class LinkedAccount(
            @Schema(description = "OAuth 제공업체", example = "google")
            @JsonProperty("provider")
            val provider: String,

            @Schema(description = "연동 생성 시간", example = "2025-01-09T10:00:00")
            @JsonProperty("createdAt")
            val createdAt: LocalDateTime
        )
    }
} 