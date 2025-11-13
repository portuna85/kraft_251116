#!/bin/bash
# Kraft 로그 분석 스크립트 (Linux/Mac)

echo "========================================"
echo "Kraft 로그 분석 도구"
echo "========================================"
echo

# Python 설치 확인
if ! command -v python3 &> /dev/null; then
    echo "[오류] Python3이 설치되어 있지 않습니다."
    echo "Python 3.7 이상을 설치하세요."
    exit 1
fi

# 로그 디렉토리 확인
if [ ! -d "logs" ]; then
    echo "[오류] logs 디렉토리가 없습니다."
    echo "애플리케이션을 먼저 실행하여 로그를 생성하세요."
    exit 1
fi

# 분석 스크립트 실행
echo "로그 분석 중..."
echo
python3 scripts/analyze-logs.py

echo
echo "========================================"
echo "분석 완료!"
echo "========================================"

