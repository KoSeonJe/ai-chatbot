# Phase 4: 대화 기능 (스트리밍) 구현 완료

## 핵심 구현 사항
- [x] OpenAiClient 스트리밍 메서드 추가
- [x] SSE (Server-Sent Events) 응답 구현
- [x] ChatService 스트리밍 대화 처리
- [x] 별도 스트리밍 엔드포인트 (/api/chats/stream)

## 생성/수정된 파일
| 파일 | 설명 |
|------|------|
| `OpenAiClient.kt` | streamChat 메서드 추가 |
| `ChatService.kt` | createStreamingChat 메서드 추가 |
| `ChatController.kt` | /api/chats/stream 엔드포인트 추가 |
| `StreamEvent.kt` | SSE 이벤트 DTO |
| `ChatDtos.kt` | isStreaming 파라미터 추가 |

## API 엔드포인트
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/chats` | 일반 대화 생성 |
| POST | `/api/chats/stream` | 스트리밍 대화 생성 (SSE) |

## SSE 이벤트 포맷
```
event: token
data: {"type":"token","content":"안녕"}

event: token
data: {"type":"token","content":"하세요"}

event: done
data: {"type":"done","chatId":1,"threadId":1}
```

## 테스트 방법
```bash
# 환경 변수 설정
export OPENAI_API_KEY=your-api-key

# 서버 실행
./gradlew bootRun

# 일반 대화
curl -X POST http://localhost:8080/api/chats \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"question": "안녕하세요"}'

# 스트리밍 대화
curl -N -X POST http://localhost:8080/api/chats/stream \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"question": "1부터 10까지 세어주세요"}'
```

## 다음 단계
Phase 5: 데모 페이지 + 확장 로드맵
