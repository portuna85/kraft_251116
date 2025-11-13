# Kraft - Spring Boot Application

Spring Boot 3.5.7 기반 웹 애플리케이션 with OAuth2, JPA, Docker

## 🚀 주요 기능

### 🔐 OAuth2 소셜 로그인
- **Google 로그인**: Google 계정으로 간편 로그인
- **Naver 로그인**: Naver 계정으로 간편 로그인
- **세션 관리**: Redis 기반 세션 저장소 (초고속 성능)
- **자동 회원가입**: 최초 로그인 시 자동으로 사용자 정보 저장

### 📝 게시판 CRUD
- **게시글 목록**: 최신순 정렬, 페이징 지원
- **게시글 상세**: 모달 팝업으로 상세 내용 확인
- **게시글 작성/수정/삭제**: 인증된 사용자만 가능
- **반응형 디자인**: 모바일/태블릿/데스크톱 지원

### 🎨 사용자 인터페이스
- **Thymeleaf 템플릿**: 서버 사이드 렌더링
- **모던 CSS**: 깔끔하고 직관적인 UI/UX
- **Vanilla JavaScript**: 의존성 없는 가벼운 프론트엔드
- **비동기 통신**: Fetch API를 활용한 SPA 같은 경험

## 🛠️ 기술 스택

### Backend
- **Spring Boot 3.5.7**
- **Java 25**
- **Spring Data JPA**
- **Spring Security + OAuth2**
- **Spring Session Data Redis**
- **Flyway** (DB 마이그레이션)

### Database & Infrastructure
- **MariaDB 10.11** (메인 데이터베이스)
- **Redis 7.4** (캐시, 세션 저장소)
- **MinIO** (S3 호환 객체 스토리지)

### DevOps
- **Docker & Docker Compose**
- **Gradle 9.2.0**
- **Logback** (구조화된 로깅)

## 📋 Prerequisites

- **Java 25** (Gradle toolchain에서 자동 다운로드)
- **Docker Desktop** (Windows/Mac) 또는 Docker Engine (Linux)
- **Docker Compose v2**

## ⚡ 빠른 시작

### 1. 저장소 클론
```bash
git clone <repository-url>
cd kraft
```

### 2. 환경 변수 설정
```bash
# .env 파일 생성 (이미 존재하면 스킵)
cp .env.example .env

# OAuth2 클라이언트 정보 입력 (필수!)
# .env 파일을 열어서 다음 항목 수정:
# - GOOGLE_CLIENT_ID
# - GOOGLE_CLIENT_SECRET
# - NAVER_CLIENT_ID
# - NAVER_CLIENT_SECRET
```

### 3. Docker 서비스 시작
```bash
# MariaDB, Redis, MinIO 시작
docker-compose up -d mariadb redis minio

# 서비스 상태 확인 (모두 healthy 될 때까지 대기)
docker-compose ps
```

### 4. 애플리케이션 실행
```bash
# Windows
.\gradlew bootRun

# Linux/Mac
./gradlew bootRun
```

### 5. 브라우저에서 접속
```
http://localhost:8080
```

## 🐳 Docker Compose 서비스

| 서비스 | 포트 | 용도 | Health Check |
|--------|------|------|--------------|
| **mariadb** | 3306 | 메인 데이터베이스 | ✅ |
| **redis** | 6379 | 캐시 & 세션 | ✅ |
| **minio** | 9000, 9001 | 객체 스토리지 | ✅ |
| **app** | 8080 | Spring Boot | - |

상세 정보: [DOCKER_COMPOSE_GUIDE.md](DOCKER_COMPOSE_GUIDE.md)

## 📁 프로젝트 구조
```
kraft/
├── src/
│   ├── main/
│   │   ├── java/com/kraft/
│   │   │   ├── config/          # 설정 (Security, JPA, etc.)
│   │   │   ├── domain/          # 엔티티 & 레포지토리
│   │   │   ├── service/         # 비즈니스 로직
│   │   │   ├── web/             # 컨트롤러 & DTO
│   │   │   └── Application.java # 메인 클래스
│   │   └── resources/
│   │       ├── db/migration/    # Flyway 마이그레이션 스크립트
│   │       ├── static/          # CSS, JS
│   │       ├── templates/       # Thymeleaf 템플릿
│   │       ├── application.yml  # 공통 설정
│   │       ├── application-dev.yml   # 개발 환경
│   │       ├── application-prod.yml  # 운영 환경
│   │       └── logback-spring.xml    # 로그 설정
│   └── test/                    # 테스트 코드
├── docker/
│   └── mariadb/init/           # DB 초기화 스크립트
├── logs/                        # 애플리케이션 로그
├── secret/                      # OAuth2 시크릿 (gitignore)
├── docker-compose.yml          # Docker Compose 설정
├── .env                        # 환경 변수 (gitignore)
└── build.gradle                # Gradle 빌드 설정
```

## 🔧 개발 가이드

### Gradle 빌드
```bash
# 빌드
.\gradlew build

# 테스트
.\gradlew test

# 클린 빌드
.\gradlew clean build
```

### 테스트 실행
```bash
# 모든 테스트
.\gradlew test

# 특정 테스트
.\gradlew test --tests "PostApiControllerTest"

# 테스트 커버리지
.\gradlew test jacocoTestReport
```

### 로그 확인
```bash
# 실시간 로그
Get-Content logs\kraft-app.log -Wait -Tail 50

# 에러 로그만
Get-Content logs\kraft-error.log -Tail 100

# SQL 쿼리 로그
Get-Content logs\kraft-sql.log -Tail 50
```

상세 정보: [logs/README.md](logs/README.md)

## 📊 데이터베이스

### Flyway 마이그레이션
- **위치**: `src/main/resources/db/migration/`
- **현재 버전**: V2 (users, posts, session 테이블)
- **자동 실행**: 애플리케이션 시작 시

### 스키마
```sql
-- Users 테이블
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  create_at TIMESTAMP NOT NULL,
  modified_at TIMESTAMP NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  picture VARCHAR(1024),
  role VARCHAR(50) NOT NULL
);

-- Posts 테이블
CREATE TABLE posts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(500) NOT NULL,
  content TEXT NOT NULL,
  author VARCHAR(255),
  user_id BIGINT,
  create_at TIMESTAMP NOT NULL,
  modified_at TIMESTAMP NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## 🔐 보안

### 환경별 설정
- **개발**: `application-dev.yml` (로컬 MariaDB)
- **운영**: `application-prod.yml` (외부 DB, Redis 클러스터)
- **테스트**: `application-test.yml` (H2 인메모리)

### Secret 관리
- `.env` 파일 사용 (Git에서 제외)
- `secret/` 디렉토리 (Git에서 제외)
- 운영 환경: AWS Secrets Manager 또는 환경 변수 사용 권장

## 🌐 API 엔드포인트

### 인증
- `GET /` - 홈페이지 (게시글 목록)
- `GET /oauth2/authorization/google` - Google 로그인
- `GET /oauth2/authorization/naver` - Naver 로그인
- `GET /logout` - 로그아웃

### 게시글 API
- `GET /api/post` - 게시글 목록
- `GET /api/post/{id}` - 게시글 상세
- `POST /api/post` - 게시글 작성 (인증 필요)
- `PUT /api/post/{id}` - 게시글 수정 (인증 필요)
- `DELETE /api/post/{id}` - 게시글 삭제 (인증 필요)

## 📖 문서

### 📘 종합 문서
모든 프로젝트 문서가 하나로 통합되었습니다:
- **[종합 문서 (COMPREHENSIVE_DOCUMENTATION.md)](COMPREHENSIVE_DOCUMENTATION.md)** ⭐
  - 프로젝트 개요
  - Docker Compose 가이드
  - Secret 통합 완료 보고서
  - Redis 세션 마이그레이션
  - 로그 분석 및 리팩토링
  - 로그 관리 가이드

### 📂 추가 문서
- [로그 관리 가이드](logs/README.md) - 로그 파일 관리 및 분석
- [Secret 관리 가이드](secret/README.md) - OAuth2 설정 방법

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 라이선스

This project is licensed under the MIT License.

## 👤 작성자

**Kraft Development Team**

## 🙏 감사의 말

- Spring Boot Team
- Thymeleaf Team
- MariaDB Team
- Redis Team
- MinIO Team

---

**Made with ❤️ using Spring Boot**
cp .env.example .env
# .env 파일 수정...

# 2. 애플리케이션 빌드 및 서비스 시작
./gradlew build
docker-compose up --build
```

### 로컬 개발 모드
```bash
# 1. 환경변수 로드
export $(cat .env | xargs)  # Linux/Mac
# 또는 PowerShell: Get-Content .env | ForEach-Object { $var = $_.Split('='); [Environment]::SetEnvironmentVariable($var[0], $var[1]) }

# 2. MariaDB만 Docker로 실행
docker-compose up mariadb

# 3. 애플리케이션을 로컬에서 실행
./gradlew bootRun --args='--spring.profiles.active=dev,oauth'
```

## 📱 웹 애플리케이션 사용법

### 1. 애플리케이션 접속
브라우저에서 `http://localhost:8080` 접속

### 2. 로그인 (선택사항)
- **비로그인**: 게시글 목록 조회 및 상세보기만 가능
- **Google 로그인**: 우측 상단 "Google 로그인" 버튼 클릭
- **Naver 로그인**: 우측 상단 "Naver 로그인" 버튼 클릭

### 3. 게시글 작성 (로그인 필요)
1. 우측 상단 "글쓰기" 버튼 클릭
2. 제목, 작성자, 내용 입력
3. "등록" 버튼 클릭

### 4. 게시글 조회 (모든 사용자)
- **목록**: 메인 페이지에서 모든 게시글 카드 형태로 표시
- **상세보기**: 게시글 카드에서 "상세보기" 버튼 클릭 → 모달 팝업

### 5. 게시글 수정 (로그인 필요)
1. 게시글 카드에서 "수정" 버튼 클릭
2. 제목 또는 내용 수정
3. "수정" 버튼 클릭

### 6. 게시글 삭제 (로그인 필요)
- **목록에서**: 게시글 카드에서 "삭제" 버튼 클릭 → 확인
- **수정 페이지에서**: "삭제" 버튼 클릭 → 확인

### 7. 로그아웃
우측 상단 "로그아웃" 버튼 클릭

## 📁 프로젝트 구조

```
kraft/
├── src/
│   ├── main/
│   │   ├── java/com/kraft/
│   │   │   ├── Application.java                    # 메인 진입점
│   │   │   ├── config/
│   │   │   │   ├── JpaConfig.java                 # JPA Auditing 설정
│   │   │   │   ├── WebConfig.java                 # Web MVC 설정
│   │   │   │   └── auth/
│   │   │   │       ├── SecurityConfig.java        # Spring Security 설정
│   │   │   │       ├── LoginUser.java             # 세션 사용자 어노테이션
│   │   │   │       ├── LoginUserArgumentResolver.java
│   │   │   │       ├── CustomOAuth2UserService.java
│   │   │   │       └── dto/
│   │   │   │           ├── OAuthAttributes.java   # OAuth2 속성 매핑
│   │   │   │           └── SessionUser.java       # 세션 저장용 DTO
│   │   │   ├── domain/
│   │   │   │   ├── BaseEntity.java               # JPA Auditing 베이스
│   │   │   │   ├── post/
│   │   │   │   │   ├── Post.java                 # 게시글 엔티티
│   │   │   │   │   └── PostRepository.java       # 게시글 리포지토리
│   │   │   │   └── user/
│   │   │   │       ├── User.java                 # 사용자 엔티티
│   │   │   │       ├── UserRepository.java       # 사용자 리포지토리
│   │   │   │       └── Role.java                 # 권한 Enum
│   │   │   ├── service/
│   │   │   │   └── PostService.java              # 게시글 서비스
│   │   │   └── web/
│   │   │       ├── HomeController.java           # 웹 페이지 컨트롤러
│   │   │       ├── PostApiController.java        # REST API 컨트롤러
│   │   │       └── dto/
│   │   │           ├── PostSaveRequestDto.java   # 게시글 등록 DTO
│   │   │           ├── PostUpdateRequestDto.java # 게시글 수정 DTO
│   │   │           ├── PostResponseDto.java      # 게시글 응답 DTO
│   │   │           └── PostsListResponseDto.java # 게시글 목록 DTO
│   │   └── resources/
│   │       ├── application.yml                    # 공통 설정
│   │       ├── application-dev.yml               # 개발 환경 설정
│   │       ├── application-oauth.yml             # OAuth2 설정
│   │       ├── application-prod.yml              # 프로덕션 설정
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── style.css                 # 메인 스타일시트
│   │       │   └── js/
│   │       │       ├── app.js                    # 메인 스크립트
│   │       │       ├── post-save.js              # 게시글 작성 스크립트
│   │       │       └── post-update.js            # 게시글 수정 스크립트
│   │       └── templates/
│   │           ├── index.html                    # 메인 페이지
│   │           ├── posts-save.html               # 게시글 작성 페이지
│   │           └── posts-update.html             # 게시글 수정 페이지
│   └── test/
│       ├── java/com/kraft/
│       │   ├── config/
│       │   │   └── TestSecurityConfig.java       # 테스트용 시큐리티 설정
│       │   ├── domain/post/
│       │   │   └── PostRepositoryTest.java       # 리포지토리 테스트
│       │   └── web/
│       │       └── PostApiControllerTest.java    # 컨트롤러 테스트
│       └── resources/
│           └── application-test.yml              # 테스트 환경 설정
├── .env                                          # 환경변수 (Git 무시)
├── docker-compose.yml                            # Docker Compose 설정
├── build.gradle                                  # Gradle 빌드 설정
└── README.md                                     # 프로젝트 문서
```

## 🔒 민감정보 관리

### Git 무시 파일
다음 파일들은 **절대 Git에 커밋하지 마세요**:
- `.env` - 환경변수
- `secret/` - OAuth2 credential JSON 파일
- `application-oauth.yml` - OAuth 설정 (환경변수 사용 권장)
- `application-*.local.yml` - 로컬 개발용 설정

### 예시 파일
- `.env.example` - 환경변수 템플릿
- `application-oauth.yml.example` - OAuth 설정 템플릿
- `secret/*.json.example` - OAuth credential 템플릿
- `docker-compose.yml.example` - Docker Compose 템플릿

### 프로덕션 환경 보안
- **환경변수**: 시스템 환경변수 또는 `.env` 파일
- **Docker**: `docker-compose.yml`의 environment 또는 secrets
- **Kubernetes**: Secrets 리소스
- **AWS**: Systems Manager Parameter Store / Secrets Manager
- **Azure**: Key Vault
- **GCP**: Secret Manager

## 환경별 설정 파일

### 공통 설정
- `src/main/resources/application.yml` - 기본 설정 (기본 활성 프로파일: dev)

### 개발 환경
- `src/main/resources/application-dev.yml` - 개발용 DB 연결 정보
  - MariaDB: `localhost:3306/kraft_db`
  - User: `kraft_user` / Password: `Gkstmvns1!`

### 테스트 환경
- `src/test/resources/application-test.yml` - H2 인메모리 DB 사용

### 프로덕션 환경
- `src/main/resources/application-prod.yml` - 프로덕션 설정
  - 데이터베이스 비밀번호는 환경 변수 `DB_PASSWORD`로 주입 권장
  - JPA ddl-auto: validate (스키마 변경 불가)
  - H2 콘솔 비활성화

## 보안 설정 (SecurityConfig)

### Spring Security 구성
최신 Spring Security 6.x 권장 방식 적용:
- **WebSecurityConfigurerAdapter 대신 SecurityFilterChain 빈 사용**
- Lambda DSL 기반 설정

### CSRF 정책
- **웹 페이지**: CSRF 보호 활성화 (기본)
- **API 엔드포인트** (`/api/v1/**`): CSRF 비활성화 (RESTful API는 stateless)
- **H2 콘솔**: CSRF 비활성화 (개발 편의)

### CORS 정책
- **개발 환경**: `localhost:*`, `127.0.0.1:*` 허용
- **프로덕션 환경**: 특정 도메인만 허용 (예: `https://yourdomain.com`)
  - `application-prod.yml` 사용 시 `prodCorsConfigurationSource` 빈 활성화

### Frame Options
- **SAMEORIGIN**: 같은 도메인에서만 iframe 허용 (H2 콘솔 사용 가능하되 보안 강화)

### 인증/권한
- **OAuth2 로그인**: Google, Naver, Kakao 등 (CustomOAuth2UserService)
- **API 엔드포인트**: `ROLE_USER` 권한 필요
- **정적 리소스**: 인증 불필요 (`/css/**`, `/js/**`, `/images/**`, `/static/**`)

### 로그아웃
- 세션 무효화
- `JSESSIONID` 쿠키 삭제
- 로그아웃 후 `/`로 리다이렉트

## 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew test --tests com.kraft.config.auth.SecurityConfigTest
./gradlew test --tests com.kraft.domain.post.PostRepositoryTest
```

## 📊 로그 관리 및 분석

### 로그 파일 구조
```
logs/
├── kraft.log              # 전체 애플리케이션 로그
├── kraft-error.log        # 에러 로그만
├── kraft-sql.log          # SQL 쿼리 로그
├── kraft-security.log     # 보안/인증 로그
├── kraft-app.log          # 비즈니스 로직 로그
└── archive/               # 자동 아카이브 (압축)
```

### 로그 분석 실행

```bash
# Windows
analyze-logs.bat

# Linux/Mac
./analyze-logs.sh
```

분석 결과는 다음과 같이 제공됩니다:
- **텍스트 리포트**: `logs/analysis-report-YYYYMMDD-HHMMSS.txt`
- **JSON 결과**: `logs/analysis-result.json`

### 실시간 로그 모니터링

```bash
# Windows PowerShell
Get-Content logs\kraft.log -Wait -Tail 50

# Linux/Mac
tail -f logs/kraft.log

# Docker 컨테이너
docker logs -f kraft-app
```

상세한 로그 관리 가이드는 [docs/LOG_MANAGEMENT.md](docs/LOG_MANAGEMENT.md)를 참조하세요.

## 주요 엔드포인트

### 웹 페이지
- `/` - 메인 페이지 (게시글 목록, 읽기는 비로그인 가능)
- `/posts/save` - 게시글 작성 (로그인 필요)
- `/posts/update/{id}` - 게시글 수정 (로그인 필요)
- `/profile` - 프로필 페이지 (공개)

### OAuth2 로그인
- `/oauth2/authorization/google` - Google 로그인
- `/oauth2/authorization/naver` - Naver 로그인
- `/logout` - 로그아웃

### API
- `POST /api/post` - 게시글 등록 (USER 권한 필요)
- `GET /api/post/{id}` - 게시글 조회 (공개)
- `GET /api/post/list` - 게시글 목록 (공개)
- `PUT /api/post/{id}` - 게시글 수정 (USER 권한 필요)
- `DELETE /api/post/{id}` - 게시글 삭제 (USER 권한 필요)

### 개발 도구
- `/h2-console` - H2 콘솔 (개발/테스트 환경에서만 사용, 프로덕션에서는 비활성화)

## Docker Compose 구성

### 서비스
1. **MariaDB** (`mariadb:10.11`)
   - 포트: `3306:3306`
   - 데이터베이스: `kraft_db`
   - 사용자: `kraft_user`
   - 볼륨: `kraft_mariadb_data` (데이터 영속성)

2. **App** (Kraft Spring Boot)
   - 포트: `8080:8080`
   - 프로파일: `dev`
   - MariaDB 헬스체크 완료 후 시작

## 프로덕션 배포 시 체크리스트

### 보안
- [ ] CORS 설정에서 `allowedOrigins`를 실제 도메인으로 제한
- [ ] H2 콘솔 비활성화 확인 (`application-prod.yml`)
- [ ] 데이터베이스 비밀번호를 환경 변수로 주입
- [ ] HTTPS 사용 (리버스 프록시 또는 내장 SSL 설정)

### 데이터베이스
- [ ] JPA `ddl-auto`를 `validate` 또는 `none`으로 설정
- [ ] 데이터베이스 마이그레이션 도구 사용 (Flyway, Liquibase 권장)
- [ ] 백업 정책 수립

### 모니터링
- [ ] 로그 파일 관리 (rotation, 보관 기간)
- [ ] 애플리케이션 메트릭 수집 (Actuator, Prometheus 등)
- [ ] 에러 추적 도구 연동 (Sentry, Rollbar 등)

## 문제 해결

### MariaDB 연결 실패
```bash
# MariaDB 컨테이너 상태 확인
docker ps
docker logs kraft-mariadb-1

# 네트워크 확인
docker network ls
docker network inspect kraft_kraft_default
```

## 🛠 문제 해결

### MariaDB 연결 실패
```bash
# MariaDB 컨테이너 상태 확인
docker ps | grep mariadb
docker logs kraft-mariadb-1

# 사용자 권한 확인
docker exec kraft-mariadb-1 mariadb -uroot -proot -e "SELECT User, Host FROM mysql.user WHERE User='kraft_user';"

# 연결 테스트
docker exec kraft-mariadb-1 mariadb -ukraft_user -pGkstmvns1! kraft_db -e "SELECT 1;"

# 컨테이너 재시작 (초기화 스크립트 재실행)
docker-compose down -v
docker-compose up -d mariadb
```

### 권한 에러 발생 시
```
Access denied for user 'kraft_user'@'172.19.0.1'
```

**해결**: Docker 볼륨을 삭제하고 재생성하여 초기화 스크립트가 실행되도록 합니다.
```bash
docker-compose down -v
docker volume rm kraft_mariadb_data -f
docker-compose up -d mariadb
```

상세한 문제 해결 방법은 [docs/DATABASE_CONNECTION_FIX.md](docs/DATABASE_CONNECTION_FIX.md)를 참조하세요.

### 테스트 실패
```bash
# 캐시 삭제 후 재빌드
./gradlew clean build --no-build-cache
```

## 📚 문서 가이드

프로젝트 문서는 다음과 같이 구성되어 있습니다:

### 주요 문서
1. **README.md** (이 문서)
   - 프로젝트 개요 및 빠른 시작 가이드
   - 환경 설정 및 실행 방법

2. **[SECURITY.md](SECURITY.md)**
   - 보안 설정 및 민감정보 관리 가이드
   - 환경변수 설정 방법
   - Git 커밋 전 체크리스트
   - OAuth2 설정 가이드

3. **[CHANGELOG.md](CHANGELOG.md)**
   - 프로젝트 변경 이력
   - 리팩토링 및 업데이트 내역

4. **[secret/README.md](secret/README.md)**
   - Secret 디렉토리 사용법
   - OAuth credentials 설정 가이드

### 빠른 참조
- **보안 설정**: [SECURITY.md](SECURITY.md) 참조
- **변경 이력**: [CHANGELOG.md](CHANGELOG.md) 참조
- **OAuth 설정**: [secret/README.md](secret/README.md) 참조

## 참고 자료

### 공식 문서
- [Spring Security 6.x Documentation](https://docs.spring.io/spring-security/reference/)
- [Spring Boot 3.x Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MariaDB Documentation](https://mariadb.com/kb/en/)

### 보안 관련
- [OWASP Secrets Management](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
- [12-Factor App: Config](https://12factor.net/config)

