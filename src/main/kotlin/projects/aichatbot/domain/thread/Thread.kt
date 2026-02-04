package projects.aichatbot.domain.thread

import jakarta.persistence.*
import projects.aichatbot.domain.user.User
import java.time.OffsetDateTime

@Entity
@Table(name = "threads")
class Thread(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
) {
    fun isExpired(): Boolean =
        updatedAt.plusMinutes(30).isBefore(OffsetDateTime.now())

    fun touch() {
        updatedAt = OffsetDateTime.now()
    }
}
