#!/bin/bash
# Логин на docker hub
docker login -u aasurov -p "$DOCKER_HUB_TOKEN"

# Шаг 1: Получение ID контейнера с фронтом
FRONTEND_CONTAINER_ID=$(docker ps -qf "name=root-frontend-1")

# Шаг 2: Удаление всех контейнеров, кроме контейнера с фронтом
docker stop $(docker ps -q | grep -v $FRONTEND_CONTAINER_ID) && docker rm $(docker ps -aq | grep -v $FRONTEND_CONTAINER_ID)

# Удаление всех образов, кроме образа с фронтом
docker images -q | grep -v $(docker images -q aasurov/anvilcoder-frontend) | xargs -r docker rmi

# Шаг 3: Загрузка образов с Docker Hub
docker pull aasurov/anvilcoder:checkpoint-manager-$PROJECT_VERSION

# Шаг 4: Запуск контейнеров с новыми образами
docker-compose -f docker-compose.prod.yml up -d

# Шаг 5: Удаление всех файлов внутри каталога
rm -f app.jar deploy.sh docker-compose.prod.yml Dockerfile .env
