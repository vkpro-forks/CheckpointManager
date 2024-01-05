#!/bin/bash

#TODO пока не работает

# Проверка статуса основных сервисов
echo "Проверка статуса основных сервисов..."
while ! docker-compose -f docker-compose.dep.yml ps | grep "Up"; do
  echo "Основные сервисы еще не запущены. Подождем..."
  sleep 10 # Подождать 10 секунд перед следующей проверкой
done

echo "Все основные сервисы запущены. Запускаем ELK..."

# Переход в каталог с конфигурацией ELK
cd ./config/docker-elk || exit

# Запуск ELK стека
docker-compose -f docker-compose.prod.yml up -d

echo "ELK стек запущен."
