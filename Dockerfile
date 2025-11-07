FROM eclipse-temurin:21-jdk AS builder
WORKDIR /home/app

# Copy project files (the Gradle wrapper is included in the repo)
COPY . .

# Ensure the wrapper is executable and build the fat jar (skip tests for speed)
RUN chmod +x ./gradlew || true
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /home/app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
