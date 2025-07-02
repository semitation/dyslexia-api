#!/bin/bash

CONTAINER_NAME="dyslexia_app"
APP_PATH="/home/ubuntu/app"
ARTIFACT_PATH="/opt/codedeploy-agent/deployment-root/$DEPLOYMENT_GROUP_ID/$DEPLOYMENT_ID/deployment-archive"

if [ -f "$APP_PATH/.env" ]; then
  export $(grep -v '^#' $APP_PATH/.env | xargs)
else
  echo "$APP_PATH/.env 파일을 찾을 수 없습니다."
  exit 1
fi

NEW_IMAGE_URI=$(cat $ARTIFACT_PATH/imagedefinitions.json | jq -r '.[0].imageUri')

aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

cd $APP_PATH

if [ $(docker ps -q -f name=^${CONTAINER_NAME}$) ]; then
  docker stop $CONTAINER_NAME
  docker rm $CONTAINER_NAME
fi

docker pull $NEW_IMAGE_URI

docker run -d \
  --name $CONTAINER_NAME \
  --env-file .env \
  -p 8080:8080 \
  -v $APP_PATH/logs:/var/log/dyslexia-app \
  --restart unless-stopped \
  $NEW_IMAGE_URI

docker image prune -af