# DevLog

개발자를 위한 블로그 플랫폼. 게시글 작성, 코드 스니펫 공유, 시리즈 관리, 실시간 알림 기능을 제공합니다.

```
devlog/
├── backend/      Spring Boot 백엔드 API 서버
├── frontend/     React 프론트엔드
└── docker-compose.yml
```

---

## Backend

### 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| ORM | Spring Data JPA + QueryDSL |
| DB | MySQL 8 |
| Cache | Redis |
| Auth | JWT + OAuth2 (GitHub) |
| Test | JUnit 5 + Mockito |

### 주요 기능

- **게시글** — 마크다운 작성, 태그, 임시저장, 발행
- **댓글** — Closure Table 기반 1단계 대댓글
- **시리즈** — 게시글 묶음 관리 및 순서 조정
- **좋아요** — 게시글·댓글 좋아요 (Redis 캐싱)
- **알림** — SSE 실시간 알림 (좋아요, 댓글, 팔로워 발행)
- **코드 스니펫** — 언어별 코드 조각 공유 및 검색
- **통계** — 게시글별 조회수·좋아요 집계 대시보드
- **팔로우** — 유저 팔로우/언팔로우
- **이미지 업로드** — 서버 로컬 저장 후 URL 반환

### API 구조

```
/api/auth          로그인, 회원가입, 토큰 재발급
/api/posts         게시글 CRUD, 검색
/api/comments      댓글 CRUD
/api/series        시리즈 CRUD
/api/likes         좋아요 토글
/api/snippets      코드 스니펫 CRUD, 검색
/api/notifications 알림 목록, SSE 구독
/api/users         프로필, 팔로우
/api/stat          통계 대시보드
/api/images        이미지 업로드
```

### 로컬 개발 환경 설정

**1. 환경변수 파일 생성**

`backend/src/main/resources/application-local.properties` 파일을 직접 생성합니다 (`.gitignore` 적용):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/devlog?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=<db_username>
spring.datasource.password=<db_password>
spring.jpa.hibernate.ddl-auto=update

jwt.secret=<256비트 이상의 랜덤 문자열>
jwt.access-token-expiry=3600000
jwt.refresh-token-expiry=604800000

spring.data.redis.host=localhost
spring.data.redis.port=6379

spring.security.oauth2.client.registration.github.client-id=<github_client_id>
spring.security.oauth2.client.registration.github.client-secret=<github_client_secret>
spring.security.oauth2.client.registration.github.redirect-uri=http://localhost:8080/login/oauth2/code/github
```

**2. 사전 준비**

- MySQL 8, Redis 실행
- `SPRING_PROFILES_ACTIVE=local` 환경변수 설정

**3. 실행**

```bash
cd backend
./gradlew bootRun
# http://localhost:8080
```

### 테스트 실행

```bash
cd backend
./gradlew test
# 42개 단위 테스트 (Mockito 기반)
```

### 트러블슈팅

| 증상 | 원인 | 해결 |
|------|------|------|
| `Access denied for user` | DB 계정·비밀번호 불일치 | `application-local.properties` 값 재확인 |
| `Connection refused` (Redis) | Redis 미실행 | `redis-server` 실행 |
| `JWT signature does not match` | jwt.secret 누락 또는 변경 | secret 값 동일하게 유지 |
| `invalid_client` (OAuth2) | GitHub App client-id/secret 불일치 | GitHub Developer Settings 재확인 |
| QueryDSL Q클래스 없음 | 첫 빌드 시 생성 안 됨 | `./gradlew compileJava` 선행 실행 |

---

## Frontend

### 기술 스택

| 분류 | 기술 |
|---|---|
| Framework | React 19 + Vite 8 |
| Styling | Tailwind CSS v4 |
| Routing | React Router v7 |
| 서버 상태 | TanStack Query v5 |
| 전역 상태 | Zustand v5 |
| HTTP | Axios (JWT 인터셉터) |
| 마크다운 | react-markdown + remark-gfm + rehype-highlight |
| 실시간 알림 | @microsoft/fetch-event-source (SSE) |
| 날짜 | dayjs |
| 아이콘 | lucide-react |
| 차트 | Recharts |

### 프로젝트 구조

```
frontend/src/
├── api/                        # Axios 인스턴스 및 API 함수
│   ├── axios.js                # 인터셉터 (JWT 자동 주입, 401 재발급)
│   ├── auth.js
│   ├── post.js
│   ├── comment.js
│   ├── like.js
│   ├── notification.js
│   ├── series.js
│   ├── snippet.js
│   └── user.js
├── components/
│   ├── MarkdownRenderer.jsx    # 마크다운 렌더러 (공용)
│   ├── layout/
│   │   ├── Layout.jsx
│   │   └── Navbar.jsx          # SSE 실시간 알림 연결
│   ├── post/
│   │   ├── PostCard.jsx
│   │   └── PostEditor.jsx      # 마크다운 에디터 + 자동저장 (초안)
│   └── comment/
│       ├── CommentForm.jsx
│       └── CommentItem.jsx
├── hooks/
│   └── useAuth.js              # 로그인/회원가입/로그아웃 훅
├── pages/
│   ├── MainPage.jsx
│   ├── LoginPage.jsx
│   ├── SignupPage.jsx
│   ├── PostDetailPage.jsx      # 마크다운 렌더링 + 시리즈 네비게이션
│   ├── PostCreatePage.jsx      # 드래프트 키 draft:new
│   ├── PostEditPage.jsx        # 드래프트 키 draft:{postId}
│   ├── ProfilePage.jsx         # 게시글/시리즈 탭 + 팔로우
│   ├── SeriesPage.jsx
│   ├── SnippetsPage.jsx
│   ├── SnippetDetailPage.jsx
│   ├── SnippetCreatePage.jsx
│   ├── SnippetEditPage.jsx
│   ├── DashboardPage.jsx       # 통계 차트
│   └── NotificationPage.jsx   # 알림 목록 + 클릭 시 게시글 이동
└── store/
    └── authStore.js            # Zustand (로그인 상태, 토큰)
```

### 주요 기능

#### 인증
- 이메일/비밀번호 로그인 및 회원가입
- GitHub OAuth2 소셜 로그인
- Access Token 만료 시 Refresh Token으로 자동 재발급 (Axios 인터셉터)
- 로그아웃 시 토큰 전체 초기화

#### 게시글
- 마크다운 에디터 (작성/미리보기 탭 전환)
- 태그 입력 (Enter / 쉼표 구분)
- 임시저장(DRAFT) / 발행 구분
- 키워드·태그 검색 및 페이지네이션

#### 초안 자동저장
- 입력 중단 3초 후 localStorage에 자동저장 (debounce)
- 페이지 재방문 시 저장된 초안이 있으면 복원 배너 표시
- 발행/수정/임시저장 성공 시 localStorage 초안 자동 삭제
- 에디터 하단에 자동저장 시간 표시

| 페이지 | localStorage 키 |
|--------|----------------|
| 게시글 작성 | `draft:new` |
| 게시글 수정 | `draft:{postId}` |

#### 시리즈
- 게시글을 묶어 시리즈로 관리
- 게시글 상세 페이지에서 시리즈 배너 및 이전/다음 편 네비게이션
- 프로필 페이지에서 유저의 시리즈 목록 조회

#### 소셜
- 게시글·댓글 좋아요 토글
- 댓글 + 대댓글 작성/수정/삭제
- 유저 팔로우/언팔로우

#### 실시간 알림
- SSE(Server-Sent Events)로 좋아요·댓글·답글 알림 실시간 수신
- Navbar 알림 배지 즉시 갱신
- 알림 클릭 시 연관 게시글 이동
- 알림 읽음 처리

#### 코드 스니펫
- 언어 필터, 키워드 검색
- macOS 스타일 코드블록 + 클립보드 복사

#### 통계 대시보드
- 게시글별 조회수·좋아요 Recharts 바 차트

### 페이지 라우팅

| 경로 | 페이지 | 인증 필요 |
|------|--------|----------|
| `/` | 메인 (게시글 목록) | ❌ |
| `/login` | 로그인 | ❌ |
| `/signup` | 회원가입 | ❌ |
| `/posts/:postId` | 게시글 상세 | ❌ |
| `/users/:username` | 유저 프로필 | ❌ |
| `/series/:seriesId` | 시리즈 상세 | ❌ |
| `/snippets` | 스니펫 목록 | ❌ |
| `/snippets/:snippetId` | 스니펫 상세 | ❌ |
| `/write` | 게시글 작성 | ✅ |
| `/posts/:postId/edit` | 게시글 수정 | ✅ |
| `/snippets/new` | 스니펫 작성 | ✅ |
| `/snippets/:snippetId/edit` | 스니펫 수정 | ✅ |
| `/notifications` | 알림 목록 | ✅ |
| `/dashboard` | 통계 대시보드 | ✅ |

### 로컬 개발 환경 설정

```bash
cd frontend
npm install
npm run dev
# http://localhost:5173
```

`vite.config.js`에 백엔드 프록시 설정:

```js
server: {
  proxy: {
    '/api': 'http://localhost:8080',
    '/oauth2': 'http://localhost:8080',
  }
}
```

> `/api` 요청은 `localhost:8080`으로 자동 프록시됩니다.

### 트러블슈팅

| 증상 | 원인 | 해결 |
|------|------|------|
| `/api` 요청이 404 | 백엔드 미실행 또는 프록시 설정 누락 | 백엔드 실행 후 `vite.config.js` 프록시 확인 |
| SSE 연결 안 됨 | 인증 토큰 없음 / 백엔드 CORS 미설정 | 로그인 상태 및 백엔드 CORS 설정 확인 |
| OAuth2 리다이렉트 실패 | redirect-uri 불일치 | GitHub App 설정과 `application-local.properties` 값 일치 여부 확인 |
| 초안 복원 배너 안 뜸 | localStorage 키 불일치 | 브라우저 DevTools → Application → localStorage 확인 |

---

## Docker (전체 실행)

모든 서비스를 한 번에 실행합니다.

**1. 환경변수 설정**

프로젝트 루트에 `.env` 파일을 생성합니다 (`.gitignore` 적용, 커밋 금지):

```bash
# .env
MYSQL_ROOT_PASSWORD=<mysql_root_password>
MYSQL_DATABASE=devlog
MYSQL_USER=<mysql_user>
MYSQL_PASSWORD=<mysql_password>
JWT_SECRET=<256비트 이상의 랜덤 문자열>
GITHUB_CLIENT_ID=<github_client_id>
GITHUB_CLIENT_SECRET=<github_client_secret>
GITHUB_REDIRECT_URI=http://localhost/login/oauth2/code/github
```

**2. 빌드 및 실행**

```bash
docker compose up -d        # 첫 빌드: 5~10분 소요
docker compose logs -f      # 로그 확인
docker compose down         # 종료
```

**3. 접속**

| 서비스 | 주소 |
|---|---|
| 프론트엔드 | http://localhost |
| 백엔드 직접 | http://localhost:8080 |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |

> Nginx가 `/api/*`, `/oauth2/*`, SSE 요청을 백엔드로 프록시합니다.

### 트러블슈팅

| 증상 | 원인 | 해결 |
|------|------|------|
| 백엔드 컨테이너가 즉시 종료 | `.env` 누락 또는 DB 연결 실패 | `.env` 파일 생성 및 MySQL 컨테이너 기동 확인 |
| 프론트엔드 빌드 실패 | Node.js 버전 불일치 | `frontend/Dockerfile` 내 Node 버전 확인 |
| Gradle 빌드 실패 | `gradle-wrapper.jar` 누락 | `backend/gradle/wrapper/gradle-wrapper.jar` 존재 확인 |
| 포트 충돌 | 로컬 MySQL/Redis와 충돌 | `docker-compose.yml` 포트 변경 또는 로컬 서비스 중지 |

---

## 브랜치 전략

```
main          배포 브랜치
└── feature/* 기능 개발
```
