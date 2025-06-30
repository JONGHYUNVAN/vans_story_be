# OAuth 도메인 설계 및 플로우 문서

## 📋 개요

이 문서는 MSA 환경에서 OAuth 소셜 로그인을 지원하기 위해 설계된 OAuth 도메인의 User Flow와 Data Flow를 상세하게 설명합니다.

**핵심 정책**: OAuth 로그인은 기존 사용자가 사전에 link를 통해 연동을 완료한 계정만 허용됩니다. 새로운 OAuth 계정으로 자동 가입은 지원하지 않습니다.

### 🏗️ 아키텍처 전제 조건

- **중간 서버**: OAuth 제공업체(Google, Kakao, Naver 등)와의 실제 OAuth 인증 처리
- **백엔드 서버**: 중간 서버로부터 `provider`와 `providerId`만 받아서 사용자 관리 및 JWT 토큰 발급
- **기존 인증 시스템**: JWT 기반 인증 시스템과 완전 호환

## 🎯 설계 목표

1. **기존 시스템 유지**: 현재 JWT 인증 시스템을 그대로 활용
2. **유연한 연동**: 일반 계정과 OAuth 계정 간 자유로운 연결/해제
3. **다중 OAuth 지원**: 한 사용자가 여러 OAuth 제공업체 계정 연결 가능
4. **중복 방지**: 같은 OAuth 계정은 하나의 사용자에만 연결
5. **명시적 연동**: OAuth 로그인은 사전 연동된 계정만 허용 (자동 가입 없음)

## 🗄️ 데이터 모델

### User-OAuth 관계
```Users (1) ←→ (N) UserOAuths
```

### UserOAuth 엔티티
```kotlin
class UserOAuth {
    val id: Long                    // 자동 생성 ID
    val userId: Long               // Users 테이블 참조
    val provider: String           // OAuth 제공업체 (google, kakao, naver)
    val providerId: String         // OAuth 제공업체의 사용자 ID
    val providerEmail: String?     // OAuth 제공업체 이메일 (현재 미사용)
    val createdAt: LocalDateTime   // 연동 생성 시간
    val updatedAt: LocalDateTime   // 연동 수정 시간
}
```

### 제약 조건
- `(provider, providerId)` 복합 유니크 인덱스
- 같은 OAuth 계정은 하나의 사용자에만 연결 가능

## 🔄 API 엔드포인트 및 플로우

### 1. OAuth 소셜 로그인
> `POST /api/v1/oauth/login`

#### 📝 목적
중간 서버에서 OAuth 인증을 완료한 후, 백엔드에서 사용자를 식별하고 JWT 토큰을 발급합니다.

#### 📊 User Flow
```
📱 사용자
    ↓
🌐 프론트엔드 (OAuth 로그인 버튼 클릭)
    ↓
🔀 중간 서버 (OAuth 인증 처리)
    ↓ ⟷ OAuth 제공업체 (Google, Kakao 등)
    ↓
🖥️ 백엔드 (/oauth/login)
    ↓
❓ 기존 OAuth 연동 확인
    ├─ ✅ 기존 연동 → JWT 토큰 발급 → 🎉 로그인 완료
    └─ ❌ 연동 없음 → ⚠️ 오류 응답 (연동 필요)
```

#### 🔄 Data Flow

**Request**
```json
{
  "provider": "google",
  "providerId": "google_user_12345"
}
```

**Processing Flow**
1. **OAuth 연동 정보 조회**
   ```kotlin
   // Exposed ORM을 통한 조회
   val existingOAuth = oauthRepository.findByProviderAndProviderId("google", "google_user_12345")
   ```

2. **Case A: 기존 OAuth 연동 존재**
   - 연결된 User 정보 조회
   - JWT 토큰 발급 (Access Token + Refresh Token)
   - 응답 헤더에 토큰 설정

3. **Case B: 연동되지 않은 OAuth 계정**
   - CustomException 발생: "연동되지 않은 OAuth 계정입니다. 먼저 기존 계정에 OAuth 연동을 설정해주세요."
   - 400 Bad Request 응답

**Response (성공)**
```http
HTTP/1.1 200 OK
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Set-Cookie: refreshToken=eyJhbGciOiJIUzI1NiIs...; HttpOnly; Secure; Path=/

{
  "success": true,
  "data": null,
  "message": "OAuth 로그인이 완료되었습니다."
}
```

**Response (연동 없음)**
```http
HTTP/1.1 400 Bad Request

{
  "success": false,
  "data": null,
  "message": "연동되지 않은 OAuth 계정입니다. 먼저 기존 계정에 OAuth 연동을 설정해주세요."
}
```

---

### 2. OAuth 계정 연결
> `POST /api/v1/oauth/link`

#### 📝 목적
이미 로그인된 사용자가 추가로 OAuth 계정을 연결합니다. (예: 일반 회원가입 후 구글 계정 추가 연결)

#### 📊 User Flow
```
👤 로그인된 사용자
    ↓
⚙️ 설정 페이지 (계정 연결 메뉴)
    ↓
🔗 OAuth 계정 연결 요청
    ↓
🔀 중간 서버 (OAuth 인증)
    ↓ ⟷ OAuth 제공업체
    ↓
🖥️ 백엔드 (/oauth/link)
    ↓
🔍 중복 연결 체크
    ├─ ❌ 이미 연결됨 → 오류 응답
    └─ ✅ 연결 가능 → OAuth 연동 저장
    ↓
🎉 연결 완료
```

#### 🔄 Data Flow

**Request**
```json
POST /api/v1/oauth/link
Authorization: Bearer <access_token>

{
  "provider": "kakao",
  "providerId": "kakao_user_67890"
}
```

**Processing Flow**
1. **JWT 토큰에서 사용자 ID 추출**
   ```kotlin
   // UserDetails의 username을 Long으로 변환 (현재 구현)
   val userId = userDetails.username.toLongOrNull()
       ?: throw IllegalArgumentException("유효하지 않은 사용자 정보입니다.")
   ```

2. **중복 연결 체크**
   ```kotlin
   // 이미 다른 사용자에게 연결된 OAuth 계정인지 확인
   oauthRepository.existsByProviderAndProviderId("kakao", "kakao_user_67890")
   
   // 현재 사용자가 이미 같은 제공업체로 연결했는지 확인
   oauthRepository.existsByUserIdAndProvider(123, "kakao")
   ```

3. **OAuth 연동 정보 저장**
   ```kotlin
   // Exposed ORM을 통한 저장
   oauthRepository.save(
       userId = 123,
       provider = "kakao", 
       providerId = "kakao_user_67890",
       providerEmail = null
   )
   ```

**Response**
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
  "message": "OAuth 계정이 성공적으로 연결되었습니다."
}
```

---

### 3. OAuth 계정 연결 해제
> `DELETE /api/v1/oauth/unlink`

#### 📝 목적
연결된 OAuth 계정을 해제합니다. (단, 최소 1개의 로그인 수단은 유지되어야 함)

#### 📊 User Flow
```
👤 로그인된 사용자
    ↓
⚙️ 설정 페이지
    ↓
📋 연결된 계정 목록 조회
    ↓
🗑️ 특정 OAuth 계정 해제 선택
    ↓
🖥️ 백엔드 (/oauth/unlink)
    ↓
🔍 연결된 계정 존재 확인
    ├─ ❌ 존재하지 않음 → 오류 응답
    └─ ✅ 존재함 → OAuth 연동 삭제
    ↓
🎉 해제 완료
```

#### 🔄 Data Flow

**Request**
```json
DELETE /api/v1/oauth/unlink
Authorization: Bearer <access_token>

{
  "provider": "google"
}
```

**Processing Flow**
1. **JWT 토큰에서 사용자 ID 추출**
   ```kotlin
   // Controller의 extractUserId 메서드 사용
   val userId = extractUserId(userDetails)
   ```
2. **연결된 OAuth 계정 확인**
   ```kotlin
   // OAuth 계정 존재 확인
   oauthRepository.findByUserIdAndProvider(123, "google")
   ```
3. **OAuth 연동 정보 삭제**
   ```kotlin
   // OAuth 연동 삭제
   oauthRepository.deleteByUserIdAndProvider(123, "google")
   ```

**Response**
```json
{
  "success": true,
  "data": null,
  "message": "OAuth 계정 연결이 해제되었습니다."
}
```

---

### 4. 연결된 OAuth 계정 조회
> `GET /api/v1/oauth/linked`

#### 📝 목적
현재 사용자에게 연결된 모든 OAuth 계정 목록을 조회합니다.

#### 📊 User Flow
```
👤 로그인된 사용자
    ↓
⚙️ 설정 페이지 접근
    ↓
🖥️ 백엔드 (/oauth/linked)
    ↓
🔍 연결된 계정 목록 조회 (DB)
    ↓
📤 목록 반환 (JSON)
    ↓
🖼️ UI에 표시
```

#### 🔄 Data Flow

**Request**
```http
GET /api/v1/oauth/linked
Authorization: Bearer <access_token>
```

**Processing Flow**
1. **JWT 토큰에서 사용자 ID 추출**
   ```kotlin
   // Controller의 extractUserId 메서드 사용
   val userId = extractUserId(userDetails)
   ```
2. **사용자의 모든 OAuth 연동 정보 조회**
   ```kotlin
   // 사용자의 모든 OAuth 계정 조회
   oauthRepository.findAllByUserId(123)
   ```

**Response**
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
  "message": "연결된 OAuth 계정을 조회했습니다."
}
```

## 🔐 보안 고려사항

### 1. 중복 연결 방지
- `(provider, providerId)` 복합 유니크 제약으로 DB 레벨에서 방지
- 애플리케이션 레벨에서도 사전 체크

### 2. JWT 토큰 보안
- Access Token: Authorization 헤더 (짧은 만료 시간)
- Refresh Token: HttpOnly 쿠키 (긴 만료 시간, XSS 방지)

### 3. OAuth 계정 검증
- 중간 서버에서 OAuth 제공업체와의 실제 인증 완료 후에만 요청
- `providerId`는 OAuth 제공업체에서 검증된 값만 사용

## 📈 확장 가능성

### 1. 이메일 정보 추가
향후 중간 서버에서 이메일 정보도 제공할 경우:
- `providerEmail` 필드 활용
- 계정 병합 로직 추가 가능

### 2. 프로필 정보 동기화
OAuth 제공업체의 프로필 정보(이름, 프로필 이미지 등) 동기화 기능

### 3. 소셜 기능 확장
- 친구 찾기 (같은 OAuth 제공업체 사용자)
- 소셜 공유 기능

## 🚨 예외 상황 처리

### 1. OAuth 로그인 실패
- 중간 서버에서 잘못된 `providerId` 전달
- 데이터베이스 연결 오류
- JWT 토큰 생성 실패

### 2. 계정 연결 실패
- 이미 다른 사용자에게 연결된 OAuth 계정
- 동일한 제공업체로 이미 연결된 상태
- 인증되지 않은 사용자의 요청

### 3. 계정 해제 실패
- 존재하지 않는 OAuth 연결 정보
- 마지막 로그인 수단 해제 시도 (향후 구현 예정)

## 🎯 결론

이 OAuth 도메인은 **최소한의 정보(provider + providerId)**만으로 **완전한 OAuth 소셜 로그인 시스템**을 제공합니다. 기존 JWT 인증 시스템과 완벽하게 호환되며, 사용자는 일반 계정과 OAuth 계정을 자유롭게 연결/해제할 수 있습니다.

**핵심 장점:**
- 🔒 **보안**: JWT + HttpOnly 쿠키 조합
- 🔄 **유연성**: 다중 OAuth 계정 지원
- 🚀 **확장성**: 새로운 OAuth 제공업체 쉽게 추가 가능
- 🛠️ **호환성**: 기존 인증 시스템과 완전 호환 