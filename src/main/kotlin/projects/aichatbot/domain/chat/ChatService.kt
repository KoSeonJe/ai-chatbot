package projects.aichatbot.domain.chat

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import projects.aichatbot.common.exception.BusinessException
import projects.aichatbot.common.exception.ErrorCode
import projects.aichatbot.domain.chat.dto.*
import projects.aichatbot.domain.thread.Thread
import projects.aichatbot.domain.thread.ThreadRepository
import projects.aichatbot.domain.user.User
import projects.aichatbot.domain.user.UserRepository
import projects.aichatbot.domain.user.UserRole
import projects.aichatbot.infra.openai.OpenAiClient
import projects.aichatbot.infra.openai.OpenAiMessage
import java.util.concurrent.Executors

@Service
@Transactional(readOnly = true)
class ChatService(
    private val chatRepository: ChatRepository,
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository,
    private val openAiClient: OpenAiClient
) {
    private val objectMapper = ObjectMapper()
    private val executor = Executors.newCachedThreadPool()

    @Transactional
    fun createChat(userId: Long, question: String, model: String? = null): ChatResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val thread = findOrCreateThread(userId, user)
        val chatHistory = buildChatHistory(thread.id)
        val answer = openAiClient.chat(question, chatHistory, model)

        val chat = Chat(
            thread = thread,
            question = question,
            answer = answer
        )
        chatRepository.save(chat)
        thread.touch()

        return ChatResponse(
            chatId = chat.id,
            threadId = thread.id,
            question = chat.question,
            answer = chat.answer,
            createdAt = chat.createdAt
        )
    }

    fun createStreamingChat(userId: Long, question: String, model: String? = null): SseEmitter {
        val emitter = SseEmitter(60000L)

        executor.execute {
            try {
                val user = userRepository.findById(userId)
                    .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

                val thread = findOrCreateThreadSync(userId, user)
                val chatHistory = buildChatHistory(thread.id)

                openAiClient.streamChat(
                    question = question,
                    chatHistory = chatHistory,
                    model = model,
                    onToken = { token ->
                        try {
                            val event = StreamEvent.token(token)
                            emitter.send(SseEmitter.event()
                                .name("token")
                                .data(objectMapper.writeValueAsString(event)))
                        } catch (e: Exception) {
                            // Client disconnected
                        }
                    },
                    onComplete = { fullAnswer ->
                        try {
                            val chat = saveChatSync(thread, question, fullAnswer)
                            val event = StreamEvent.done(chat.id, thread.id)
                            emitter.send(SseEmitter.event()
                                .name("done")
                                .data(objectMapper.writeValueAsString(event)))
                            emitter.complete()
                        } catch (e: Exception) {
                            emitter.completeWithError(e)
                        }
                    },
                    onError = { errorMessage ->
                        try {
                            val event = StreamEvent.error(errorMessage)
                            emitter.send(SseEmitter.event()
                                .name("error")
                                .data(objectMapper.writeValueAsString(event)))
                            emitter.complete()
                        } catch (e: Exception) {
                            emitter.completeWithError(e)
                        }
                    }
                )
            } catch (e: Exception) {
                try {
                    val event = StreamEvent.error(e.message ?: "알 수 없는 오류")
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data(objectMapper.writeValueAsString(event)))
                    emitter.complete()
                } catch (ex: Exception) {
                    emitter.completeWithError(ex)
                }
            }
        }

        return emitter
    }

    fun getChats(userId: Long, role: UserRole, page: Int, size: Int, sortDirection: String): ChatListResponse {
        val sort = if (sortDirection.equals("asc", ignoreCase = true)) {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }
        val pageable = PageRequest.of(page, size, sort)

        val chatsPage = if (role == UserRole.ADMIN) {
            chatRepository.findAllWithThreadAndUser(pageable)
        } else {
            chatRepository.findAllByUserId(userId, pageable)
        }

        val threadGroups = chatsPage.content
            .groupBy { it.thread }
            .map { (thread, chats) ->
                ThreadChatsResponse(
                    threadId = thread.id,
                    userId = thread.user.id,
                    userName = thread.user.name,
                    chats = chats.map { chat ->
                        ChatItem(
                            chatId = chat.id,
                            question = chat.question,
                            answer = chat.answer,
                            createdAt = chat.createdAt
                        )
                    },
                    createdAt = thread.createdAt
                )
            }

        return ChatListResponse(
            threads = threadGroups,
            totalPages = chatsPage.totalPages,
            totalElements = chatsPage.totalElements,
            currentPage = page
        )
    }

    @Transactional
    fun deleteThread(userId: Long, threadId: Long) {
        val thread = threadRepository.findById(threadId)
            .orElseThrow { BusinessException(ErrorCode.THREAD_NOT_FOUND) }

        if (thread.user.id != userId) {
            throw BusinessException(ErrorCode.FORBIDDEN, "자신의 스레드만 삭제할 수 있습니다")
        }

        chatRepository.deleteAllByThreadId(threadId)
        threadRepository.delete(thread)
    }

    private fun findOrCreateThread(userId: Long, user: User): Thread {
        val latestThread = threadRepository.findLatestByUserId(userId)

        return if (latestThread == null || latestThread.isExpired()) {
            threadRepository.save(Thread(user = user))
        } else {
            latestThread
        }
    }

    @Synchronized
    private fun findOrCreateThreadSync(userId: Long, user: User): Thread {
        val latestThread = threadRepository.findLatestByUserId(userId)

        return if (latestThread == null || latestThread.isExpired()) {
            threadRepository.save(Thread(user = user))
        } else {
            latestThread
        }
    }

    @Synchronized
    private fun saveChatSync(thread: Thread, question: String, answer: String): Chat {
        val chat = Chat(
            thread = thread,
            question = question,
            answer = answer
        )
        chatRepository.save(chat)
        thread.touch()
        threadRepository.save(thread)
        return chat
    }

    private fun buildChatHistory(threadId: Long): List<OpenAiMessage> {
        val previousChats = chatRepository.findByThreadIdOrderByCreatedAtAsc(threadId)

        return previousChats.flatMap { chat ->
            listOf(
                OpenAiMessage(role = "user", content = chat.question),
                OpenAiMessage(role = "assistant", content = chat.answer)
            )
        }
    }
}
