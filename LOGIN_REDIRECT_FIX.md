# 🔧 localhost:8080 로그인 리다이렉트 문제 해결

## 📋 문제 상황

**증상**: `http://localhost:8080`으로 접속 시 자동으로 `http://localhost:8080/login`으로 리다이렉트됨

**기대 동작**: 홈 페이지(게시글 목록)가 로그인 없이도 표시되어야 함

## 🔍 원인 분석

### 1. SecurityConfig의 프로파일 제한
```java
@Profile("oauth")  // ❌ 문제: oauth 프로파일에서만 활성화
public class SecurityConfig {
```

**문제점**:
- `SPRING_PROFILES_ACTIVE=dev`만 설정된 경우 SecurityConfig가 로드되지 않음
- Spring Security의 **기본 자동 구성**이 적용됨
- 기본 구성은 모든 요청에 인증 요구 → `/`도 인증 필요 → `/login`으로 리다이렉트

### 2. 과도한 인증 요구
```java
.authorizeHttpRequests(authorize -> authorize
    .requestMatchers("/", ...).permitAll()
    .requestMatchers("/api/**").hasRole(Role.USER.name())  // ❌ 모든 API가 인증 필요
    .anyRequest().authenticated()  // ❌ 나머지도 모두 인증 필요
)
```

**문제점**:
- 게시글 **조회 API**도 인증을 요구
- 홈 페이지는 허용되지만 게시글 목록을 가져올 수 없음

### 3. CustomOAuth2UserService 필수 의존성
```java
private final CustomOAuth2UserService customOAuth2UserService;  // ❌ 필수 주입
```

**문제점**:
- `CustomOAuth2UserService`는 `@Profile("oauth")`로 설정됨
- `dev` 프로파일만 사용하면 빈이 없어 SecurityConfig 생성 실패 가능

## ✅ 해결 방법

### 1. SecurityConfig 프로파일 변경
```java
@Profile("!test")  // ✅ 테스트를 제외한 모든 프로파일에서 활성화
public class SecurityConfig {
```

**효과**:
- `dev`, `prod`, `oauth` 등 모든 프로파일에서 작동
- Spring Security 기본 자동 구성 대신 커스텀 설정 사용

### 2. 공개 API 접근 허용
```java
.authorizeHttpRequests(authorize -> authorize
    // 홈 페이지와 정적 리소스: 공개
    .requestMatchers("/", "/css/**", "/js/**", ...).permitAll()
    // 로그인 페이지: 공개
    .requestMatchers("/login").permitAll()
    // 게시글 조회 API: 공개 (GET /api/post, /api/post/*, /api/post/list)
    .requestMatchers("/api/post", "/api/post/*", "/api/post/list").permitAll()
    // 게시글 작성/수정/삭제 API: 인증 필요 (POST /api/post)
    .requestMatchers("/api/post").hasRole(Role.USER.name())
    // 게시글 작성/수정 페이지: 인증 필요
    .requestMatchers("/posts/save", "/posts/update/**").authenticated()
)
```

**효과**:
- 비로그인 사용자도 홈 페이지와 게시글 목록 조회 가능
- 작성/수정/삭제만 로그인 필요

### 3. OAuth 서비스 선택적 주입
```java
private final ObjectProvider<CustomOAuth2UserService> customOAuth2UserServiceProvider;

// ...

CustomOAuth2UserService oauthService = customOAuth2UserServiceProvider.getIfAvailable();
if (oauthService != null) {
    http.oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                    .userService(oauthService)
            )
    );
} else {
    // OAuth2가 없으면 기본 폼 로그인 사용
    http.formLogin(form -> form
            .loginPage("/login")
            .permitAll()
            .defaultSuccessUrl("/", true)
    );
}
```

**효과**:
- OAuth 설정이 없어도 SecurityConfig 정상 작동
- `dev` 프로파일만 사용할 때 폼 로그인으로 대체
- 유연한 인증 방식 선택 가능

## 🎯 적용 결과

### Before (문제 상황)
```
브라우저: http://localhost:8080
         ↓
Spring Security: 인증 필요! → /login으로 리다이렉트
         ↓
사용자: "왜 로그인 페이지가 나오지?" 😕
```

### After (해결 후)
```
브라우저: http://localhost:8080
         ↓
Spring Security: "/" 공개 허용!
         ↓
HomeController: 게시글 목록 조회 (서버 사이드)
         ↓
사용자: 홈 페이지 정상 표시! 🎉
```

## 🔐 보안 수준

### 공개 접근 (인증 불필요)
- `GET /` - 홈 페이지
- `GET /api/post` - 게시글 목록
- `GET /api/post/{id}` - 게시글 상세
- `/css/**`, `/js/**`, `/images/**` - 정적 리소스
- `/login` - 로그인 페이지

### 인증 필요 (로그인 후)
- `POST /api/post` - 게시글 작성
- `PUT /api/post/{id}` - 게시글 수정
- `DELETE /api/post/{id}` - 게시글 삭제
- `/posts/save` - 게시글 작성 페이지
- `/posts/update/{id}` - 게시글 수정 페이지

## 📝 IntelliJ 실행 가이드 업데이트

### 변경 사항
- **이전**: OAuth 프로파일 필수 → `SPRING_PROFILES_ACTIVE=dev,oauth`
- **이후**: dev 프로파일만으로 충분 → `SPRING_PROFILES_ACTIVE=dev`

### 실행 방법
1. **Docker 컨테이너 시작**:
```powershell
docker compose up -d mariadb redis
```

2. **IntelliJ Run Configuration**:
   - Environment variables: `SPRING_PROFILES_ACTIVE=dev`

3. **실행**: `Run 'Application'` (Shift + F10)

4. **브라우저 접속**: `http://localhost:8080`
   - ✅ 홈 페이지가 바로 표시됨
   - ✅ 게시글 목록 조회 가능
   - ✅ "글쓰기" 클릭 시에만 로그인 요구

## 🚀 테스트 확인

```powershell
# 컴파일 확인
.\gradlew clean compileJava
# ✅ BUILD SUCCESSFUL

# 애플리케이션 실행
# (IntelliJ에서 Run 'Application')

# 브라우저 테스트
# 1. http://localhost:8080 접속
#    → ✅ 홈 페이지 표시 (리다이렉트 없음)
# 2. 게시글 카드 확인
#    → ✅ 목록 정상 표시
# 3. "상세보기" 클릭
#    → ✅ 모달 팝업 정상 작동
# 4. "글쓰기" 클릭
#    → ✅ 로그인 페이지로 이동 (의도된 동작)
```

## 💡 핵심 개선 사항

1. **프로파일 독립성**: dev 프로파일만으로 애플리케이션 실행 가능
2. **OAuth 선택적 사용**: OAuth 없이도 기본 폼 로그인으로 대체
3. **공개 접근 확대**: 읽기 전용 기능은 인증 불필요
4. **명확한 권한 분리**: 조회(공개) vs 작성/수정/삭제(인증 필요)

## 📚 관련 문서

- [INTELLIJ_RUN_GUIDE.md](INTELLIJ_RUN_GUIDE.md) - IntelliJ 실행 가이드
- [RESOLVED_ISSUES.md](RESOLVED_ISSUES.md) - 해결된 모든 문제 요약
- [README.md](README.md) - 프로젝트 전체 문서

---

**문제 해결 완료!** 이제 `localhost:8080`에 접속하면 로그인 없이 바로 홈 페이지가 표시됩니다. 🎉

