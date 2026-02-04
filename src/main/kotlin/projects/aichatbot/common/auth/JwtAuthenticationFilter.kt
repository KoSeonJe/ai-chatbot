package projects.aichatbot.common.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    private val pathMatcher = AntPathMatcher()

    private val whitelist = listOf(
        "/api/auth/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/h2-console/**",
        "/",
        "/index.html",
        "/favicon.ico",
        "/error"
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI

        if (isWhitelisted(path)) {
            filterChain.doFilter(request, response)
            return
        }

        val token = extractToken(request)

        if (token == null || !jwtProvider.validateToken(token)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            response.writer.write("""{"success":false,"data":null,"error":{"code":"UNAUTHORIZED","message":"인증이 필요합니다"}}""")
            return
        }

        val authUser = AuthUser(
            userId = jwtProvider.getUserIdFromToken(token),
            email = jwtProvider.getEmailFromToken(token),
            role = jwtProvider.getRoleFromToken(token)
        )
        request.setAttribute("authUser", authUser)

        filterChain.doFilter(request, response)
    }

    private fun isWhitelisted(path: String): Boolean {
        return whitelist.any { pattern -> pathMatcher.match(pattern, path) }
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}
