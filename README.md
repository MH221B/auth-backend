# Auth Backend

Lightweight Spring Boot authentication backend used for demos and exercises. It provides a minimal user registration flow and is configured to run locally via Gradle or Docker Compose.

## What's in this repo

- Spring Boot application (Java)
- PostgreSQL for persistence (via Docker Compose)
- Simple user registration endpoint: POST /user/register
- Configuration examples for local development and production

## Prerequisites

- Java (JDK 11+ recommended) if running locally
- Docker & Docker Compose (recommended for local development)
- PowerShell (Windows) or another shell

## Quick start — Docker Compose (recommended)

1. Copy the example env file to `.env` and edit values. Do NOT commit your real `.env`:

   PowerShell:

       Copy-Item .env.example .env

2. Start the application and Postgres using the development compose (includes optional dev services):

   PowerShell (recommended when using the dev override):

       docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d

   Or without the dev override (production-like):

       docker-compose up --build -d

3. View logs (app):

       docker-compose logs -f app

4. Stop and remove containers:

       docker-compose down

Notes:
- When running with Docker Compose, prefer setting SPRING_DATASOURCE_URL to use the `postgres` service host: `jdbc:postgresql://postgres:5432/auth`.
- If host port 5432 is already taken, change the host mapping for the Postgres service in `docker-compose.yml` (e.g., `5433:5432`).

## Running locally with Gradle

Run the application using the bundled Gradle wrapper (PowerShell):

  .\gradlew.bat bootRun

Or build a jar and run it:

  .\gradlew.bat build; java -jar build\libs\auth-backend-0.0.1-SNAPSHOT.jar

## Configuration & environment

Environment variables (or `.env` when using Compose) control DB connection and profiles. See `.env.example` for defaults.

Important variables:

- SPRING_DATASOURCE_URL — JDBC URL for Postgres (e.g. `jdbc:postgresql://postgres:5432/auth` when using compose)
- SPRING_DATASOURCE_USERNAME — DB username
- SPRING_DATASOURCE_PASSWORD — DB password
- SPRING_PROFILES_ACTIVE — (optional) set to `prod` for production config

## API

This project currently exposes a small set of endpoints. The main public endpoint:

- POST /user/register — register a new user

Example request (curl):

  curl -X POST http://localhost:8080/user/register \
    -H "Content-Type: application/json" \
    -d '{"username":"alice","email":"alice@example.com","password":"s3cret"}'

Success response: 200 OK with a JSON body containing the created user DTO (id, username, email, etc.).

Note: An authentication/login endpoint is implemented in the service layer but no public controller mapping exists for it in this branch.

## Tests

Run unit tests with Gradle:

  .\gradlew.bat test

## Production notes

For production deployments (Render, Neon, etc.):

- Use a managed Postgres instance and set the appropriate SPRING_DATASOURCE_URL/USERNAME/PASSWORD in the platform's environment settings.
- Use `application-prod.properties` by setting SPRING_PROFILES_ACTIVE=prod.
- Avoid storing secrets in the repository; use the platform's secrets management.
- Use a DB migration tool (Flyway/Liquibase) for schema changes rather than `ddl-auto=update` in production.

## Files of interest

- `src/main/java/.../controller/AuthController.java` — registration endpoint
- `.env.example` — example environment variables
- `docker-compose.yml` & `docker-compose.dev.yml` — compose stacks for prod-like and dev scenarios

## Troubleshooting

- If containers fail to start, inspect logs with `docker-compose logs` and check that `SPRING_DATASOURCE_URL` points to the correct host (inside compose use `postgres`).
- If the app cannot bind to port 8080, ensure no other service is using that port or change the mapping in the compose file.

---
