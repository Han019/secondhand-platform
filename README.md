# Secondhand Platform

지역 기반 중고거래 웹 프로젝트입니다. 현재 프론트엔드의 인증·상품 흐름을 Spring Boot API에 연결하는 단계입니다.

## 현재 가능한 흐름

- 회원가입, 로그인, 로그아웃, 로그인 ID·닉네임 중복 확인
- 상품 목록·키워드 검색·가격 정렬·페이지 이동
- 상품 상세 및 판매자 공개 정보·이미지 조회
- 로그인 후 상품 사진 포함 등록, 본인 상품 수정·상태 변경·삭제, 사진 추가·삭제·순서 변경

채팅, 관심, 마이페이지, 이메일 인증, 지역·가격·상태 필터, 카테고리, 임시저장은 아직 구현되지 않았습니다. 관련 화면은 준비 중으로 표시합니다.

## 기술 스택

- 프론트엔드: React, TypeScript, Vite, Tailwind CSS
- 백엔드: Java 21, Spring Boot, Spring Security, JPA
- 저장소: 기본 H2 메모리 DB, Redis(인증 토큰), Supabase Storage(S3 호환 이미지)

## 로컬 실행

Java 21과 Node.js, npm, Redis가 필요합니다. 상품 이미지 등록·조회에는 Supabase Storage 설정이 필요합니다. 백엔드 기본 DB는 H2 메모리 DB로, 서버를 재시작하면 데이터가 초기화됩니다.

```bash
cd backend
export JWT_SECRET='32자 이상 임의의 비밀값'
export SUPABASE_URL='<Supabase S3 endpoint>'
export SUPABASE_REGION='<region>'
export SUPABASE_ACCESS_KEY='<access-key>'
export SUPABASE_SECRET_KEY='<secret-key>'
export SUPABASE_BUCKET='<private-bucket>'
export RESEND_API_KEY='<resend-api-key>'
./gradlew bootRun
```

다른 터미널에서:

```bash
cd frontend
npm install
npm run dev
```

프론트엔드는 Vite 개발 서버(기본 `http://localhost:5173`)에서 실행합니다. `/api` 요청은 `http://localhost:8080`으로 프록시합니다. 별도 API 서버 주소가 필요하면 `VITE_API_BASE_URL`을 설정하세요. 이 경우 해당 서버의 CORS 설정도 필요합니다. 백엔드의 PostgreSQL `local` 프로필은 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `REDIS_USERNAME`, `REDIS_PASSWORD`를 추가로 요구합니다.

## API

| 영역 | 주요 엔드포인트 |
| --- | --- |
| 인증 | `POST /api/auth/signup`, `POST /api/auth/login`, `POST /api/auth/logout`, `POST /api/auth/refresh` |
| 중복 확인 | `GET /api/auth/check/login-id`, `GET /api/auth/check/nickname` |
| 상품 | `GET/POST /api/products`, `GET/PATCH/DELETE /api/products/{id}`, `PATCH /api/products/{id}/status` |
| 상품 이미지 | `POST /api/products/{id}/images`, `PATCH /api/products/{id}/images/order`, `DELETE /api/products/{id}/images/{imageId}` |

상품 등록은 `multipart/form-data`로 `product` JSON 파트와 `image` 파일 파트(1~10장)를 보냅니다. 목록 응답은 페이지 객체의 `content`, 상세 응답은 `images` 배열을 사용합니다. Swagger UI: `http://localhost:8080/api/docs`. [노션 API 명세](https://app.notion.com/p/3d6138ae9be581deb2decdd8ab325d88)를 함께 참고하세요.

## 확인

```bash
cd backend && ./gradlew test
cd ../frontend && npm run build && npm test
```

> 초안입니다. 배포 주소와 실제 운영 환경 설정은 배포 후 추가할 예정입니다.
