# Hotel Management API

Spring Boot REST API for managing hotels with MySQL persistence, JWT authentication, and role-based access control.

## Overview

This project demonstrates a compact Spring Boot REST application for hotel data management. It uses repository-backed JPA persistence, JWT-based stateless authentication, public user registration, and role-protected hotel endpoints to model a simple secured admin-and-user workflow.

## Concepts and Features Covered

- Spring Boot REST API setup
- Spring Data JPA repository pattern
- MySQL-backed persistence
- Spring Security with JWT authentication
- Stateless session policy with a custom JWT filter
- Method-level authorization with `@PreAuthorize`
- Public user registration and token-based login flow
- Custom `UserDetailsService` for username-based lookup
- `POST` endpoint for creating hotel records
- `GET` endpoint for retrieving a hotel by ID
- `GET` endpoint for listing all hotels
- `DELETE` endpoint for removing a hotel by ID
- `GET` endpoint for listing registered users

## Tech Stack

- Java 17
- Spring Boot 2.7
- Spring Web
- Spring Data JPA
- Spring Security
- MySQL
- Maven
- Lombok
- JJWT
- JUnit 5

## Project Structure

```text
hotel/
├── CHANGELOG.md
├── README.md
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/cn/hotel/
    │   │   ├── config/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   ├── jwt/
    │   │   ├── model/
    │   │   ├── repository/
    │   │   ├── security/
    │   │   ├── service/
    │   │   └── HotelApplication.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/cn/hotel/
```

## How to Run

1. Open a terminal in the project root.
2. Update the MySQL connection values in `src/main/resources/application.yml` if needed.
3. Run `mvn test`.
4. Run `mvn spring-boot:run`.
5. Register a user with `POST /user/register`.
6. Obtain a token with `POST /auth/login`.
7. Use protected endpoints with `Authorization: Bearer <token>`.

Available endpoints:

- `POST /auth/login`
- `GET /user`
- `POST /user/register`
- `POST /hotel/create`
- `GET /hotel/id/{id}`
- `GET /hotel/getAll`
- `DELETE /hotel/remove/id/{id}`

Access notes:

- `/user/register` and `/auth/login` are public
- `ADMIN` users can create, list, and delete hotels, and list users
- `NORMAL` users can retrieve hotels by ID

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

## Learning Highlights

- Demonstrates the shift from basic auth to stateless JWT authentication in Spring Security
- Shows how a custom filter can authenticate requests from bearer tokens
- Uses JPA repositories to keep persistence simple while focusing on the security flow
- Keeps the API compact and readable for learning role-based endpoint protection

## GitHub Metadata

- Suggested repository description: `Spring Boot REST API for hotel record management with MySQL persistence, JWT authentication, and role-based access control.`
- Suggested topics: `java`, `java-17`, `spring-boot`, `spring-security`, `spring-data-jpa`, `mysql`, `rest-api`, `hotel-management`, `jwt`, `maven`, `learning-project`, `portfolio-project`
