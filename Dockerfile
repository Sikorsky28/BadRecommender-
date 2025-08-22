FROM openjdk:17-jdk-slim

# Создаем директорию для приложения
WORKDIR /app

# Копируем JAR файл
COPY build/libs/BadRecommender-0.0.1-SNAPSHOT.jar app.jar

# Копируем credentials файл для Google Sheets
COPY google-credentials.json google-credentials.json

# Открываем порт
EXPOSE 8080

# Настраиваем переменные окружения
ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
