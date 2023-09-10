# Используйте официальный образ OpenJDK для запуска вашего приложения
FROM openjdk:17

# Скопируйте jar файл в контейнер
COPY build/libs/app.jar /app.jar

# Запустите ваше приложение
CMD ["java", "-jar", "/app.jar"]
