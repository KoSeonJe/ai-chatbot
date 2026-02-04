package projects.aichatbot.domain.chat

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import projects.aichatbot.common.auth.AuthUser
import projects.aichatbot.common.auth.CurrentUser
import projects.aichatbot.common.response.ApiResponse
import projects.aichatbot.domain.chat.dto.ChatListResponse
import projects.aichatbot.domain.chat.dto.ChatResponse
import projects.aichatbot.domain.chat.dto.CreateChatRequest

@Tag(name = "대화", description = "AI 대화 API")
@RestController
@RequestMapping("/api")
class ChatController(
    private val chatService: ChatService
) {

    @Operation(
        summary = "AI와 대화하기",
        description = """
            질문을 입력하면 AI가 답변을 생성합니다.

            📌 사용 예시:
            - "안녕하세요"
            - "Kotlin의 장점을 설명해주세요"
            - "1부터 10까지 세어주세요"

            💡 팁:
            - 구체적인 질문이 더 좋은 답변을 받을 수 있습니다
            - 30분 이내의 대화는 이전 맥락이 유지됩니다
            - model 파라미터로 다른 모델을 지정할 수 있습니다
        """
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "대화 생성 성공"),
        SwaggerApiResponse(responseCode = "401", description = "인증 필요"),
        SwaggerApiResponse(responseCode = "503", description = "AI 서비스 오류")
    ])
    @PostMapping("/chats")
    fun createChat(
        @Valid @RequestBody request: CreateChatRequest,
        @CurrentUser authUser: AuthUser
    ): ResponseEntity<ApiResponse<ChatResponse>> {
        val response = chatService.createChat(authUser.userId, request.question, request.model)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "대화 목록 조회",
        description = """
            대화 목록을 스레드별로 그룹화하여 조회합니다.

            📌 권한:
            - MEMBER: 자신의 대화만 조회
            - ADMIN: 모든 사용자의 대화 조회

            💡 정렬:
            - sort=desc (기본값): 최신순
            - sort=asc: 오래된순
        """
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
        SwaggerApiResponse(responseCode = "401", description = "인증 필요")
    ])
    @GetMapping("/chats")
    fun getChats(
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "정렬 방향 (asc/desc)") @RequestParam(defaultValue = "desc") sort: String,
        @CurrentUser authUser: AuthUser
    ): ResponseEntity<ApiResponse<ChatListResponse>> {
        val response = chatService.getChats(authUser.userId, authUser.role, page, size, sort)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "스레드 삭제",
        description = """
            스레드와 해당 스레드의 모든 대화를 삭제합니다.

            ⚠️ 주의:
            - 자신의 스레드만 삭제할 수 있습니다
            - 삭제된 대화는 복구할 수 없습니다
        """
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "삭제 성공"),
        SwaggerApiResponse(responseCode = "401", description = "인증 필요"),
        SwaggerApiResponse(responseCode = "403", description = "권한 없음"),
        SwaggerApiResponse(responseCode = "404", description = "스레드 없음")
    ])
    @DeleteMapping("/threads/{threadId}")
    fun deleteThread(
        @Parameter(description = "스레드 ID") @PathVariable threadId: Long,
        @CurrentUser authUser: AuthUser
    ): ResponseEntity<ApiResponse<Map<String, Boolean>>> {
        chatService.deleteThread(authUser.userId, threadId)
        return ResponseEntity.ok(ApiResponse.success(mapOf("deleted" to true)))
    }
}
