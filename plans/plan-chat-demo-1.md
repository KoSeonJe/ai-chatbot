# Phase 1: 프로젝트 초기화

> **시간 제한**: 20분
> **선행 조건**: 없음
> **결과 상태**: Swagger UI에서 API 문서 확인 가능, H2 콘솔 접속 가능

---

## 목표

Spring Boot 애플리케이션이 구동되고, Swagger UI(`/swagger-ui.html`)와 H2 Console(`/h2-console`)에 접속할 수 있는 상태

---

## 작업 항목

### 1.1 의존성 설정
- [ ] `build.gradle.kts`에 Spring Boot 플러그인 추가
- [ ] 의존성 추가:
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-validation`
  - `springdoc-openapi-starter-webmvc-ui` (Swagger)
  - `h2` (런타임)
  - `jackson-module-kotlin`

### 1.2 애플리케이션 설정
- [ ] `application.yml` 생성
  - 서버 포트: 8080
  - H2 콘솔 활성화
  - JPA ddl-auto: create-drop
  - OpenAPI 정보 설정

### 1.3 패키지 구조 생성
- [ ] 베이스 패키지: `com.example.chatbot`
- [ ] 하위 패키지 구조:
  ```
  ├── config/
  ├── domain/
  │   ├── user/
  │   ├── thread/
  │   ├── chat/
  │   └── feedback/
  ├── common/
  │   ├── exception/
  │   └── response/
  └── infra/
      └── openai/
  ```

### 1.4 공통 응답/예외 처리
- [ ] `ApiResponse<T>` 공통 응답 클래스
- [ ] `ErrorResponse` 에러 응답 클래스
- [ ] `BusinessException` 커스텀 예외
- [ ] `GlobalExceptionHandler` (@RestControllerAdvice)

### 1.5 Swagger 설정
- [ ] `SwaggerConfig` 클래스 생성
- [ ] API 제목: "AI Chatbot Demo API"
- [ ] 설명: "VIP 고객 시연용 AI 챗봇 API"
- [ ] JWT Bearer 인증 스킴 설정

---

## 산출물

| 파일 | 설명 |
|------|------|
| `build.gradle.kts` | Spring Boot 의존성 포함 |
| `application.yml` | 서버/DB/Swagger 설정 |
| `ChatbotApplication.kt` | 메인 클래스 |
| `SwaggerConfig.kt` | Swagger 설정 |
| `ApiResponse.kt` | 공통 응답 |
| `GlobalExceptionHandler.kt` | 전역 예외 처리 |

---

## Pause 1: 검증 지점

```bash
# 1. 애플리케이션 구동
./gradlew bootRun

# 2. 확인 항목
```

| 검증 항목 | 확인 방법 | 기대 결과 |
|-----------|-----------|-----------|
| 서버 구동 | `http://localhost:8080` | 404 (정상) |
| Swagger UI | `http://localhost:8080/swagger-ui.html` | API 문서 페이지 표시 |
| H2 Console | `http://localhost:8080/h2-console` | DB 콘솔 로그인 화면 |

---

## 다음 Phase로 진행 조건

- [x] 서버가 정상 구동됨
- [x] Swagger UI 접속 가능
- [x] H2 Console 접속 가능

<!-- 확장 포인트: 프로덕션 환경에서는 PostgreSQL/MySQL로 전환, 환경별 profile 분리 필요 -->
