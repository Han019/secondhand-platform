# Secondhand Platform Backend

당근클론 프로젝트의 Spring Boot 백엔드입니다.

## 실행

Java 21 이상이 필요합니다. 프로젝트에 포함된 Gradle Wrapper를 사용합니다.

```bash
./gradlew bootRun
```

로컬 프로필을 사용할 경우:

```bash
./gradlew bootRun -Dspring.profiles.active=local
```

## 주요 URL

- 헬스 체크: http://localhost:8080/api/v1/health
- Actuator 헬스 체크: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI 문서: http://localhost:8080/api-docs

## 빌드 및 테스트

```bash
./gradlew clean build
```
