FROM gradle:jdk21-jammy AS build
WORKDIR /app
COPY build.gradle settings.gradle /app/
COPY src /app/src
RUN gradle clean build -x test

FROM openjdk:21-slim-buster
WORKDIR /app
COPY --from=build /app/build/libs/BadRecommender-0.0.1-SNAPSHOT.jar /app/app.jar
COPY google-credentials.json /app/google-credentials.json
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
