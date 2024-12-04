# Vans Story Backend

## 개발 환경
### 버전 정보
- Spring Boot: 3.2.0
- Dependency Management: 1.1.4
- Jakarta Persistence API: 3.1.0
- Hibernate Core: 6.2.7.Final
- Java: 23
- Gradle: 8.5

### 주요 의존성
#### 스프링 부트 핵심 스타터
- Spring Boot Starter Web
- Spring Boot Starter Data JPA

#### 보안 관련
- Spring Security
- OAuth2 Client
- Spring Boot Starter Validation

#### JSON 처리
- Jackson Databind
- Jackson Datatype JSR310

#### 데이터베이스
- MariaDB
- H2 Database (테스트용)
- QueryDSL
- Jakarta Persistence API

#### 개발 도구
- Spring Boot DevTools
- SpringDoc OpenAPI (Swagger UI)

#### 로깅
- Logback Classic

#### 코드 생성 도구
- Lombok
- MapStruct
- Spring Boot Configuration Processor

#### 테스트
- Spring Boot Starter Test
- Spring Security Test
- JUnit Platform Launcher

#### 인증
- JJWT (JSON Web Token)

#### 캐싱
- Spring Boot Starter Cache
- Caffeine Cache

### 개발 환경 (dev)
#### 데이터베이스
- H2 Database 사용
- 콘솔 접근: `http://localhost:8080/h2-console`
- URL: `jdbc:h2:file:./data/blogdb`
- Username: sa
- Password: (empty)

#### JPA 설정
- ddl-auto: update (테이블 자동 생성/수정)
- SQL 포맷팅 활성화
- SQL 쿼리 로깅 활성화

#### 서버 설정
- Port: 8080
- 문자 인코딩: UTF-8

### 운영 환경 (prod)
#### 데이터베이스
- MariaDB 사용
- URL: `jdbc:mariadb://your-production-db:3306/blog_prod`
- 접속 정보는 환경 변수에서 관리
  - DB_USERNAME
  - DB_PASSWORD

#### JPA 설정
- ddl-auto: validate (스키마 검증만 수행)
- SQL 로깅 비활성화
- OSIV 비활성화

#### 서버 설정
- Port: 80
- 문자 인코딩: UTF-8

### Swagger UI 설정
- 접근 경로: `http://localhost:8080/swagger-ui.html`
- API 문서 JSON: `/api-docs`
- 기능:
  - API 그룹 DESC 정렬
  - 메서드 기준 정렬
  - 요청 소요 시간 표시
  - Actuator API 문서화

### JWT 설정
- 개발 환경: 테스트용 시크릿 키 사용
- 운영 환경: 환경 변수 `JWT_SECRET`에서 시크릿 키 로드
- 토큰 유효 기간: 24시간 (86400초)

### 로깅 설정
#### 개발 환경
- SQL 쿼리 로깅: DEBUG
- SQL 파라미터: TRACE
- 애플리케이션 로그: DEBUG

#### 운영 환경
- SQL 쿼리 로깅: INFO
- SQL 파라미터: INFO
- 애플리케이션 로그: INFO

## 프로젝트 구조

### 전역 설정
#### Security 설정 (`SecurityConfig.java`)
- CSRF 보호 비활성화
- 세션 관리: STATELESS (JWT 사용)
- 공개 엔드포인트: 
  - `/api/v1/auth/**`
  - `/swagger-ui/**`
  - `/v3/api-docs/**`

#### Swagger 설정 (`SwaggerConfig.java`)
- API 문서 제목: Vans Story API
- 버전: v1.0.0
- 설명: Vans Story 블로그 API 문서

### 글로벌 컴포넌트
#### API 응답 형식 (`ApiResponse.java`)  
{
"success": boolean,
"data": T,
"message": String
}  
- success: API 호출 성공 여부
- data: 응답 데이터 (제네릭 타입)
- message: 오류 메시지 (실패 시)

#### 예외 처리
- `CustomException.java`: 사용자 정의 예외 클래스
- `GlobalExceptionHandler.java`: 전역 예외 처리기
  - 모든 예외를 ApiResponse 형식으로 변환
  - HTTP 500 응답 코드 반환

#### 기본 엔티티 (`BaseEntity.java`)
모든 엔티티의 기본 클래스로 다음 필드 포함:
- createdAt: 생성 일시
- updatedAt: 수정 일시
- JPA Auditing 사용하여 자동 관리

## 환경 설정
### 프로필 설정
- 기본적으로 `dev` 프로필이 활성화되어 있습니다.
- 프로필별로 다른 설정을 적용할 수 있습니다 (`application-{profile}.yml`).

