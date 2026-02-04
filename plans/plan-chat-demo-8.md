# Phase 8: 분석 및 보고 기능 [선택]

> **시간 제한**: 20분
> **선행 조건**: Phase 6 완료 (Phase 7과 병렬 가능)
> **결과 상태**: 관리자가 사용자 활동 기록을 조회하고 CSV 보고서를 다운로드 가능
> **우선순위**: ⚠️ 선택 - 시간이 남으면 구현

---

## 목표

관리자가 하루 동안의 서비스 사용 현황(회원가입, 로그인, 대화 생성 수)을 파악하고, 대화 내역 CSV 보고서를 생성할 수 있는 상태

---

## 작업 항목

### 8.1 사용자 활동 기록 API
- [ ] `AdminController` 생성 (`/api/admin`)
- [ ] 활동 기록 조회 API
  ```
  GET /api/admin/activity
  Response: {
    date: "2026-02-03",
    signupCount: Long,    // 하루 동안 회원가입 수
    loginCount: Long,     // 하루 동안 로그인 수
    chatCount: Long       // 하루 동안 대화 생성 수
  }
  ```
- [ ] **참고**: 요청 시점 기준 하루(당일 00:00 ~ 현재) 동안의 기록

### 8.2 로그인 기록 추가
- [ ] 로그인 횟수 추적을 위한 방법 선택:
  - **방안 1**: `User` 엔티티에 `lastLoginAt`, `loginCount` 필드 추가
  - **방안 2**: 별도 `LoginLog` 테이블 생성
- [ ] `AuthService.login()` 시 로그인 기록 저장

### 8.3 CSV 보고서 생성
- [ ] `ReportService` 생성
- [ ] 대화 내역 CSV 다운로드 API
  ```
  GET /api/admin/reports/chats
  Response: CSV 파일 다운로드
  Content-Type: text/csv
  Content-Disposition: attachment; filename="chats_report_20260203.csv"
  ```
- [ ] CSV 컬럼:
  - 날짜 (createdAt)
  - 사용자 이메일
  - 사용자 이름
  - 질문
  - 답변
- [ ] **범위**: 요청 시점 기준 하루(당일 00:00 ~ 현재) 동안의 대화

### 8.4 권한 검증
- [ ] 모든 `/api/admin/**` 엔드포인트에 ADMIN 권한 필수

---

## 산출물

| 파일 | 설명 |
|------|------|
| `AdminController.kt` | 관리자 API |
| `AdminService.kt` | 활동 기록 조회 로직 |
| `ReportService.kt` | CSV 보고서 생성 |
| `ActivityResponse.kt` | 활동 기록 응답 DTO |
| `LoginLog.kt` (선택) | 로그인 기록 엔티티 |

---

## 테스트 코드 (생산성 향상 목적)

### 8.5 필수 테스트

| 테스트 | 목적 | 우선순위 |
|--------|------|----------|
| `AdminControllerTest` | API E2E 성공 케이스 검증 | P1 (필수) |

### 테스트 작성 기준

```
✅ 작성할 테스트:
- **API E2E 통합 테스트 (Happy Path 성공 케이스 필수)**
- CSV 다운로드 헤더 검증

❌ 생략할 테스트:
- 단순 조회 로직
```

---

## Pause 8: 검증 지점

### Swagger UI에서 테스트

```bash
# 1. 사용자 활동 기록 조회
GET /api/admin/activity
Authorization: Bearer {adminToken}
# 기대: 당일 회원가입, 로그인, 대화 생성 수

# 2. 대화 CSV 다운로드
GET /api/admin/reports/chats
Authorization: Bearer {adminToken}
# 기대: CSV 파일 다운로드

# 3. 일반 사용자 접근
GET /api/admin/activity
Authorization: Bearer {memberToken}
# 기대: 403 Forbidden
```

### CSV 파일 검증

```csv
날짜,사용자이메일,사용자이름,질문,답변
2026-02-03T10:30:00,user@example.com,홍길동,"안녕하세요","안녕하세요! 무엇을 도와드릴까요?"
2026-02-03T10:35:00,user@example.com,홍길동,"오늘 날씨 어때?","..."
```

| 검증 항목 | 기대 결과 |
|-----------|-----------|
| 활동 기록 조회 | 당일 회원가입/로그인/대화 수 |
| CSV 다운로드 | 파일 다운로드, 올바른 포맷 |
| CSV 내용 | 당일 대화만 포함, 사용자 정보 포함 |
| 권한 검증 | MEMBER 역할 접근 차단 |

---

## 다음 Phase로 진행 조건

- [ ] 사용자 활동 기록 조회 동작 (당일 기준)
- [ ] CSV 보고서 다운로드 동작 (당일 기준)
- [ ] ADMIN 권한 검증 동작
