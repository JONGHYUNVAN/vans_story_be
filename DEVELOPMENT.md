# 개발 가이드

## 목차
- [개발 환경 설정](#개발-환경-설정)
- [프로젝트 구조](#프로젝트-구조)
- [개발 워크플로우](#개발-워크플로우)
- [코딩 컨벤션](#코딩-컨벤션)
- [테스트 가이드](#테스트-가이드)
- [디버깅](#디버깅)
- [문제 해결](#문제-해결)

## 개발 환경 설정

### 1. 필수 도구 설치

#### JDK 21
```bash
# Windows (Chocolatey)
choco install openjdk21

# macOS (Homebrew)
brew install openjdk@21

# Ubuntu
sudo apt install openjdk-21-jdk
```

#### MariaDB 설치 및 설정
```bash
# Windows (Chocolatey)
choco install mariadb

# macOS (Homebrew)
brew install mariadb
brew services start mariadb

# Ubuntu
sudo apt install mariadb-server
sudo systemctl start mariadb
```

#### 데이터베이스 초기 설정
```sql
# MariaDB 접속
mysql -u root -p

# 데이터베이스 생성
CREATE DATABASE vans_story_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 사용자 생성 및 권한 부여
CREATE USER 'vans_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON vans_story_db.* TO 'vans_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. 프로젝트 설정

#### 환경 변수 설정
프로젝트 루트에 `.env` 파일을 생성:

```env
# 데이터베이스 설정
DB_URL=jdbc:mariadb://localhost:3306/vans_story_db
DB_USERNAME=vans_user
DB_PASSWORD=your_password

# JWT 설정
VANS_BLOG_JWT_SECRET_KEY=your_super_secret_jwt_key_at_least_32_characters_long
VANS_BLOG_JWT_ACCESS_TOKEN_VALIDITY=18000
VANS_BLOG_JWT_REFRESH_TOKEN_VALIDITY=604800

# 서버 설정
SERVER_PORT=8080
LOG_LEVEL=INFO
SHOW_SQL=true
GENERATE_DDL=true
```

#### IDE 설정 (IntelliJ IDEA 권장)
1. **Kotlin 플러그인** 활성화
2. **Gradle 자동 새로고침** 활성화
3. **코드 스타일 설정**:
   - File > Settings > Editor > Code Style > Kotlin
   - Scheme: Default 또는 팀 컨벤션

### 3. 프로젝트 실행

```bash
# 의존성 다운로드
./gradlew build

# 개발 서버 실행
./gradlew bootRun

# 또는 IDE에서 VansStoryBeApplication.kt 실행
```

## 프로젝트 구조

### 아키텍처 개요
```
Domain-Driven Design (DDD) + Layered Architecture
├── Presentation Layer (Controller)
├── Application Layer (Service)
├── Domain Layer (Entity, Repository Interface)
└── Infrastructure Layer (Repository Implementation)
```

### 핵심 패키지별 역할

#### `config/`
- **목적**: 애플리케이션 전역 설정
- **주요 파일**:
  - `SecurityConfig.kt`: Spring Security 설정
  - `CorsConfig.kt`: CORS 정책 설정
  - `SwaggerConfig.kt`: API 문서화 설정

#### `domain/`
- **목적**: 비즈니스 로직의 핵심
- **구조**: 각 도메인별로 분리
  ```
  domain/
  ├── auth/     # 인증 도메인
  ├── user/     # 사용자 도메인
  └── post/     # 포스트 관련 (현재는 service 디렉토리만 존재)
  ```

#### `global/`
- **목적**: 전역적으로 사용되는 유틸리티
- **주요 구성요소**:
  - `exception/`: 예외 처리
  - `response/`: API 응답 표준화
  - `mapper/`: 객체 변환

### 레이어별 책임

1. **Controller**: HTTP 요청/응답 처리, 입력 검증
2. **Service**: 비즈니스 로직 구현
3. **Repository**: 데이터 접근 추상화
4. **Entity**: 도메인 모델 표현

## 개발 워크플로우

### 1. 브랜치 전략
```bash
# 새 기능 개발
git checkout -b feature/기능명
git checkout -b feature/user-profile-update

# 버그 수정
git checkout -b bugfix/버그명
git checkout -b bugfix/jwt-token-refresh

# 핫픽스
git checkout -b hotfix/긴급수정명
```

### 2. 개발 프로세스
1. **이슈 생성** → GitHub Issues
2. **브랜치 생성** → feature/기능명
3. **개발 진행** → 코드 작성 + 테스트
4. **테스트 실행** → `./gradlew test`
5. **커밋** → 의미있는 커밋 메시지
6. **Pull Request** → 코드 리뷰
7. **머지** → main 브랜치

### 3. 커밋 메시지 컨벤션
```
타입(스코프): 제목

본문 (선택사항)

- feat: 새로운 기능 추가
- fix: 버그 수정
- docs: 문서 수정
- style: 코드 포맷팅
- refactor: 코드 리팩토링
- test: 테스트 추가/수정
- chore: 빌드 설정 등

예시:
feat(auth): JWT 토큰 갱신 기능 추가
fix(user): 사용자 정보 조회 시 NPE 수정
docs(readme): API 엔드포인트 문서 업데이트
```

## 코딩 컨벤션

### Kotlin 컨벤션
```kotlin
// 1. 클래스명: PascalCase
class UserService

// 2. 함수명: camelCase
fun getUserById(id: Long): User

// 3. 변수명: camelCase
val userName = "van"

// 4. 상수명: UPPER_SNAKE_CASE
const val MAX_LOGIN_ATTEMPTS = 5

// 5. 널 안전성 활용
fun processUser(user: User?) {
    user?.let { 
        // 안전한 처리
    }
}
```

### API 설계 원칙
```kotlin
// 1. RESTful URL 설계
@GetMapping("/api/v1/users/{id}")
@PostMapping("/api/v1/users")
@PatchMapping("/api/v1/users/{id}")
@DeleteMapping("/api/v1/users/{id}")

// 2. 표준 HTTP 상태 코드 사용
return ResponseEntity.ok(data)           // 200
return ResponseEntity.created(uri)       // 201
return ResponseEntity.noContent()        // 204
return ResponseEntity.badRequest()       // 400

// 3. 일관된 응답 형식
ApiResponse.success(data)
ApiResponse.error("에러 메시지")
```

## 테스트 가이드

### 테스트 구조
```
src/test/kotlin/
├── auth/           # 인증 관련 테스트
├── domain/         # 도메인별 테스트
└── integration/    # 통합 테스트
```

### 단위 테스트 작성 예시
```kotlin
@Test
fun `사용자 ID로 사용자 정보를 조회할 수 있다`() {
    // Given
    val userId = 1L
    val expectedUser = User(id = userId, email = "test@example.com")
    every { userRepository.findById(userId) } returns expectedUser

    // When
    val result = userService.getUserById(userId)

    // Then
    result shouldBe expectedUser
    verify(exactly = 1) { userRepository.findById(userId) }
}
```

### 테스트 실행
```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "UserServiceTest"

# 특정 테스트 메서드 실행
./gradlew test --tests "UserServiceTest.사용자_ID로_사용자_정보를_조회할_수_있다"

# 테스트 결과 리포트 확인
open build/reports/tests/test/index.html
```

## 디버깅

### 로그 활용
```kotlin
// 로거 선언
private val logger = KotlinLogging.logger {}

// 로그 레벨별 사용
logger.debug { "디버그 정보: $debugInfo" }
logger.info { "정보: $info" }
logger.warn { "경고: $warning" }
logger.error(exception) { "에러 발생: $errorMessage" }
```

### 개발 도구 활용
1. **H2 Console** (테스트 시): http://localhost:8080/h2-console
2. **Swagger UI**: http://localhost:8080/swagger-ui.html
3. **Actuator**: http://localhost:8080/actuator/health

## 문제 해결

### 자주 발생하는 문제들

#### 1. 데이터베이스 연결 실패
```bash
# 원인: MariaDB 서비스 미실행
sudo systemctl start mariadb

# 원인: 잘못된 데이터베이스 URL/계정
# .env 파일의 DB_URL, DB_USERNAME, DB_PASSWORD 확인
```

#### 2. JWT 토큰 관련 오류
```bash
# 원인: VANS_BLOG_JWT_SECRET_KEY 키가 너무 짧음
# .env 파일에서 VANS_BLOG_JWT_SECRET_KEY를 32자 이상으로 설정
```

#### 3. 빌드 실패
```bash
# Gradle 캐시 정리
./gradlew clean

# 의존성 재다운로드
./gradlew build --refresh-dependencies
```

#### 4. 포트 충돌
```bash
# 8080 포트 사용 중인 프로세스 확인
lsof -i :8080

# 다른 포트 사용 (.env 파일)
SERVER_PORT=8081
```

### 도움이 되는 명령어
```bash
# 프로젝트 상태 확인
./gradlew projects

# 의존성 트리 확인
./gradlew dependencies

# 빌드 정보 확인
./gradlew buildEnvironment

# 코드 문서 생성
./gradlew dokkaHtml
```



---

## 추가 리소스
- [Kotlin 공식 문서](https://kotlinlang.org/docs/)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Exposed 문서](https://github.com/JetBrains/Exposed)
- [Kotest 문서](https://kotest.io/) 