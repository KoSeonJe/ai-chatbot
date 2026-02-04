package projects.aichatbot.domain.chat

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import projects.aichatbot.common.exception.BusinessException
import projects.aichatbot.common.exception.ErrorCode
import projects.aichatbot.domain.chat.dto.*
import projects.aichatbot.domain.thread.Thread
import projects.aichatbot.domain.thread.ThreadRepository
import projects.aichatbot.domain.user.UserRepository
import projects.aichatbot.domain.user.UserRole
import projects.aichatbot.infra.openai.OpenAiClient
import projects.aichatbot.infra.openai.OpenAiMessage

@Service
@Transactional(readOnly = true)
class ChatService(
    private val chatRepository: ChatRepository,
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository,
    private val openAiClient: OpenAiClient
) {

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

    private fun findOrCreateThread(userId: Long, user: projects.aichatbot.domain.user.User): Thread {
        val latestThread = threadRepository.findLatestByUserId(userId)

        return if (latestThread == null || latestThread.isExpired()) {
            threadRepository.save(Thread(user = user))
        } else {
            latestThread
        }
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
