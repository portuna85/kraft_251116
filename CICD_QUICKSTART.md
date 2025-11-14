# 🚀 GitHub Actions CI/CD 설정 완료

## ✅ 설치된 워크플로우

프로젝트에 다음 GitHub Actions 워크플로우가 설치되었습니다:

### 1. **CI - Build and Test** (`.github/workflows/ci.yml`)
- main, develop 브랜치 푸시/PR 시 자동 실행
- JDK 25 설정 및 Gradle 빌드
- 단위 테스트 실행 및 리포트 생성
- JAR 아티팩트 업로드

### 2. **CD - Deploy to Production** (`.github/workflows/cd.yml`)
- main 브랜치 푸시 또는 태그 생성 시 실행
- Docker 이미지 빌드 및 GitHub Container Registry 푸시
- SSH를 통한 프로덕션 서버 자동 배포

### 3. **Code Quality Check** (`.github/workflows/code-quality.yml`)
- 코드 품질 정적 분석 (Checkstyle, SpotBugs)
- SonarCloud 통합 (선택사항)

### 4. **Dependency Security Check** (`.github/workflows/dependency-check.yml`)
- 매주 월요일 자동 실행
- 보안 취약점 검사
- Dependabot PR 자동 병합

### 5. **PR Quality Gate** (`.github/workflows/pr-check.yml`)
- PR 생성/업데이트 시 자동 실행
- 빌드, 테스트, 커버리지 검사
- PR에 테스트 결과 자동 코멘트

### 6. **Release** (`.github/workflows/release.yml`)
- 버전 태그 생성 시 자동 실행
- 체인지로그 생성
- GitHub Release 자동 생성

## 📋 추가 파일

### Dependabot 설정
- `.github/dependabot.yml`: Gradle, GitHub Actions, Docker 의존성 자동 업데이트

### 이슈/PR 템플릿
- `.github/ISSUE_TEMPLATE/bug_report.md`: 버그 리포트 템플릿
- `.github/ISSUE_TEMPLATE/feature_request.md`: 기능 요청 템플릿
- `.github/pull_request_template.md`: PR 템플릿

### 배포 스크립트
- `deploy.sh`: Linux/macOS용 배포 스크립트
- `deploy.ps1`: Windows PowerShell용 배포 스크립트

### Docker 설정
- `docker-compose.prod.yml`: 프로덕션 환경용 Docker Compose
- `.env.example`: 환경변수 예제 파일

### 문서
- `README.md`: 프로젝트 README
- `CI_CD_SETUP.md`: 상세한 CI/CD 설정 가이드

## 🔧 Gradle 개선사항

### 추가된 플러그인
- **Jacoco**: 코드 커버리지 측정
- **SonarQube**: 정적 코드 분석

### 새로운 Gradle 태스크
```bash
# CI 환경용 테스트
./gradlew ciTest

# CI 환경용 빌드
./gradlew ciBuild

# 코드 커버리지 리포트
./gradlew jacocoTestReport

# 커버리지 검증
./gradlew jacocoTestCoverageVerification

# SonarQube 분석
./gradlew sonar
```

### Spring Boot Actuator
- 헬스체크 엔드포인트 추가: `/actuator/health`
- Liveness/Readiness 프로브 지원
- 메트릭 수집: `/actuator/metrics`

## 🚀 다음 단계

### 1. GitHub Repository 설정

```bash
# 변경사항 커밋
git add .
git commit -m "feat: Add GitHub Actions CI/CD pipeline"

# GitHub에 푸시
git push origin main
```

### 2. GitHub Secrets 설정

Repository Settings > Secrets and variables > Actions에서 설정:

#### 필수 Secrets
```
# 데이터베이스
MARIADB_ROOT_PASSWORD
MARIADB_DATABASE
MARIADB_USER
MARIADB_PASSWORD

# OAuth
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
NAVER_CLIENT_ID
NAVER_CLIENT_SECRET

# Redis
REDIS_HOST
REDIS_PORT

# MinIO
MINIO_ROOT_USER
MINIO_ROOT_PASSWORD

# 배포 서버
SERVER_HOST
SERVER_USER
SERVER_PORT
SSH_PRIVATE_KEY
```

#### 선택사항 Secrets
```
SONAR_TOKEN        # SonarCloud 사용 시
CODECOV_TOKEN      # Codecov 사용 시
SLACK_WEBHOOK      # Slack 알림 사용 시
```

### 3. 프로덕션 서버 준비

서버에서 다음 단계 실행:

```bash
# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# 애플리케이션 디렉토리 생성
sudo mkdir -p /app/kraft
cd /app/kraft

# docker-compose.prod.yml 복사
# (GitHub에서 다운로드 또는 직접 복사)

# .env 파일 생성 (.env.example 참조)
nano .env

# SSH 키 설정
ssh-keygen -t ed25519 -C "github-actions"
cat ~/.ssh/id_ed25519.pub >> ~/.ssh/authorized_keys
# GitHub Secrets에 개인키 등록
```

### 4. 첫 배포 테스트

```bash
# 로컬에서 태그 생성
git tag -a v1.0.0 -m "First release"
git push origin v1.0.0

# GitHub Actions 탭에서 워크플로우 실행 확인
```

## 📊 모니터링

### GitHub Actions
- https://github.com/YOUR_USERNAME/kraft/actions

### 애플리케이션 헬스
```bash
# 헬스체크
curl http://YOUR_SERVER:8080/actuator/health

# 상세 정보
curl http://YOUR_SERVER:8080/actuator/health/liveness
curl http://YOUR_SERVER:8080/actuator/health/readiness

# 메트릭
curl http://YOUR_SERVER:8080/actuator/metrics
```

### 로그 확인
```bash
# 서버에서
docker-compose -f docker-compose.prod.yml logs -f app

# 또는 배포 스크립트 로그
tail -f /app/kraft/deploy.log
```

## 🎯 사용 시나리오

### 일반 개발
```bash
# 1. 기능 브랜치 생성
git checkout -b feature/new-feature

# 2. 코드 작성 및 커밋
git commit -m "feat: add new feature"

# 3. 푸시 (자동으로 CI 실행)
git push origin feature/new-feature

# 4. PR 생성 (자동으로 품질 검사)

# 5. 머지 (main 브랜치에 자동 배포)
```

### 릴리스
```bash
# 버전 태그 생성 및 푸시
git tag -a v1.2.3 -m "Release v1.2.3"
git push origin v1.2.3

# 자동으로:
# - Release 워크플로우 실행
# - JAR 파일 빌드
# - GitHub Release 생성
# - 체인지로그 자동 생성
```

### 핫픽스
```bash
# 핫픽스 브랜치 생성
git checkout -b hotfix/critical-bug

# 수정 및 커밋
git commit -m "fix: critical bug"

# main에 머지 (자동 배포)
git checkout main
git merge hotfix/critical-bug
git push origin main
```

## 🔍 트러블슈팅

### 빌드 실패
```bash
# 로컬에서 빌드 테스트
./gradlew clean build

# 캐시 문제 시
./gradlew clean --no-configuration-cache
```

### 배포 실패
```bash
# SSH 연결 확인
ssh -i ~/.ssh/github-actions user@server

# Docker 로그 확인
docker-compose -f docker-compose.prod.yml logs app
```

### 테스트 실패
```bash
# 로컬에서 테스트
./gradlew test

# 특정 테스트만
./gradlew test --tests PostRepositoryTest
```

## 📚 참고 문서

- [CI/CD 상세 가이드](CI_CD_SETUP.md)
- [프로젝트 README](README.md)
- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## ✅ 완료 체크리스트

- [x] GitHub Actions 워크플로우 생성
- [x] Dependabot 설정
- [x] 이슈/PR 템플릿 생성
- [x] Docker 프로덕션 설정
- [x] 배포 스크립트 작성
- [x] Gradle 빌드 최적화
- [x] Actuator 헬스체크 추가
- [x] 문서 작성
- [ ] GitHub Secrets 설정 (사용자가 해야 할 작업)
- [ ] 프로덕션 서버 준비 (사용자가 해야 할 작업)
- [ ] 첫 배포 테스트 (사용자가 해야 할 작업)

---

**축하합니다! 🎉**

GitHub Actions를 통한 완전한 CI/CD 파이프라인이 설정되었습니다.
이제 코드를 푸시하면 자동으로 빌드, 테스트, 배포가 이루어집니다.

