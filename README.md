# Hotel Management API

Spring Boot REST API for managing hotels with MySQL persistence, JWT authentication, and rating-service integration via `RestTemplate`.

## Overview

This project demonstrates a compact Spring Boot REST application for hotel data management. It uses repository-backed JPA persistence, JWT-based stateless authentication, public user registration, and role-protected hotel endpoints. In this version, hotel lookup by ID enriches the hotel rating by calling an external rating service.

## Concepts and Features Covered

- Spring Boot REST API setup
- Spring Data JPA repository pattern
- MySQL-backed persistence
- Spring Security with JWT authentication (stateless)
- Method-level authorization with `@PreAuthorize`
- User registration and token-based login flow
- `RestTemplate` communicator pattern for calling an external rating service
- `GET` endpoint for retrieving a hotel by ID (rating fetched from rating service)
- `GET` endpoint for listing all hotels
- `POST` endpoint for creating a hotel record
- `DELETE` endpoint for deleting a hotel record
- `GET` endpoint for listing registered users

## Tech Stack

- Java 17
- Spring Boot 2.7
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- MySQL
- Maven
- Lombok
- JJWT

## Project Structure

```text
hotel/
├── CHANGELOG.md
├── README.md
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
    └── main/
        ├── java/com/cn/hotel/
        │   ├── communicator/
        │   ├── config/
        │   ├── controller/
        │   ├── dto/
        │   ├── exceptions/
        │   ├── jwt/
        │   ├── model/
        │   ├── repository/
        │   ├── security/
        │   ├── service/
        │   └── HotelApplication.java
        └── resources/
            └── application.yml
```

## How to Run

1. Open a terminal in the project root.
2. Update the MySQL connection values in `src/main/resources/application.yml` if needed.
3. Ensure the rating service is available at `http://localhost:8081`.
4. Run `mvn test`.
5. Run `mvn spring-boot:run`.
6. Register a user with `POST /user/register`.
7. Obtain a token with `POST /auth/login`.
8. Call protected endpoints with `Authorization: Bearer <token>`.

Available endpoints:

- `POST /auth/login`
- `GET /user`
- `POST /user/register`
- `POST /hotel/create`
- `GET /hotel/id/{id}`
- `GET /hotel/getAll`
- `DELETE /hotel/remove/id/{id}`

Notes:

- `/user/register` and `/auth/login` are public.
- `GET /hotel/id/{id}` expects the `Authorization` header and forwards the JWT to the rating service.
- Roles are enforced with `@PreAuthorize` (`ADMIN` for create/list/delete, `NORMAL` for get-by-id).

Example request body for user registration:

```json
{
  "username": "john",
  "password": "john123"
}
```

Example request body for login:

```json
{
  "username": "john",
  "password": "john123"
}
```

Example request body for hotel creation:

```json
{
  "name": "Sea View Inn",
  "rating": 8,
  "city": "Goa"
}
```

## GitHub Metadata

- Suggested repository description: `Spring Boot REST API for hotel record management with MySQL persistence, JWT authentication, and rating-service integration via RestTemplate.`
- Suggested topics: `java`, `java-17`, `spring-boot`, `spring-security`, `spring-data-jpa`, `mysql`, `rest-api`, `hotel-management`, `jwt`, `resttemplate`, `microservices`, `maven`, `learning-project`, `portfolio-project`

