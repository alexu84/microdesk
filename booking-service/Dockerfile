FROM bellsoft/liberica-openjdk-alpine:21 AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x gradlew && ./gradlew test
RUN ./gradlew bootJar -x test

FROM bellsoft/liberica-openjre-alpine:21
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
