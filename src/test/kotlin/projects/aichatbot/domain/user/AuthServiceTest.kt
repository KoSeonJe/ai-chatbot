package projects.aichatbot.domain.user

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import projects.aichatbot.common.exception.BusinessException

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    lateinit var authService: AuthService

    @Test
    fun `회원가입 성공`() {
        val result = authService.signup("test@test.com", "password", "테스트")

        assertThat(result.email).isEqualTo("test@test.com")
        assertThat(result.name).isEqualTo("테스트")
    }

    @Test
    fun `중복 이메일 가입 실패`() {
        authService.signup("dup@test.com", "password", "유저1")

        assertThrows<BusinessException> {
            authService.signup("dup@test.com", "password", "유저2")
        }
    }

    @Test
    fun `로그인 성공 시 토큰 반환`() {
        authService.signup("login@test.com", "password", "테스트")

        val token = authService.login("login@test.com", "password")

        assertThat(token.accessToken).isNotBlank()
        assertThat(token.tokenType).isEqualTo("Bearer")
    }

    @Test
    fun `잘못된 비밀번호로 로그인 실패`() {
        authService.signup("wrong@test.com", "password", "테스트")

        assertThrows<BusinessException> {
            authService.login("wrong@test.com", "wrongpassword")
        }
    }
}
