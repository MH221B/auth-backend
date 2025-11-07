# Auth Backend

Lightweight Spring Boot authentication backend used for demos and exercises. It provides a minimal user registration flow and is intended to be run on the host (locally) via the bundled Gradle wrapper or by running the built jar.
This repository includes a Docker Compose file that starts a Postgres database for local development, but the Spring Boot application itself no longer runs inside a container.

## What's in this repo

- Spring Boot application (Java)
- PostgreSQL for persistence (via Docker Compose)
- Simple user registration endpoint: POST /user/register
- Configuration examples for local development and production

## Prerequisites

- Java (JDK 11+ recommended) to run the application locally
- PowerShell (Windows) or another shell for running the commands below
- Docker & Docker Compose (optional) only if you want to run the bundled Postgres database in a container

## Quick start — Run locally (recommended)

This project is intended to be run on your host machine (IDE or the Gradle wrapper). The included `docker-compose.yml` only starts a Postgres database for local development — it does not start the Spring Boot app.

1. Copy the example env file to `.env` and edit values. Do NOT commit your real `.env`:

   PowerShell:

     Copy-Item .env.example .env

2. (Optional) Start Postgres with Docker Compose if you want a local DB container:

   PowerShell:

     docker-compose up -d

   View Postgres logs:

     docker-compose logs -f auth-backend-postgres

   Stop and remove containers:

     docker-compose down

3. Run the Spring Boot application on your host using the Gradle wrapper (PowerShell):

   Run in-place (development):

     .\gradlew.bat bootRun

   Or build and run the jar:

     .\gradlew.bat build; java -jar build\libs\auth-backend-0.0.1-SNAPSHOT.jar

Notes:

- The Compose file is provided only to run a Postgres database for local development. The application is expected to run on the host and connect to that database via `localhost` (or the host port mapped in `docker-compose.yml`).
- When connecting to a DB started by Compose from your host, set `SPRING_DATASOURCE_URL` to something like `jdbc:postgresql://localhost:5433/auth` (adjust the host port if you changed the mapping).

## Configuration & environment

Environment variables (or the `.env` file when using Compose for Postgres) control DB connection and profiles. See `.env.example` for defaults.
  
Important variables:

- SPRING_DATASOURCE_URL — JDBC URL for Postgres (e.g. `jdbc:postgresql://localhost:5433/auth` when running the app on your host)
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
- `docker-compose.yml` — Compose stack; use it to start a Postgres container only (database), not the application

## Troubleshooting

- If containers fail to start, inspect logs with `docker-compose logs` and check that `SPRING_DATASOURCE_URL` points to the correct host (inside compose use `postgres`).
- If the app cannot bind to port 8080, ensure no other service is using that port or change the mapping in the compose file.
- If the application cannot connect to Postgres, verify that the container is running (`docker-compose ps`) and that `SPRING_DATASOURCE_URL` points to `localhost` with the correct host port (default `jdbc:postgresql://localhost:5433/auth`).
- If you run the app inside Docker Compose, use `postgres` as the DB host in the JDBC URL (e.g. `jdbc:postgresql://postgres:5432/auth`).
- If the app cannot bind to port 8080, ensure no other service is using that port or change the mapping in the compose file.

---
