package projects.aichatbot.domain.chat.port

/**
 * AI 챗봇 서비스 추상화 포트
 *
 * 이 인터페이스를 통해 다양한 AI 제공자를 쉽게 교체할 수 있습니다.
 * - OpenAI (GPT)
 * - Anthropic (Claude)
 * - Google (Gemini)
 * - 자체 학습 모델 (RAG 포함)
 */
interface AiChatPort {

    fun chat(
        question: String,
        chatHistory: List<ChatMessage> = emptyList(),
        model: String? = null,
        context: String? = null
    ): String

    fun streamChat(
        question: String,
        chatHistory: List<ChatMessage> = emptyList(),
        model: String? = null,
        context: String? = null,
        onToken: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit
    )
}

/**
 * 대화 메시지 (도메인 모델)
 */
data class ChatMessage(
    val role: Role,
    val content: String
) {
    enum class Role {
        USER, ASSISTANT, SYSTEM
    }
}
