# ShareHub

ShareHub is a community resource-sharing platform built with Spring Boot. It is designed to connect people who can give useful resources with people who need support.

The project supports two clients from the same backend:

- A Thymeleaf web experience for browser users.
- A JSON REST API for a future mobile application or other clients.

## Features

- Session-based registration, login and logout.
- `DONOR` and `RECIPIENT` user roles.
- BCrypt password hashing through Spring Security.
- Thymeleaf login, registration and protected dashboard pages.
- Profile page populated from the authenticated user.
- Resources discovery page prepared for future listings.
- PostgreSQL persistence with Flyway database migrations.
- Swagger/OpenAPI documentation.
- Latitude and longitude fields prepared for location-aware features.
- Docker Compose setup for the application and PostgreSQL.

## Technology

- Java 17
- Spring Boot 3.1.5
- Spring Web MVC
- Spring Data JPA
- Spring Security 6
- Thymeleaf and Thymeleaf Spring Security extras
- PostgreSQL 15
- Flyway
- Maven Wrapper
- Bootstrap Icons and custom responsive CSS

## Architecture

The web and mobile flows share the same service and repository layers:

```text
Browser                  Mobile app
	 |                         |
	 v                         v
WebController          AuthController / API controllers
	 |                         |
	 +-----------+-------------+
							 v
					Service layer
							 |
							 v
			 Repository + PostgreSQL
```

`WebController` returns Thymeleaf views. REST controllers return JSON and are intended to be consumed by the mobile application. Business logic belongs in services so it is not duplicated between clients.

## Project Structure

```text
src/main/java/com/sharehub/sharehub/
├── config/                  Spring Security configuration
├── controller/              Web and REST controllers
├── dto/                     Request and response objects
├── entity/                  JPA entities
├── exception/               Global exception handling
├── repository/              Spring Data repositories
└── service/                 Authentication and user services

src/main/resources/
├── db/migration/             Flyway SQL migrations
├── static/
│   ├── css/style.css         Shared visual styles
│   └── js/main.js            Auth and logout interactions
└── templates/
		├── auth/                 Login and registration pages
		├── dashboard/            Protected dashboard
		├── profile/              Authenticated profile page
		├── resources/            Resources discovery page
		├── error/                Error page
		└── fragments/             Reusable header and footer fragments
```

## Requirements

- JDK 17 or newer.
- PostgreSQL 15 or a compatible PostgreSQL server.
- Git, if cloning the repository.

## Database Setup

The default local configuration expects:

```text
Host:     localhost
Port:     5432
Database: sharehub
Username: postgres
Password: postgres
```

Create the database before starting the application:

```sql
CREATE DATABASE sharehub;
```

Flyway automatically applies `V1__Create_User_Table.sql` on startup. The migration creates the `users` table, role column, account timestamps, and nullable `latitude` and `longitude` columns.

For a real deployment, replace the default credentials with environment variables or an external secret manager.

## Run Locally

From the project directory:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
./mvnw.cmd spring-boot:run
```

Open:

- Web app: <http://localhost:8080/login>
- Register: <http://localhost:8080/register>
- Swagger UI: <http://localhost:8080/swagger-ui/index.html>

## Run with Docker Compose

Docker Compose starts PostgreSQL and the Spring Boot application:

```bash
docker compose up --build
```

The application is available at <http://localhost:8080>. To stop the stack:

```bash
docker compose down
```

To remove the persisted PostgreSQL volume as well:

```bash
docker compose down -v
```

## REST API

All authentication endpoints are under `/api/auth`.

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
	"email": "member@example.com",
	"password": "secret123",
	"name": "Community Member",
	"phone": "01700000000",
	"address": "Dhaka",
	"latitude": 23.8103,
	"longitude": 90.4125,
	"role": "DONOR"
}
```

`role` must be either `DONOR` or `RECIPIENT`. Registration creates a session automatically.

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
	"email": "member@example.com",
	"password": "secret123"
}
```

The response creates a session cookie named `SHAREHUB_SESSION`. Mobile clients should persist and send the session cookie with later requests.

### Session Information

```http
GET /api/auth/session-info
```

### Logout

```http
POST /api/auth/logout
```

### Protected Dashboard API

```http
GET /api/dashboard
```

This endpoint requires an authenticated session and returns the current user's email, authorities and authentication state.

## Web Routes

| Route | Access | Purpose |
|---|---|---|
| `/login` | Public | Thymeleaf login page |
| `/register` | Public | Thymeleaf registration page |
| `/dashboard` | Authenticated | User dashboard |
| `/profile` | Authenticated | Current user's profile |
| `/resources` | Authenticated | Resource discovery view |
| `/` | Authenticated | Redirects to dashboard |

## Build and Test

Compile and package without starting the database-dependent test context:

```bash
./mvnw package -DskipTests
```

Run the full test suite:

```bash
./mvnw test
```

The integration context test requires PostgreSQL credentials that match `application.properties` or the active environment. If PostgreSQL is unavailable or the password differs, the test context will fail during Flyway initialization.

## Current Scope

The authentication and first web experience are in place. The next product layer can add:

- Donation and resource listing entities.
- Donor listing creation and management.
- Recipient requests and request history.
- Search, filtering and location-based discovery.
- Profile editing and account settings.
- Mobile API clients and refreshable authentication.

## License

No explicit license has been added yet.

