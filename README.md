# Hotel Management API

Spring Boot REST API for managing hotels with MySQL persistence, JWT authentication, and OAuth2 login support via Keycloak and Google.

## Overview

This release keeps the hotel-management workflow intact while refreshing the security layer. The application supports Keycloak-backed JWT/resource-server access, Google OAuth2 login, and role-based endpoint protection through Spring Security annotations.

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
- `/login` custom page backed by Thymeleaf
- Google user authority mapping from the local user table

## Tech Stack

- Java 17
- Spring Boot 3.3
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring OAuth2 Client
- Spring OAuth2 Resource Server
- Thymeleaf
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
        ├── java/com/cn/hotelDemo/
        │   ├── config/
        │   ├── controller/
        │   ├── dto/
        │   ├── model/
        │   ├── repository/
        │   ├── service/
        │   └── HotelDemoApplication.java
        └── resources/
            ├── application.yml
            └── templates/
                └── login.html
```

## How to Run

1. Open a terminal in the project root.
2. Update MySQL, Keycloak, and Google client settings in `src/main/resources/application.yml` if needed. The Google entries are placeholders in the published template.
3. Run `mvn test`.
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
- `GET /user/getUsers`
- `GET /user/getUsers/{id}`
- `POST /user/createUser`
- `DELETE /user/remove/id/{id}`

Access notes:

- `/login` is public.
- `GET /hotel/id/{id}` is for `NORMAL` users.
- `POST /hotel/create`, `GET /hotel/getAll`, and `DELETE /hotel/remove/id/{id}` are restricted to admin-style access.
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

## GitHub Metadata

- Suggested repository description: `Spring Boot REST API for hotel record management with MySQL persistence, JWT authentication, and OAuth2 login support via Keycloak and Google.`
- Suggested topics: `java`, `java-17`, `spring-boot`, `spring-security`, `spring-data-jpa`, `mysql`, `rest-api`, `hotel-management`, `jwt`, `oauth2`, `keycloak`, `google-login`, `thymeleaf`, `maven`, `learning-project`, `portfolio-project`
