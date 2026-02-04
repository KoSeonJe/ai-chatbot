# AI Chatbot Demo - 구현 가이드

> 이 문서는 AI Agent가 구현 시 반드시 따라야 할 지침입니다.

---

## 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 목적 | VIP 고객사 긴급 시연용 AI 챗봇 API |
| 스택 | Spring Boot 3.x + Kotlin 1.9+ + Gradle |
| 제한 시간 | 3시간 이내 |
| 핵심 목표 | "API를 통해 AI를 활용할 수 있다" 시연 |

---

## 구현 워크플로우

### 1단계: Phase 문서 읽기

```
plan-chat-demo-{N}.md 파일을 읽고 작업 항목 파악
plan 모드로 전환해 먼저 계획을 세운뒤, 개발자에게 허가받기.
```

### 2단계: 구현

```
- 작업 항목을 순서대로 구현
- 테스트 코드 작성 (P1 우선순위만 필수)
- Pause 검증 지점에서 동작 확인
- 구현 완성 시마다 문서 업데이트. 문서 업데이트하면서 빠진 것 없는지 병행.
```

### 3단계: 문서화 (구현 완료 후)

각 Phase 완료 시 `docs/phase-{N}-summary.md` 작성:

```markdown
# Phase {N}: {제목} 구현 완료

## 핵심 구현 사항
- [ ] 구현된 기능 1
- [ ] 구현된 기능 2

## 생성된 파일
| 파일 | 설명 |
|------|------|
| `파일명.kt` | 역할 설명 |

## API 엔드포인트 (해당 시)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/...` | 기능 |

## 테스트 방법
```bash
# Swagger 또는 curl 예시
```

### 4단계: 사용자 승인 요청 ⭐

```
⚠️ 중요: 구현 완료 후 반드시 사용자에게 확인 요청

예시:
"Phase 2 인증 기능 구현이 완료되었습니다.
 - 회원가입/로그인 API
 - JWT 토큰 발급/검증
 - 테스트 코드 작성 완료

 docs/phase-2-summary.md에 정리했습니다.
 확인 후 진행해도 될까요?"
```

### 5단계: Git 커밋 (승인 후)

사용자 승인을 받은 후에만 커밋:

```bash
# 커밋 컨벤션 (한국어, Best Practice)
git commit -m "feat: Phase 2 인증 기능 구현

- JWT 기반 인증 (Spring Security 없이)
- 회원가입/로그인 API
- JwtAuthenticationFilter + CurrentUserArgumentResolver
- AuthService, JwtProvider 테스트 코드

Co-Authored-By: Claude <noreply@anthropic.com>"
```

## 다음 단계
Phase {N+1}로 진행
```

**커밋 타입:**
| 타입 | 용도 |
|------|------|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 리팩토링 |
| `test` | 테스트 추가 |
| `docs` | 문서 수정 |
| `chore` | 빌드, 설정 변경 |

---

## 우선순위 규칙

### 필수 Phase (시연 목표 달성)

| Phase | 내용 | 시간 |
|-------|------|------|
| 1 | 프로젝트 초기화 | 20분 |
| 2 | 인증 기능 | 25분 |
| 3 | 대화 기능 (기본) | 40분 |
| 4 | 대화 기능 (스트리밍) | 20분 |
| 5 | 데모 페이지 + 확장 로드맵 | 20분 |
| 6 | 마무리 + 시연 준비 | 15분 |

**총 140분 (2시간 20분)** → 여유 시간 40분

### 선택 Phase (시간 남으면)

| Phase | 내용 | 시간 |
|-------|------|------|
| 7 | 피드백 기능 | 25분 |
| 8 | 분석 및 보고 | 20분 |

---

## 아키텍처 규칙

### 인증
- Spring Security **사용 안 함**
- `OncePerRequestFilter` + `ArgumentResolver` 조합
- `@CurrentUser` 어노테이션으로 인증 사용자 주입

### 응답 형식
```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ErrorResponse?
)
```

### 예외 처리
- `BusinessException` 커스텀 예외
- `GlobalExceptionHandler` (@RestControllerAdvice)

### API 문서
- SpringDoc OpenAPI (Swagger)
- 고객 친화적 설명 추가

---

## 코딩 컨벤션

### 기본 원칙
- **주석 사용 금지**: 코드 자체로 의도를 명확히 표현
- **의미 있는 네이밍**: 변수/함수/클래스 이름으로 동작 설명
- **단일 책임 원칙**: 하나의 함수/클래스는 하나의 책임만

### 계층별 책임

#### Controller
```kotlin
// Controller는 API 통신 코드만 담당
@RestController
class ChatController(private val chatService: ChatService) {
    
    @PostMapping("/api/chats")
    fun createChat(
        @RequestBody request: CreateChatRequest,
        @CurrentUser authUser: AuthUser
    ): ResponseEntity<ApiResponse<ChatResponse>> {
        val response = chatService.createChat(authUser.userId, request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
```
- HTTP 요청/응답 처리
- 입력 검증 (DTO)
- Service 호출 및 응답 반환

#### Service
```kotlin
@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val threadRepository: ThreadRepository,
    private val openAiClient: OpenAiClient
) {
    
    fun createChat(userId: Long, request: CreateChatRequest): ChatResponse {
        val thread = findOrCreateThread(userId)
        val chatHistory = getChatHistory(thread.id)
        val answer = openAiClient.chat(request.question, chatHistory)
        val chat = Chat(thread = thread, question = request.question, answer = answer)
        chatRepository.save(chat)
        thread.touch()
        return ChatResponse.from(chat)
    }
    
    private fun findOrCreateThread(userId: Long): Thread {
        // 비즈니스 로직 구현
    }
}
```
- 모든 비즈니스 로직 구현
- 객체지향 설계 (캡슐화, 다형성 활용)
- 트랜잭션 관리
- 외부 API 호출

#### Repository
```kotlin
@Repository
interface ChatRepository : JpaRepository<Chat, Long> {
    
    @Query("""
        SELECT c FROM Chat c 
        WHERE c.thread.id = :threadId 
        ORDER BY c.createdAt ASC
    """)
    fun findByThreadIdOrderByCreatedAtAsc(threadId: Long): List<Chat>
    
    @Query("""
        SELECT c FROM Chat c 
        JOIN FETCH c.thread t 
        JOIN FETCH t.user u 
        WHERE u.id = :userId
    """)
    fun findAllByUserIdWithThread(userId: Long, pageable: Pageable): Page<Chat>
}
```
- JPQL 우선 사용 (복잡한 쿼리)
- 단순 CRUD는 메서드명 규칙 활용
- N+1 문제 방지 (JOIN FETCH 활용)

### Swagger 문서화 (한국어)
```kotlin
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
        - isStreaming=true로 설정하면 실시간으로 답변이 표시됩니다
    """
)
@ApiResponses(value = [
    ApiResponse(responseCode = "200", description = "대화 생성 성공"),
    ApiResponse(responseCode = "401", description = "인증 필요"),
    ApiResponse(responseCode = "429", description = "요청 한도 초과")
])
@PostMapping("/api/chats")
fun createChat(...)
```

### Kotlin 베스트 프랙티스
```kotlin
// ✅ 가변성 최소화
val items = mutableListOf<String>()  // ❌
val items = listOf<String>()           // ✅

// ✅ 널 안전성 활용
fun findUser(id: Long): User?          // ✅ nullable 명시
    ?: throw UserNotFoundException()    // ✅ 엘비스 연산자 활용

// ✅ 확장 함수 활용
fun String.toSlug() = this.lowercase().replace(" ", "-")

// ✅ Scope 함수 적절히 사용
val user = User().apply {
    name = "홍길동"
    email = "hong@test.com"
}

// ✅ 데이터 클래스 활용
data class ChatResponse(
    val id: Long,
    val question: String,
    val answer: String
) {
    companion object {
        fun from(chat: Chat) = ChatResponse(
            id = chat.id,
            question = chat.question,
            answer = chat.answer
        )
    }
}

// ✅ Sealed Class로 상태 표현
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: String, val message: String) : ApiResult<Nothing>()
}
```

### 예외 처리
```kotlin
// Service 레벨에서 비즈니스 예외 발생
if (user.isExpired()) {
    throw BusinessException(ErrorCode.USER_EXPIRED, "사용자 계정이 만료되었습니다")
}

// GlobalExceptionHandler에서 공통 처리
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ApiResponse.error(e.errorCode.code, e.message))
    }
}
```

### 네이밍 컨벤션
| 구성요소 | 규칙 | 예시 |
|---------|------|------|
| 클래스 | PascalCase | `ChatService`, `UserRepository` |
| 함수/변수 | camelCase | `createChat`, `findById` |
| 상수 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 패키지 | lowercase | `domain.chat`, `common.auth` |
| Boolean | is/has/can 접두사 | `isActive`, `hasPermission` |

### 컬렉션 처리
```kotlin
// ✅ 함수형 스타일 활용
val activeUsers = users
    .filter { it.isActive }
    .map { it.toDto() }
    .sortedBy { it.createdAt }

// ✅ GroupBy 활용
val chatsByThread = chats.groupBy { it.threadId }

// ✅ Any/All/None 활용
val hasAdmin = users.any { it.role == UserRole.ADMIN }
```

---

## 테스트 작성 기준

```
✅ 작성할 테스트 (P1):
- 핵심 비즈니스 로직
- 버그 발생 시 디버깅 오래 걸리는 부분
- 외부 의존성은 Mock 처리

❌ 생략할 테스트:
- Controller 통합 테스트 (Swagger로 충분)
- Repository 단순 CRUD (JPA가 보장)
- 예외 케이스 전체 (Happy path 우선)
```

---

## 참고 문서

| 문서 | 용도 |
|------|------|
| `CLAUDE.md` | 프로젝트 컨텍스트, Phase 진행 상황 |
| `plan-chat-demo-overview.md` | 전체 실행 계획 |
| `plan-chat-demo-{N}.md` | 각 Phase 상세 작업 |
| `DEMO_GUIDE.md` | 고객 시연 가이드 |
| `EXTENSION_ROADMAP.md` | RAG 확장 로드맵 |

---

## 주의사항

1. **시간 엄수**: 3시간 제한, Phase 6까지 필수 완료
2. **승인 필수**: 구현 완료 후 반드시 사용자 확인
3. **커밋 규칙**: 승인 없이 커밋 금지, 한국어 메시지
4. **문서화**: 각 Phase 완료 시 summary 작성
5. **우선순위**: P1 테스트만 필수, 나머지는 시간 여유 시
6. **과잉 구현 금지**: 요구사항에 없는 기능 추가하지 않기
