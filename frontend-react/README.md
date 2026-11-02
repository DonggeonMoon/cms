# frontend-react

CMS의 화면(사용자 사이트 + 관리자)입니다. React 19 · React Router 8 · Vite 8 · TypeScript.

## 실행

```bash
npm install
npm run dev        # http://localhost:5173 — /api 요청은 http://localhost:8080 으로 프록시
npm test           # Vitest
npm run build      # tsc 타입 체크 + 프로덕션 빌드 (dist/)
```

백엔드는 저장소 루트에서 `./gradlew bootRun` 으로 실행합니다.

## 구조

| 경로 | 내용 |
|---|---|
| `src/api/client.ts` | fetch 래퍼. 세션 쿠키 사용, `XSRF-TOKEN` 쿠키를 `X-XSRF-TOKEN` 헤더로 전송, 에러 응답을 `ApiError` 로 변환 |
| `src/api/endpoints.ts` | 도메인별 API 함수 (`siteApi`, `boardApi`, `shopApi`, `adminApi` …) |
| `src/auth/` | 로그인 상태(`AuthContext`)와 권한 가드(`RequireRole`) |
| `src/components/SchemaForm.tsx` | 필드 정의만으로 폼을 그리는 공통 폼 (HTML, 이미지·첨부 업로드 포함) |
| `src/components/AdminCrudPage.tsx` | 관리자 목록·검색·등록·수정·삭제 공통 화면 |
| `src/pages/` | 사용자 화면 (게시판, 질문답변, 컨텐츠, 아카이브, 상점, 민원, 마이페이지) |
| `src/pages/admin/` | 관리자 화면 |

페이지 이동 시마다 `/api/statistics/visits` 로 방문이 기록되어 관리자 방문 통계에 쓰입니다.
