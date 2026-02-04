package projects.aichatbot.domain.admin

import jakarta.persistence.*
import projects.aichatbot.domain.user.User
import java.time.OffsetDateTime

@Entity
@Table(name = "login_logs")
class LoginLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
