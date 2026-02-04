# AI Chatbot Demo - 프로젝트 컨텍스트

> 이 파일은 세션 간 컨텍스트 유지를 위한 프로젝트 지시사항입니다.

---

## 프로젝트 개요

**목적**: AI 챗봇 API 서비스
**핵심 가치**: Swagger UI로 API 테스트 가능

---

## 기술 스택

| 영역 | 기술 | 비고 |
|------|------|------|
| Language | Kotlin 1.9+ | |
| Framework | Spring Boot 3.x | |
| Database | H2 (in-memory) | |
| ORM | Spring Data JPA | |
| Auth | JWT (jjwt) | Spring Security 없이 필터 기반 |
| API Doc | SpringDoc OpenAPI | Swagger UI |
| AI | OpenAI Chat Completions | |

---

## 아키텍처 결정사항

### 인증 (경량화)
- Spring Security **사용 안 함**
- `OncePerRequestFilter` + `ArgumentResolver` 조합
- `@CurrentUser` 어노테이션으로 인증 사용자 주입

### 대화 컨텍스트
- 스레드 기반 30분 타임아웃
- 같은 스레드 내 대화는 OpenAI에 히스토리로 전달

### 스트리밍
- SSE (Server-Sent Events) 방식
- 별도 엔드포인트(`/api/chats/stream`) 사용

---

## ERD

```
users (1) ──── (N) threads (1) ──── (N) chats
  │                                      │
  └──────────── (N) feedbacks (N) ───────┘
```

**테이블 요약**:
- `users`: id, email, password, name, role(member/admin), created_at
- `threads`: id, user_id, created_at, updated_at
- `chats`: id, thread_id, question, answer, created_at
- `feedbacks`: id, user_id, chat_id, is_positive, status(pending/resolved), created_at

---

## API 엔드포인트 명세

### 인증
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 (email, password, name) |
| POST | `/api/auth/login` | 로그인 → JWT 발급 |

### 대화
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/chats` | 대화 생성 (일반/스트리밍) |
| | | - isStreaming=true 시 SSE 스트리밍 응답 |
| GET | `/api/chats` | 대화 목록 (스레드 그룹화, 정렬, 페이지네이션) |
| DELETE | `/api/threads/{id}` | 스레드 삭제 (자기 것만) |

**권한**: MEMBER는 자기 대화만 조회, ADMIN은 전체 조회 가능

### 피드백
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/feedbacks` | 피드백 생성 |
| GET | `/api/feedbacks/me` | 내 피드백 목록 (필터링, 정렬, 페이지네이션) |
| GET | `/api/admin/feedbacks` | 전체 피드백 (관리자) |
| PATCH | `/api/admin/feedbacks/{id}` | 상태 변경 (관리자) |

**규칙**:
- MEMBER: 자기 대화에만 피드백 가능
- ADMIN: 모든 대화에 피드백 가능
- 동일 사용자는 같은 대화에 1개 피드백만
- 서로 다른 사용자는 같은 대화에 각각 피드백 가능

### 분석 및 보고
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/admin/activity` | 사용자 활동 기록 (당일) |
| GET | `/api/admin/reports/chats` | CSV 보고서 다운로드 (당일) |

**활동 기록**: 당일 회원가입 수, 로그인 수, 대화 생성 수
**CSV 보고서**: 당일 모든 대화 + 사용자 정보

---

## 패키지 구조

```
com.example.chatbot/
├── config/           # 설정 (Swagger, Filter, WebMvc)
├── domain/
│   ├── user/         # 사용자, 인증
│   ├── thread/       # 대화 스레드
│   ├── chat/         # 대화
│   └── feedback/     # 피드백
├── common/
│   ├── exception/    # 예외 처리
│   ├── response/     # 공통 응답
│   └── auth/         # JWT, 필터, ArgumentResolver
└── infra/
    └── openai/       # OpenAI 클라이언트
```

---

## Phase 진행 상황

실행 계획 문서: `plan-chat-demo-*.md`

| Phase | 내용 | 시간 | 상태 | 필수 |
|-------|------|------|------|------|
| 1 | 프로젝트 초기화 | 20분 | ✅ 완료 | ✅ |
| 2 | 인증 기능 | 25분 | ✅ 완료 | ✅ |
| 3 | 대화 기능 (기본) | 40분 | ✅ 완료 | ✅ |
| 4 | 대화 기능 (스트리밍) | 20분 | ✅ 완료 | ✅ |
| 5 | 데모 페이지 + 확장 로드맵 | 20분 | ⬜ 미시작 | ✅ |
| 6 | **마무리 + 시연 준비** ⭐ | 15분 | ⬜ 미시작 | ✅ |
| 7 | 피드백 기능 | 25분 | ⬜ 미시작 | 선택 |
| 8 | 분석 및 보고 | 20분 | ⬜ 미시작 | 선택 |

**상태 범례**: ⬜ 미시작 | 🔄 진행중 | ✅ 완료
**시연 최소 요건**: Phase 6까지 완료 시 시연 가능 (140분)
**선택 기능**: Phase 7, 8은 시간이 남으면 구현 (45분)

---

## 실행 방법

```bash
# 환경 변수
export OPENAI_API_KEY=your-api-key

# 서버 실행
./gradlew bootRun

# 접속
# - 데모: http://localhost:8080/
# - Swagger: http://localhost:8080/swagger-ui.html
# - H2: http://localhost:8080/h2-console
```

---

## 세션 재개 시 지시사항

1. 이 파일의 **Phase 진행 상황** 확인
2. 미완료 Phase의 `plan-chat-demo-{N}.md` 읽기
3. 해당 Phase 작업 항목부터 이어서 진행
4. Phase 완료 시 이 파일의 상태를 ✅로 업데이트

---

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-02-03 | 초기 테크 스펙 기반 CLAUDE.md 생성 |
| 2026-02-03 | 실제 요구사항에 맞춰 수정 (Phase 8→6, 역할명 변경 등) |
| 2026-02-04 | Phase 진행 상황 초기화 (구현 전 상태) |
