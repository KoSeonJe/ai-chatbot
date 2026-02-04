package projects.aichatbot.domain.chat.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class StreamEvent(
    val type: String,
    val content: String? = null,
    val chatId: Long? = null,
    val threadId: Long? = null,
    val error: String? = null
) {
    companion object {
        fun token(content: String) = StreamEvent(type = "token", content = content)
        fun done(chatId: Long, threadId: Long) = StreamEvent(type = "done", chatId = chatId, threadId = threadId)
        fun error(message: String) = StreamEvent(type = "error", error = message)
    }
}
