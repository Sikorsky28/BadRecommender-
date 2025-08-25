FROM gradle:8.4-jdk21 AS build
WORKDIR /app
COPY build.gradle settings.gradle /app/
COPY src /app/src
RUN gradle clean build -x test --info

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/BadRecommender-0.0.1-SNAPSHOT.jar /app/app.jar
# google-credentials.json будет добавлен через Secret Files в Render.com
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
