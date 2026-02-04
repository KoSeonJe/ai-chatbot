package projects.aichatbot.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import projects.aichatbot.common.auth.JwtAuthenticationFilter

@Configuration
class FilterConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    @Bean
    fun jwtFilterRegistration(): FilterRegistrationBean<JwtAuthenticationFilter> {
        val registration = FilterRegistrationBean<JwtAuthenticationFilter>()
        registration.filter = jwtAuthenticationFilter
        registration.addUrlPatterns("/api/*")
        registration.order = 1
        return registration
    }
}
