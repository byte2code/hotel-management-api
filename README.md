# Hotel Management API

Spring Boot REST API for managing hotel records with in-memory storage, validation, CRUD endpoints, and rating-service integration through a dedicated RestTemplate communicator.

## Overview

This project demonstrates a compact Spring Boot REST application for managing hotel data. It extends the original CRUD version by integrating a separate rating service so hotel creation, lookup, update, and delete operations can synchronize rating information across services while keeping the main hotel workflow easy to follow.

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
- RestTemplate communicator design for rating-service integration

## Tech Stack

- Java
- Spring Boot
- Spring Web
- Spring Validation
- Maven
- JUnit 5

## Project Structure

```text
hotel-management-api/
├── CHANGELOG.md
├── README.md
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/cn/hotel/
    │   │   ├── communicator/
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
2. Run `./mvnw test`.
3. Run `./mvnw spring-boot:run`.
4. Ensure the rating service is available on `http://localhost:8081`.
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
- Introduces a dedicated communicator class for external rating-service calls
- Keeps hotel persistence intentionally simple so the service-integration flow stays easy to understand

## GitHub Metadata

- Suggested repository description: `Spring Boot REST API for hotel record management with CRUD operations, validation, and rating-service integration through a RestTemplate communicator.`
- Suggested topics: `java`, `spring-boot`, `rest-api`, `hotel-management`, `crud-api`, `validation`, `resttemplate`, `maven`, `learning-project`, `portfolio-project`
