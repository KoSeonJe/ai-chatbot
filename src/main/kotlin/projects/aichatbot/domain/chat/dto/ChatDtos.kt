package projects.aichatbot.domain.chat.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

@Schema(description = "대화 생성 요청")
data class CreateChatRequest(
    @Schema(description = "질문", example = "안녕하세요, 자기소개 해주세요.")
    @field:NotBlank(message = "질문은 필수입니다")
    val question: String,

    @Schema(description = "사용할 모델 (선택)", example = "gpt-4o")
    val model: String? = null,

    @Schema(description = "스트리밍 응답 여부", example = "false")
    val isStreaming: Boolean? = false
)

@Schema(description = "대화 생성 응답")
data class ChatResponse(
    @Schema(description = "대화 ID")
    val chatId: Long,
    @Schema(description = "스레드 ID")
    val threadId: Long,
    @Schema(description = "질문")
    val question: String,
    @Schema(description = "AI 답변")
    val answer: String,
    @Schema(description = "생성 시간")
    val createdAt: OffsetDateTime
)

@Schema(description = "스레드별 대화 목록")
data class ThreadChatsResponse(
    @Schema(description = "스레드 ID")
    val threadId: Long,
    @Schema(description = "사용자 ID")
    val userId: Long,
    @Schema(description = "사용자 이름")
    val userName: String,
    @Schema(description = "대화 목록")
    val chats: List<ChatItem>,
    @Schema(description = "스레드 생성 시간")
    val createdAt: OffsetDateTime
)

@Schema(description = "대화 항목")
data class ChatItem(
    @Schema(description = "대화 ID")
    val chatId: Long,
    @Schema(description = "질문")
    val question: String,
    @Schema(description = "답변")
    val answer: String,
    @Schema(description = "생성 시간")
    val createdAt: OffsetDateTime
)

@Schema(description = "대화 목록 페이징 응답")
data class ChatListResponse(
    @Schema(description = "스레드별 대화 목록")
    val threads: List<ThreadChatsResponse>,
    @Schema(description = "전체 페이지 수")
    val totalPages: Int,
    @Schema(description = "전체 요소 수")
    val totalElements: Long,
    @Schema(description = "현재 페이지")
    val currentPage: Int
)
