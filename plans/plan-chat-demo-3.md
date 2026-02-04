# Phase 3: 대화 기능 (기본)

> **시간 제한**: 40분
> **선행 조건**: Phase 2 완료
> **결과 상태**: OpenAI API 연동 대화 생성, 스레드 기반 컨텍스트 관리 동작

---

## 목표

사용자가 질문을 보내면 OpenAI API를 통해 AI 답변을 받고, 30분 이내 대화는 같은 스레드로 묶여 컨텍스트가 유지되는 상태

---

## 작업 항목

### 3.1 Thread 도메인
- [ ] `Thread` 엔티티 생성
  ```kotlin
  @Entity
  @Table(name = "threads")
  class Thread(
      @Id @GeneratedValue
      val id: Long = 0,
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "user_id")
      val user: User,
      val createdAt: OffsetDateTime = OffsetDateTime.now(),
      var updatedAt: OffsetDateTime = OffsetDateTime.now()
  ) {
      fun isExpired(): Boolean =
          updatedAt.plusMinutes(30).isBefore(OffsetDateTime.now())

      fun touch() { updatedAt = OffsetDateTime.now() }
  }
  ```
- [ ] `ThreadRepository` (+ 사용자별 최신 스레드 조회 쿼리)

### 3.2 Chat 도메인
- [ ] `Chat` 엔티티 생성
  ```kotlin
  @Entity
  @Table(name = "chats")
  class Chat(
      @Id @GeneratedValue
      val id: Long = 0,
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "thread_id")
      val thread: Thread,
      val question: String,
      var answer: String = "",
      val createdAt: OffsetDateTime = OffsetDateTime.now()
  )
  ```
- [ ] `ChatRepository`

### 3.3 OpenAI API 연동
- [ ] `application.yml`에 OpenAI API 키 설정
- [ ] `OpenAiClient` 클래스 생성
  - RestTemplate 또는 WebClient 사용
  - Chat Completions API 호출
  - 요청 DTO: `OpenAiRequest`
  - 응답 DTO: `OpenAiResponse`
- [ ] 메시지 히스토리 구성 (시스템 프롬프트 + 이전 대화)

### 3.4 대화 서비스
- [ ] `ChatService` 생성
  - `createChat(userId, question, model?)`: 대화 생성
    1. 사용자의 최신 스레드 조회
    2. 스레드가 없거나 만료됨 → 새 스레드 생성
    3. 해당 스레드의 이전 대화 조회 (컨텍스트)
    4. OpenAI API 호출 (model 파라미터 있으면 해당 모델 사용)
    5. Chat 엔티티 저장
    6. 스레드 updatedAt 갱신
  - `getChats(userId, role, page, size, sort)`: 대화 목록 조회 (스레드 그룹화)
    - MEMBER: 자신의 대화만 조회
    - ADMIN: 모든 사용자의 대화 조회 가능
  - `deleteThread(userId, threadId)`: 스레드 삭제 (자신의 스레드만)

### 3.5 대화 API
- [ ] `ChatController` 생성 (`/api/chats`)
- [ ] 대화 생성 API
  ```
  POST /api/chats
  Request: {
    question: String,
    model: String? = null  // 선택적 모델 지정
  }
  Response: { chatId, threadId, question, answer }
  ```
- [ ] 대화 목록 조회 API
  ```
  GET /api/chats?page=0&size=20&sort=createdAt,desc
  Response: { threads: [{ threadId, chats: [...], createdAt }], totalPages }
  ```
  - 정렬: createdAt 기준 오름차순/내림차순
  - 페이지네이션 지원
  - MEMBER는 자신의 대화만, ADMIN은 전체 조회 가능
- [ ] 스레드 삭제 API
  ```
  DELETE /api/threads/{threadId}
  Response: { success: true }
  ```
  - 각 유저는 자신의 스레드만 삭제 가능

---

## 산출물

| 파일 | 설명 |
|------|------|
| `Thread.kt` | 스레드 엔티티 |
| `ThreadRepository.kt` | 스레드 레포지토리 |
| `Chat.kt` | 대화 엔티티 |
| `ChatRepository.kt` | 대화 레포지토리 |
| `OpenAiClient.kt` | OpenAI API 클라이언트 |
| `OpenAiRequest.kt` | OpenAI 요청 DTO |
| `OpenAiResponse.kt` | OpenAI 응답 DTO |
| `ChatService.kt` | 대화 비즈니스 로직 |
| `ChatController.kt` | 대화 API |

---

## 테스트 코드 (생산성 향상 목적)

> ⚠️ 3시간 제한: OpenAI API 호출은 Mock 처리, 핵심 로직만 검증
> 💡 스레드 타임아웃, 권한 검증 등 버그 발생 시 디버깅 오래 걸리는 부분 집중

### 3.6 필수 테스트

| 테스트 | 목적 | 우선순위 |
|--------|------|----------|
| `ChatServiceTest` | 스레드 생성/만료 로직 검증 | P1 |
| `ThreadTest` | 30분 타임아웃 로직 검증 | P1 |
| `ChatControllerTest` | API E2E 성공 케이스 검증 | P1 (필수) |

```kotlin
// ChatControllerTest.kt - E2E 성공 케이스
@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {
    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var openAiClient: OpenAiClient // 외부 의존성 Mock

    @Test
    fun `대화 생성 E2E`() {
        // Mock 설정
        given(openAiClient.chat(any())).willReturn("Mock Answer")

        mockMvc.perform(post("/api/chats")
            .header("Authorization", "Bearer {valid_token}")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"question": "hello"}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.answer").value("Mock Answer"))
    }
}
```

```kotlin
// ChatServiceTest.kt
@SpringBootTest
class ChatServiceTest {
    @Autowired lateinit var chatService: ChatService
    @MockBean lateinit var openAiClient: OpenAiClient  // API 호출 Mock

    @BeforeEach
    fun setup() {
        // OpenAI 응답 Mock
        whenever(openAiClient.chat(any())).thenReturn("Mock AI 응답")
    }

    @Test
    fun `새 대화 시 스레드 생성`() {
        val result = chatService.createChat(userId = 1L, question = "안녕")
        assertThat(result.threadId).isNotNull()
    }

    @Test
    fun `30분 내 대화는 같은 스레드 사용`() {
        val first = chatService.createChat(userId = 1L, question = "첫번째")
        val second = chatService.createChat(userId = 1L, question = "두번째")
        assertThat(first.threadId).isEqualTo(second.threadId)
    }

    @Test
    fun `MEMBER는 자신의 대화만 조회`() {
        // userId=1의 대화 생성
        chatService.createChat(userId = 1L, question = "내 대화")

        val result = chatService.getChats(userId = 1L, role = UserRole.MEMBER)
        assertThat(result).allMatch { it.userId == 1L }
    }

    @Test
    fun `자신의 스레드만 삭제 가능`() {
        val chat = chatService.createChat(userId = 1L, question = "테스트")

        assertThrows<ForbiddenException> {
            chatService.deleteThread(userId = 999L, threadId = chat.threadId)
        }
    }
}
```

```kotlin
// ThreadTest.kt - 단위 테스트 (빠른 실행)
class ThreadTest {
    @Test
    fun `30분 경과 시 만료`() {
        val thread = Thread(user = mockUser).apply {
            // 31분 전으로 설정
            updatedAt = OffsetDateTime.now().minusMinutes(31)
        }
        assertThat(thread.isExpired()).isTrue()
    }

    @Test
    fun `30분 이내면 유효`() {
        val thread = Thread(user = mockUser).apply {
            updatedAt = OffsetDateTime.now().minusMinutes(29)
        }
        assertThat(thread.isExpired()).isFalse()
    }

    @Test
    fun `touch 호출 시 updatedAt 갱신`() {
        val thread = Thread(user = mockUser)
        val before = thread.updatedAt
        Thread.sleep(10)
        thread.touch()
        assertThat(thread.updatedAt).isAfter(before)
    }
}
```

### 테스트 작성 기준

```
✅ 작성할 테스트:
- 스레드 타임아웃 로직 (30분 만료 판단)
- 권한 검증 (MEMBER/ADMIN 분리, 소유권 체크)
- OpenAI 호출은 Mock 처리 (외부 의존성 제거)
- **API E2E 통합 테스트 (Happy Path 성공 케이스 필수)**

❌ 생략할 테스트:
- 페이지네이션/정렬 (JPA가 보장)
```

---

## Pause 3: 검증 지점

### Swagger UI에서 테스트

```bash
# 1. 첫 번째 대화 생성
POST /api/chats
Authorization: Bearer {token}
{
  "question": "안녕하세요, 자기소개 해주세요."
}
# 기대: AI 답변 + 새 threadId 생성

# 2. 연속 대화 (30분 내)
POST /api/chats
Authorization: Bearer {token}
{
  "question": "방금 뭐라고 했죠?"
}
# 기대: 같은 threadId, 이전 대화 컨텍스트 반영된 답변

# 3. 모델 지정 대화
POST /api/chats
Authorization: Bearer {token}
{
  "question": "안녕",
  "model": "gpt-4o"
}
# 기대: 지정된 모델로 응답 생성

# 4. 대화 목록 조회 (정렬)
GET /api/chats?page=0&size=10&sort=createdAt,desc
Authorization: Bearer {token}
# 기대: 스레드별로 그룹화된 대화 목록 (최신순)

# 5. 대화 목록 조회 (오름차순)
GET /api/chats?page=0&size=10&sort=createdAt,asc
Authorization: Bearer {token}
# 기대: 스레드별로 그룹화된 대화 목록 (오래된순)

# 6. 관리자 전체 조회
GET /api/chats?page=0&size=10
Authorization: Bearer {adminToken}
# 기대: 모든 사용자의 대화 조회

# 7. 스레드 삭제
DELETE /api/threads/{threadId}
Authorization: Bearer {token}
# 기대: 200 OK

# 8. 타인 스레드 삭제 시도
DELETE /api/threads/{otherUserThreadId}
Authorization: Bearer {token}
# 기대: 403 Forbidden
```

| 검증 항목 | 기대 결과 |
|-----------|-----------|
| 대화 생성 | AI 답변 반환, Chat/Thread 저장 |
| 컨텍스트 유지 | 30분 내 대화는 같은 스레드 |
| 모델 지정 | 파라미터로 전달된 모델 사용 |
| 대화 목록 | 스레드별 그룹화, 페이지네이션, 정렬 |
| 권한 분리 | MEMBER는 자기 것만, ADMIN은 전체 |
| 스레드 삭제 | 스레드 및 관련 대화 삭제, 자기 것만 |

---

## 다음 Phase로 진행 조건

- [ ] OpenAI API 연동 성공
- [ ] 대화 생성 시 AI 답변 반환
- [ ] 30분 스레드 타임아웃 동작
- [ ] 대화 목록 조회 동작 (정렬, 페이지네이션)
- [ ] MEMBER/ADMIN 권한 분리 동작
- [ ] 스레드 삭제 동작 (소유권 검증)
