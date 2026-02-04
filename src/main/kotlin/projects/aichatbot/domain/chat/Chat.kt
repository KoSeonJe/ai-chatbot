package projects.aichatbot.domain.chat

import jakarta.persistence.*
import projects.aichatbot.domain.thread.Thread
import java.time.OffsetDateTime

@Entity
@Table(name = "chats")
class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    val thread: Thread,

    @Column(nullable = false, columnDefinition = "TEXT")
    val question: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var answer: String = "",

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
