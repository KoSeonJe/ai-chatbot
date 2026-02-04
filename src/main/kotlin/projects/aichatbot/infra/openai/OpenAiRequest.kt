package projects.aichatbot.infra.openai

data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val max_tokens: Int = 1000,
    val stream: Boolean = false
)

data class OpenAiMessage(
    val role: String,
    val content: String
)
