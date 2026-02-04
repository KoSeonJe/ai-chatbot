package projects.aichatbot.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "회원가입 요청")
data class SignupRequest(
    @Schema(description = "이메일", example = "user@example.com")
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @Schema(description = "비밀번호", example = "password123")
    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다")
    val password: String,

    @Schema(description = "이름", example = "홍길동")
    @field:NotBlank(message = "이름은 필수입니다")
    val name: String
)

@Schema(description = "회원가입 응답")
data class SignupResponse(
    @Schema(description = "사용자 ID")
    val userId: Long,
    @Schema(description = "이메일")
    val email: String,
    @Schema(description = "이름")
    val name: String
)

@Schema(description = "로그인 요청")
data class LoginRequest(
    @Schema(description = "이메일", example = "user@example.com")
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @Schema(description = "비밀번호", example = "password123")
    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String
)

@Schema(description = "로그인 응답")
data class LoginResponse(
    @Schema(description = "액세스 토큰")
    val accessToken: String,
    @Schema(description = "토큰 타입", example = "Bearer")
    val tokenType: String = "Bearer"
)
