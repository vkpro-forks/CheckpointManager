#!/bin/bash

# Шаг 1: Получение версии проекта из файла сборки Gradle
PROJECT_VERSION=$(grep -Po "version = '\K[^']+" build.gradle)

# Шаг 2: Экспортирование версии проекта
export PROJECT_VERSION

# Шаг 3: Сборка проекта
./gradlew build

# Шаг 4: Переименовываем JAR файл для удобства копирования и использования в Dockerfile
mv build/libs/CheckpointManager-$PROJECT_VERSION-SNAPSHOT.jar build/libs/app.jar

# Шаг 5: Обновление docker-compose версии проекта
echo "PROJECT_VERSION=$PROJECT_VERSION" > .env

# Шаг 6: Сборка Docker образа с помощью docker-compose
docker-compose build

# Шаг 7: Tagging и Push образов на Docker Hub
docker tag checkpoint-manager:latest aasurov/anvilcoder:checkpoint-manager-$PROJECT_VERSION
docker tag postgres:latest aasurov/anvilcoder:postgres-$PROJECT_VERSION
docker push aasurov/anvilcoder:checkpoint-manager-$PROJECT_VERSION
docker push aasurov/anvilcoder:postgres-$PROJECT_VERSION

# Шаг 8: Копирование Dockerfile, deploy.sh и JAR файла на удаленный сервер
scp .env root@185.233.83.75:~
scp Dockerfile root@185.233.83.75:~
scp deploy.sh root@185.233.83.75:~
scp build/libs/app.jar root@185.233.83.75:~

#Шаг 9: Передача версии на уделнный сервер
ssh root@185.233.83.75 "PROJECT_VERSION=$PROJECT_VERSION bash -s" < deploy.sh

