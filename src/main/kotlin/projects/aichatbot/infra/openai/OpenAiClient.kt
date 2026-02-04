package projects.aichatbot.infra.openai

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import projects.aichatbot.common.exception.BusinessException
import projects.aichatbot.common.exception.ErrorCode
import projects.aichatbot.domain.chat.port.AiChatPort
import projects.aichatbot.domain.chat.port.ChatMessage
import java.io.BufferedReader
import java.io.InputStreamReader

@Component
class OpenAiChatAdapter(
    @Value("\${openai.api-key}") private val apiKey: String,
    @Value("\${openai.model}") private val defaultModel: String,
    @Value("\${openai.max-tokens}") private val maxTokens: Int
) : AiChatPort {

    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper()
    private val apiUrl = "https://api.openai.com/v1/chat/completions"

    override fun chat(
        question: String,
        chatHistory: List<ChatMessage>,
        model: String?,
        context: String?
    ): String {
        val messages = buildMessages(question, chatHistory, context)
        val request = OpenAiRequest(
            model = model ?: defaultModel,
            messages = messages,
            max_tokens = maxTokens
        )

        return try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(apiKey)
            }
            val entity = HttpEntity(request, headers)
            val response = restTemplate.postForObject(apiUrl, entity, OpenAiResponse::class.java)
            response?.choices?.firstOrNull()?.message?.content
                ?: throw BusinessException(ErrorCode.OPENAI_API_ERROR, "AI 응답이 비어있습니다")
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.OPENAI_API_ERROR, "AI 서비스 호출 실패: ${e.message}")
        }
    }

    override fun streamChat(
        question: String,
        chatHistory: List<ChatMessage>,
        model: String?,
        context: String?,
        onToken: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val messages = buildMessages(question, chatHistory, context)
        val request = OpenAiRequest(
            model = model ?: defaultModel,
            messages = messages,
            max_tokens = maxTokens,
            stream = true
        )

        try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(apiKey)
            }

            val fullAnswer = StringBuilder()

            restTemplate.execute(
                apiUrl,
                HttpMethod.POST,
                { clientRequest ->
                    clientRequest.headers.addAll(headers)
                    objectMapper.writeValue(clientRequest.body, request)
                },
                { response ->
                    BufferedReader(InputStreamReader(response.body)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val data = line ?: continue
                            if (data.startsWith("data: ")) {
                                val jsonData = data.substring(6).trim()
                                if (jsonData == "[DONE]") {
                                    onComplete(fullAnswer.toString())
                                    return@use
                                }
                                try {
                                    val chunk = objectMapper.readTree(jsonData)
                                    val content = chunk.path("choices")
                                        .firstOrNull()
                                        ?.path("delta")
                                        ?.path("content")
                                        ?.asText()

                                    if (!content.isNullOrEmpty()) {
                                        fullAnswer.append(content)
                                        onToken(content)
                                    }
                                } catch (e: Exception) {
                                    // Skip malformed JSON
                                }
                            }
                        }
                    }
                }
            )
        } catch (e: Exception) {
            onError("AI 서비스 호출 실패: ${e.message}")
        }
    }

    private fun buildMessages(
        question: String,
        chatHistory: List<ChatMessage>,
        context: String?
    ): List<OpenAiMessage> {
        val systemPrompt = buildSystemPrompt(context)
        val historyMessages = chatHistory.map { it.toOpenAiMessage() }

        return listOf(systemPrompt) + historyMessages + OpenAiMessage(role = "user", content = question)
    }

    private fun buildSystemPrompt(context: String?): OpenAiMessage {
        val basePrompt = "당신은 친절하고 도움이 되는 AI 어시스턴트입니다. 한국어로 답변해주세요."

        val content = if (context != null) {
            """
            |$basePrompt
            |
            |다음은 참고해야 할 문서 내용입니다:
            |---
            |$context
            |---
            |위 문서 내용을 참고하여 질문에 답변해주세요.
            """.trimMargin()
        } else {
            basePrompt
        }

        return OpenAiMessage(role = "system", content = content)
    }

    private fun ChatMessage.toOpenAiMessage(): OpenAiMessage {
        val role = when (this.role) {
            ChatMessage.Role.USER -> "user"
            ChatMessage.Role.ASSISTANT -> "assistant"
            ChatMessage.Role.SYSTEM -> "system"
        }
        return OpenAiMessage(role = role, content = this.content)
    }
}
