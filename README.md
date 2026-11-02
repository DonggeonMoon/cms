## 기능

- 아카이브
- 게시판
    - 게시글
        - 일반
        - 갤러리
    - 댓글
- 질문답변
- 컨텐츠
- 비즈니스
    - 주문
        - 상품 (다) - 주문 (일)
    - 예약
        - 프로그램 (다) - 예약 (일)
    - 결제
        - 주문, 예약 (다) - 결제 (일)
- 공공기관
    - 신고
    - 만족도 조사

- 관리자
    - 배너
    - 디자인
    - 메뉴
    - 대시보드
  - 설정
      - 사이트
      - 통계

- 공통
    - 파일
    - 공통코드(카테고리)
    - 회원, 로그인
        - 관리자
        - 회원
        - 비회원

## 공통 설정

- 보안
- 로깅

## 구성

| 영역 | 기술 |
|---|---|
| 백엔드 | Java 21, Spring Boot 4.1, Spring Security 7 (세션 + 쿠키 CSRF), JPA + Querydsl, MyBatis(통계 집계) |
| DB | PostgreSQL 18 (개발용 `docker-compose.yml`), 테스트는 H2 |
| 프론트엔드 | `frontend-react/` — React 19, React Router 8, Vite 8, TypeScript |
| 화면 | React가 담당. Thymeleaf 의존성과 템플릿은 추후 구현을 위해 그대로 둠 |

API는 모두 `/api/**` 아래에 있고, 관리자 API는 `/api/admin/**` (ADMIN 권한)입니다.

## 개발 환경 실행

```bash
# 1. DB (값은 공용 .env.development 의 DB_* 사용)
docker compose --env-file .env.development up -d

# 2. 백엔드 (http://localhost:8080)
./gradlew bootRun

# 3. 프론트엔드 (http://localhost:5173, /api 는 8080 으로 프록시)
cd frontend-react && npm install && npm run dev
```

- 관리자 테이블이 비어 있으면 시작 시 초기 관리자 `admin` 이 생성됩니다. 비밀번호는 `CMS_ADMIN_PASSWORD`
  환경변수, 없으면 개발용 기본값을 씁니다(`application.yml` 의 `cms.security.initial-admin-password`).
  관리자 로그인 화면은 `/admin/login` 입니다.
- 업로드 파일은 `./uploads` 에 저장됩니다(gitignore).
- `CMS_CRYPTO_MASTER_KEY` 환경변수(암호화 마스터키)가 없으면 서버가 시작되지 않습니다. 개발은 `.env.development`,
  배포는 GitHub Secrets 에 둡니다. 값을 바꾸면 저장된 키를 풀 수 없으니 한 번 정한 값은 유지합니다.

### 개인정보 암호화

- 회원·관리자·비회원의 이름, 성별, 이메일, 전화번호, 주소와 주문 수령인·연락처·주소, 예약 신청자·연락처는
  SEED / AES / ARIA(GCM) 중 선택한 알고리즘으로 암호화해 저장합니다. 로그인 아이디는 암호화하지 않습니다.
- 암호화 키(UUID)는 관리자가 처음 로그인할 때 만들어지고 마스터키로 감싸 DB 에 보관됩니다. 기본 알고리즘은 AES 입니다.
- 관리자 > 보안 > 암호화 키 관리에서 키를 확인하고, 키를 재생성하거나 알고리즘을 바꿀 수 있습니다.
  바꾸면 저장된 값을 모두 복호화한 뒤 새 설정으로 다시 암호화합니다.
- 관리자 > 보안 > API 키 관리에서 외부 서비스 키를 이름과 값으로 등록합니다(값은 암호화 저장).
  지도처럼 키가 필요한 설정에서는 값을 직접 입력하지 않고 등록한 키 이름을 고릅니다. 사용 중인 키는 삭제할 수 없습니다.
- 보안 메뉴(암호화 키 관리, API 키 관리)는 로그인한 상태에서도 비밀번호를 한 번 더 확인해야 열리고,
  확인은 마지막 사용 후 5분 동안 유지됩니다. 틀린 비밀번호는 로그인 실패 횟수에 포함됩니다.
- 이름·이메일은 DB 에서 검색할 수 없으므로 관리자 회원 검색은 아이디로만 합니다.

### 샘플 사이트 "중고천국"

DB 볼륨이 비어 있는 상태로 처음 띄우면 `docker/postgres/init/01-junggo-heaven.sql` 이 실행되어 중고거래 샘플 사이트가
기본 데이터로 들어갑니다(컨텐츠, 게시판, 아카이브, 질문답변, 지도·지점, 상품·할인·주문, 프로그램·예약, 신고, 만족도,
배너·팝업, 개인정보 문구).

- 관리자 `admin` / `admin1234!`, 회원 `happybuyer`, `retro_mania`, `camping_lee`, `gadget_kim` / `junggo1234!`
- 상품·프로그램·팝업 사진 파일은 DB 덤프에 들어가지 않으므로 처음 한 번 복사합니다: `mkdir -p uploads && cp -R docker/sample-uploads/. uploads/`
  (사진은 위키미디어 공용의 자유 라이선스 이미지이며, 출처는 `scripts/sample-images/credits.json` 과 사이트의 "사진 출처" 페이지에 있습니다.)
- 이미 데이터가 있는 볼륨에는 적용되지 않습니다. 처음부터 다시 받으려면 `docker compose --env-file .env.development down -v` 후 다시 띄웁니다.
- 샘플 데이터는 `scripts/seed_sample_site.py` 가 CMS API 로 만든 것입니다. 엔티티가 바뀌어 다시 만들 때는 빈 DB 에서
  서버를 띄우고 스크립트를 실행한 뒤 `pg_dump --no-owner --no-privileges --schema=public` 결과로 SQL 파일을 교체합니다
  (`CREATE SCHEMA public;` 줄은 지웁니다). 업로드된 사진도 `uploads/` 에서 `docker/sample-uploads/` 로 다시 복사합니다.
- 사이트 테마는 관리자 > 사이트의 "테마"에서 고릅니다. 중고천국은 `market` 테마를 씁니다.

## 테스트

```bash
./gradlew test                       # 백엔드 (H2)
cd frontend-react && npm test        # 프론트엔드 (Vitest)
cd frontend-react && npm run build   # 타입 체크 + 빌드
```

## 비고

- 결제는 실제 PG 없이 목업(`MockPaymentGateway`)으로 처리합니다. 결제 요청은 금액 검증 후 즉시 승인되고, 취소 시 환불 처리됩니다.
- 사용자가 작성한 HTML(게시글, 컨텐츠, 답변 등)은 저장 시 OWASP HTML Sanitizer로 정화됩니다.
- 비즈니스에 포인트 적립 추가 예정