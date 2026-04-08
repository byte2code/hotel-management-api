# Hotel Management API

Spring Boot REST API for managing hotel records with JPA, MySQL persistence, HTTP Basic security, and role-based endpoint access.

## Overview

This project demonstrates a compact Spring Boot REST application for hotel data management. It evolves the earlier in-memory version into a database-backed API with repository-based persistence, DTO-driven creation flow, and security rules that separate admin-only management actions from normal-user read access.

## Concepts and Features Covered

- Spring Boot REST API setup
- Spring Data JPA repository pattern
- MySQL-backed persistence
- HTTP Basic authentication with Spring Security
- In-memory users with `NORMAL` and `ADMIN` roles
- Method-level authorization with `@PreAuthorize`
- DTO-based hotel creation requests
- `POST` endpoint for creating hotel records
- `GET` endpoint for retrieving a hotel by ID
- `GET` endpoint for listing all hotels
- `DELETE` endpoint for removing a hotel by ID
- JPA entity mapping with generated primary keys

## Tech Stack

- Java 17
- Spring Boot 2.7
- Spring Web
- Spring Data JPA
- Spring Security
- MySQL
- Maven
- Lombok
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
    │   │   ├── model/
    │   │   ├── repository/
    │   │   ├── service/
    │   │   └── HotelApplication.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/cn/hotel/
            └── HotelApplicationTests.java
```

## How to Run

1. Open a terminal in the project root.
2. Update the MySQL connection values in `src/main/resources/application.yml` if needed.
3. Run `mvn test`.
4. Run `mvn spring-boot:run`.
5. Use HTTP Basic authentication with one of the configured users:
   Username: `tony`
   Password: `password`
   Role: `NORMAL`

   Username: `steve`
   Password: `nopassword`
   Role: `ADMIN`
6. Use the API under `http://localhost:8082/hotel`.

Available endpoints:

- `POST /hotel/create`
- `GET /hotel/id/{id}`
- `GET /hotel/getAll`
- `DELETE /hotel/remove/id/{id}`

Access notes:

- `ADMIN` users can create, list, and delete hotels
- `NORMAL` users can retrieve hotels by ID

Example request body:

```json
{
  "name": "Sea View Inn",
  "rating": 8,
  "city": "Goa"
}
```

## Learning Highlights

- Demonstrates the shift from in-memory state to repository-backed JPA persistence
- Shows how DTOs can keep request input separate from the persistence entity
- Uses role-based authorization to separate read and management actions
- Keeps the API compact and focused for learning Spring Security and JPA together

## GitHub Metadata

- Suggested repository description: `Spring Boot REST API for hotel record management with JPA, MySQL persistence, HTTP Basic security, and role-based access control.`
- Suggested topics: `java`, `java-17`, `spring-boot`, `spring-security`, `spring-data-jpa`, `mysql`, `rest-api`, `hotel-management`, `basic-auth`, `maven`, `learning-project`, `portfolio-project`
