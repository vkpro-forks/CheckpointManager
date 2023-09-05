#!/bin/bash

# Шаг 1: Удаление и остановка текущих контейнеров
docker-compose down

# Шаг 2: Загрузка образов с Docker Hub
docker pull aasurov/anvilcoder:checkpoint-manager-$PROJECT_VERSION
docker pull aasurov/anvilcoder:postgres-$PROJECT_VERSION

# Шаг 3: Запуск контейнеров с новыми образами
docker-compose up -d
