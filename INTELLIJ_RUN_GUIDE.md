# IntelliJ Run 'Application' 실행 가이드

## 문제 해결 완료
다음 문제들이 해결되었습니다:
1. ✅ Flyway 검증 실패 (migration not applied to database)
2. ✅ OAuth2 클라이언트 ID 누락으로 인한 빈 생성 실패
3. ✅ Docker Compose 환경변수 구문 오류

## IntelliJ Run Configuration 설정

### 1. Run Configuration 열기
- IntelliJ IDEA 상단: `Run` → `Edit Configurations...`
- 또는 `Application` 옆 드롭다운 → `Edit Configurations...`

### 2. Environment Variables 설정
Run Configuration에서 `Environment variables` 필드에 다음을 추가:

```
SPRING_PROFILES_ACTIVE=dev;DB_HOST=localhost;DB_PORT=3306;DB_USER=kraft_user;DB_PASSWORD=Gkstmvns1!
```

또는 개별적으로:
- `SPRING_PROFILES_ACTIVE` = `dev`
- `DB_HOST` = `localhost`
- `DB_PORT` = `3306`
- `DB_USER` = `kraft_user`
- `DB_PASSWORD` = `Gkstmvns1!`

### 3. 사전 준비: Docker로 MariaDB 실행

IntelliJ에서 실행하기 전에 PowerShell에서:

```powershell
# MariaDB와 Redis만 Docker로 실행
docker compose up -d mariadb redis

# 상태 확인 (healthy 확인)
docker compose ps

# 로그 확인
docker logs kraft-mariadb --follow
```

### 4. IntelliJ에서 Application 실행
- `Run` → `Run 'Application'` (Shift + F10)
- 또는 상단 초록색 실행 버튼 클릭

## 예상 로그 확인 사항

애플리케이션 시작 시 다음을 확인하세요:

1. **DataSource 연결 성공**:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

2. **Flyway 마이그레이션**:
```
Flyway migration completed successfully (dev initializer).
```
또는
```
Flyway baseline + migrate succeeded (dev initializer).
```

3. **서버 시작 성공**:
```
Tomcat started on port(s): 8080 (http)
Started Application in X.XXX seconds
```

## 문제 해결

### Flyway 버전 충돌 오류
증상: 
```
An attempt was made to call a method that does not exist.
org.flywaydb.database.mysql.mariadb.MariaDBDatabaseType.handlesJDBCUrl
'boolean org.flywaydb.core.internal.util.UrlUtils.isSecretManagerUrl(java.lang.String, java.lang.String)'
```

원인: `flyway-core`와 `flyway-mysql` 버전 불일치

해결: ✅ **이미 해결됨** - `build.gradle`에서 명시적 버전 지정 제거, Spring Boot dependency management가 호환 버전 관리

### MariaDB 연결 실패 (Access denied)
증상: `Access denied for user 'kraft_user'@'localhost'`

해결:
1. `.env` 파일의 비밀번호 확인
2. Docker MariaDB 재시작:
```powershell
docker compose down mariadb
docker compose up -d mariadb
```

### Flyway 검증 실패
증상: `Validate failed: Migrations have failed validation`

해결: 이미 `FlywayDevInitializer`와 `FlywaySafeMigrationStrategy`가 자동으로 baseline을 수행합니다.
만약 계속 문제가 발생하면:
```sql
-- MariaDB에 직접 접속하여
DROP TABLE flyway_schema_history;
```

### OAuth 관련 오류
증상: `Client id of registration 'google' must not be empty`

해결: 
- `SPRING_PROFILES_ACTIVE`에서 `oauth` 제거 (기본값은 이미 `dev`로 변경됨)
- OAuth를 사용하려면 `.env`에 실제 클라이언트 ID 입력 후:
```
SPRING_PROFILES_ACTIVE=dev,oauth
```

## 간단 체크리스트

- [ ] Docker Desktop 실행 중
- [ ] `docker compose up -d mariadb redis` 실행
- [ ] MariaDB healthy 상태 확인
- [ ] IntelliJ Run Configuration에 환경변수 설정
- [ ] `SPRING_PROFILES_ACTIVE=dev` 설정
- [ ] Run 'Application' 실행
- [ ] `http://localhost:8080` 접속 가능 확인

## 다음 단계

애플리케이션이 정상 시작되면:
- 브라우저에서 `http://localhost:8080` 접속
- `/api/post/list` 등 API 엔드포인트 테스트
- OAuth 로그인이 필요하면 Google/Naver 클라이언트 ID 설정 후 `oauth` 프로파일 활성화

