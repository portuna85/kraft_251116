# Kraft - Spring Boot Application

[![CI](https://github.com/YOUR_USERNAME/kraft/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/kraft/actions/workflows/ci.yml)
[![CD](https://github.com/YOUR_USERNAME/kraft/actions/workflows/cd.yml/badge.svg)](https://github.com/YOUR_USERNAME/kraft/actions/workflows/cd.yml)
[![Code Quality](https://github.com/YOUR_USERNAME/kraft/actions/workflows/code-quality.yml/badge.svg)](https://github.com/YOUR_USERNAME/kraft/actions/workflows/code-quality.yml)

현대적인 Spring Boot 3.5 기반 웹 애플리케이션

## 📋 주요 기능

- ✅ Spring Boot 3.5.7 + Java 25
- ✅ JPA/Hibernate with MariaDB
- ✅ OAuth2 인증 (Google, Naver)
- ✅ Redis 세션 관리
- ✅ MinIO 객체 스토리지
- ✅ Flyway 데이터베이스 마이그레이션
- ✅ GitHub Actions CI/CD
- ✅ Docker & Docker Compose
- ✅ 코드 커버리지 (Jacoco)
- ✅ 정적 분석 (SonarQube)

## 🚀 빠른 시작

### 필수 요구사항

- Java 25
- Docker & Docker Compose
- Gradle 8.x

### 로컬 개발 환경 설정

1. **저장소 클론**
```bash
git clone https://github.com/YOUR_USERNAME/kraft.git
cd kraft
```

2. **환경변수 설정**
```bash
cp .env.example .env
# .env 파일을 편집하여 필요한 값을 설정
```

3. **Docker Compose로 인프라 실행**
```bash
docker-compose up -d mariadb redis minio
```

4. **애플리케이션 실행**
```bash
./gradlew bootRun
```

5. **브라우저에서 확인**
```
http://localhost:8080
```

## 🏗️ 프로젝트 구조

```
kraft/
├── .github/
│   ├── workflows/          # GitHub Actions 워크플로우
│   ├── dependabot.yml      # Dependabot 설정
│   └── ISSUE_TEMPLATE/     # 이슈 템플릿
├── src/
│   ├── main/
│   │   ├── java/com/kraft/
│   │   │   ├── config/     # 설정 클래스
│   │   │   ├── domain/     # 도메인 엔티티
│   │   │   ├── web/        # 컨트롤러
│   │   │   └── service/    # 비즈니스 로직
│   │   └── resources/
│   │       ├── db/migration/   # Flyway 마이그레이션
│   │       ├── static/         # 정적 리소스
│   │       └── templates/      # Thymeleaf 템플릿
│   └── test/               # 테스트 코드
├── docker/                 # Docker 관련 파일
├── secret/                 # 시크릿 파일 (git 무시됨)
├── docker-compose.yml      # 개발용 Docker Compose
├── docker-compose.prod.yml # 프로덕션용 Docker Compose
└── build.gradle            # Gradle 빌드 설정
```

## 🔧 개발

### 빌드

```bash
# 빌드 (테스트 포함)
./gradlew build

# 빌드 (테스트 제외)
./gradlew build -x test

# 테스트만 실행
./gradlew test

# 코드 커버리지 리포트 생성
./gradlew jacocoTestReport
```

### 테스트

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests PostRepositoryTest

# 테스트 결과 확인
open build/reports/tests/test/index.html
```

### 코드 품질

```bash
# SonarQube 분석
./gradlew sonar

# 커버리지 검증
./gradlew jacocoTestCoverageVerification
```

## 🚢 배포

### Docker 이미지 빌드

```bash
# 애플리케이션 빌드
./gradlew bootJar

# Docker 이미지 빌드
docker build -t kraft:latest .

# Docker 이미지 실행
docker run -p 8080:8080 kraft:latest
```

### 프로덕션 배포

GitHub Actions를 통한 자동 배포:

1. `main` 브랜치에 푸시
2. GitHub Actions가 자동으로 빌드 및 배포
3. 배포 상태는 Actions 탭에서 확인

자세한 내용은 [CI/CD 설정 가이드](CI_CD_SETUP.md)를 참조하세요.

## 📊 모니터링

### Actuator 엔드포인트

- 헬스체크: `http://localhost:8080/actuator/health`
- 메트릭: `http://localhost:8080/actuator/metrics`
- 정보: `http://localhost:8080/actuator/info`

### 로그 확인

```bash
# 애플리케이션 로그
tail -f logs/kraft.log

# 에러 로그
tail -f logs/kraft-error.log

# SQL 로그
tail -f logs/kraft-sql.log

# Docker 로그
docker-compose logs -f app
```

## 🔐 보안

### Secret 관리

민감한 정보는 `secret/` 디렉토리에 JSON 파일로 저장:

- `secret/mariadb.json` - 데이터베이스 자격증명
- `secret/redis.json` - Redis 설정
- `secret/google_secret.json` - Google OAuth
- `secret/naver_secret.json` - Naver OAuth
- `secret/minio.json` - MinIO 자격증명

### 환경변수

프로덕션 환경에서는 환경변수 또는 Secret Manager를 사용하세요.

## 🤝 기여

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 📞 문의

문제가 발생하거나 질문이 있으시면 [Issues](https://github.com/YOUR_USERNAME/kraft/issues)에 등록해주세요.

## 🙏 감사

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Thymeleaf](https://www.thymeleaf.org/)
- [MariaDB](https://mariadb.org/)
- [Redis](https://redis.io/)
- [MinIO](https://min.io/)

