package projects.aichatbot.domain.chat

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import projects.aichatbot.common.exception.BusinessException
import projects.aichatbot.domain.user.AuthService
import projects.aichatbot.domain.user.UserRole
import projects.aichatbot.infra.openai.OpenAiClient

@SpringBootTest
@Transactional
class ChatServiceTest {

    @Autowired
    lateinit var chatService: ChatService

    @Autowired
    lateinit var authService: AuthService

    @MockitoBean
    lateinit var openAiClient: OpenAiClient

    private var userId: Long = 0

    @BeforeEach
    fun setup() {
        whenever(openAiClient.chat(any(), any(), anyOrNull())).thenReturn("Mock AI 응답")
        val user = authService.signup("chat-test@test.com", "password", "테스트유저")
        userId = user.userId
    }

    @Test
    fun `새 대화 시 스레드 생성`() {
        val result = chatService.createChat(userId, "안녕")

        assertThat(result.threadId).isNotNull()
        assertThat(result.answer).isEqualTo("Mock AI 응답")
    }

    @Test
    fun `30분 내 대화는 같은 스레드 사용`() {
        val first = chatService.createChat(userId, "첫번째")
        val second = chatService.createChat(userId, "두번째")

        assertThat(first.threadId).isEqualTo(second.threadId)
    }

    @Test
    fun `MEMBER는 자신의 대화만 조회`() {
        chatService.createChat(userId, "내 대화")

        val result = chatService.getChats(userId, UserRole.MEMBER, 0, 10, "desc")

        assertThat(result.threads).allMatch { it.userId == userId }
    }

    @Test
    fun `자신의 스레드만 삭제 가능`() {
        val chat = chatService.createChat(userId, "테스트")

        assertThrows<BusinessException> {
            chatService.deleteThread(userId = 999L, threadId = chat.threadId)
        }
    }

    @Test
    fun `스레드 삭제 성공`() {
        val chat = chatService.createChat(userId, "테스트")

        chatService.deleteThread(userId, chat.threadId)

        val result = chatService.getChats(userId, UserRole.MEMBER, 0, 10, "desc")
        assertThat(result.threads).isEmpty()
    }
}
