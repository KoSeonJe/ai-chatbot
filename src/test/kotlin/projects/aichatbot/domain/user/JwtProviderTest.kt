package projects.aichatbot.domain.user

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class JwtProviderTest {

    @Autowired
    lateinit var jwtProvider: projects.aichatbot.common.auth.JwtProvider

    @Test
    fun `토큰 생성 및 검증`() {
        val token = jwtProvider.generateToken(1L, "test@test.com", UserRole.MEMBER)

        assertThat(jwtProvider.validateToken(token)).isTrue()
        assertThat(jwtProvider.getUserIdFromToken(token)).isEqualTo(1L)
        assertThat(jwtProvider.getEmailFromToken(token)).isEqualTo("test@test.com")
        assertThat(jwtProvider.getRoleFromToken(token)).isEqualTo(UserRole.MEMBER)
    }

    @Test
    fun `잘못된 토큰 검증 실패`() {
        assertThat(jwtProvider.validateToken("invalid-token")).isFalse()
    }
}
