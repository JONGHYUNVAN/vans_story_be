# API 문서

## 목차
- [개요](#개요)
- [공통 응답 형식](#공통-응답-형식)
- [인증 API](#인증-api)
- [OAuth API](#oauth-api)
- [사용자 API](#사용자-api)
- [에러 코드](#에러-코드)
- [예제 코드](#예제-코드)

## 개요

### 기본 정보
- **Base URL**: `http://localhost:8080`
- **API Version**: `v1`
- **Content-Type**: `application/json`
- **Authentication**: JWT Bearer Token

### 인증 방식
- **Access Token**: `Authorization: Bearer <token>` 헤더로 전송
- **Refresh Token**: HTTP-Only 쿠키로 전송
- **Token 만료 시간**:
  - Access Token: 5시간 (18000초)
  - Refresh Token: 7일 (604800초)

## 공통 응답 형식

모든 API는 다음과 같은 공통 응답 형식을 사용합니다:

```json
{
  "success": true,
  "data": { ... },
  "message": null
}
```

### 응답 필드
| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | boolean | API 호출 성공 여부 |
| `data` | T | 응답 데이터 (성공 시에만 포함) |
| `message` | string | 에러 메시지 (실패 시에만 포함) |

## 인증 API

Base URL: `/api/v1/auth`

### 1. 로그인

사용자 인증을 처리하고 JWT 토큰을 발급합니다.

```http
POST /api/v1/auth/login
```

#### 요청 본문
```json
{
  "email": "user@example.com",
  "password": "Password1!"
}
```

#### 요청 필드 검증
| 필드 | 타입 | 필수 | 제약 조건 |
|------|------|------|----------|
| `email` | string | ✅ | 유효한 이메일 형식 |
| `password` | string | ✅ | 8자 이상, 영문/숫자/특수문자 조합 |

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": null,
  "message": null
}
```

**응답 헤더:**
- `Authorization: Bearer eyJhbGciOiJIUzI1NiIs...`

**응답 쿠키:**
- `refreshToken=eyJhbGciOiJIUzI1NiIs...; HttpOnly; Secure; Path=/`

#### 실패 응답 (401 Unauthorized)
```json
{
  "success": false,
  "data": null,
  "message": "이메일 또는 비밀번호가 올바르지 않습니다"
}
```

---

### 2. 토큰 갱신

Refresh Token을 사용하여 새로운 Access Token을 발급합니다.

```http
POST /api/v1/auth/refresh
```

#### 요청 조건
- **쿠키**: `refreshToken=<refresh_token>`

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": null,
  "message": null
}
```

**응답 헤더:**
- `Authorization: Bearer <new_access_token>`

**응답 쿠키:**
- `refreshToken=<new_refresh_token>; HttpOnly; Secure; Path=/`

#### 실패 응답 (401 Unauthorized)
```json
{
  "success": false,
  "data": null,
  "message": "유효하지 않은 토큰입니다"
}
```

---

### 3. 로그아웃

사용자 로그아웃을 처리하고 Refresh Token을 만료시킵니다.

```http
POST /api/v1/auth/logout
```

#### 성공 응답 (204 No Content)
- 응답 본문 없음

**응답 쿠키:**
- `refreshToken=; Max-Age=0; HttpOnly; Secure; Path=/`

---

## OAuth API

Base URL: `/api/v1/oauth`

OAuth 소셜 로그인을 통한 사용자 인증 및 계정 연동을 관리합니다.

### 1. OAuth 소셜 로그인 (임시 코드 발급)

OAuth 제공업체를 통해 로그인하고 임시 인증 코드를 발급합니다.

```http
POST /api/v1/oauth/login
```

#### 요청 본문
```json
{
  "provider": "google",
  "providerId": "google_user_12345"
}
```

#### 요청 필드 검증
| 필드 | 타입 | 필수 | 제약 조건 |
|------|------|------|----------|
| `provider` | string | ✅ | 50자 이하 (google, kakao, naver 등) |
| `providerId` | string | ✅ | 100자 이하, OAuth 제공업체 사용자 ID |

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": {
    "code": "oauth_temp_abc123def456"
  },
  "message": null
}
```

#### 실패 응답 (400 Bad Request)
```json
{
  "success": false,
  "data": null,
  "message": "OAuth 제공업체는 필수입니다"
}
```

---

### 2. 임시 코드를 JWT 토큰으로 교환

OAuth 로그인에서 발급받은 임시 코드를 실제 JWT 토큰으로 교환합니다.

```http
POST /api/v1/oauth/exchange
```

#### 요청 본문
```json
{
  "code": "oauth_temp_abc123def456"
}
```

#### 요청 필드 검증
| 필드 | 타입 | 필수 | 제약 조건 |
|------|------|------|----------|
| `code` | string | ✅ | 임시 인증 코드 (5분 후 만료) |

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": null,
  "message": null
}
```

**응답 헤더:**
- `Authorization: Bearer eyJhbGciOiJIUzI1NiIs...`

**응답 쿠키:**
- `refreshToken=eyJhbGciOiJIUzI1NiIs...; HttpOnly; Secure; Path=/`

#### 실패 응답 (400 Bad Request)
```json
{
  "success": false,
  "data": null,
  "message": "유효하지 않은 인증 코드입니다"
}
```

#### 실패 응답 (401 Unauthorized)
```json
{
  "success": false,
  "data": null,
  "message": "만료된 인증 코드입니다"
}
```

---

### 3. OAuth 계정 연결

현재 로그인된 사용자에게 OAuth 계정을 연결합니다.

```http
POST /api/v1/oauth/link
Authorization: Bearer <access_token>
```

#### 요청 본문
```json
{
  "provider": "kakao",
  "providerId": "kakao_user_67890"
}
```

#### 요청 필드 검증
| 필드 | 타입 | 필수 | 제약 조건 |
|------|------|------|----------|
| `provider` | string | ✅ | 50자 이하 (google, kakao, naver 등) |
| `providerId` | string | ✅ | 100자 이하, OAuth 제공업체 사용자 ID |

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 45,
    "userId": 123,
    "provider": "kakao",
    "providerId": "kakao_user_67890",
    "providerEmail": null,
    "createdAt": "2025-01-09T10:30:00",
    "updatedAt": "2025-01-09T10:30:00"
  },
  "message": null
}
```

#### 실패 응답 (409 Conflict)
```json
{
  "success": false,
  "data": null,
  "message": "이미 다른 계정에 연결된 OAuth 계정입니다"
}
```

---

### 4. OAuth 계정 연결 해제

현재 로그인된 사용자의 OAuth 계정 연결을 해제합니다.

```http
DELETE /api/v1/oauth/unlink
Authorization: Bearer <access_token>
```

#### 요청 본문
```json
{
  "provider": "google"
}
```

#### 요청 필드 검증
| 필드 | 타입 | 필수 | 제약 조건 |
|------|------|------|----------|
| `provider` | string | ✅ | 50자 이하 (google, kakao, naver 등) |

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": null,
  "message": null
}
```

#### 실패 응답 (404 Not Found)
```json
{
  "success": false,
  "data": null,
  "message": "연결된 google 계정이 없습니다"
}
```

---

### 5. 연결된 OAuth 계정 조회

현재 로그인된 사용자의 연결된 OAuth 계정 목록을 조회합니다.

```http
GET /api/v1/oauth/linked
Authorization: Bearer <access_token>
```

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": {
    "linkedAccounts": [
      {
        "provider": "google",
        "providerEmail": null,
        "createdAt": "2025-01-08T15:20:00"
      },
      {
        "provider": "kakao",
        "providerEmail": null,
        "createdAt": "2025-01-09T10:30:00"
      }
    ]
  },
  "message": null
}
```

#### 응답 필드
| 필드 | 타입 | 설명 |
|------|------|------|
| `linkedAccounts` | array | 연결된 OAuth 계정 목록 |
| `provider` | string | OAuth 제공업체명 |
| `providerEmail` | string\|null | OAuth 제공업체 이메일 (현재 미사용) |
| `createdAt` | string | 연결 생성 시간 (ISO 8601 형식) |

---

## 사용자 API

Base URL: `/api/v1/users`

### 1. 사용자 목록 조회

모든 사용자 목록을 조회합니다. (관리자만 접근 가능)

```http
GET /api/v1/users
Authorization: Bearer <access_token>
```

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "홍길동",
      "email": "user1@example.com",
      "nickname": "길동이",
      "role": "USER",
      "createdAt": "2024-03-19 10:00:00",
      "updatedAt": "2024-03-19 10:00:00"
    },
    {
      "id": 2,
      "name": "김철수",
      "email": "admin@example.com",
      "nickname": "철수",
      "role": "ADMIN",
      "createdAt": "2024-03-18 09:00:00",
      "updatedAt": "2024-03-18 09:00:00"
    }
  ],
  "message": null
}
```

---

### 2. 사용자 생성

새로운 사용자를 생성합니다. 

```http
POST /api/v1/users
Authorization: Bearer <access_token>
Content-Type: application/json
```

#### 요청 본문
```json
{
  "name": "홍길동",
  "email": "user@example.com",
  "password": "Password1!",
  "nickname": "길동이"
}
```

#### 요청 필드 검증
| 필드 | 타입 | 필수 | 제약 조건 |
|------|------|------|----------|
| `email` | string | ✅ | 유효한 이메일 형식 |
| `password` | string | ✅ | 8자 이상, 영문/숫자/특수문자 조합 |
| `nickname` | string | ✅ | 2-50자 |

#### 성공 응답 (201 Created)
```json
{
  "success": true,
  "data": null,
  "message": null
}
```

---

### 3. 사용자 정보 조회

특정 사용자의 정보를 조회합니다.

```http
GET /api/v1/users/{id}
Authorization: Bearer <access_token>
```

#### 경로 매개변수
| 매개변수 | 타입 | 설명 |
|----------|------|------|
| `id` | number | 사용자 고유 식별자 |

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "user@example.com",
    "nickname": "길동이",
    "role": "USER",
    "createdAt": "2024-03-19 10:00:00",
    "updatedAt": "2024-03-19 10:00:00"
  },
  "message": null
}
```

---

### 4. 이메일로 닉네임 조회

이메일을 통해 사용자의 닉네임을 조회합니다.

```http
GET /api/v1/users/email/{email}
```

#### 경로 매개변수
| 매개변수 | 타입 | 설명 |
|----------|------|------|
| `email` | string | 사용자 이메일 주소 |

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": "길동이",
  "message": null
}
```

---

### 5. 사용자 정보 수정

사용자 정보를 수정합니다. (본인 또는 관리자만 가능)

```http
PATCH /api/v1/users/{id}
Authorization: Bearer <access_token>
Content-Type: application/json
```

#### 요청 본문
```json
{
  "password": "NewPassword1!",
  "email": "newemail@example.com",
  "nickname": "새로운닉네임",
  "role": "USER"
}
```

#### 요청 필드 (모든 필드 선택사항)
| 필드 | 타입 | 제약 조건 |
|------|------|----------|
| `password` | string | 8자 이상, 영문/숫자/특수문자 조합 |
| `email` | string | 유효한 이메일 형식 |
| `nickname` | string | 2-50자 |
| `role` | string | "USER" 또는 "ADMIN" |

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "newemail@example.com",
    "nickname": "새로운닉네임",
    "role": "USER",
    "createdAt": "2024-03-19 10:00:00",
    "updatedAt": "2024-03-20 14:30:00"
  },
  "message": null
}
```

---

### 6. 사용자 삭제

사용자를 삭제합니다. (본인 또는 관리자만 가능)

```http
DELETE /api/v1/users/{id}
Authorization: Bearer <access_token>
```

#### 성공 응답 (204 No Content)
- 응답 본문 없음

---

## 에러 코드

### HTTP 상태 코드

| 상태 코드 | 설명 | 예시 |
|-----------|------|------|
| 200 | 성공 | 요청 처리 완료 |
| 201 | 생성됨 | 리소스 생성 완료 |
| 204 | 내용 없음 | 삭제 처리 완료 |
| 400 | 잘못된 요청 | 입력 값 검증 실패 |
| 401 | 인증 실패 | 토큰 없음 또는 만료 |
| 403 | 권한 없음 | 접근 권한 부족 |
| 404 | 찾을 수 없음 | 존재하지 않는 리소스 |
| 500 | 서버 오류 | 내부 서버 에러 |

### 공통 에러 응답

#### 400 Bad Request
```json
{
  "success": false,
  "data": null,
  "message": "이메일은 필수입니다"
}
```

#### 401 Unauthorized
```json
{
  "success": false,
  "data": null,
  "message": "인증이 필요합니다"
}
```

#### 403 Forbidden
```json
{
  "success": false,
  "data": null,
  "message": "접근 권한이 없습니다"
}
```

#### 404 Not Found
```json
{
  "success": false,
  "data": null,
  "message": "사용자를 찾을 수 없습니다"
}
```

---

## 예제 코드

### cURL

#### 로그인
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password1!"
  }'
```

#### 사용자 목록 조회
```bash
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### 사용자 생성
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "name": "홍길동",
    "email": "user@example.com",
    "password": "Password1!",
    "nickname": "길동이"
  }'
```

### OAuth API 예제

#### OAuth 소셜 로그인 (임시 코드 발급)
```bash
curl -X POST http://localhost:8080/api/v1/oauth/login \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "providerId": "google_user_12345"
  }'
```

#### 임시 코드를 JWT 토큰으로 교환
```bash
curl -X POST http://localhost:8080/api/v1/oauth/exchange \
  -H "Content-Type: application/json" \
  -d '{
    "code": "oauth_temp_abc123def456"
  }'
```

#### OAuth 계정 연결
```bash
curl -X POST http://localhost:8080/api/v1/oauth/link \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "provider": "kakao",
    "providerId": "kakao_user_67890"
  }'
```

#### OAuth 계정 연결 해제
```bash
curl -X DELETE http://localhost:8080/api/v1/oauth/unlink \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "provider": "google"
  }'
```

#### 연결된 OAuth 계정 조회
```bash
curl -X GET http://localhost:8080/api/v1/oauth/linked \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 추가 정보

### Swagger UI
개발 환경에서는 Swagger UI를 통해 API를 테스트할 수 있습니다:
- **URL**: http://localhost:8080/swagger-ui.html

### 개발 도구
- **Postman Collection**: 프로젝트 루트의 `postman/` 디렉토리 참조
- **API 테스트**: `src/test/kotlin/` 디렉토리의 통합 테스트 참조 