# Secondhand Platform Backend

프로젝트의 Spring Boot 백엔드입니다.

## 실행

Java 21, Redis, `JWT_SECRET`, `RESEND_API_KEY` 및 Supabase Storage S3 설정(`SUPABASE_URL`, `SUPABASE_REGION`, `SUPABASE_ACCESS_KEY`, `SUPABASE_SECRET_KEY`, `SUPABASE_BUCKET`)이 필요합니다. 기본 데이터베이스는 H2 메모리 DB입니다. 프로젝트에 포함된 Gradle Wrapper를 사용합니다.

```bash
./gradlew bootRun
```

PostgreSQL 로컬 프로필을 사용할 경우 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `REDIS_USERNAME`, `REDIS_PASSWORD`도 설정합니다.

```bash
./gradlew bootRun -Dspring.profiles.active=local
```

## 주요 URL

- 헬스 체크: http://localhost:8080/api/v1/health
- Actuator 헬스 체크: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/api/docs
- OpenAPI 문서: http://localhost:8080/api-docs

## 빌드 및 테스트

```bash
./gradlew clean build
```
