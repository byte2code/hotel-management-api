# Hotel Management API

Spring Boot REST API for managing hotel records with in-memory storage, validation, and basic CRUD endpoints.

## Overview

This project demonstrates a compact Spring Boot REST application for managing hotel data. It is designed as a learning project that shows how controllers, services, validation, and exception handling work together in a simple API without introducing database complexity.

## Concepts and Features Covered

- Spring Boot REST API setup
- `POST` endpoint for creating hotel records
- `GET` endpoint for retrieving a hotel by ID
- `GET` endpoint for listing all hotels
- `PUT` endpoint for updating a hotel
- `DELETE` endpoint for removing a hotel by ID
- Bean validation for request payloads
- Custom exception handling for missing hotels
- In-memory data storage using `List` and `Map`

## Tech Stack

- Java
- Spring Boot
- Spring Web
- Spring Validation
- Maven
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
    │   │   ├── controller/
    │   │   ├── exceptions/
    │   │   ├── model/
    │   │   ├── service/
    │   │   └── HotelApplication.java
    │   └── resources/
    └── test/
        └── java/com/cn/hotel/
```

## How to Run

1. Open a terminal in the project root.
2. Run `chmod +x mvnw` once if needed.
3. Run `./mvnw test`.
4. Run `./mvnw spring-boot:run`.
5. Use the API under `http://localhost:8080/hotel`.

Available endpoints:

- `POST /hotel/create`
- `GET /hotel/id/{id}`
- `GET /hotel/getAll`
- `PUT /hotel/update`
- `DELETE /hotel/remove/id/{id}`

Example request body:

```json
{
  "id": "H101",
  "name": "Sea View Inn",
  "rating": 8,
  "city": "Goa"
}
```

## Learning Highlights

- Demonstrates a clean controller-service split in a Spring Boot REST project
- Shows how validation annotations can protect incoming request data
- Uses a custom not-found exception to return clearer API responses
- Keeps persistence intentionally simple so the API flow is easy to understand

## GitHub Metadata

- Suggested repository description: `Spring Boot REST API for hotel record management with CRUD operations, request validation, and in-memory storage.`
- Suggested topics: `java`, `spring-boot`, `rest-api`, `hotel-management`, `crud-api`, `validation`, `maven`, `learning-project`, `portfolio-project`
