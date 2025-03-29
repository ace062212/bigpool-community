#!/bin/bash

# 서비스 계정 키 파일 경로
KEY_FILE="/Users/pdg0622/Downloads/bigpool-community-firebase-adminsdk-fbsvc-184c969c39.json"

# 파일 존재 확인
if [ ! -f "$KEY_FILE" ]; then
  echo "오류: 파일을 찾을 수 없습니다: $KEY_FILE"
  exit 1
fi

# Base64로 인코딩
echo "Firebase 서비스 계정 키를 Base64로 인코딩합니다..."
BASE64_ENCODED=$(cat "$KEY_FILE" | base64)

echo ""
echo "인코딩된 키를 Railway 환경 변수로 설정하세요."
echo "환경 변수 이름: FIREBASE_CREDENTIALS"
echo ""
echo "인코딩된 키 값:"
echo "$BASE64_ENCODED"
echo ""
echo "키가 너무 길 경우, 다음 명령으로 인코딩된 키를 클립보드에 복사할 수 있습니다:"
echo "cat \"$KEY_FILE\" | base64 | pbcopy" 