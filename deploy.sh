#!/bin/bash
#логин на docker hub
docker login -u aasurov -p "$DOCKER_HUB_TOKEN"


# Шаг 1: Удаление ВСЕХ контейнеров и ВСЕХ образов независимо работают они или нет
docker stop $(docker ps -q) && docker rm $(docker ps -aq)
docker rmi $(docker images -q)

# Шаг 2: Загрузка образов с Docker Hub
docker pull aasurov/anvilcoder:checkpoint-manager-$PROJECT_VERSION

# Шаг 3: Запуск контейнеров с новыми образами
docker-compose -f docker-compose.prod.yml up -d

# Шаг 4: Удаление всех файлов внутри каталога
rm -f app.jar deploy.sh docker-compose.prod.yml Dockerfile
