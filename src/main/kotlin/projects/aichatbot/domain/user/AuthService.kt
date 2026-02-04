package projects.aichatbot.domain.user

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import projects.aichatbot.common.auth.JwtProvider
import projects.aichatbot.common.exception.BusinessException
import projects.aichatbot.common.exception.ErrorCode
import projects.aichatbot.domain.user.dto.LoginResponse
import projects.aichatbot.domain.user.dto.SignupResponse

@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    @Transactional
    fun signup(email: String, password: String, name: String): SignupResponse {
        if (userRepository.existsByEmail(email)) {
            throw BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS)
        }

        val user = User(
            email = email,
            password = passwordEncoder.encode(password),
            name = name
        )

        val savedUser = userRepository.save(user)

        return SignupResponse(
            userId = savedUser.id,
            email = savedUser.email,
            name = savedUser.name
        )
    }

    fun login(email: String, password: String): LoginResponse {
        val user = userRepository.findByEmail(email)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        if (!passwordEncoder.matches(password, user.password)) {
            throw BusinessException(ErrorCode.INVALID_PASSWORD)
        }

        val token = jwtProvider.generateToken(user.id, user.email, user.role)

        return LoginResponse(accessToken = token)
    }
}
