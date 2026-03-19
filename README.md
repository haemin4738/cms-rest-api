# CMS REST API

Spring Boot 기반의 간단한 CMS(Content Management System) REST API입니다.

---

## 프로젝트 실행 방법

### 사전 요구사항
- Java 25 이상
- Gradle

### 실행
```bash
./gradlew bootRun
```

윈도우의 경우:
```bash
gradlew.bat bootRun
```

## 테스트 실행
```bash
./gradlew test
```

| 테스트 클래스 | 설명 |
|------|------|
| `ContentServiceTest` | 서비스 레이어 단위 테스트 (Mockito) |
| `ContentControllerTest` | CRUD 및 권한 검증 통합 테스트 (MockMvc) |
| `AuthControllerTest` | 로그인 / 회원가입 통합 테스트 (MockMvc) |


### 접속 주소

| 항목 | URL |
|------|-----|
| API 서버 | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

### H2 Console 접속 정보
- JDBC URL: `jdbc:h2:mem:test`
- Username: `sa`
- Password: *(없음)*

---

## 로그인 방식

**JWT (JSON Web Token) Bearer 인증** 방식을 사용합니다.

1. `POST /api/auth/register` 로 회원가입
2. `POST /api/auth/login` 으로 로그인 후 `accessToken` 발급
3. 이후 인증이 필요한 API 호출 시 헤더에 포함
```
   Authorization: Bearer <accessToken>
```
4. 토큰 유효 기간: **24시간**

---

## 구현 내용

### 콘텐츠 CRUD
| 기능 | 메서드 | URL |
|------|--------|-----|
| 콘텐츠 생성 | POST | `/api/contents` |
| 콘텐츠 목록 조회 (페이징) | GET | `/api/contents` |
| 검색 + 정렬 함께 | `GET /api/contents?title=스프링&sort=viewCount,desc` |
| 콘텐츠 상세 조회 | GET | `/api/contents/{id}` |
| 콘텐츠 수정 | PUT | `/api/contents/{id}` |
| 콘텐츠 삭제 | DELETE | `/api/contents/{id}` |

### 로그인 / 회원가입
| 기능 | 메서드 | URL |
|------|--------|-----|
| 로그인 | POST | `/api/auth/login` |
| 회원가입 | POST | `/api/auth/register` |

### 접근 권한
| API | 비인증 | USER | ADMIN |
|-----|--------|------|-------|
| 로그인 / 회원가입 | ✅ | ✅ | ✅ |
| 콘텐츠 목록 / 상세 조회 | ✅ | ✅ | ✅ |
| 콘텐츠 생성 | ❌ | ✅ | ✅ |
| 콘텐츠 수정 / 삭제 | ❌ | 본인만 | ✅ 전체 |

---

## 구현 기능

| 기능 | 설명 |
|------|------|
| 회원가입 API | `POST /api/auth/register` — 가입 후 JWT 자동 발급 |
| 제목 검색 | `GET /api/contents?title=검색어` — 부분 일치, 대소문자 무시 |
| 조회수 자동 증가 | 상세 조회 시마다 `view_count` + 1 |
| JPA Auditing | 생성자 / 수정자 / 생성일 / 수정일 자동 기록 |
| 공통 응답 포맷 | `ApiResponse<T>` — success, message, data 구조 통일 |
| 전역 예외 처리 | `GlobalExceptionHandler` — 에러 응답 일관 처리 |
| Swagger UI | `/swagger-ui.html` — Bearer 토큰 인증 포함 문서화 |
| p6spy | SQL 쿼리 로깅 |
| 입력값 검증 | `@Valid` + Hibernate Validator — title 빈값, 100자 초과 시 400 반환 |
| 토큰 만료 에러 개선 | `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler` — 401/403 JSON 응답 |
| 정렬 옵션 | `sort=viewCount,desc` 등 URL 파라미터로 정렬 가능 |


---

## DB Schema
```sql
CREATE TABLE users (
    id       BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL
);

CREATE TABLE contents (
    id                 BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    title              VARCHAR(100) NOT NULL,
    description        TEXT,
    view_count         BIGINT NOT NULL DEFAULT 0,
    created_date       TIMESTAMP,
    created_by         VARCHAR(50) NOT NULL,
    last_modified_date TIMESTAMP,
    last_modified_by   VARCHAR(50)
);
```

---

## 프로젝트 구조
```
src/main/java/com/malgn/
├── domain/
│   ├── user/
│   │   ├── controller/   # AuthController
│   │   ├── dto/          # AuthDto
│   │   ├── entity/       # User
│   │   ├── repository/   # UserRepository
│   │   └── service/      # AuthService
│   └── content/
│       ├── controller/   # ContentController
│       ├── dto/          # ContentDto
│       ├── entity/       # Content
│       ├── repository/   # ContentRepository
│       └── service/      # ContentService
├── configure/
│   ├── security/         # SecurityConfiguration
│   ├── AppConfiguration  # JPA Auditing
│   └── SwaggerConfiguration
|   
└── global/
    ├── dto/              # ApiResponse
    ├── exception/        # GlobalExceptionHandler
    └── security/         # JWT 관련 클래스
```

---

## 사용한 AI 도구

| 도구 | 활용 방식 |
|------|----------|
| Claude | 프로젝트 구현 보조. 에러 해결에 활용. 생성된 코드는 검토 후 적용. |
| Spring 공식 문서 | Spring Boot, JPA 사용 참조. |
