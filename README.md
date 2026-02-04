# 🤖 AI Chatbot Demo

### **📄 [과제 수행 보고서 (과제 분석, AI 활용, 핵심 기능 설명)](docs/assignment-report.md)**

> VIP 고객 시연용 AI 챗봇 API 서비스

OpenAI GPT 기반의 대화형 AI 챗봇 API입니다. Swagger UI로 API를 테스트하거나, 데모 페이지에서 직접 AI와 대화할 수 있습니다.

---

## ✨ 주요 기능

- **JWT 인증**: 회원가입/로그인 기반 보안
- **AI 대화**: OpenAI GPT 기반 자연스러운 대화
- **실시간 스트리밍**: SSE 기반 타이핑 효과
- **대화 컨텍스트**: 30분 이내 대화 맥락 유지
- **데모 페이지**: 비개발자도 바로 체험 가능

---

## 🚀 빠른 시작

### 1. 환경 설정

```bash
# OpenAI API 키 설정 (필수)
export OPENAI_API_KEY=your-api-key
```

### 2. 서버 실행

```bash
./gradlew bootRun
```

### 3. 접속

| URL | 설명 |
|-----|------|
| http://localhost:8080 | 데모 페이지 |
| http://localhost:8080/swagger-ui.html | API 문서 |
| http://localhost:8080/h2-console | DB 콘솔 |

---

## 📖 API 엔드포인트

### 인증

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 발급) |

### 대화

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/chats` | 대화 생성 (일반, 스티리밍) |
| GET | `/api/chats` | 대화 목록 조회 |
| DELETE | `/api/threads/{id}` | 스레드 삭제 |

---

## 🎮 데모 페이지 사용법

1. http://localhost:8080 접속
2. **회원가입** → 이름, 이메일, 비밀번호 입력
3. **로그인** → 채팅 화면 전환
4. **대화** → AI가 실시간으로 답변!

---

## 🛠 기술 스택

| 영역 | 기술 |
|------|------|
| Language | Kotlin 1.9+ |
| Framework | Spring Boot 3.x |
| Database | H2 (in-memory) |
| Auth | JWT (jjwt) |
| API Doc | SpringDoc OpenAPI |
| AI | OpenAI Chat Completions |

---

## 📁 프로젝트 구조

```
src/main/kotlin/projects/aichatbot/
├── config/           # Swagger, Filter, WebMvc 설정
├── common/
│   ├── auth/         # JWT, 필터, @CurrentUser
│   ├── exception/    # 예외 처리
│   └── response/     # 공통 응답
├── domain/
│   ├── user/         # 인증 (회원가입/로그인)
│   ├── thread/       # 대화 스레드
│   ├── feedback/       # 피드백(미구현)
│   └── chat/         # AI 대화
└── infra/
    └── openai/       # OpenAI API 클라이언트
```

---

## 📚 문서

| 문서 | 설명 |
|------|------|
| [DEMO_GUIDE.md](docs/DEMO_GUIDE.md) | 고객 시연 가이드 |
| [EXTENSION_ROADMAP.md](docs/EXTENSION_ROADMAP.md) | RAG 확장 로드맵 |

---

## 🔮 향후 확장 계획

1. **RAG (Retrieval-Augmented Generation)**
   - 자사 대외비 문서 학습
   - 도메인 특화 AI 챗봇
