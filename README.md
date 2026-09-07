# Contact Management System

[![Build](https://github.com/AbdullAdil/cohort-9-java-16770-abdullah/actions/workflows/build.yml/badge.svg)](https://github.com/AbdullAdil/cohort-9-java-16770-abdullah/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AbdullAdil_cohort-9-java-16770-abdullah&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AbdullAdil_cohort-9-java-16770-abdullah)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=AbdullAdil_cohort-9-java-16770-abdullah&metric=coverage)](https://sonarcloud.io/summary/new_code?id=AbdullAdil_cohort-9-java-16770-abdullah)

Cohort 9 — JAVA Fullstack (JAVA+ReactJS) assignment for Abdullah Adil.

A web app where users register, sign in, and manage their own contacts. Each
contact has a name, title, and any number of labelled email addresses and phone
numbers. Contacts are private to the user who created them.

## Tech stack

**Backend** — Java 21, Spring Boot 4.1, Spring Data JPA / Hibernate, Spring
Security with JWT, Bean Validation, Slf4j + Logback, Flyway, JUnit 5 + Mockito,
SQL Server (H2 for local development).

**Frontend** — React 19, Vite, React Router, axios.

**Tooling** — Maven wrapper, JaCoCo, SonarCloud, GitHub Actions, Docker Compose.

## Layout

```text
backend/    Spring Boot API
frontend/   React single page app
docker-compose.yml   SQL Server for local development
```

## Running it

You need JDK 21+ and Node 20+. Both parts run at the same time, in two
terminals.

> **On Windows / PowerShell**, use `.\mvnw.cmd` instead of `./mvnw`, and
> `npm.cmd` instead of `npm` (PowerShell blocks the `npm.ps1` wrapper by
> default). Set environment variables with `$env:NAME="value"` rather than
> inline, for example:
>
> ```powershell
> $env:JAVA_HOME="C:\path\to\jdk-21"
> .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
> ```

### Backend

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Starts on http://localhost:8080 using an in-memory H2 database, so there is
nothing to install first. The data is wiped on every restart. The H2 console is
at http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:contactsdb`, user
`sa`, no password).

On the dev profile a demo account is created automatically, already populated
with twelve contacts so the list, paging and search have something to show:

| Email | Password |
| --- | --- |
| `demo@example.com` | `password123` |

You can register your own account instead; either works. The seeding only
happens on the dev profile, so this account never exists in a real database.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Starts on http://localhost:5173 and proxies `/api` to the backend, so there is
no CORS setup in development. If the backend is on a different port, set
`BACKEND_URL` before starting, for example `BACKEND_URL=http://localhost:8081`.

### Running against SQL Server

The `prod` profile targets SQL Server, and Flyway creates the schema from
`backend/src/main/resources/db/migration`.

Put the credentials in a `.env` file in the repository root, so they stay out of
your shell history. The file is gitignored:

```dotenv
SA_PASSWORD=choose-a-strong-password
DB_USERNAME=sa
DB_PASSWORD=choose-a-strong-password
JWT_SECRET=a-long-random-secret-at-least-32-characters
```

Docker Compose picks that up on its own:

```bash
docker compose up -d
```

Wait for it to finish starting, then create the (empty) database. Flyway builds
the tables, but it can't create the database itself:

```bash
docker exec -e SA_PASSWORD contact-management-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -C -Q "IF DB_ID('contactsdb') IS NULL CREATE DATABASE contactsdb;"
```

Then run the backend. The credentials have no defaults, so nothing usable is
committed to the repo:

```bash
cd backend && set -a && . ../.env && set +a && ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

On PowerShell, load the `.env` values first and then start it:

```powershell
Get-Content .env | ForEach-Object { if ($_ -match '^(.+?)=(.*)$') { Set-Item "env:$($Matches[1])" $Matches[2] } }
cd backend; .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

## Tests

```bash
cd backend && ./mvnw verify
```

47 tests covering the service, controller and repository layers, plus a
coverage report written to `backend/target/site/jacoco/`.

## API

All `/api/contacts` and `/api/users` endpoints need an
`Authorization: Bearer <token>` header, using the token returned by register or
login.

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/auth/register` | Register with an email or a phone number |
| POST | `/api/auth/login` | Sign in with either identifier |
| GET | `/api/users/me` | Details of the signed-in user |
| PUT | `/api/users/me/password` | Change password |
| GET | `/api/contacts` | List contacts, paginated (`page`, `size`, `search`) |
| POST | `/api/contacts` | Create a contact |
| GET | `/api/contacts/{id}` | Get one contact |
| PUT | `/api/contacts/{id}` | Update a contact |
| DELETE | `/api/contacts/{id}` | Delete a contact |

## Code quality

`mvnw verify` produces a JaCoCo coverage report, and GitHub Actions runs the
build, the tests, and the frontend lint on every push. SonarCloud analysis runs
too, as long as a `SONAR_TOKEN` secret is set on the repository; the project and
organization keys are in `backend/pom.xml`.
