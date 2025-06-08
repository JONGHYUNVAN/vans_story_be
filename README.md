# Vans Story Backend

## 프로젝트 소개
Vans Story 블로그의 백엔드 서버입니다. Kotlin과 Spring Boot를 사용하여 개발되었습니다.

## 주요 기능
- 블로그 포스트 CRUD
- 사용자 인증 및 권한 관리
- 댓글 시스템
- 태그 관리
- 카테고리 관리

## 기술 스택
- Kotlin 1.9.22
- Spring Boot 3.5.0
- Java 21
- Exposed (Kotlin SQL 프레임워크)
- MariaDB
- JWT 기반 인증

## 시작하기

### 필수 조건
- JDK 21
- Gradle 8.5
- MariaDB

### 환경 설정
1. `.env` 파일을 프로젝트 루트에 생성하고 다음 환경변수를 설정합니다:
```env
# 데이터베이스
DB_URL=jdbc:mariadb://localhost:3306/로컬db명
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your jwt secret key
```

### 실행
```bash
# 개발 환경 실행
./gradlew bootRun

# 테스트 실행
./gradlew test

# 빌드
./gradlew build
```

## API 엔드포인트

### 인증
- POST `/api/v1/auth/login` - 로그인
- POST `/api/v1/auth/refresh` - 토큰 갱신
- POST `/api/v1/auth/logout` - 로그아웃

### 블로그 포스트
- GET `/api/v1/posts` - 포스트 목록 조회
- GET `/api/v1/posts/{id}` - 포스트 상세 조회
- POST `/api/v1/posts` - 포스트 작성
- PUT `/api/v1/posts/{id}` - 포스트 수정
- DELETE `/api/v1/posts/{id}` - 포스트 삭제

### 댓글
- GET `/api/v1/posts/{postId}/comments` - 댓글 목록 조회
- POST `/api/v1/posts/{postId}/comments` - 댓글 작성
- PUT `/api/v1/comments/{id}` - 댓글 수정
- DELETE `/api/v1/comments/{id}` - 댓글 삭제

### 태그
- GET `/api/v1/tags` - 태그 목록 조회
- POST `/api/v1/tags` - 태그 생성
- DELETE `/api/v1/tags/{id}` - 태그 삭제

### 카테고리
- GET `/api/v1/categories` - 카테고리 목록 조회
- POST `/api/v1/categories` - 카테고리 생성
- PUT `/api/v1/categories/{id}` - 카테고리 수정
- DELETE `/api/v1/categories/{id}` - 카테고리 삭제

## API 문서
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI 명세: `http://localhost:8080/v3/api-docs`

## 프로젝트 구조
```
src/
├── main/
│   ├── kotlin/
│   │   └── blog/
│   │       ├── config/     # 설정 클래스
│   │       ├── domain/     # 도메인 모델
│   │       ├── repository/ # 데이터 접근 계층
│   │       ├── service/    # 비즈니스 로직
│   │       └── web/        # API 엔드포인트
│   └── resources/
│       └── application.yml # 애플리케이션 설정
└── test/                   # 테스트 코드
```

## 문서
자세한 문서는 다음 위치에서 확인할 수 있습니다:
- API 문서: `/docs/dokka`
- 모듈 정보: `Module.md`
- 개발 가이드: `/docs/development.md`
- 보안 가이드: `/docs/security.md`
- 데이터베이스 가이드: `/docs/database.md`

## 라이선스
이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

