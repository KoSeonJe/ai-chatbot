# Phase 4: 대화 기능 (스트리밍)

> **시간 제한**: 20분
> **선행 조건**: Phase 3 완료
> **결과 상태**: SSE 기반 스트리밍으로 AI 답변이 실시간 전송되는 상태

---

## 목표

`isStreaming=true` 파라미터 사용 시 SSE(Server-Sent Events)로 AI 답변이 토큰 단위로 실시간 전송되어, "AI가 타이핑하는" 사용자 경험 제공

---

## 작업 항목

### 4.1 OpenAI 스트리밍 연동
- [ ] `OpenAiClient`에 스트리밍 메서드 추가
  ```kotlin
  fun streamChat(
      messages: List<ChatMessage>,
      model: String,
      onToken: (String) -> Unit,
      onComplete: () -> Unit
  )
  ```
- [ ] OpenAI API `stream: true` 옵션 사용
- [ ] SSE 응답 파싱 (`data: {...}` 형식)
- [ ] `[DONE]` 시그널 처리

### 4.2 SSE 응답 구현
- [ ] `ChatController`에서 단일 엔드포인트에서 isStreaming 파라미터로 분기
  ```kotlin
  @PostMapping("/chats")
  fun createChat(
      @Valid @RequestBody request: CreateChatRequest,
      @CurrentUser authUser: AuthUser
  ): ResponseEntity<*> {
      return if (request.isStreaming == true) {
          // SSE 스트리밍 응답
          ResponseEntity.ok()
              .contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
              .body(chatService.createStreamingChat(authUser.userId, request))
      } else {
          // 일반 응답
          ResponseEntity.ok(chatService.createChat(authUser.userId, request))
      }
  }
  ```

### 4.3 SSE 이벤트 포맷
- [ ] 스트리밍 응답 형식 정의
  ```
  event: token
  data: {"content": "안녕"}

  event: token
  data: {"content": "하세요"}

  event: done
  data: {"chatId": 1, "threadId": 1}
  ```

### 4.4 ChatService 스트리밍 처리
- [ ] `createStreamingChat` 메서드 추가
  - 스레드 로직은 동일
  - OpenAI 스트리밍 호출
  - 완료 후 Chat 엔티티 저장

---

## 산출물

| 파일 | 설명 |
|------|------|
| `OpenAiClient.kt` | 스트리밍 메서드 추가 |
| `ChatController.kt` | SSE 엔드포인트 추가 |
| `ChatService.kt` | 스트리밍 대화 처리 |
| `StreamEvent.kt` | SSE 이벤트 DTO |

---

## 테스트 코드 (생산성 향상 목적)

> ⚠️ 3시간 제한: SSE 스트리밍은 curl로 수동 테스트가 더 효율적
> 💡 스트리밍 테스트는 복잡하므로 최소한만 작성

### 4.5 선택적 테스트

| 테스트 | 목적 | 우선순위 |
|--------|------|----------|
| `StreamEventTest` | SSE 이벤트 직렬화 검증 | P2 (선택) |

```kotlin
// StreamEventTest.kt - 이벤트 포맷만 검증
class StreamEventTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `토큰 이벤트 직렬화`() {
        val event = StreamEvent.token("안녕")
        val json = objectMapper.writeValueAsString(event)
        assertThat(json).contains("\"content\":\"안녕\"")
    }

    @Test
    fun `완료 이벤트 직렬화`() {
        val event = StreamEvent.done(chatId = 1, threadId = 2)
        val json = objectMapper.writeValueAsString(event)
        assertThat(json).contains("\"chatId\":1")
        assertThat(json).contains("\"threadId\":2")
    }
}
```

### 테스트 작성 기준

```
✅ 작성할 테스트:
- SSE 이벤트 DTO 직렬화 (JSON 포맷 검증)
- **API E2E 통합 테스트 (연결 성공여부 및 Content-Type 확인 필수)**

❌ 생략할 테스트 (curl로 수동 검증):
- 실제 SSE 스트리밍 데이터 전송 (테스트 복잡도 높음)
- OpenAI 스트리밍 API 호출

💡 스트리밍은 curl 테스트가 더 빠르고 직관적:
   # 일반 응답
   curl -N -H "Authorization: Bearer {token}" \
        -H "Content-Type: application/json" \
        -d '{"question": "테스트"}' \
        http://localhost:8080/api/chats
   
   # 스트리밍 응답
   curl -N -H "Authorization: Bearer {token}" \
        -H "Content-Type: application/json" \
        -d '{"question": "테스트", "isStreaming": true}' \
        http://localhost:8080/api/chats
```

---

## Pause 4: 검증 지점

### curl로 스트리밍 테스트

```bash
# 일반 응답 (isStreaming 없거나 false)
curl -N -H "Authorization: Bearer {token}" \
     -H "Content-Type: application/json" \
     -d '{"question": "안녕하세요"}' \
     http://localhost:8080/api/chats

# SSE 스트리밍 테스트 (isStreaming: true)
curl -N -H "Authorization: Bearer {token}" \
     -H "Content-Type: application/json" \
     -d '{"question": "1부터 10까지 천천히 세어주세요", "isStreaming": true}' \
     http://localhost:8080/api/chats
```

### Swagger UI 제한 사항
- Swagger UI는 SSE를 완전히 지원하지 않음
- 스트리밍 테스트는 curl 사용

| 검증 항목 | 기대 결과 |
|-----------|-----------|
| 스트리밍 응답 | 토큰 단위로 실시간 전송 |
| 이벤트 포맷 | `event: token`, `event: done` 구분 |
| 완료 이벤트 | chatId, threadId 포함 |
| DB 저장 | 스트리밍 완료 후 Chat 엔티티 저장 |
| 일반/스트리밍 분리 | 단일 엔드포인트에서 isStreaming 파라미터로 구분 |

---

## 다음 Phase로 진행 조건

- [ ] SSE 스트리밍 응답 동작
- [ ] 토큰 단위 실시간 전송
- [ ] 완료 후 DB 저장
- [ ] 일반 응답과 스트리밍 응답 모두 동작
