# AI Chatbot Demo - 실행 계획 개요

> **총 소요 시간**: 3시간 (180분)
> **목표**: AI 챗봇 API MVP 완성 + 고객 시연 준비
> **핵심 가치**: API를 통해 AI를 활용할 수 있음을 시연

---

## 시간 배분

```
[필수] Phase 1 ████░░░░░░░░░░░░░░░░░░░░░░░░░░  20분  프로젝트 초기화
[필수] Phase 2 ██████░░░░░░░░░░░░░░░░░░░░░░░░  25분  인증 기능
[필수] Phase 3 ████████████░░░░░░░░░░░░░░░░░░  40분  대화 기능 (기본)
[필수] Phase 4 ████░░░░░░░░░░░░░░░░░░░░░░░░░░  20분  대화 기능 (스트리밍)
[필수] Phase 5 ████░░░░░░░░░░░░░░░░░░░░░░░░░░  20분  시연용 데모 페이지 ⭐
[필수] Phase 6 ███░░░░░░░░░░░░░░░░░░░░░░░░░░░  15분  마무리 + 시연 준비
───────────────────────────────────────────────────────────────────────
                                             140분  필수 완료 (2시간 20분)

[선택] Phase 7 ██████░░░░░░░░░░░░░░░░░░░░░░░░  25분  피드백 기능
[선택] Phase 8 ████░░░░░░░░░░░░░░░░░░░░░░░░░░  20분  분석 및 보고 기능
───────────────────────────────────────────────────────────────────────
                                              45분  선택 기능 (시간 남으면)
```

**시연 최소 요건**: Phase 1-6 완료 시 고객 시연 가능 (140분)
**여유 시간**: 40분 → Phase 7, 8 구현 또는 버그 수정/테스트에 활용

---

## Phase 의존성 그래프

```
Phase 1 (초기화)
    │
    ▼
Phase 2 (인증)
    │
    ▼
Phase 3 (대화 기본)
    │
    ▼
Phase 4 (스트리밍)
    │
    ▼
Phase 5 (데모 페이지) ⭐
    │
    ▼
Phase 6 (마무리) ────────── 여기까지 필수! 시연 가능 상태
    │
    ├──────────────────┐
    ▼                  ▼
Phase 7 (피드백)    Phase 8 (분석/보고)
    │                  │
    └────── 시간 남으면 ─┘
```

**필수**: Phase 1-6 (140분)
**선택**: Phase 7-8 (45분) - 시간이 남으면 병렬 진행 가능

---

## Phase별 요약

### 필수 Phase (시연 목표 달성)

| Phase | 파일 | 결과 상태 | 시간 |
|-------|------|-----------|------|
| 1 | [plan-chat-demo-1.md](./plan-chat-demo-1.md) | Swagger UI 접속 가능 | 20분 |
| 2 | [plan-chat-demo-2.md](./plan-chat-demo-2.md) | JWT 인증 동작 | 25분 |
| 3 | [plan-chat-demo-3.md](./plan-chat-demo-3.md) | OpenAI 대화 생성 | 40분 |
| 4 | [plan-chat-demo-4.md](./plan-chat-demo-4.md) | SSE 스트리밍 동작 | 20분 |
| 5 | [plan-chat-demo-5.md](./plan-chat-demo-5.md) | **데모 페이지 + 확장 문서** | 20분 |
| 6 | [plan-chat-demo-6.md](./plan-chat-demo-6.md) | **시연 준비 완료** | 15분 |

### 선택 Phase (시간 남으면)

| Phase | 파일 | 결과 상태 | 시간 |
|-------|------|-----------|------|
| 7 | [plan-chat-demo-7.md](./plan-chat-demo-7.md) | 피드백 CRUD 동작 | 25분 |
| 8 | [plan-chat-demo-8.md](./plan-chat-demo-8.md) | 활동기록/CSV 다운로드 | 20분 |

---

## 핵심 산출물

### API 엔드포인트

| Method | Endpoint | 설명 | 우선순위 |
|--------|----------|------|----------|
| POST | `/api/auth/signup` | 회원가입 | P1 필수 |
| POST | `/api/auth/login` | 로그인 | P1 필수 |
| POST | `/api/chats` | 대화 생성 (일반) | P1 필수 |
| POST | `/api/chats/stream` | 대화 생성 (스트리밍) | P1 필수 |
| GET | `/api/chats` | 대화 목록 조회 | P1 필수 |
| DELETE | `/api/threads/{id}` | 스레드 삭제 | P1 필수 |
| POST | `/api/feedbacks` | 피드백 생성 | P2 선택 |
| GET | `/api/feedbacks/me` | 내 피드백 목록 | P2 선택 |
| GET | `/api/admin/feedbacks` | 전체 피드백 (관리자) | P2 선택 |
| PATCH | `/api/admin/feedbacks/{id}` | 피드백 상태 변경 (관리자) | P2 선택 |
| GET | `/api/admin/activity` | 사용자 활동 기록 (관리자) | P2 선택 |
| GET | `/api/admin/reports/chats` | CSV 보고서 다운로드 (관리자) | P2 선택 |

### 파일 구조

```
kotlin-chatbot/
├── src/main/kotlin/com/example/chatbot/
│   ├── ChatbotApplication.kt
│   ├── config/
│   │   ├── SwaggerConfig.kt
│   │   ├── FilterConfig.kt
│   │   └── WebMvcConfig.kt
│   ├── domain/
│   │   ├── user/
│   │   │   ├── User.kt
│   │   │   ├── UserRepository.kt
│   │   │   ├── AuthController.kt
│   │   │   └── AuthService.kt
│   │   ├── thread/
│   │   │   ├── Thread.kt
│   │   │   └── ThreadRepository.kt
│   │   ├── chat/
│   │   │   ├── Chat.kt
│   │   │   ├── ChatRepository.kt
│   │   │   ├── ChatController.kt
│   │   │   └── ChatService.kt
│   │   └── feedback/              # [선택] Phase 7
│   │       ├── Feedback.kt
│   │       ├── FeedbackRepository.kt
│   │       ├── FeedbackController.kt
│   │       └── FeedbackService.kt
│   ├── common/
│   │   ├── exception/
│   │   │   ├── BusinessException.kt
│   │   │   └── GlobalExceptionHandler.kt
│   │   ├── response/
│   │   │   ├── ApiResponse.kt
│   │   │   └── ErrorResponse.kt
│   │   └── auth/
│   │       ├── JwtProvider.kt
│   │       ├── JwtAuthenticationFilter.kt
│   │       ├── AuthUser.kt
│   │       └── CurrentUserArgumentResolver.kt
│   └── infra/
│       └── openai/
│           ├── OpenAiClient.kt
│           ├── OpenAiRequest.kt
│           └── OpenAiResponse.kt
├── src/main/resources/
│   ├── application.yml
│   └── static/
│       └── index.html
├── EXTENSION_ROADMAP.md    # RAG 확장 로드맵
└── DEMO_GUIDE.md           # 고객 시연 가이드
```

---

## 실행 명령어

```bash
# 환경 변수 설정
export OPENAI_API_KEY=your-api-key

# 서버 실행
./gradlew bootRun

# 접속 URL
# - 데모 페이지: http://localhost:8080/
# - Swagger UI: http://localhost:8080/swagger-ui.html
# - H2 Console: http://localhost:8080/h2-console
```

---

## LLM 실행 가이드

### 각 Phase 실행 방법

```
1. 해당 Phase의 plan-chat-demo-{N}.md 파일 읽기
2. 작업 항목을 순서대로 구현
3. Pause 검증 지점에서 테스트 수행
4. "다음 Phase로 진행 조건" 모두 만족 시 다음 Phase로 이동
```

### 우선순위 기반 실행 전략

```
[1단계] Phase 1-6 완료 (140분)
        → 시연 가능 상태 확보

[2단계] 남은 시간 확인
        → 40분 이상: Phase 7 또는 8 선택
        → 40분 미만: 테스트/버그 수정에 집중

[3단계] Phase 7, 8은 독립적
        → 시간에 따라 하나만 선택 가능
        → 피드백(7)이 시연에 더 직관적
```

---

## 기술 스택 요약

| 영역 | 기술 |
|------|------|
| Language | Kotlin 1.9+ |
| Framework | Spring Boot 3.x |
| Database | H2 (in-memory) |
| ORM | Spring Data JPA |
| Auth | JWT (jjwt) |
| API Doc | SpringDoc OpenAPI (Swagger) |
| AI | OpenAI Chat Completions API |
