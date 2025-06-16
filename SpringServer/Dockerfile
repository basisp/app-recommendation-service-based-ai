# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /home/gradle/src
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build -x test --no-daemon

# Package stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"] 