#!/bin/bash
#логин на docker hub
docker login -u aasurov -p $DOCKER_HUB_TOKEN


# Шаг 1: Удаление и остановка текущих контейнеров
docker-compose -f docker-compose.remote.yml down

# Шаг 2: Загрузка образов с Docker Hub
docker pull aasurov/anvilcoder:checkpoint-manager-$PROJECT_VERSION

# Шаг 3: Запуск контейнеров с новыми образами
docker-compose -f docker-compose.remote.yml up -d
