# DevLog — 개발자 커뮤니티 플랫폼

> 개발자들이 기술 포스팅을 쓰고, 댓글·좋아요·팔로우로 소통하는 커뮤니티 플랫폼

---

## 프로젝트 정보

| 항목 | 내용 |
|------|------|
| 프로젝트명 | DevLog |
| 버전 | v0.8.0 |
| 시작일 | 2026-04-22 |
| 마지막 업데이트 | 2026-05-23 |
| 개발 환경 | Java 21 / Spring Boot 4.0.5 |
| DB | MySQL 8.0.45 |
| 캐시 | Redis 3.x |

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security 7.0.4 + JWT (jjwt 0.12.6) |
| ORM | Spring Data JPA + Hibernate 7.2.7 |
| DB | MySQL 8.0.45 |
| Cache | Redis (Lettuce) |
| OAuth2 | GitHub OAuth2 |
| Query | QueryDSL 5.1.0 |
| 실시간 알림 | Spring SseEmitter |
| Build | Gradle |

---

## 패키지 구조

```
src/main/java/com/devlog/
├── auth/
│   ├── controller/       AuthController.java
│   ├── service/          AuthService.java, CustomUserDetailsService.java
│   ├── oauth2/           CustomOAuth2UserService.java, OAuth2SuccessHandler.java
│   └── dto/
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
│   ├── repository/       UserRepository.java, FollowRepository.java
│   ├── service/          UserService.java
│   ├── controller/       UserController.java
│   └── dto/
├── post/
│   ├── domain/           Post.java, PostStatus.java, Tag.java, PostTag.java
│   ├── repository/       PostRepository.java, PostRepositoryImpl.java, TagRepository.java
│   ├── service/          PostService.java, ViewCountScheduler.java
│   ├── controller/       PostController.java
│   └── dto/
├── series/
│   ├── domain/           Series.java, SeriesPost.java
│   ├── repository/       SeriesRepository.java, SeriesPostRepository.java
│   ├── service/          SeriesService.java
│   ├── controller/       SeriesController.java
│   └── dto/
├── comment/
│   ├── domain/           Comment.java, CommentClosure.java
│   ├── repository/
│   ├── service/          CommentService.java
│   ├── controller/       CommentController.java
│   └── dto/
├── like/
│   ├── domain/           Like.java, LikeTarget.java
│   ├── event/            LikeEvent.java
│   ├── repository/
│   ├── service/          LikeService.java
│   └── controller/
├── notification/
│   ├── domain/           Notification.java, NotificationType.java
│   ├── repository/
│   ├── service/          NotificationService.java  # SSE 에미터 관리
│   ├── controller/       NotificationController.java
│   └── dto/
└── BaseTimeEntity.java
```

---

## 진행 현황

### v0.1.0 — User 도메인 + JWT 인증
- [x] User 엔티티 설계 (일반 로그인 / OAuth2 분리 구조)
- [x] BaseTimeEntity (JPA Auditing)
- [x] Follow 엔티티
- [x] JWT Access Token + Refresh Token 발급
- [x] JwtAuthenticationFilter (stateless 인증)
- [x] Redis 블랙리스트 (로그아웃) + Refresh Token Rotation
- [x] 일반 회원가입 / 로그인 API
- [x] 팔로우 / 언팔로우 API
- [x] 프로필 조회 / 수정 API

### v0.2.0 — GitHub OAuth2 소셜 로그인
- [x] CustomOAuth2UserService (신규 가입 / 기존 계정 연동)
- [x] OAuth2SuccessHandler (JWT 발급)
- [x] 이메일 비공개 계정 처리 (noreply 이메일 대체)

### v0.3.0 — Post 도메인
- [x] Post 엔티티 (DRAFT / PUBLISHED 상태)
- [x] Tag / PostTag 엔티티 (다대다)
- [x] QueryDSL 동적 검색 (키워드 / 태그)
- [x] 게시글 CRUD API
- [x] Redis 조회수 캐싱 + ViewCountScheduler (배치 반영)

### v0.4.0 — Comment 도메인
- [x] 클로저 테이블 구조 (Comment / CommentClosure)
- [x] 댓글 CRUD + 대댓글 1단계
- [x] 소프트 딘리트

### v0.5.0 — Like / Notification
- [x] Like 엔티티 (게시글 + 댓글 통합)
- [x] 좋아요 토글 API
- [x] Spring Event 기반 비동기 알림 처리
- [x] 알림 목록 / 읽음 처리 API

### v0.6.0 — 성능 최적화
- [x] N+1 문제 해결 (fetch join)
- [x] QueryDSL fetch join 적용
- [x] 좋아요 수 Redis 캐싱 (10분 TTL)
- [x] open-in-view=false 설정

### v0.7.0 — 시리즈 / SSE 알림 / 팔로우 개선
- [x] **시리즈 기능** — Series / SeriesPost 엔티티, CRUD + 게시글 추가/제거 API
- [x] **SSE 실시간 알림** — SseEmitter 기반 좋아요 알림 즉시 push
- [x] **팔로우 isFollowing** — 프로필 API에 현재 로그인 유저의 팔로우 여부 포함

### v1.0.0 — 배포 `(예정)`
- [ ] Docker 컨테이너화
- [ ] GitHub Actions CI/CD
- [ ] AWS EC2 배포
- [ ] Swagger API 문서 정리

---

## API 목록

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
| GET | /api/users/{username}/profile | 프로필 조회 (isFollowing 포함) | ❌ |
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

### Series
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/series | 시리즈 생성 | ✅ |
| GET | /api/series/{seriesId} | 시리즈 상세 | ❌ |
| PUT | /api/series/{seriesId} | 시리즈 수정 | ✅ |
| DELETE | /api/series/{seriesId} | 시리즈 삭제 | ✅ |
| GET | /api/series/user/{username} | 유저 시리즈 목록 | ❌ |
| POST | /api/series/{seriesId}/posts | 시리즈에 게시글 추가 | ✅ |
| DELETE | /api/series/{seriesId}/posts/{postId} | 시리즈에서 게시글 제거 | ✅ |
| GET | /api/series/post/{postId} | 게시글이 속한 시리즈 | ❌ |

### Comment
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/posts/{postId}/comments | 댓글 작성 | ✅ |
| GET | /api/posts/{postId}/comments | 댓글 목록 조회 | ❌ |
| PUT | /api/comments/{commentId} | 댓글 수정 | ✅ |
| DELETE | /api/comments/{commentId} | 댓글 삭제 | ✅ |

### Like
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/posts/{postId}/likes | 게시글 좋아요 토글 | ✅ |
| GET | /api/posts/{postId}/likes | 게시글 좋아요 수 | ❌ |
| POST | /api/comments/{commentId}/likes | 댓글 좋아요 토글 | ✅ |

### Notification
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | /api/notifications/subscribe | SSE 연결 | ✅ |
| GET | /api/notifications | 알림 목록 | ✅ |
| GET | /api/notifications/unread-count | 읽지 않은 알림 수 | ✅ |
| PATCH | /api/notifications/{id}/read | 알림 읽음 처리 | ✅ |

---

## 로컈 실행 방법

### 사전 준비
- Java 21
- MySQL 8.x — 스키마명 `sns`
- Redis (로컈 실행)

### Redis 실행
```bash
redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru
```

### application.properties 설정
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sns?useSSL=false&serverTimezone=Asia/Seoul
spring.datasource.username=root
spring.datasource.password=****

jwt.secret=your-secret-key-minimum-256-bits
jwt.access-token-expiry=3600000
jwt.refresh-token-expiry=604800000

spring.data.redis.host=localhost
spring.data.redis.port=6379

spring.security.oauth2.client.registration.github.client-id=****
spring.security.oauth2.client.registration.github.client-secret=****
spring.security.oauth2.client.registration.github.redirect-uri=http://localhost:8080/login/oauth2/code/github
```

### 빌드 및 실행
```bash
./gradlew bootRun
```

---

## 트러블슈팅

### 1. @Id import 오류
**원인** IntelliJ 자동완성이 `org.springframework.data.annotation.Id`를 먼저 제안.
```java
// ❌ import org.springframework.data.annotation.Id;
// ✅ import jakarta.persistence.Id;
```

### 2. Hibernate dialect 오류
**원인** Spring Boot 3.x+ 에서 MySQL8Dialect 제거됨. `application.properties`에서 dialect 설정 전체 삭제.

### 3. Post 태그 수정 시 중복 키 에러
**원인** `clearTags()` 후 orphanRemoval이 즉시 반영 안 됨.
```java
post.clearTags();
entityManager.flush(); // 에 정 후 재삽입
```

### 4. GitHub OAuth2 — email null
**원인** GitHub 이메일 비공개 설정 시 API 응답에서 email null.
```java
if (email == null) email = getLogin() + "@users.noreply.github.com";
```

### 5. LazyInitializationException — OAuth2 로그인 시
**원인** `OAuth2UserAdapter.getAuthorities()`에서 트랜짝션 밖에서 `user.getRole()` 호출.
```java
// 트랜짝션 안에서 role 미리 추출 후 전달
String role = user.getRole().name();
```

### 6. Redis Connection refused
**해결** `redis-server` 먼저 실행 후 `redis-cli ping`으로 PONG 확인.

### 7. /api/users/signup 401
**해결** SecurityConfig `permitAll()` 목록에 `/api/users/signup` 추가.

---

## 업데이트 로그

| 버전 | 날짜 | 내용 |
|------|------|------|
| v0.8.0 | 2026-05-23 | README 업데이트 |
| v0.7.0 | 2026-05-23 | 시리즈 기능, SSE 실시간 알림, 팔로우 isFollowing |
| v0.6.0 | 2026-05-10 | 성능 최적화 (N+1 해결, Redis 캐싱) |
| v0.5.0 | 2026-05-10 | Like / Notification (Spring Event 비동기 알림) |
| v0.4.0 | 2026-05-10 | Comment 도메인 (클로저 테이블, 소프트 딘리트) |
| v0.3.0 | 2026-05-08 | Post 도메인, QueryDSL 검색, Redis 조회수 캐싱 |
| v0.2.0 | 2026-04-22 | GitHub OAuth2 소셜 로그인 |
| v0.1.0 | 2026-04-22 | User 도메인, JWT 인증, Redis 연동 |
