# ✅ Flyway 버전 충돌 문제 해결 완료

## 🔴 발생한 오류

```
***************************
APPLICATION FAILED TO START
***************************

An attempt was made to call a method that does not exist.
org.flywaydb.database.mysql.mariadb.MariaDBDatabaseType.handlesJDBCUrl
'boolean org.flywaydb.core.internal.util.UrlUtils.isSecretManagerUrl(java.lang.String, java.lang.String)'

flyway-mysql-11.7.2.jar vs flyway-core-10.21.0.jar
```

## 🔍 원인 분석

**버전 불일치**:
- `flyway-core`: 10.21.0 (명시적으로 지정됨)
- `flyway-mysql`: 11.7.2 (Spring Boot가 관리하는 최신 버전)

**문제점**:
- `flyway-mysql` 11.7.2가 `flyway-core` 11.x 버전의 API를 사용
- `flyway-core` 10.21.0에는 해당 메서드(`isSecretManagerUrl`)가 존재하지 않음
- 결과: `NoSuchMethodError` 발생으로 애플리케이션 시작 실패

## ✅ 해결 방법

### 변경 내용: `build.gradle`

**Before** (문제 있는 코드):
```groovy
// Database
runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
implementation 'org.flywaydb:flyway-mysql'

// Flyway for DB migrations (최신 버전으로 업그레이드 - MariaDB 10.11 지원)
implementation 'org.flywaydb:flyway-core:10.21.0'  // ❌ 명시적 버전 지정
```

**After** (해결됨):
```groovy
// Database
runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'

// Flyway for DB migrations - Spring Boot manages compatible versions
implementation 'org.flywaydb:flyway-core'        // ✅ 버전 관리 위임
implementation 'org.flywaydb:flyway-mysql'
```

### 핵심 원칙

**Spring Boot Dependency Management 활용**:
- Spring Boot 3.5.7은 호환되는 Flyway 버전을 자동으로 관리
- 명시적 버전 지정 제거 → Spring Boot가 `flyway-core`와 `flyway-mysql`의 호환 버전 선택
- 결과: 두 라이브러리 모두 동일한 버전(예: 10.x 또는 11.x)으로 통일

## 🧪 검증 결과

```powershell
# 의존성 새로고침 및 컴파일
.\gradlew clean compileJava --refresh-dependencies --no-daemon
# ✅ BUILD SUCCESSFUL

# 테스트 실행
.\gradlew test --no-daemon --tests "com.kraft.web.PostApiControllerTest"
# ✅ BUILD SUCCESSFUL - 모든 테스트 통과
```

## 📝 교훈

### ✅ 올바른 방법
1. **Spring Boot BOM(Bill of Materials) 신뢰**
   - Spring Boot가 관리하는 의존성은 버전을 명시하지 않음
   - 자동으로 테스트된 호환 버전 조합 사용

2. **버전 명시가 필요한 경우**
   - Spring Boot가 관리하지 않는 라이브러리만 명시
   - 또는 특정 버그 픽스가 필요한 경우

### ❌ 피해야 할 실수
- Spring Boot가 관리하는 의존성에 임의로 버전 지정
- 일부만 버전 고정하고 나머지는 자동 관리 → 버전 불일치 발생

## 🚀 IntelliJ에서 실행하기

이제 Flyway 버전 충돌이 해결되었으므로 정상적으로 실행됩니다:

1. **Docker 컨테이너 시작**:
```powershell
docker compose up -d mariadb redis
```

2. **IntelliJ Run Configuration**:
   - Environment variables: `SPRING_PROFILES_ACTIVE=dev`

3. **실행**: `Run 'Application'` (Shift + F10)

예상 로그:
```
✓ HikariPool-1 - Start completed
✓ Flyway migration completed successfully
✓ Tomcat started on port(s): 8080
✓ Started Application in X.XXX seconds
```

## 📚 참고 문서

- [Spring Boot Dependency Management](https://docs.spring.io/spring-boot/docs/current/reference/html/dependency-versions.html)
- [Flyway Version Compatibility](https://documentation.red-gate.com/flyway)
- `INTELLIJ_RUN_GUIDE.md` - 전체 실행 가이드
- `RESOLVED_ISSUES.md` - 해결된 모든 문제 요약

