# DevLog Frontend

> 개발자 커뮤니티 플랫폼 **DevLog**의 프론트엔드

## 기술 스택

| 분류 | 기술 |
|------|------|
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

## 로컬 실행

```bash
npm install
npm run dev
```

`/api` 요청은 `vite.config.js`의 프록시 설정으로 `localhost:8080`으로 자동 전달됩니다.
