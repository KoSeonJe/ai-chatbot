package projects.aichatbot.domain.thread

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import projects.aichatbot.domain.user.User
import java.time.OffsetDateTime

class ThreadTest {

    private val mockUser = User(
        id = 1L,
        email = "test@test.com",
        password = "password",
        name = "테스트"
    )

    @Test
    fun `30분 경과 시 만료`() {
        val thread = Thread(user = mockUser).apply {
            updatedAt = OffsetDateTime.now().minusMinutes(31)
        }

        assertThat(thread.isExpired()).isTrue()
    }

    @Test
    fun `30분 이내면 유효`() {
        val thread = Thread(user = mockUser).apply {
            updatedAt = OffsetDateTime.now().minusMinutes(29)
        }

        assertThat(thread.isExpired()).isFalse()
    }

    @Test
    fun `touch 호출 시 updatedAt 갱신`() {
        val thread = Thread(user = mockUser)
        val before = thread.updatedAt

        java.lang.Thread.sleep(10)
        thread.touch()

        assertThat(thread.updatedAt).isAfter(before)
    }
}
