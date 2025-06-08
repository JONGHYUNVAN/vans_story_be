# Module blog.vans_story_be

Vans Story 블로그의 백엔드 API 서버입니다.

## 패키지 구조

### blog.vans_story_be.domain
도메인 모델 및 비즈니스 로직을 포함하는 패키지입니다.
- 사용자 관리
- 게시글 관리
- 댓글 관리
- 태그 관리
- 카테고리 관리

### blog.vans_story_be.config
애플리케이션 설정을 포함하는 패키지입니다.
- 보안 설정
- Swagger 설정
- 데이터베이스 설정
- CORS 설정

### blog.vans_story_be.web
API 엔드포인트 및 컨트롤러를 포함하는 패키지입니다.
- REST API 구현
- 요청/응답 처리
- API 문서화

### blog.vans_story_be.security
보안 관련 설정 및 구현을 포함하는 패키지입니다.
- JWT 인증
- API Key 인증
- 권한 관리

### blog.vans_story_be.common
공통 유틸리티 및 예외 처리를 포함하는 패키지입니다.
- 예외 처리
- 응답 포맷
- 유틸리티 클래스

## 기술 스택

- Kotlin 1.9.22
- Spring Boot 3.5.0
- Exposed (Kotlin SQL 프레임워크)
- MariaDB
- JWT
- MapStruct
- Kotlin Coroutines
- SpringDoc OpenAPI

## 주요 기능

- 사용자 인증 및 권한 관리
  - JWT 기반 인증
  - API Key 인증
  - 역할 기반 접근 제어
- 게시글 관리
  - CRUD 작업
  - 태그 및 카테고리 연동
  - 마크다운 지원
- 댓글 시스템
  - 계층형 댓글
  - 댓글 수정/삭제
- 파일 관리
  - 이미지 업로드
  - 파일 저장소 연동
- API 문서화
  - Swagger UI
  - OpenAPI 3.0 명세 