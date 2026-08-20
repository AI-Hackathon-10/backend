FROM gradle:8-jdk17-alpine AS builder
WORKDIR /build

COPY build.gradle settings.gradle ./
RUN gradle build -x test --no-daemon || true

COPY . .
RUN gradle clean build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /build/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "app.jar"]