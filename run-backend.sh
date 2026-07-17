#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"

if ! docker ps >/dev/null 2>&1; then
  echo "Starting Docker Desktop..."
  "/c/Program Files/Docker/Docker/Docker Desktop.exe" >/dev/null 2>&1 &
  until docker ps >/dev/null 2>&1; do sleep 5; done
fi

docker compose --env-file .env up -d

set -a
. ./.env
set +a

cd backend
"$JAVA_HOME/bin/java" -jar target/cryptopal-core-0.0.1-SNAPSHOT.jar
