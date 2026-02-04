package projects.aichatbot.domain.thread

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ThreadRepository : JpaRepository<Thread, Long> {

    @Query("SELECT t FROM Thread t WHERE t.user.id = :userId ORDER BY t.updatedAt DESC LIMIT 1")
    fun findLatestByUserId(userId: Long): Thread?

    fun findAllByUserId(userId: Long): List<Thread>
}
