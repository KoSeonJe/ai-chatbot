package projects.aichatbot.domain.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import projects.aichatbot.common.response.ApiResponse
import projects.aichatbot.domain.user.dto.LoginRequest
import projects.aichatbot.domain.user.dto.LoginResponse
import projects.aichatbot.domain.user.dto.SignupRequest
import projects.aichatbot.domain.user.dto.SignupResponse

@Tag(name = "인증", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @Operation(
        summary = "회원가입",
        description = """
            새로운 사용자 계정을 생성합니다.

            📌 사용 예시:
            - 이메일, 비밀번호, 이름을 입력하여 가입
            - 가입 후 로그인하여 토큰을 발급받으세요
        """
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "회원가입 성공"),
        SwaggerApiResponse(responseCode = "400", description = "잘못된 입력"),
        SwaggerApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
    ])
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<ApiResponse<SignupResponse>> {
        val response = authService.signup(request.email, request.password, request.name)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "로그인",
        description = """
            이메일과 비밀번호로 로그인합니다.

            📌 사용 방법:
            1. 이메일과 비밀번호 입력
            2. 발급받은 토큰을 상단 Authorize 버튼에 입력
            3. 인증이 필요한 API 사용 가능
        """
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "로그인 성공"),
        SwaggerApiResponse(responseCode = "401", description = "비밀번호 불일치"),
        SwaggerApiResponse(responseCode = "404", description = "사용자 없음")
    ])
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = authService.login(request.email, request.password)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
