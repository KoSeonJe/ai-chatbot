package projects.aichatbot.domain.feedback

import jakarta.persistence.*
import projects.aichatbot.domain.chat.Chat
import projects.aichatbot.domain.user.User
import java.time.OffsetDateTime

@Entity
@Table(
    name = "feedbacks",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "chat_id"])
    ]
)
class Feedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat,

    @Column(nullable = false)
    val isPositive: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FeedbackStatus = FeedbackStatus.PENDING,

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

enum class FeedbackStatus {
    PENDING, RESOLVED
}
