package projects.aichatbot.domain.user

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import projects.aichatbot.domain.user.dto.LoginRequest
import projects.aichatbot.domain.user.dto.SignupRequest

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `회원가입 성공 E2E`() {
        val request = SignupRequest("e2e@test.com", "password", "E2E유저")

        mockMvc.post("/api/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.email") { value("e2e@test.com") }
            jsonPath("$.data.name") { value("E2E유저") }
        }
    }

    @Test
    fun `로그인 성공 E2E`() {
        val signupRequest = SignupRequest("login-e2e@test.com", "password", "로그인유저")
        mockMvc.post("/api/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(signupRequest)
        }

        val loginRequest = LoginRequest("login-e2e@test.com", "password")
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.accessToken") { exists() }
            jsonPath("$.data.tokenType") { value("Bearer") }
        }
    }
}
