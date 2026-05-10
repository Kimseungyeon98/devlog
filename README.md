# DevLog — 개발자 커뮤니티 플랫폼

> 개발자들이 기술 포스팅을 쓰고, 댓글·좋아요·팔로우로 소통하는 커뮤니티 플랫폼

---

## 📌 프로젝트 정보

| 항목 | 내용 |
|------|------|
| 프로젝트명 | DevLog |
| 버전 | v0.3.0 |
| 시작일 | 2026-04-22 |
| 마지막 업데이트 | 2026-05-08 |
| 개발 환경 | Java 21 / Spring Boot 4.0.5 |
| DB | MySQL 8.0.45 |
| 캐시 | Redis 3.x (Windows) |

---

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security 7.0.4 + JWT (jjwt 0.12.6) |
| ORM | Spring Data JPA + Hibernate 7.2.7 |
| DB | MySQL 8.0.45 |
| Cache | Redis (Lettuce) |
| OAuth2 | GitHub OAuth2 (spring-boot-starter-oauth2-client) |
| Query | QueryDSL 5.1.0 |
| Docs | SpringDoc OpenAPI (Swagger) 2.8.6 |
| Build | Gradle |

---

## 🗂 패키지 구조

```
src/main/java/com/devlog/
├── auth/
│   ├── controller/       AuthController.java
│   ├── service/          AuthService.java, CustomUserDetailsService.java
│   ├── oauth2/           CustomOAuth2UserService.java, OAuth2SuccessHandler.java
│   │                     OAuth2UserAdapter.java, GithubUserInfo.java
│   └── dto/
│       ├── request/      LoginRequest.java
│       └── response/     TokenResponse.java
├── jwt/
│   ├── JwtProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   └── JwtAccessDeniedHandler.java
├── config/
│   ├── SecurityConfig.java
│   └── QueryDslConfig.java
├── user/
│   ├── domain/           User.java, UserRole.java, Follow.java, UserAuthProvider.java
│   ├── repository/       UserRepository.java, FollowRepository.java, UserAuthProviderRepository.java
│   ├── service/          UserService.java
│   ├── controller/       UserController.java
│   └── dto/
│       ├── request/      SignUpRequest.java, UpdateProfileRequest.java
│       └── response/     UserProfileResponse.java
├── post/
│   ├── domain/           Post.java, PostStatus.java, Tag.java, PostTag.java
│   ├── repository/       PostRepository.java, PostRepositoryCustom.java
│   │                     PostRepositoryImpl.java, TagRepository.java, PostTagRepository.java
│   ├── service/          PostService.java, ViewCountScheduler.java
│   ├── controller/       PostController.java
│   └── dto/
│       ├── request/      CreatePostRequest.java, UpdatePostRequest.java, PostSearchRequest.java
│       └── response/     PostResponse.java, PostSummaryResponse.java
└── BaseTimeEntity.java
```

---

## 🚀 진행 현황

### v0.1.0 — User 도메인 + JWT 인증
- [x] User 엔티티 설계 (일반 로그인 / OAuth2 분리 구조)
- [x] BaseTimeEntity (JPA Auditing)
- [x] Follow 엔티티
- [x] UserAuthProvider 엔티티 (소셜 로그인 연동)
- [x] JWT Access Token + Refresh Token 발급
- [x] JwtAuthenticationFilter (stateless 인증)
- [x] Redis 블랙리스트 (로그아웃)
- [x] Refresh Token Rotation (Redis 저장)
- [x] Spring Security 설정 (CSRF disable, Stateless)
- [x] 일반 회원가입 / 로그인 API
- [x] 팔로우 / 언팔로우 API
- [x] 프로필 조회 / 수정 API

### v0.2.0 — GitHub OAuth2 소셜 로그인
- [x] GitHub OAuth App 등록
- [x] CustomOAuth2UserService (신규 가입 / 기존 계정 연동)
- [x] OAuth2SuccessHandler (JWT 발급)
- [x] 이메일 비공개 계정 처리 (noreply 이메일 대체)

### v0.3.0 — Post 도메인
- [x] Post 엔티티 설계 (DRAFT / PUBLISHED 상태)
- [x] Tag / PostTag 엔티티 설계 (다대다)
- [x] QueryDSL 동적 검색 (키워드 / 태그)
- [x] 게시글 작성 / 수정 / 삭제 API
- [x] 게시글 목록 조회 (페이징)
- [x] Redis 조회수 캐싱 + 배치 반영 (ViewCountScheduler)

### v0.4.0 — Comment 도메인 `(예정)`
- [ ] 중첩 댓글 구조 설계 (인접 리스트 vs 클로저 테이블 검토)
- [ ] 댓글 작성 / 수정 / 삭제 API

### v0.5.0 — Like / Notification `(예정)`
- [ ] 게시글 좋아요
- [ ] 비동기 알림 (Spring Event or @Async)

### v0.6.0 — 성능 최적화 `(예정)`
- [ ] N+1 문제 해결
- [ ] 추가 Redis 캐싱 전략

### v1.0.0 — 배포 `(예정)`
- [ ] Docker 컨테이너화
- [ ] GitHub Actions CI/CD
- [ ] AWS EC2 배포
- [ ] Swagger API 문서 정리

---

## ⚙️ 로컬 실행 방법

### 사전 준비
- Java 21
- MySQL 8.x — 스키마명 `sns`
- Redis (로컬 실행)

### Redis 실행
```bash
redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru
```

### application.properties 설정
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sns?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=****

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

jwt.secret=your-secret-key-minimum-256-bits
jwt.access-token-expiry=3600000
jwt.refresh-token-expiry=604800000

spring.data.redis.host=localhost
spring.data.redis.port=6379

spring.security.oauth2.client.registration.github.client-id=****
spring.security.oauth2.client.registration.github.client-secret=****
spring.security.oauth2.client.registration.github.scope=user:email,read:user
spring.security.oauth2.client.registration.github.redirect-uri=http://localhost:8080/login/oauth2/code/github
```

### 빌드 및 실행
```bash
./gradlew bootRun
```

---

## 📡 API 목록

### Auth
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/auth/login | 로그인 | ❌ |
| POST | /api/auth/reissue | Access Token 재발급 | ❌ |
| POST | /api/auth/logout | 로그아웃 | ✅ |
| GET | /oauth2/authorization/github | GitHub 소셜 로그인 | ❌ |

### User
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/users/signup | 회원가입 | ❌ |
| GET | /api/users/{username}/profile | 프로필 조회 | ❌ |
| GET | /api/users/me | 내 정보 조회 | ✅ |
| PUT | /api/users/me | 프로필 수정 | ✅ |
| POST | /api/users/{followingId}/follow | 팔로우 | ✅ |
| DELETE | /api/users/{followingId}/follow | 언팔로우 | ✅ |

### Post
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/posts | 게시글 작성 | ✅ |
| GET | /api/posts | 게시글 목록 검색 | ❌ |
| GET | /api/posts/{postId} | 게시글 단건 조회 | ❌ |
| GET | /api/posts/me | 내 게시글 목록 | ✅ |
| PUT | /api/posts/{postId} | 게시글 수정 | ✅ |
| DELETE | /api/posts/{postId} | 게시글 삭제 | ✅ |

---

### 1. @Id import 오류 — 기본 키 인식 실패
**에러** `지속성 엔티티 'User'에는 기본 키가 포함되어야 합니다.`

**원인** IntelliJ 자동완성이 `org.springframework.data.annotation.Id`를 먼저 제안해 잘못 import됨.

**해결**
```java
// ❌ import org.springframework.data.annotation.Id;
// ✅ import jakarta.persistence.Id;
```

---

### 2. Hibernate dialect 오류 — MySQL8Dialect 클래스 없음
**에러** `Unable to resolve name [org.hibernate.dialect.MySQL8Dialect]`

**원인** Spring Boot 3.x + Hibernate 6.x부터 dialect 자동 감지. `MySQL8Dialect` 클래스 제거됨.

**해결** `application.properties`에서 dialect 설정 줄 삭제.
```properties
# ❌ 삭제
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

---

### 3. follow 테이블 — updated_at 컬럼 누락
**에러** `Schema validation: missing column [updated_at] in table [follow]`

**원인** Follow 엔티티가 BaseTimeEntity 상속 중인데 DDL에 `updated_at` 컬럼 누락.

**해결**
```sql
ALTER TABLE follow ADD COLUMN updated_at DATETIME NOT NULL
  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
```

---

### 4. bio 컬럼 타입 불일치
**에러** `Schema validation: wrong column type in [bio]; found [text] but expecting [longtext]`

**원인** `@Lob` 사용 시 Hibernate 6.x는 CLOB(LONGTEXT)을 기대하지만 DDL은 TEXT.

**해결**
```java
// ❌ @Lob
// ✅ @Column(columnDefinition = "TEXT")
private String bio;
```

---

### 5. UserAuthProvider — created_at null 저장 실패
**에러** `Column 'created_at' cannot be null [insert into user_auth_providers]`

**원인** `@CreatedDate` 사용했지만 `@EntityListeners` 누락으로 Auditing 미동작.

**해결**
```java
@EntityListeners(AuditingEntityListener.class)
public class UserAuthProvider { ... }
```

---

### 6. JwtAccessDeniedHandler — handle() 구현 오류
**에러** `abstract 메서드 'handle()' 구현 필요`

**원인** Spring Boot 3.x는 `javax.servlet` → `jakarta.servlet` 사용. import 불일치 시 시그니처 미일치.

**해결**
```java
// ❌ import javax.servlet.http.HttpServletRequest;
// ✅ import jakarta.servlet.http.HttpServletRequest;
```

---

### 7. JpaRepository import 오류
**에러** 자동완성에 `org.hibernate.*` 내부 클래스만 표시.

**원인** Gradle sync 미완료 또는 IntelliJ 캐시 문제.

**해결** `File → Invalidate Caches → Invalidate and Restart` 후 Gradle 새로고침.

---

### 8. /api/users/signup 401 Unauthorized
**원인** SecurityConfig `permitAll()` 목록에 `/api/users/signup` 미포함.

**해결**
```java
.requestMatchers("/api/auth/**", "/api/users/signup", "/api/users/*/profile").permitAll()
```

---

### 9. /api/auth/login 401 — UserDetailsService 미구현
**에러** 로그에 `Using generated security password` 경고 출력.

**원인** `UserDetailsService` 구현체 없어서 Spring Security가 기본 `inMemoryUserDetailsManager` 등록.

**해결** `CustomUserDetailsService` 구현 후 `@Service` 등록.

---

### 10. GitHub OAuth2 — 404 오류
**원인** GitHub OAuth App Callback URL 불일치 또는 Client ID 오기입.

**해결** Callback URL 수정.
```
# GitHub OAuth App 설정
Authorization callback URL: http://localhost:8080/login/oauth2/code/github
```

---

### 11. GitHub OAuth2 — email null 저장 실패
**에러** `Column 'email' cannot be null`

**원인** GitHub 이메일 비공개 설정 시 API 응답에서 email이 null.

**해결**
```java
if (email == null) {
    email = getLogin() + "@users.noreply.github.com";
}
```

---

### 12. Redis Connection refused
**에러** `java.net.ConnectException: Connection refused`

**원인** Redis 서버 미실행 상태에서 Refresh Token 저장 시도.

**해결**
```bash
redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru
redis-cli ping  # PONG 확인
```

---

### 13. LazyInitializationException — OAuth2 로그인 시 getRole() 실패
**에러** `LazyInitializationException: Could not initialize proxy [User#2] — no session`

**원인** `OAuth2UserAdapter.getAuthorities()`에서 `user.getRole()` 호출 시 트랜잭션 밖이라 Lazy 프록시 초기화 불가.

**해결** `@Transactional` 안에서 role 미리 추출 후 전달.
```java
String role = user.getRole().name(); // 트랜잭션 안에서 추출
return new OAuth2UserAdapter(user, attributes, role);
```

---

### 14. Springdoc + QueryDSL 버전 충돌
**에러** `Failed to introspect Class [QuerydslPredicateOperationCustomizer]`

**원인** springdoc 2.5.0이 QueryDSL의 `TypeInformation` 클래스를 찾지 못함.

**해결** springdoc 버전 업그레이드.
```groovy
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6'
```

---

### 15. Post 태그 수정 시 중복 키 에러
**에러** `Duplicate entry '1-1' for key 'post_tags.uk_post_tags'`

**원인** `clearTags()` 후 `orphanRemoval`이 즉시 반영되지 않아 같은 태그 재삽입 시 중복 발생.

**해결** `clearTags()` 후 `entityManager.flush()`로 즉시 DB 반영.
```java
post.clearTags();
entityManager.flush();
applyTags(post, request.tags());
```

---

### 16. Redis 비밀번호 설정 충돌 — 로컬 환경
**에러** `ERR Client sent AUTH, but no password is set`

**원인** `application-local.properties`에 `REDIS_PASSWORD`가 설정되어 있지만 로컬 Redis는 비밀번호 없음.

**해결** `application-local.properties`에서 `spring.data.redis.password` 주석 처리.

---

## 📝 업데이트 로그

| 버전 | 날짜 | 내용 |
|------|------|------|
| v0.3.0 | 2026-05-08 | Post 도메인, QueryDSL 검색, Redis 조회수 캐싱 |
| v0.2.0 | 2026-04-22 | GitHub OAuth2 소셜 로그인 연동 |
| v0.1.0 | 2026-04-22 | User 도메인, JWT 인증, Redis 연동 |
