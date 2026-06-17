# Hotel Management API

![CI](https://github.com/byte2code/hotel-management-api/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![Tests](https://img.shields.io/badge/tests-13%20passing-brightgreen?logo=junit5&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

Spring Boot REST API for managing hotels, rooms, bookings, and cached room availability with MySQL persistence, Redis caching, JWT authentication, OAuth2 login support via Keycloak and Google, Swagger/OpenAPI 3 docs, and GitHub Actions CI.

## Overview

This release keeps the hotel-management workflow intact while refreshing the security layer, extending the domain model, and adding a persistent audit trail. The application supports Keycloak-backed JWT/resource-server access, Google OAuth2 login, Redis-backed room availability, and role-based endpoint protection through Spring Security annotations.

## Architecture

| Layer | Responsibility |
| --- | --- |
| API layer | Exposes hotel, room, user, and booking endpoints |
| Domain layer | Models hotels, rooms, users, and booking lifecycle states |
| Security layer | Uses JWT, OAuth2 login, and method-level authorization |
| Cache layer | Caches room-availability lookups in Redis and invalidates them on booking changes |
| Audit layer | Persists booking and security-sensitive actions in an audit log |
| Persistence layer | Stores hotel, room, user, and booking data in MySQL |
| View layer | Provides a Thymeleaf login page for browser sign-in |

## Concepts and Features Covered

- Spring Boot REST API setup
- Spring Data JPA repository pattern
- MySQL-backed persistence
- Spring Security with JWT resource-server support
- OAuth2 login with Keycloak
- OAuth2 login support for Google
- Method-level authorization with `@PreAuthorize`
- Public user registration and user listing
- Hotel creation, retrieval, listing, and deletion endpoints
- Room management for hotel inventory
- Booking creation with request/confirm/reject statuses
- Date-range validation for room availability
- Concurrency-safe booking writes using a locked room lookup
- Redis-backed caching for room availability searches
- Cache invalidation when rooms or bookings change
- Persistent audit logging for login, profile access, CRUD, and booking actions
- `/login` custom page backed by Thymeleaf
- Google user authority mapping from the local user table
- **Swagger/OpenAPI 3 integration for interactive API documentation at `/swagger-ui.html`**
- Global exception handling via `@RestControllerAdvice` for structured JSON error responses
- Spring Security integration tests with `MockMvc` and `@WithMockUser`
- **GitHub Actions CI pipeline — automated `mvn test` on every push and pull request**

## Tech Stack

- Java 17
- Spring Boot 3.3
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring OAuth2 Client
- Spring OAuth2 Resource Server
- Spring Cache
- Thymeleaf
- Redis
- MySQL
- Hibernate/JPA auditing patterns
- Maven
- Lombok
- JJWT
- Springdoc OpenAPI (Swagger UI)
- H2 (test isolation)
- GitHub Actions (CI)

## Project Structure

```text
hotel/
├── .github/
│   └── workflows/
│       └── ci.yml              ← GitHub Actions CI
├── CHANGELOG.md
├── README.md
├── docker-compose.override.yml.example
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/cn/hotelDemo/
    │   │   ├── config/          (HotelSecurityConfig, OpenApiConfig)
    │   │   ├── controller/      (Hotel, Room, Booking, User, Audit, Login)
    │   │   ├── dto/
    │   │   ├── exception/       (HotelNotFoundException, UserNotFoundException, ApiError, GlobalExceptionHandler)
    │   │   ├── model/
    │   │   ├── repository/
    │   │   ├── service/
    │   │   └── HotelDemoApplication.java
    │   └── resources/
    │       ├── application.yml
    │       └── templates/
    │           └── login.html
    └── test/
        ├── java/com/cn/hotelDemo/
        │   ├── config/          (HotelSecurityConfigTest)
        │   ├── controller/      (SecurityIntegrationTest)
        │   └── service/         (AuditServiceTest, BookingServiceTest, RoomServiceTest)
        └── resources/
            └── application.yml  (H2 in-memory test config)
```

## Environment Configuration

The application uses environment variables for sensitive or environment-specific settings. Create a `.env` file in the project root (or copy `docker-compose.override.yml.example` to `docker-compose.override.yml` if using Docker Compose) and configure these variables:

* `DB_HOST`: The MySQL server hostname (default: `localhost`)
* `DB_PASSWORD`: The MySQL database password
* `DATASOURCE_USERNAME`: The MySQL username (default: `demouser`)
* `KEYCLOAK_CLIENT_SECRET`: Keycloak client secret for OIDC registration
* `KEYCLOAK_ISSUER_URI`: Keycloak token issuer realm URI
* `KEYCLOAK_AUTH_SERVER_URL`: Keycloak authentication server endpoint URL
* `GOOGLE_CLIENT_ID`: Google OAuth client ID (optional)
* `GOOGLE_CLIENT_SECRET`: Google OAuth client secret (optional)
* `REDIS_HOST`: Redis server hostname (default: `localhost`)
* `REDIS_PORT`: Redis server port (default: `6379`)

See [.env.example](.env.example) and [docker-compose.override.yml.example](docker-compose.override.yml.example) for templates.

## How to Run

1. Open a terminal in the project root.
2. Copy `.env.example` to `.env` and configure your credentials and connection strings.
3. Run `mvn test` (executes the test suite against an isolated in-memory H2 database, requiring no active MySQL or Redis instances).
4. Run `mvn spring-boot:run`.
5. Open `http://localhost:8082/login` for the custom login page.
6. Use the API under `http://localhost:8082`.

Available endpoints:

- `GET /login`
- `GET /hotel/userDetail`
- `POST /hotel/create`
- `GET /hotel/id/{id}`
- `GET /hotel/getAll`
- `DELETE /hotel/remove/id/{id}`
- `POST /hotel/rooms/create`
- `GET /hotel/rooms/id/{id}`
- `GET /hotel/rooms/hotel/{hotelId}`
- `GET /hotel/rooms/hotel/{hotelId}/available?checkInDate=YYYY-MM-DD&checkOutDate=YYYY-MM-DD`
- `GET /hotel/rooms/getAll`
- `POST /hotel/bookings/create`
- `POST /hotel/bookings/cancel/{id}`
- `GET /hotel/bookings/id/{id}`
- `GET /hotel/bookings/getAll`
- `GET /hotel/bookings/user/{userId}`
- `GET /hotel/bookings/hotel/{hotelId}`
- `GET /audit/getAll`
- `GET /user/getUsers`
- `GET /user/getUsers/{id}`
- `POST /user/createUser`
- `DELETE /user/remove/id/{id}`

Access notes:

- `/login` is public.
- `GET /hotel/id/{id}` is for `NORMAL` users.
- `POST /hotel/create`, `GET /hotel/getAll`, and `DELETE /hotel/remove/id/{id}` are restricted to admin-style access.
- `POST /hotel/rooms/create` and `GET /hotel/rooms/getAll` are admin-only operations.
- `GET /audit/getAll` is admin-only.
- `GET /hotel/bookings/getAll` and `GET /hotel/bookings/hotel/{hotelId}` are admin-only operations.
- `POST /hotel/bookings/create` is available to authenticated hotel users.
- `GET /hotel/userDetail` uses the authenticated OIDC principal.
- Google login uses local user-role mapping from the MySQL user table.

Example user registration body:

```json
{
  "username": "john",
  "password": "john123",
  "email": "john@example.com",
  "role": "NORMAL"
}
```

Example hotel creation body:

```json
{
  "name": "Sea View Inn",
  "rating": 8,
  "city": "Goa"
}
```

Example room creation body:

```json
{
  "hotelId": 1,
  "roomNumber": "101",
  "roomType": "Deluxe",
  "capacity": 2,
  "nightlyRate": 4500.00,
  "status": "AVAILABLE"
}
```

Example room availability check:

`GET /hotel/rooms/hotel/1/available?checkInDate=2026-06-05&checkOutDate=2026-06-08`

Example booking creation body:

```json
{
  "hotelId": 1,
  "roomId": 1,
  "userId": 2,
  "checkInDate": "2026-06-05",
  "checkOutDate": "2026-06-08",
  "guestCount": 2,
  "specialRequests": "Late check-in"
}
```

Example booking response:

```json
{
  "bookingReference": "BOOK-1A2B3C4D",
  "status": "CONFIRMED",
  "message": "Booking confirmed successfully",
  "bookingId": 10
}
```

## Authentication Flow

```mermaid
flowchart LR
    Browser["🌐 Browser"] --> Login["GET /login"]
    Login --> Keycloak["Keycloak OIDC"]
    Login --> Google["Google OIDC"]
    Keycloak --> Token["JWT / OIDC claims"]
    Google --> Token
    Token --> Security["HotelSecurityConfig"]
    Security --> RoleMap["Role mapping from token + local user table"]
    RoleMap --> PreAuth["@PreAuthorize"]
    PreAuth --> Endpoint["Controller endpoint"]
    Endpoint --> Audit["Audit log entry"]
    PreAuth -->|denied| ExHandler["GlobalExceptionHandler → ApiError (403)"]
```

## Booking Flow

```mermaid
flowchart LR
    Guest["Authenticated Guest"] --> Create["POST /bookings/create"]
    Guest --> Cancel["POST /bookings/cancel/{id}"]

    Create --> RoomLock["Pessimistic room lock"]
    RoomLock --> Overlap["Date-range overlap check"]
    Overlap -->|available| Confirmed["CONFIRMED"]
    Overlap -->|conflict| Rejected["REJECTED"]

    Cancel --> StateCheck["Status transition check"]
    StateCheck -->|valid| Cancelled["CANCELLED"]
    StateCheck -->|invalid| Error["400 ApiError JSON"]

    Confirmed --> CacheEvict["Evict room-availability cache"]
    Rejected --> CacheEvict
    Cancelled --> CacheEvict

    Confirmed --> Audit["Audit log"]
    Rejected --> Audit
    Cancelled --> Audit
```

## System Flow

```mermaid
flowchart LR
    Client["Client / Swagger UI"] --> Security["Spring Security"]
    Security --> UserAPI["/user/*"]
    Security --> HotelAPI["/hotel/*"]
    Security --> AuditAPI["/audit/*"]
    Security --> SwaggerUI["/swagger-ui.html"]

    HotelAPI --> Hotels["Hotel CRUD"]
    HotelAPI --> Rooms["Room inventory"]
    HotelAPI --> Bookings["Booking lifecycle"]

    Rooms --> Cache["Redis cache"]
    Rooms --> LockedRoom["Pessimistic lock"]
    LockedRoom --> Availability["Date-range check"]
    Availability --> Cache

    Bookings --> Audit["Audit trail"]
    Hotels --> Audit

    Hotels -.->|missing ID| CustomEx["HotelNotFoundException / UserNotFoundException"]
    CustomEx -.-> ExHandler
    Security -.->|error| ExHandler["GlobalExceptionHandler"]
    ExHandler --> ErrorJSON["ApiError JSON response"]

    CI["GitHub Actions CI"] --> Tests["mvn test on H2"]
```

## GitHub Metadata

- Suggested repository description: `Spring Boot REST API for hotel, room, booking, and audit-log management with MySQL, Redis caching, JWT/OAuth2 auth, Swagger/OpenAPI 3 docs, Spring Security tests, and GitHub Actions CI.`
- Suggested topics: `java`, `java-17`, `spring-boot`, `spring-boot-3`, `spring-security`, `spring-security-test`, `spring-data-jpa`, `spring-validation`, `spring-cache`, `redis`, `mysql`, `h2-database`, `rest-api`, `hotel-management`, `room-booking`, `room-availability`, `cache-invalidation`, `audit-log`, `observability`, `concurrency`, `pessimistic-locking`, `jwt`, `oauth2`, `keycloak`, `google-login`, `thymeleaf`, `swagger`, `openapi`, `springdoc`, `github-actions`, `ci-cd`, `maven`, `learning-project`, `portfolio-project`
