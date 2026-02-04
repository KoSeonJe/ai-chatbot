package projects.aichatbot.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("AI Chatbot Demo API")
                    .description("""
                        VIP 고객 시연용 AI 챗봇 API

                        ## 주요 기능
                        - 회원가입 및 로그인 (JWT 인증)
                        - AI와 대화하기 (일반/스트리밍)
                        - 대화 기록 조회 및 삭제

                        ## 인증 방법
                        1. /api/auth/signup 으로 회원가입
                        2. /api/auth/login 으로 로그인하여 토큰 발급
                        3. 상단 Authorize 버튼 클릭 후 토큰 입력
                    """.trimIndent())
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("AI Chatbot Team")
                            .email("support@example.com")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT 토큰을 입력하세요 (Bearer 접두사 없이)")
                    )
            )
    }
}
