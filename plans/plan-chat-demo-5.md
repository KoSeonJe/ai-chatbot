# Phase 5: 시연용 데모 페이지 + 확장 로드맵

> **시간 제한**: 20분
> **선행 조건**: Phase 4 완료
> **결과 상태**: 브라우저에서 AI 챗봇 체험 가능 + RAG 확장 로드맵 문서화
> **중요도**: ⭐ 시연 핵심 - 이 Phase까지 완료하면 기본 시연 가능

---

## 목표

1. API spec에 익숙하지 않은 고객이 **Swagger 없이도** 직접 AI와 대화할 수 있는 시각적 데모 환경 제공
2. 향후 **자사 대외비 문서 학습(RAG)** 확장 방안을 문서화하여 고객에게 로드맵 제시

---

## 작업 항목

### 5.1 HTML 페이지 생성
- [ ] `src/main/resources/static/index.html` 생성
- [ ] 단일 파일에 HTML + CSS + JavaScript 포함
- [ ] 반응형 디자인 (모바일 대응)

### 5.2 UI 구성요소
- [ ] 헤더: "AI Chatbot Demo" 로고
- [ ] 로그인/회원가입 폼 (토글)
- [ ] 채팅 영역
  - 메시지 버블 (사용자: 오른쪽, AI: 왼쪽)
  - AI 타이핑 인디케이터 (스트리밍 중)
  - 스크롤 자동 하단 이동
- [ ] 입력 영역: 텍스트 입력 + 전송 버튼

### 5.3 JavaScript 기능
- [ ] 인증 처리
  ```javascript
  async function login(email, password) {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const data = await res.json();
    localStorage.setItem('token', data.data.accessToken);
  }
  ```
- [ ] 스트리밍 대화 처리
  ```javascript
  async function sendMessage(question) {
    const response = await fetch('/api/chats', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ question, isStreaming: true })
    });

    // isStreaming=true로 요청하면 SSE 스트리밍 응답
    const reader = response.body.getReader();
    const decoder = new TextDecoder();

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value);
      appendToCurrentMessage(parseSSE(chunk));
    }
  }
  ```

### 5.4 스타일링
- [ ] 모던 채팅 UI (ChatGPT 스타일 참고)
- [ ] 애니메이션: 메시지 등장, 타이핑 효과

### 5.5 RAG 확장 로드맵 문서 작성
- [ ] `EXTENSION_ROADMAP.md` 생성
- [ ] 문서 내용:
  ```markdown
  # AI Chatbot 확장 로드맵

  ## 현재 아키텍처
  - OpenAI Chat Completions API 직접 연동
  - 스레드 기반 대화 컨텍스트 관리

  ## Phase 1: RAG (Retrieval-Augmented Generation) 도입

  ### 목표
  자사 대외비 문서를 학습시켜 도메인 특화 AI 챗봇 구현

  ### 구현 방안
  1. **문서 임베딩 파이프라인**
     - 문서 업로드 API 추가
     - 텍스트 청킹 (chunk size: 500-1000 tokens)
     - OpenAI Embeddings API로 벡터 변환

  2. **벡터 데이터베이스 도입**
     - 옵션 A: Pinecone (관리형, 빠른 도입)
     - 옵션 B: Weaviate (오픈소스, 온프레미스)
     - 옵션 C: PostgreSQL + pgvector (기존 인프라 활용)

  3. **검색 증강 생성 흐름**
     ```
     사용자 질문 → 질문 임베딩 → 유사 문서 검색 →
     컨텍스트 + 질문 → OpenAI → 답변
     ```

  ### 코드 확장 포인트
  - `infra/embedding/EmbeddingClient.kt` - 임베딩 생성
  - `infra/vectordb/VectorStore.kt` - 벡터 저장/검색
  - `domain/document/` - 문서 관리 도메인
  - `ChatService.kt` - RAG 컨텍스트 주입

  ## Phase 2: Fine-tuning (선택적)
  - 피드백 데이터 기반 모델 미세조정
  - 도메인 특화 응답 품질 향상

  ## Phase 3: 멀티모달 확장
  - 이미지/PDF 문서 처리
  - GPT-4 Vision 연동
  ```

---

## 산출물

| 파일 | 설명 |
|------|------|
| `static/index.html` | 데모 페이지 (단일 파일) |
| `EXTENSION_ROADMAP.md` | RAG 확장 로드맵 문서 |
| `DEMO_GUIDE.md` | 고객 시연 가이드 |

---

## 예시 레이아웃

```
┌─────────────────────────────────────────────────────────┐
│  🤖 AI Chatbot Demo                        [로그아웃]   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 👤 안녕하세요, 자기소개 해주세요.               │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 🤖 안녕하세요! 저는 AI 어시스턴트입니다.        │   │
│  │    무엇이든 물어보세요.                         │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 🤖 ▌ (타이핑 중...)                             │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
├─────────────────────────────────────────────────────────┤
│  [메시지 입력...]                              [전송]   │
└─────────────────────────────────────────────────────────┘
```

---

## Pause 5: 검증 지점

### 브라우저에서 테스트

```
1. http://localhost:8080/ 접속
2. 회원가입 → 로그인
3. 메시지 입력 → 전송
4. 스트리밍 응답 실시간 렌더링 확인
```

| 검증 항목 | 기대 결과 |
|-----------|-----------|
| 페이지 로드 | 로그인 폼 표시 |
| 로그인 | 토큰 저장, 채팅 UI 전환 |
| 메시지 전송 | AI 답변 스트리밍 표시 |
| 타이핑 효과 | 글자가 하나씩 나타남 |
| 확장 문서 | `EXTENSION_ROADMAP.md` 존재 |

### 시연 가능 상태 확인

이 Phase 완료 시 다음 시연이 가능해야 함:
- ✅ 회원가입/로그인
- ✅ AI와 실시간 대화
- ✅ 스트리밍 응답
- ✅ `DEMO_GUIDE.md` 기반 고객 설명
- ✅ 향후 RAG 확장 계획 설명

---

## 다음 Phase로 진행 조건

- [ ] 데모 페이지 정상 로드
- [ ] 회원가입/로그인 동작
- [ ] 스트리밍 대화 동작
- [ ] `EXTENSION_ROADMAP.md` 작성 완료
