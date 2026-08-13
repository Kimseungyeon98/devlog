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
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

jwt.secret=your-secret-key-minimum-256-bits-long
jwt.access-token-expiry=3600000
jwt.refresh-token-expiry=604800000

spring.data.redis.host=localhost
spring.data.redis.port=6379

spring.security.oauth2.client.registration.github.client-id=your_github_client_id
spring.security.oauth2.client.registration.github.client-secret=your_github_client_secret
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

---

## Frontend

### 기술 스택

| 분류 | 기술 |
|---|---|
| Language | JavaScript (ES2024) |
| Framework | React 19 |
| Bundler | Vite |
| Styling | Tailwind CSS v4 |
| State | Zustand + React Query |
| HTTP | Axios |
| 기타 | dayjs, lucide-react, highlight.js |

### 주요 기능

- **게시글** — 마크다운 에디터 (프리뷰·편집 토글), 임시저장 (localStorage)
- **코드 스니펫** — 언어 필터, 키워드 검색, macOS 스타일 코드블록
- **시리즈** — 연재글 목록 및 탐색
- **알림** — SSE 실시간 수신, 읽음 처리
- **통계 대시보드** — 조회수·좋아요 차트 (Recharts)
- **OAuth2** — GitHub 소셜 로그인
- **이미지 업로드** — 에디터 내 드래그&클릭 업로드
- **반응형** — Tailwind 기반 모바일 대응

### 로컬 개발 환경 설정

```bash
cd frontend
npm install
npm run dev
# http://localhost:5173
# /api 요청은 localhost:8080으로 자동 프록시 (vite.config.js)
```

---

## Docker (전체 실행)

모든 서비스를 한 번에 실행합니다.

**1. 환경변수 설정**

```bash
# 프로젝트 루트에 .env 파일 생성
cat > .env << EOF
MYSQL_ROOT_PASSWORD=root1234
MYSQL_DATABASE=devlog
MYSQL_USER=devlog
MYSQL_PASSWORD=devlog1234
JWT_SECRET=your-secret-key-minimum-256-bits-long
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret
GITHUB_REDIRECT_URI=http://localhost/login/oauth2/code/github
EOF
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

---

## 브랜치 전략

```
main          배포 브랜치
└── feature/* 기능 개발
```
