# AI Chatbot 확장 로드맵

> 현재 MVP 구현 이후 자사 대외비 문서 학습(RAG) 및 고도화 계획

---

## 현재 아키텍처

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Client    │────▶│  Spring Boot │────▶│   OpenAI    │
│  (Browser)  │◀────│     API      │◀────│     API     │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   H2 (DB)   │
                    │ - Users     │
                    │ - Threads   │
                    │ - Chats     │
                    └─────────────┘
```

**현재 구현:**
- OpenAI Chat Completions API 직접 연동
- 스레드 기반 대화 컨텍스트 관리 (30분 타임아웃)
- SSE 스트리밍 응답
- JWT 인증

---

## Phase 1: RAG (Retrieval-Augmented Generation) 도입

### 목표
자사 대외비 문서를 학습시켜 도메인 특화 AI 챗봇 구현

### 확장 아키텍처

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Client    │────▶│  Spring Boot │────▶│   OpenAI    │
│  (Browser)  │◀────│     API      │◀────│     API     │
└─────────────┘     └─────────────┘     └─────────────┘
                           │                    ▲
                           │                    │ Context
                           ▼                    │
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Document   │────▶│  Embedding  │────▶│   Vector    │
│   Upload    │     │   Service   │     │     DB      │
└─────────────┘     └─────────────┘     └─────────────┘
```

### 구현 방안

#### 1. 문서 임베딩 파이프라인

```kotlin
// DocumentService.kt
@Service
class DocumentService(
    private val embeddingClient: EmbeddingClient,
    private val vectorStore: VectorStore
) {
    fun uploadDocument(file: MultipartFile) {
        val text = extractText(file)           // PDF/Word/TXT 추출
        val chunks = chunkText(text, 500)      // 500 토큰 단위 분할
        val embeddings = embeddingClient.embed(chunks)
        vectorStore.store(embeddings)
    }
}
```

**문서 처리 흐름:**
1. 문서 업로드 API (`POST /api/documents`)
2. 텍스트 추출 (Apache Tika 활용)
3. 청킹 (chunk size: 500-1000 tokens)
4. OpenAI Embeddings API로 벡터 변환
5. 벡터 DB 저장

#### 2. 벡터 데이터베이스 옵션

| 옵션 | 장점 | 단점 | 추천 상황 |
|------|------|------|----------|
| **Pinecone** | 관리형, 빠른 도입 | 비용, 외부 의존 | 빠른 PoC |
| **Weaviate** | 오픈소스, 기능 풍부 | 운영 복잡 | 온프레미스 |
| **PostgreSQL + pgvector** | 기존 인프라 활용 | 성능 한계 | 소규모 |
| **Milvus** | 고성능, 확장성 | 학습 곡선 | 대규모 |

**추천:** 초기에는 PostgreSQL + pgvector로 시작, 규모 확장 시 Pinecone 또는 Milvus로 마이그레이션

#### 3. 검색 증강 생성 흐름

```
사용자 질문: "우리 회사 휴가 정책이 어떻게 되나요?"
      │
      ▼
┌─────────────────────────────────────────────────────────┐
│ 1. 질문 임베딩                                          │
│    → OpenAI Embeddings API                              │
└─────────────────────────────────────────────────────────┘
      │
      ▼
┌─────────────────────────────────────────────────────────┐
│ 2. 유사 문서 검색 (Top-K)                               │
│    → Vector DB에서 코사인 유사도 기준 상위 5개 검색     │
└─────────────────────────────────────────────────────────┘
      │
      ▼
┌─────────────────────────────────────────────────────────┐
│ 3. 프롬프트 구성                                        │
│    System: "다음 문서를 참고하여 질문에 답변하세요"     │
│    Context: [검색된 문서 청크들]                        │
│    User: "우리 회사 휴가 정책이 어떻게 되나요?"         │
└─────────────────────────────────────────────────────────┘
      │
      ▼
┌─────────────────────────────────────────────────────────┐
│ 4. OpenAI 응답 생성                                     │
│    → 문서 기반 정확한 답변                              │
└─────────────────────────────────────────────────────────┘
```

### 코드 확장 포인트

```
src/main/kotlin/projects/aichatbot/
├── infra/
│   ├── embedding/
│   │   └── EmbeddingClient.kt      # OpenAI Embeddings API
│   └── vectordb/
│       └── VectorStore.kt          # 벡터 저장/검색
├── domain/
│   └── document/
│       ├── Document.kt             # 문서 엔티티
│       ├── DocumentChunk.kt        # 청크 엔티티
│       ├── DocumentRepository.kt
│       ├── DocumentService.kt      # 문서 처리 로직
│       └── DocumentController.kt   # 업로드/관리 API
└── domain/chat/
    └── ChatService.kt              # RAG 컨텍스트 주입 로직 추가
```

### 예상 개발 기간
- 기본 RAG 파이프라인: 2-3주
- 문서 관리 UI: 1주
- 품질 튜닝 및 테스트: 1-2주
- **총 예상: 4-6주**

---

## Phase 2: Fine-tuning (선택적)

### 목표
피드백 데이터 기반 모델 미세조정으로 도메인 특화 응답 품질 향상

### 구현 방안

1. **피드백 데이터 수집**
   - 현재 피드백 기능으로 긍정/부정 평가 축적
   - 부정 피드백 대화는 관리자가 정답 수정

2. **학습 데이터 생성**
   ```json
   {"messages": [
     {"role": "system", "content": "당신은 우리 회사 전문 AI입니다."},
     {"role": "user", "content": "휴가 신청 방법은?"},
     {"role": "assistant", "content": "휴가 신청은 HR 시스템에서..."}
   ]}
   ```

3. **OpenAI Fine-tuning API 활용**
   - 최소 50-100개 고품질 예시 필요
   - gpt-4o-mini 기반 fine-tuning 권장

### 예상 효과
- 도메인 특화 어투/스타일 학습
- 자주 묻는 질문에 대한 정확도 향상
- 응답 일관성 개선

---

## Phase 3: 멀티모달 확장

### 목표
텍스트 외 이미지, PDF 등 다양한 입력 처리

### 구현 방안

1. **이미지 처리**
   - GPT-4 Vision API 연동
   - 이미지 업로드 → Base64 인코딩 → API 전송
   - 사용 사례: 차트 분석, 문서 스캔 질의

2. **PDF 직접 처리**
   - PDF 텍스트 추출 + 이미지 페이지는 Vision 처리
   - OCR 필요 시 Tesseract 또는 Azure Document Intelligence

3. **음성 입출력 (선택)**
   - OpenAI Whisper API (음성→텍스트)
   - OpenAI TTS API (텍스트→음성)

---

## 보안 고려사항

### 데이터 보안
- [ ] 대외비 문서 암호화 저장
- [ ] 접근 권한 관리 (문서별 열람 권한)
- [ ] API 호출 로그 감사

### OpenAI API 보안
- [ ] API 키 환경변수 관리
- [ ] 데이터 학습 거부 설정 (Enterprise 플랜)
- [ ] Azure OpenAI 고려 (데이터 주권)

---

## 비용 추정

### 현재 (MVP)
| 항목 | 비용 |
|------|------|
| OpenAI GPT-4o-mini | ~$0.15 / 1M tokens |
| 월 예상 (1만 대화) | ~$10-30 |

### RAG 도입 후
| 항목 | 비용 |
|------|------|
| OpenAI Embeddings | ~$0.02 / 1M tokens |
| Vector DB (Pinecone) | $70/월~ |
| 증가된 컨텍스트 | +50% 토큰 사용 |
| 월 예상 (1만 대화) | ~$100-200 |

---

## 마일스톤

| 단계 | 내용 | 예상 기간 |
|------|------|----------|
| MVP (현재) | 기본 챗봇 + 스트리밍 | ✅ 완료 |
| Phase 1 | RAG 파이프라인 | 4-6주 |
| Phase 2 | Fine-tuning | 2-3주 |
| Phase 3 | 멀티모달 | 2-4주 |

---

## 참고 자료

- [OpenAI Embeddings Guide](https://platform.openai.com/docs/guides/embeddings)
- [RAG Best Practices](https://www.pinecone.io/learn/retrieval-augmented-generation/)
- [LangChain Documentation](https://docs.langchain.com/)
- [pgvector GitHub](https://github.com/pgvector/pgvector)
