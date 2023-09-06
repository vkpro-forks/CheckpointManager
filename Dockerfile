# Используйте официальный образ OpenJDK для запуска вашего приложения
FROM openjdk:17

# Скопируйте jar файл в контейнер
COPY CheckpointManager-0.0.8-SNAPSHOT.jar /app.jar

# Запустите ваше приложение
CMD ["java", "-jar", "/app.jar"]
