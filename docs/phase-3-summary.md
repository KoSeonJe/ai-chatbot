# Phase 3: 대화 기능 (기본) 구현 완료

## 핵심 구현 사항
- [x] Thread 엔티티 및 ThreadRepository (30분 타임아웃 로직)
- [x] Chat 엔티티 및 ChatRepository
- [x] OpenAI API 클라이언트 (RestTemplate 기반)
- [x] ChatService (대화 생성, 조회, 스레드 삭제)
- [x] ChatController (API 엔드포인트)
- [x] 테스트 코드 (ChatServiceTest, ThreadTest)

## 생성된 파일
| 파일 | 설명 |
|------|------|
| `Thread.kt` | 스레드 엔티티 (30분 타임아웃) |
| `ThreadRepository.kt` | 스레드 레포지토리 |
| `Chat.kt` | 대화 엔티티 |
| `ChatRepository.kt` | 대화 레포지토리 |
| `ChatDtos.kt` | 요청/응답 DTO |
| `ChatService.kt` | 대화 비즈니스 로직 |
| `ChatController.kt` | 대화 API 엔드포인트 |
| `OpenAiClient.kt` | OpenAI API 클라이언트 |
| `OpenAiRequest.kt` | OpenAI 요청 DTO |
| `OpenAiResponse.kt` | OpenAI 응답 DTO |

## API 엔드포인트
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/chats` | 대화 생성 (AI 답변) |
| GET | `/api/chats` | 대화 목록 조회 (스레드별 그룹화) |
| DELETE | `/api/threads/{id}` | 스레드 삭제 |

## 주요 기능
- **30분 스레드 타임아웃**: 30분 이내 대화는 같은 스레드로 묶임
- **컨텍스트 유지**: 이전 대화 히스토리가 OpenAI에 전달됨
- **권한 분리**: MEMBER는 자기 대화만, ADMIN은 전체 조회
- **모델 선택**: 요청 시 model 파라미터로 다른 모델 지정 가능

## 테스트 방법
```bash
# 환경 변수 설정
export OPENAI_API_KEY=your-api-key

# 서버 실행
./gradlew bootRun

# Swagger UI에서 테스트
# http://localhost:8080/swagger-ui.html
```

## 다음 단계
Phase 4: 대화 기능 (스트리밍) 구현
