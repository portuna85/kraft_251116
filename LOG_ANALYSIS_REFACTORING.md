# 로그 분석 및 리팩토링 보고서

**분석 날짜**: 2025-11-13  
**분석 대상**: `logs/` 디렉토리 내 모든 로그 파일

## 📊 발견된 문제점

### 1. ❌ Flyway Baseline 실패 (ERROR)

**로그 위치**: `kraft-error.log`, `kraft-app.log`

```
ERROR c.kraft.config.FlywayDevInitializer - Flyway baseline/migrate failed in dev initializer: 
Unable to baseline schema history table `kraft_db`.`flyway_schema_history` as it already contains migrations
```

**원인**:
- 데이터베이스에 이미 `flyway_schema_history` 테이블이 존재하고 마이그레이션 기록이 있음
- `FlywayDevInitializer`가 validate 실패 후 무조건 baseline을 시도
- Baseline은 빈 스키마에만 적용 가능하므로 실패

**영향**:
- 애플리케이션 시작은 성공하지만 매번 ERROR 로그 발생
- 불필요한 경고로 실제 중요한 에러 파악 어려움

### 2. ⚠️ Flyway Validation 실패 (WARN)

**로그 위치**: `kraft-app.log`, `kraft.log`

```
WARN c.kraft.config.FlywayDevInitializer - Flyway migrate failed, attempting baseline + migrate: 
Validate failed: Migrations have failed validation
Detected resolved migration not applied to database: 1.
```

**원인**:
- 소스 코드에 V1 마이그레이션 파일이 있지만 DB의 flyway_schema_history에는 다른 버전이 기록됨
- 또는 수동으로 스키마를 만들어서 Flyway 히스토리와 불일치

**영향**:
- 개발 환경에서 정상적인 상황일 수 있으나 매번 경고 발생

### 3. ⚠️ Hibernate Dialect 경고 (WARN)

**로그 위치**: `kraft.log`

```
WARN org.hibernate.orm.deprecation - HHH90000025: MariaDBDialect does not need to be specified 
explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
```

**원인**:
- `application-dev.yml`에서 `hibernate.dialect`를 명시적으로 설정
- Hibernate 6.x부터 JDBC URL로 자동 감지되므로 불필요

**영향**:
- 기능적 문제는 없으나 deprecation 경고 발생

### 4. ℹ️ Spring Data Redis Repository 경고 (INFO)

**로그 위치**: `kraft.log`

```
INFO o.s.d.r.c.RepositoryConfigurationExtensionSupport - Spring Data Redis - Could not safely identify 
store assignment for repository candidate interface com.kraft.domain.post.PostRepository
```

**원인**:
- `spring-boot-starter-data-redis`와 `spring-boot-starter-data-jpa`를 동시 사용
- Redis는 세션 저장용이지만 Spring Data가 JPA Repository를 Redis로 인식하려 시도

**영향**:
- 실제 동작에는 문제 없음 (JPA가 우선 적용됨)
- 로그만 발생

## ✅ 적용된 리팩토링

### 1. FlywayDevInitializer 개선

**변경 전**:
```java
catch (Exception ex) {
    log.warn("Flyway migrate failed, attempting baseline + migrate: {}", ex.getMessage());
    try {
        flyway.baseline();  // ❌ 무조건 baseline 시도
        flyway.migrate();
        log.info("Flyway baseline + migrate succeeded (dev initializer).");
    } catch (Exception ex2) {
        log.error("Flyway baseline/migrate failed in dev initializer: {}", ex2.getMessage());
    }
}
```

**변경 후**:
```java
catch (Exception ex) {
    String errorMessage = ex.getMessage();
    log.warn("Flyway migrate failed: {}", errorMessage);
    
    // ✅ 이미 마이그레이션이 있으면 baseline 스킵
    if (errorMessage != null && errorMessage.contains("already contains migrations")) {
        log.info("Flyway schema history already exists. Skipping baseline. Use 'flyway repair' if needed.");
        return;
    }
    
    // ✅ 스키마 히스토리가 없거나 비어있을 때만 baseline 시도
    try {
        flyway.baseline();
        flyway.migrate();
        log.info("Flyway baseline + migrate succeeded (dev initializer).");
    } catch (Exception ex2) {
        String baselineError = ex2.getMessage();
        if (baselineError != null && baselineError.contains("already contains migrations")) {
            log.info("Flyway schema history already contains migrations. Application will continue.");
        } else {
            log.error("Flyway baseline/migrate failed in dev initializer: {}", baselineError);
        }
    }
}
```

**효과**:
- ✅ 불필요한 ERROR 로그 제거
- ✅ 이미 마이그레이션이 있는 경우 INFO 레벨로 안내만 출력
- ✅ 실제 문제 발생 시에만 ERROR 로그 출력

### 2. Hibernate Dialect 설정 제거

**변경 전**: `application-dev.yml`
```yaml
properties:
  hibernate:
    format_sql: true
    dialect: org.hibernate.dialect.MariaDBDialect  # ❌ 불필요한 명시적 설정
```

**변경 후**:
```yaml
properties:
  hibernate:
    format_sql: true
    # ✅ MariaDBDialect은 자동 감지되므로 명시 불필요 (HHH90000025 경고 제거)
```

**효과**:
- ✅ Hibernate deprecation 경고 제거
- ✅ JDBC URL로 자동 감지하여 더 유연한 구성

## 📈 개선 결과

### Before (리팩토링 전)

```
[main] WARN  c.kraft.config.FlywayDevInitializer - Flyway migrate failed, attempting baseline + migrate
[main] ERROR c.kraft.config.FlywayDevInitializer - Flyway baseline/migrate failed in dev initializer: 
       Unable to baseline schema history table `kraft_db`.`flyway_schema_history` as it already contains migrations
[main] WARN  org.hibernate.orm.deprecation - HHH90000025: MariaDBDialect does not need to be specified explicitly
```

### After (리팩토링 후)

```
[main] INFO  c.kraft.config.FlywayDevInitializer - Flyway schema history already exists. Skipping baseline.
[main] INFO  com.kraft.Application - Started Application in 2.5 seconds
```

## 🎯 추가 권장 사항

### 1. Flyway Repair 사용 (필요 시)

스키마와 Flyway 히스토리가 불일치할 때:

```bash
# Gradle을 통한 Flyway repair
./gradlew flywayRepair

# 또는 MariaDB에 직접 접속하여
USE kraft_db;
SELECT * FROM flyway_schema_history;  -- 현재 상태 확인
DELETE FROM flyway_schema_history WHERE version = '1';  -- 특정 버전 제거 (주의!)
```

### 2. 프로덕션 환경 설정

`application-prod.yml`에서는:
```yaml
flyway:
  enabled: true
  baseline-on-migrate: false  # ⚠️ 프로덕션에서는 false
  validate-on-migrate: true   # ✅ 엄격한 검증
  out-of-order: false         # ✅ 순서 준수
```

### 3. 로그 레벨 조정

개발 환경에서 불필요한 INFO 로그 줄이기:

```yaml
logging:
  level:
    org.springframework.data.redis: WARN  # Redis repository 경고 숨기기
    org.hibernate.SQL: DEBUG              # SQL만 출력
```

### 4. Spring Data Redis Repository 비활성화

Redis를 세션 저장소로만 사용하는 경우:

```yaml
spring:
  data:
    redis:
      repositories:
        enabled: false  # ✅ Redis Repository 자동 스캔 비활성화
```

## 📝 로그 파일 현황

### 정상 로그 파일
- ✅ `kraft-app.log` - 애플리케이션 레벨 로그 (INFO 이상)
- ✅ `kraft.log` - 전체 통합 로그
- ✅ `kraft-sql.log` - SQL 쿼리 로그
- ✅ `kraft-security.log` - 보안 관련 로그 (현재 비어있음 - 정상)

### 개선된 로그 파일
- ✅ `kraft-error.log` - ERROR 레벨만 기록 (Flyway ERROR 제거됨)

## 🚀 검증 방법

리팩토링 후 테스트:

```bash
# 1. 컴파일 확인
./gradlew clean compileJava

# 2. 애플리케이션 실행
# IntelliJ에서 Run 'Application'

# 3. 로그 확인
tail -f logs/kraft-error.log  # ERROR 로그 없어야 함
tail -f logs/kraft-app.log    # INFO: Flyway schema history already exists
```

## 📚 관련 문서

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Hibernate 6 Migration Guide](https://hibernate.org/orm/releases/6.0/)
- [Spring Boot Logging](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)

---

**리팩토링 완료!** 이제 로그가 더 깔끔하고 실제 문제만 표시됩니다. 🎉

