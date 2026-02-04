package projects.aichatbot.domain.chat

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRepository : JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c WHERE c.thread.id = :threadId ORDER BY c.createdAt ASC")
    fun findByThreadIdOrderByCreatedAtAsc(threadId: Long): List<Chat>

    @Query("SELECT c FROM Chat c JOIN FETCH c.thread t WHERE t.user.id = :userId")
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<Chat>

    @Query("SELECT c FROM Chat c JOIN FETCH c.thread t JOIN FETCH t.user")
    fun findAllWithThreadAndUser(pageable: Pageable): Page<Chat>

    fun deleteAllByThreadId(threadId: Long)
}
