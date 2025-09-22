#!/bin/bash

set -e

CONTAINER_NAME="dyslexia_app"
APP_PATH="/home/ubuntu/app"
ARTIFACT_PATH="/opt/codedeploy-agent/deployment-root/$DEPLOYMENT_GROUP_ID/$DEPLOYMENT_ID/deployment-archive"

echo "=== 배포 시작 ==="

if [ -f "$APP_PATH/.env" ]; then
  export $(grep -v '^#' $APP_PATH/.env | xargs)
else
  echo "$APP_PATH/.env 파일을 찾을 수 없습니다."
  exit 1
fi

NEW_IMAGE_URI=$(cat $ARTIFACT_PATH/imagedefinitions.json | jq -r '.[0].imageUri')
echo "새 이미지: $NEW_IMAGE_URI"

# 현재 실행 중인 컨테이너의 이미지 확인
CURRENT_IMAGE=""
if [ $(docker ps -q -f name=^${CONTAINER_NAME}$) ]; then
  CURRENT_IMAGE=$(docker inspect $CONTAINER_NAME --format='{{.Config.Image}}' 2>/dev/null || echo "")
  echo "현재 이미지: $CURRENT_IMAGE"

  # 동일한 이미지인지 확인
  if [ "$CURRENT_IMAGE" = "$NEW_IMAGE_URI" ]; then
    echo "동일한 이미지입니다. 배포를 건너뜁니다."
    echo "컨테이너 상태:"
    docker ps -f name=$CONTAINER_NAME
    exit 0
  fi
else
  echo "실행 중인 컨테이너가 없습니다."
fi

echo "이미지가 다릅니다. 배포를 진행합니다."

# ECR 로그인
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

cd $APP_PATH

# 새 이미지 먼저 다운로드 (실패하면 기존 컨테이너 유지)
echo "새 이미지 다운로드 중..."
docker pull $NEW_IMAGE_URI

if [ $? -ne 0 ]; then
  echo "ERROR: 이미지 다운로드 실패. 기존 컨테이너를 유지합니다."
  exit 1
fi

# 이미지 다운로드 성공했을 때만 기존 컨테이너 중지
if [ $(docker ps -q -f name=^${CONTAINER_NAME}$) ]; then
  echo "기존 컨테이너 중지 중..."
  docker stop $CONTAINER_NAME
  docker rm $CONTAINER_NAME
fi

# 새 컨테이너 실행
echo "새 컨테이너 실행 중..."
docker run -d \
  --name $CONTAINER_NAME \
  --env-file .env \
  -p 8084:8084 \
  -v $APP_PATH/logs:/var/log/dyslexia-app \
  --restart unless-stopped \
  $NEW_IMAGE_URI

if [ $? -eq 0 ]; then
  echo "=== 배포 완료 ==="
  docker ps -f name=$CONTAINER_NAME
else
  echo "ERROR: 컨테이너 실행 실패"
  exit 1
fi

# 이전 이미지 정리 (신중하게)
echo "이전 이미지 정리 중..."
docker image prune -f