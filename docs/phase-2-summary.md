# Phase 2: 인증 기능 구현 완료

## 핵심 구현 사항
- [x] User 엔티티 및 UserRepository
- [x] JWT 유틸리티 (JwtProvider - 생성/검증/파싱)
- [x] JWT 인증 필터 (JwtAuthenticationFilter)
- [x] @CurrentUser 어노테이션 및 ArgumentResolver
- [x] 회원가입/로그인 API (AuthController, AuthService)
- [x] 테스트 코드 (JwtProviderTest, AuthServiceTest, AuthControllerTest)

## 생성된 파일
| 파일 | 설명 |
|------|------|
| `User.kt` | 사용자 엔티티 |
| `UserRepository.kt` | JPA 레포지토리 |
| `AuthDtos.kt` | 요청/응답 DTO |
| `AuthService.kt` | 인증 비즈니스 로직 |
| `AuthController.kt` | 인증 API 엔드포인트 |
| `JwtProvider.kt` | JWT 생성/검증 |
| `JwtAuthenticationFilter.kt` | 토큰 검증 필터 |
| `AuthUser.kt` | 인증 사용자 정보 |
| `CurrentUser.kt` | 어노테이션 |
| `CurrentUserArgumentResolver.kt` | 인증 사용자 주입 |
| `FilterConfig.kt` | 필터 등록 |
| `WebMvcConfig.kt` | ArgumentResolver 등록 |

## API 엔드포인트
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 발급) |

## 테스트 방법
```bash
# Swagger UI: http://localhost:8080/swagger-ui.html

# 1. 회원가입
POST /api/auth/signup
{
  "email": "test@example.com",
  "password": "password123",
  "name": "테스트유저"
}

# 2. 로그인
POST /api/auth/login
{
  "email": "test@example.com",
  "password": "password123"
}
# → accessToken 발급

# 3. Authorize 버튼 클릭 → 토큰 입력
```

## 다음 단계
Phase 3: 대화 기능 (기본) 구현
