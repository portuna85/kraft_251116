# GitHub Actions CI/CD 설정 가이드

## 📋 목차
1. [개요](#개요)
2. [워크플로우 설명](#워크플로우-설명)
3. [GitHub Secrets 설정](#github-secrets-설정)
4. [배포 설정](#배포-설정)
5. [사용 방법](#사용-방법)

## 개요

이 프로젝트는 GitHub Actions를 사용하여 다음과 같은 CI/CD 파이프라인을 구축했습니다:

- **CI (Continuous Integration)**: 코드 빌드 및 테스트 자동화
- **CD (Continuous Deployment)**: Docker 이미지 빌드 및 배포 자동화
- **코드 품질 검사**: 정적 분석 및 보안 취약점 검사
- **의존성 관리**: Dependabot을 통한 자동 업데이트

## 워크플로우 설명

### 1. CI - Build and Test (`ci.yml`)
- **트리거**: main, develop 브랜치에 push 또는 PR
- **작업**:
  - JDK 25 설정
  - Gradle 빌드 및 테스트
  - 테스트 리포트 생성
  - JAR 아티팩트 업로드

### 2. CD - Deploy to Production (`cd.yml`)
- **트리거**: main 브랜치 push 또는 태그 생성
- **작업**:
  - 애플리케이션 빌드
  - Docker 이미지 생성 및 GitHub Container Registry에 푸시
  - SSH를 통한 서버 배포

### 3. Code Quality Check (`code-quality.yml`)
- **트리거**: main, develop 브랜치에 push 또는 PR
- **작업**:
  - Checkstyle, SpotBugs 실행
  - SonarCloud 정적 분석
  - 코드 품질 리포트 생성

### 4. Dependency Security Check (`dependency-check.yml`)
- **트리거**: 매주 월요일 자정 또는 수동 실행
- **작업**:
  - 의존성 보안 취약점 검사
  - Dependabot PR 자동 병합

### 5. PR Quality Gate (`pr-check.yml`)
- **트리거**: PR 생성/업데이트
- **작업**:
  - 빌드 및 테스트
  - 테스트 커버리지 측정
  - PR에 테스트 결과 코멘트

### 6. Release (`release.yml`)
- **트리거**: 버전 태그 생성 (예: v1.0.0)
- **작업**:
  - 릴리스 빌드
  - 체인지로그 생성
  - GitHub Release 생성

## GitHub Secrets 설정

GitHub 저장소의 Settings > Secrets and variables > Actions에서 다음 시크릿을 설정하세요:

### 필수 Secrets

#### 데이터베이스 설정
```
MARIADB_ROOT_PASSWORD=your-root-password
MARIADB_DATABASE=kraft_db
MARIADB_USER=kraft_user
MARIADB_PASSWORD=your-db-password
```

#### OAuth 설정 (Google)
```
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

#### OAuth 설정 (Naver)
```
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret
```

#### Redis 설정
```
REDIS_HOST=redis
REDIS_PORT=6379
```

#### MinIO 설정
```
MINIO_ROOT_USER=kraft_minio
MINIO_ROOT_PASSWORD=your-minio-password
MINIO_REGION=us-east-1
```

#### 배포 서버 설정
```
SERVER_HOST=your-server-ip
SERVER_USER=your-ssh-user
SERVER_PORT=22
SSH_PRIVATE_KEY=your-ssh-private-key
```

#### 코드 품질 도구 (선택사항)
```
SONAR_TOKEN=your-sonarcloud-token
CODECOV_TOKEN=your-codecov-token
```

### Environment Variables (저장소 변수)
Settings > Secrets and variables > Actions > Variables 탭에서 설정:

```
SPRING_PROFILES_ACTIVE=prod
```

## 배포 설정

### 1. Docker 이미지 빌드 및 푸시

GitHub Container Registry를 사용합니다:

```bash
# 로컬에서 테스트
docker build -t ghcr.io/your-username/kraft:latest .
docker push ghcr.io/your-username/kraft:latest
```

### 2. 서버 설정

배포 대상 서버에서:

1. **Docker 및 Docker Compose 설치**
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
```

2. **애플리케이션 디렉토리 생성**
```bash
mkdir -p /app/kraft
cd /app/kraft
```

3. **docker-compose.yml 복사**
```bash
# GitHub 저장소에서 docker-compose.yml 다운로드
wget https://raw.githubusercontent.com/your-username/kraft/main/docker-compose.yml
```

4. **.env 파일 생성**
```bash
cat > .env << 'EOF'
# Database
MARIADB_ROOT_PASSWORD=your-root-password
MARIADB_DATABASE=kraft_db
MARIADB_USER=kraft_user
MARIADB_PASSWORD=your-db-password

# Redis
REDIS_PORT=6379

# MinIO
MINIO_ROOT_USER=kraft_minio
MINIO_ROOT_PASSWORD=your-minio-password
MINIO_REGION=us-east-1

# Application
SPRING_PROFILES_ACTIVE=prod
EOF
```

5. **SSH 키 설정**
```bash
# GitHub Actions에서 사용할 SSH 키 생성
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github-actions

# 공개 키를 authorized_keys에 추가
cat ~/.ssh/github-actions.pub >> ~/.ssh/authorized_keys

# 개인 키 내용을 GitHub Secrets의 SSH_PRIVATE_KEY에 추가
cat ~/.ssh/github-actions
```

### 3. 배포 스크립트 개선

서버에 배포 스크립트 생성:

```bash
cat > /app/kraft/deploy.sh << 'EOF'
#!/bin/bash
set -e

echo "🚀 Starting deployment..."

# Pull latest images
docker-compose pull

# Stop and remove old containers
docker-compose down

# Start new containers
docker-compose up -d

# Clean up old images
docker system prune -f

echo "✅ Deployment completed successfully!"
EOF

chmod +x /app/kraft/deploy.sh
```

## 사용 방법

### 1. 일반 개발 플로우

```bash
# 1. 브랜치 생성
git checkout -b feature/new-feature

# 2. 코드 작성 및 커밋
git add .
git commit -m "feat: add new feature"

# 3. Push (자동으로 CI 실행)
git push origin feature/new-feature

# 4. GitHub에서 PR 생성 (자동으로 PR Check 실행)

# 5. 리뷰 후 main 브랜치에 병합 (자동으로 배포)
```

### 2. 릴리스 생성

```bash
# 1. 버전 태그 생성
git tag -a v1.0.0 -m "Release version 1.0.0"

# 2. 태그 푸시 (자동으로 Release 워크플로우 실행)
git push origin v1.0.0

# 3. GitHub Releases에서 릴리스 확인
```

### 3. 수동 배포

GitHub Actions 탭에서 `CD - Deploy to Production` 워크플로우를 선택하고 "Run workflow" 클릭

### 4. 의존성 업데이트

Dependabot이 매주 월요일에 자동으로 PR을 생성합니다:
- patch 버전 업데이트는 자동으로 병합
- minor/major 버전 업데이트는 수동 리뷰 필요

## 모니터링

### 워크플로우 실행 상태 확인

```bash
# GitHub CLI 사용
gh run list
gh run view <run-id>
```

### 배포 로그 확인

```bash
# 서버에서
docker-compose logs -f app
```

## 트러블슈팅

### 빌드 실패

1. **Gradle 캐시 문제**
   - Actions 탭에서 "Cache" 삭제 후 재실행

2. **테스트 실패**
   - 로컬에서 `./gradlew clean test` 실행하여 확인

### 배포 실패

1. **SSH 연결 실패**
   - SERVER_HOST, SERVER_USER, SSH_PRIVATE_KEY 확인
   - 서버 방화벽 설정 확인

2. **Docker 이미지 Pull 실패**
   - GitHub Container Registry 권한 확인
   - 서버에서 `docker login ghcr.io` 실행

### 코드 품질 검사 실패

1. **SonarCloud 토큰 만료**
   - SONAR_TOKEN 재생성 및 업데이트

## 추가 개선 사항

### 1. Slack 알림 추가

각 워크플로우에 Slack 알림 단계 추가:

```yaml
- name: Slack notification
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### 2. 롤백 기능

배포 실패 시 이전 버전으로 자동 롤백:

```yaml
- name: Rollback on failure
  if: failure()
  run: |
    docker-compose down
    docker pull ghcr.io/${{ github.repository }}:previous
    docker-compose up -d
```

### 3. 성능 테스트

JMeter 또는 Gatling을 사용한 성능 테스트 추가

### 4. 스테이징 환경

프로덕션 배포 전 스테이징 환경에서 테스트

## 라이선스

이 프로젝트의 라이선스에 따릅니다.

