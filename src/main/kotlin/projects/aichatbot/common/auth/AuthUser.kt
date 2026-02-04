package projects.aichatbot.common.auth

import projects.aichatbot.domain.user.UserRole

data class AuthUser(
    val userId: Long,
    val email: String,
    val role: UserRole
)
