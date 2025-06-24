#!/bin/bash

COMPOSE_PROJECT_PATH="/home/ubuntu/app"
ARTIFACT_PATH="/opt/codedeploy-agent/deployment-root/$DEPLOYMENT_GROUP_ID/$DEPLOYMENT_ID/deployment-archive"

if [ -f "$COMPOSE_PROJECT_PATH/.env" ]; then
  export $(grep -v '^#' $COMPOSE_PROJECT_PATH/.env | xargs)
else
  echo "$COMPOSE_PROJECT_PATH/.env 파일을 찾을 수 없습니다."
  exit 1
fi

NEW_IMAGE_URI=$(cat $ARTIFACT_PATH/imagedefinitions.json | jq -r '.[0].imageUri')
export APP_IMAGE_URI=$NEW_IMAGE_URI

aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

cd $COMPOSE_PROJECT_PATH

docker-compose pull app

docker-compose up -d --no-deps app

docker image prune -af
