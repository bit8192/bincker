#!/usr/bin/env bash
set -e

SERVER_USER="root"
SERVER_HOST="$1"
CONTAINER_NAME="bincker"

function usage() {
  echo "$0 SERVER_HOST";
  exit;
}

if [ -z "$SERVER_HOST" ]; then
  usage;
fi

WORK_DIR="$(dirname "$0")"
cd "$WORK_DIR"
./gradlew :bootJar

LOCAL_JAR="$(ls build/libs/*.jar | head -n 1)"
JAR_NAME="app.jar"

scp "$LOCAL_JAR" "$SERVER_USER@$SERVER_HOST:/tmp/$JAR_NAME"

# shellcheck disable=SC2087
ssh "$SERVER_USER@$SERVER_HOST" <<EOF
  set -e
  if [ ! -d "/opt/bincker" ]; then
    mkdir -p /opt/bincker/blog;
  fi
  if [ ! -z "\$(docker ps --filter=name=$CONTAINER_NAME --format "{{.Names}}")" ]; then
    docker stop $CONTAINER_NAME;
  fi
  mv /tmp/$JAR_NAME /opt/bincker/$JAR_NAME;
  if [ -z "\$(docker ps -a --filter=name=$CONTAINER_NAME --format "{{.Names}}")" ]; then
    docker run -d --name $CONTAINER_NAME -v /opt/bincker:/app -w /app openjdk:17 java -jar $JAR_NAME;
  else
    docker start $CONTAINER_NAME;
  fi
EOF