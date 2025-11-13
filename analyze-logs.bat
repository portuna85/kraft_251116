@echo off
REM Kraft 로그 분석 스크립트 (Windows)

echo ========================================
echo Kraft 로그 분석 도구
echo ========================================
echo.

REM Python 설치 확인
python --version >nul 2>&1
if errorlevel 1 (
    echo [오류] Python이 설치되어 있지 않습니다.
    echo Python 3.7 이상을 설치하세요: https://www.python.org/downloads/
    pause
    exit /b 1
)

REM 로그 디렉토리 확인
if not exist "logs" (
    echo [오류] logs 디렉토리가 없습니다.
    echo 애플리케이션을 먼저 실행하여 로그를 생성하세요.
    pause
    exit /b 1
)

REM 분석 스크립트 실행
echo 로그 분석 중...
echo.
python scripts\analyze-logs.py

echo.
echo ========================================
echo 분석 완료!
echo ========================================
pause

