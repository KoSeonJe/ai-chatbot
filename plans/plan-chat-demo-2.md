# Phase 2: 인증 기능 (경량화)

> **시간 제한**: 25분
> **선행 조건**: Phase 1 완료
> **결과 상태**: 회원가입 → 로그인 → JWT 토큰 발급 및 검증 동작

---

## 목표

Spring Security 없이 JWT 기반 인증이 동작하여, 보호된 API 호출 시 토큰 검증이 수행되는 상태

---

## 작업 항목

### 2.1 User 도메인
- [ ] `User` 엔티티 생성
  ```kotlin
  @Entity
  @Table(name = "users")
  class User(
      @Id @GeneratedValue
      val id: Long = 0,
      @Column(unique = true)
      val email: String,
      val password: String,  // BCrypt 해시
      val name: String,
      @Enumerated(EnumType.STRING)
      val role: UserRole = UserRole.MEMBER,
      val createdAt: OffsetDateTime = OffsetDateTime.now()
  )

  enum class UserRole { MEMBER, ADMIN }
  ```
- [ ] `UserRepository` 인터페이스 (JpaRepository)

### 2.2 JWT 유틸리티
- [ ] `build.gradle.kts`에 `jjwt` 의존성 추가
- [ ] `JwtProvider` 클래스 생성
  - `generateToken(userId: Long, email: String, role: UserRole): String`
  - `validateToken(token: String): Boolean`
  - `getUserIdFromToken(token: String): Long`
  - 만료 시간: 24시간
  - Secret key: 환경변수 또는 application.yml

### 2.3 JWT 필터 (Spring Security 없이)
- [ ] `JwtAuthenticationFilter` (OncePerRequestFilter 상속)
  - Authorization 헤더에서 Bearer 토큰 추출
  - 토큰 유효성 검증
  - 유효하면 `AuthUser` 객체를 request attribute에 저장
  - 화이트리스트 경로 제외: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/h2-console/**`
- [ ] `FilterConfig`에서 필터 등록

### 2.4 ArgumentResolver
- [ ] `AuthUser` 데이터 클래스 (userId, email, role)
- [ ] `@CurrentUser` 어노테이션
- [ ] `CurrentUserArgumentResolver` 구현
- [ ] `WebMvcConfig`에 ArgumentResolver 등록

### 2.5 인증 API
- [ ] `AuthController` 생성 (`/api/auth`)
- [ ] 회원가입 API
  ```
  POST /api/auth/signup
  Request: { email, password, name }
  Response: { userId, email, name }
  ```
- [ ] 로그인 API
  ```
  POST /api/auth/login
  Request: { email, password }
  Response: { accessToken, tokenType: "Bearer" }
  ```
- [ ] `AuthService` 구현
  - 비밀번호: BCryptPasswordEncoder 사용

---

## 산출물

| 파일 | 설명 |
|------|------|
| `User.kt` | 사용자 엔티티 |
| `UserRepository.kt` | JPA 레포지토리 |
| `JwtProvider.kt` | JWT 생성/검증 |
| `JwtAuthenticationFilter.kt` | 토큰 검증 필터 |
| `CurrentUser.kt` | 어노테이션 |
| `CurrentUserArgumentResolver.kt` | 인증 사용자 주입 |
| `AuthController.kt` | 인증 API |
| `AuthService.kt` | 인증 비즈니스 로직 |

---

## 테스트 코드 (생산성 향상 목적)

> ⚠️ 3시간 제한: 전체 커버리지가 아닌 **핵심 로직 검증**에 집중
> 💡 수동 테스트 반복을 줄이고, 리팩토링 안전망 확보

### 2.6 필수 테스트

| 테스트 | 목적 | 우선순위 |
|--------|------|----------|
| `AuthServiceTest` | 회원가입/로그인 로직 검증 | P1 |
| `JwtProviderTest` | 토큰 생성/검증 로직 검증 | P1 |
| `AuthControllerTest` | API E2E 성공 케이스 검증 | P1 (필수) |

```kotlin
// AuthControllerTest.kt - E2E 성공 케이스
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    @Test
    fun `회원가입 성공 E2E`() {
        val request = SignupRequest("e2e@test.com", "password", "E2E유저")
        mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.email").value("e2e@test.com"))
    }
}
```

```kotlin
// AuthServiceTest.kt - 핵심 로직만 테스트
@SpringBootTest
class AuthServiceTest {
    @Autowired lateinit var authService: AuthService
    @Autowired lateinit var userRepository: UserRepository

    @Test
    fun `회원가입 성공`() {
        val result = authService.signup("test@test.com", "password", "테스트")
        assertThat(result.email).isEqualTo("test@test.com")
    }

    @Test
    fun `중복 이메일 가입 실패`() {
        authService.signup("dup@test.com", "password", "유저1")
        assertThrows<BusinessException> {
            authService.signup("dup@test.com", "password", "유저2")
        }
    }

    @Test
    fun `로그인 성공 시 토큰 반환`() {
        authService.signup("login@test.com", "password", "테스트")
        val token = authService.login("login@test.com", "password")
        assertThat(token.accessToken).isNotBlank()
    }
}
```

```kotlin
// JwtProviderTest.kt - 토큰 생성/검증
@SpringBootTest
class JwtProviderTest {
    @Autowired lateinit var jwtProvider: JwtProvider

    @Test
    fun `토큰 생성 및 검증`() {
        val token = jwtProvider.generateToken(1L, "test@test.com", UserRole.MEMBER)
        assertThat(jwtProvider.validateToken(token)).isTrue()
        assertThat(jwtProvider.getUserIdFromToken(token)).isEqualTo(1L)
    }
}
```

### 테스트 작성 기준

```
✅ 작성할 테스트:
- 핵심 비즈니스 로직 (회원가입, 로그인)
- 반복 수동 테스트가 필요한 부분 (토큰 생성/검증)
- 버그 발생 시 디버깅이 오래 걸리는 부분
- **API E2E 통합 테스트 (Happy Path 성공 케이스 필수)**

❌ 생략할 테스트:
- Repository 단순 CRUD (JPA가 보장)
- 예외 케이스 전체 (Happy path 우선)
```

---

## Pause 2: 검증 지점

### Swagger UI에서 테스트

```bash
# 1. 회원가입
POST /api/auth/signup
{
  "email": "test@example.com",
  "password": "password123",
  "name": "테스트유저"
}
# 기대: 200 OK, userId 반환

# 2. 로그인
POST /api/auth/login
{
  "email": "test@example.com",
  "password": "password123"
}
# 기대: 200 OK, accessToken 반환

# 3. 보호된 API 호출 (토큰 없이)
GET /api/chats
# 기대: 401 Unauthorized

# 4. 보호된 API 호출 (토큰 포함)
GET /api/chats
Authorization: Bearer {accessToken}
# 기대: 200 OK (또는 빈 배열)
```

| 검증 항목 | 기대 결과 |
|-----------|-----------|
| 회원가입 | 사용자 생성, userId 반환 |
| 중복 이메일 가입 | 409 Conflict |
| 로그인 성공 | JWT 토큰 반환 |
| 잘못된 비밀번호 | 401 Unauthorized |
| 토큰 없이 API 호출 | 401 Unauthorized |
| 유효 토큰으로 API 호출 | 정상 응답 |

---

## 다음 Phase로 진행 조건

- [ ] 회원가입 동작
- [ ] 로그인 시 JWT 발급
- [ ] JWT 필터가 보호된 API를 차단
- [ ] `@CurrentUser`로 인증 사용자 정보 주입 가능
