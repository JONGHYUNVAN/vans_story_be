# Vans Dev Blog Backend

## 개발 환경
### 버전 정보
- Spring Boot: 3.2.0
- Dependency Management: 1.1.4
- Jakarta Persistence API: 3.1.0
- Hibernate Core: 6.2.7.Final
- Java: 17
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
- **CustomException**: 비즈니스 로직 관련 사용자 정의 예외
- **GlobalExceptionHandler**: 전역 예외 처리기
  - `@RestControllerAdvice`를 사용한 일관된 예외 처리
  - 모든 예외를 `ApiResponse` 형식으로 변환

#### 처리되는 예외 종류
1. **일반 예외 (Exception)**
   - 처리되지 않은 모든 예외를 포괄
   - HTTP 500 (Internal Server Error) 반환

2. **사용자 정의 예외 (CustomException)**
   - "not found" 포함 시 HTTP 404 (Not Found)
   - 그 외의 경우 HTTP 400 (Bad Request)

3. **요청 데이터 파싱 예외 (HttpMessageNotReadableException)**
   - JSON 파싱 실패 시
   - HTTP 400 (Bad Request)

4. **유효성 검증 실패 (MethodArgumentNotValidException)**
   - 요청 데이터 검증(@Valid) 실패 시
   - 필드별 오류 메시지 포함
   - HTTP 400 (Bad Request)

#### 응답 형식
```json
{
    "success": false,
    "data": null,
    "message": "에러 메시지"
}
```

#### 기본 엔티티 (`BaseEntity.java`)
모든 엔티티의 기본 클래스로 다음 필드 포함:
- createdAt: 생성 일시
- updatedAt: 수정 일시
- JPA Auditing 사용하여 자동 관리

## 환경 설정
### 프로필 설정
- 기본적으로 `dev` 프로필이 활성화되어 있습니다.
- 프로필별로 다른 설정을 적용할 수 있습니다 (`application-{profile}.yml`).

## Spring Security 구현

### 인증 아키텍처
#### JWT 기반 인증
- Access Token과 Refresh Token 사용
- Access Token: Authorization 헤더로 전달 (Bearer 방식)
- Refresh Token: HTTP Only 쿠키로 안전하게 저장
- 토큰 유효 기간
  - Access Token: 30분
  - Refresh Token: 7일

#### 주요 컴포넌트
1. **SecurityConfig**
   - Spring Security 기본 설정
   - CSRF 보호 비활성화 (JWT 사용으로 인한)
   - 세션 관리: STATELESS
   - CORS 설정
   - 공개 엔드포인트:
     - `/api/v1/auth/**`
     - `/swagger-ui/**`
     - `/v3/api-docs/**`

2. **JwtProvider**
   - JWT 토큰 생성 및 검증
   - 사용자 인증 정보 추출
   - 토큰 유효성 검사

3. **JwtFilter**
   - 모든 요청에 대한 JWT 검증
   - Authorization 헤더에서 토큰 추출
   - 유효한 토큰의 경우 SecurityContext에 인증 정보 설정

4. **CustomUserDetailsService**
   - 사용자 인증 정보 로드
   - 데이터베이스에서 사용자 정보 조회
   - UserDetails 객체 생성

### 인증 프로세스
1. **로그인 (/api/v1/auth/login)**
   ```json
   {
     "email": "user@example.com",
     "password": "password"
   }
   ```
   - 응답:
     - Access Token: Authorization 헤더
     - Refresh Token: HTTP Only 쿠키

2. **토큰 갱신 (/api/v1/auth/refresh)**
   - Refresh Token 쿠키 검증
   - 새로운 Access Token 발급
   - 선택적으로 Refresh Token 재발급

### 사용자 권한
- **역할 기반 접근 제어 (RBAC)**
  - USER: 기본 사용자 권한
  - ADMIN: 관리자 권한

### 보안 설정
- 비밀번호 암호화: BCrypt 알고리즘 사용
- JWT 서명: HS256 알고리즘
- 환경별 설정:
  - 개발: 테스트용 시크릿 키
  - 운영: 환경 변수에서 시크릿 키 로드

### CORS 설정
- 허용된 출처: `http://localhost:3000`
- 허용된 메서드: GET, POST, PUT, DELETE, OPTIONS
- 인증 헤더 허용

