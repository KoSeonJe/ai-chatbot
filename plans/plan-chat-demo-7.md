# Phase 7: 피드백 기능 [선택]

> **시간 제한**: 25분
> **선행 조건**: Phase 6 완료
> **결과 상태**: 사용자가 AI 답변에 좋아요/싫어요 피드백 가능, 관리자가 피드백 관리 가능
> **우선순위**: ⚠️ 선택 - 시간이 남으면 구현

---

## 목표

사용자가 각 대화에 피드백을 남기고, 관리자가 피드백 목록을 조회/관리할 수 있는 상태

---

## 작업 항목

### 7.1 Feedback 도메인
- [ ] `Feedback` 엔티티 생성
  ```kotlin
  @Entity
  @Table(
      name = "feedbacks",
      uniqueConstraints = [
          UniqueConstraint(columnNames = ["user_id", "chat_id"])
      ]
  )
  class Feedback(
      @Id @GeneratedValue
      val id: Long = 0,
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "user_id")
      val user: User,
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "chat_id")
      val chat: Chat,
      val isPositive: Boolean,  // true: 좋아요, false: 싫어요
      @Enumerated(EnumType.STRING)
      var status: FeedbackStatus = FeedbackStatus.PENDING,
      val createdAt: OffsetDateTime = OffsetDateTime.now()
  )

  enum class FeedbackStatus { PENDING, RESOLVED }
  ```
- [ ] `FeedbackRepository`
  - 사용자+대화 중복 체크 쿼리
  - 상태/긍정여부 필터링 쿼리

### 7.2 피드백 서비스
- [ ] `FeedbackService` 생성
  - `createFeedback(userId, role, chatId, isPositive)`: 피드백 생성
    - **MEMBER**: 자신이 생성한 대화에만 피드백 가능
    - **ADMIN**: 모든 대화에 피드백 가능
    - 중복 체크: 동일 사용자가 동일 대화에 이미 피드백 남긴 경우 거부
    - **참고**: 하나의 대화에 서로 다른 사용자들이 각각 피드백 가능 (N개)
  - `getMyFeedbacks(userId, isPositive?, page, size, sort)`: 내 피드백 목록
    - 긍정/부정 필터링
    - 정렬 (createdAt 오름차순/내림차순)
    - 페이지네이션
  - `getAllFeedbacks(status?, isPositive?, page, size, sort)`: 관리자용 전체 조회
  - `updateFeedbackStatus(feedbackId, status)`: 상태 변경 (관리자)

### 7.3 피드백 API
- [ ] `FeedbackController` 생성 (`/api/feedbacks`)
- [ ] 피드백 생성 API
  ```
  POST /api/feedbacks
  Request: { chatId: Long, isPositive: Boolean }
  Response: { feedbackId, chatId, isPositive, status }
  ```
  - MEMBER: 자신의 대화에만 가능
  - ADMIN: 모든 대화에 가능
- [ ] 내 피드백 목록 API
  ```
  GET /api/feedbacks/me?isPositive=true&page=0&size=20&sort=createdAt,desc
  Response: { feedbacks: [{ feedbackId, chatId, question, answer, isPositive, status }], totalPages }
  ```
  - 긍정/부정 필터링
  - 정렬 (createdAt 오름차순/내림차순)
  - 페이지네이션
- [ ] 관리자용 피드백 목록 API
  ```
  GET /api/admin/feedbacks?status=PENDING&isPositive=false&page=0&size=20&sort=createdAt,desc
  Response: { feedbacks: [...], totalPages, totalElements }
  ```
- [ ] 피드백 상태 변경 API (관리자)
  ```
  PATCH /api/admin/feedbacks/{feedbackId}
  Request: { status: "RESOLVED" }
  Response: { feedbackId, status }
  ```

### 7.4 권한 검증
- [ ] 관리자 API에 역할 검증 추가
  ```kotlin
  if (user.role != UserRole.ADMIN) {
      throw ForbiddenException("관리자 권한이 필요합니다")
  }
  ```

---

## 산출물

| 파일 | 설명 |
|------|------|
| `Feedback.kt` | 피드백 엔티티 |
| `FeedbackStatus.kt` | 피드백 상태 enum (PENDING, RESOLVED) |
| `FeedbackRepository.kt` | 피드백 레포지토리 |
| `FeedbackService.kt` | 피드백 비즈니스 로직 |
| `FeedbackController.kt` | 피드백 API |
| `AdminFeedbackController.kt` | 관리자 피드백 API |

---

## 테스트 코드 (생산성 향상 목적)

### 7.5 필수 테스트

| 테스트 | 목적 | 우선순위 |
|--------|------|----------|
| `FeedbackServiceTest` | 중복 방지 및 권한 로직 검증 | P1 |
| `FeedbackControllerTest` | API E2E 성공 케이스 검증 | P1 (필수) |

### 테스트 작성 기준

```
✅ 작성할 테스트:
- 핵심 비즈니스 로직 (중복 체크, 권한)
- **API E2E 통합 테스트 (Happy Path 성공 케이스 필수)**

❌ 생략할 테스트:
- 단순 CRUD
```

---

## Pause 7: 검증 지점

### Swagger UI에서 테스트

```bash
# 1. 피드백 생성 (MEMBER - 자기 대화)
POST /api/feedbacks
Authorization: Bearer {memberToken}
{
  "chatId": 1,
  "isPositive": true
}
# 기대: 201 Created, feedbackId 반환

# 2. 피드백 생성 (MEMBER - 타인 대화)
POST /api/feedbacks
Authorization: Bearer {memberToken}
{
  "chatId": 999,  // 다른 사용자의 대화
  "isPositive": true
}
# 기대: 403 Forbidden

# 3. 피드백 생성 (ADMIN - 타인 대화)
POST /api/feedbacks
Authorization: Bearer {adminToken}
{
  "chatId": 999,  // 다른 사용자의 대화
  "isPositive": false
}
# 기대: 201 Created (관리자는 모든 대화에 가능)

# 4. 중복 피드백 시도
POST /api/feedbacks
Authorization: Bearer {memberToken}
{
  "chatId": 1,
  "isPositive": false
}
# 기대: 409 Conflict

# 5. 내 피드백 목록 (필터링)
GET /api/feedbacks/me?isPositive=true&sort=createdAt,desc
Authorization: Bearer {memberToken}
# 기대: 긍정 피드백만, 최신순 정렬

# 6. 관리자 피드백 조회
GET /api/admin/feedbacks?status=PENDING
Authorization: Bearer {adminToken}
# 기대: 전체 피드백 목록 (필터링)

# 7. 피드백 상태 변경
PATCH /api/admin/feedbacks/1
Authorization: Bearer {adminToken}
{
  "status": "RESOLVED"
}
# 기대: 200 OK, 상태 변경됨

# 8. MEMBER가 상태 변경 시도
PATCH /api/admin/feedbacks/1
Authorization: Bearer {memberToken}
{
  "status": "RESOLVED"
}
# 기대: 403 Forbidden
```

| 검증 항목 | 기대 결과 |
|-----------|-----------|
| MEMBER 피드백 생성 | 자기 대화에만 가능 |
| ADMIN 피드백 생성 | 모든 대화에 가능 |
| 중복 방지 | 동일 사용자+대화 → 409 |
| 다중 사용자 피드백 | 같은 대화에 다른 사용자들 각각 피드백 가능 |
| 필터링/정렬 | isPositive, createdAt 동작 |
| 상태 변경 | ADMIN만 가능 |

---

## 다음 Phase로 진행 조건

- [ ] 피드백 CRUD 동작
- [ ] MEMBER는 자기 대화만, ADMIN은 전체 대화에 피드백 가능
- [ ] 동일 사용자 중복 피드백 방지
- [ ] 관리자 전용 API 권한 검증
- [ ] 필터링 및 페이지네이션 동작
