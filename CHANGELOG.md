# Changelog

All notable changes to this project are documented in this file.

## [v7.0.0] - 2026-04-26

### Summary
Seventh version of the Hotel Management API that modernizes the demo around Spring Boot 3.3, Keycloak, and Google OAuth2 login support.

### Highlights

- Upgraded the application line to the `hotelDemo` package and Spring Boot 3.3.
- Added Google OAuth2 registration alongside the existing Keycloak configuration.
- Kept JWT resource-server support in place for token-based access.
- Preserved the hotel CRUD and user-registration flows under Spring Security method rules.
- Added a Thymeleaf-backed custom `/login` page.
- Refreshed the tracked repository tree from the new demo snapshot.

### Notes

This release is primarily a security and configuration refresh rather than a change to the core hotel-management workflow.

## [v6.0.0] - 2026-04-17

### Summary
Sixth version of the Hotel Management API that adds rating-service integration so hotel lookup can fetch the latest rating from an external service.

### Highlights

- Added `RatingServiceCommunicator` (RestTemplate-based) for rating-service calls.
- Added `RatingResponse` DTO and `HttpRatingServiceNotFound` exception.
- Updated `GET /hotel/id/{id}` to accept the `Authorization` header and forward the JWT to the rating service.
- Kept the existing JWT login and role-protected hotel endpoints intact.

### Notes

This version introduces inter-service communication for read-time enrichment of hotel ratings.

## [v5.0.0] - 2026-04-17

### Summary
Fifth version of the Hotel Management API that strengthens the JWT security flow with PBKDF2 password encoding and CSRF token cookie support.

### Highlights

- Replaced BCrypt-based password encoding with `Pbkdf2PasswordEncoder`.
- Updated user registration to persist PBKDF2-hashed passwords.
- Added `CookieCsrfTokenRepository` configuration in the security filter chain.
- Kept the existing JWT login, registration, and role-protected hotel endpoints intact.
- Refreshed the README to present the project as a stronger Spring Security learning showcase.

### Notes

This version focuses on security configuration refinement rather than changing the main hotel-management workflow.

## [v4.0.0] - 2026-04-10

### Summary
Fourth version of the Hotel Management API that upgrades the security model from HTTP Basic authentication to JWT-based stateless authentication.

### Highlights

- Added `POST /auth/login` for token-based authentication.
- Added `AuthController` and `AuthService` for login and JWT issuance.
- Added `JwtAuthenticationFilter` and `JwtAuthenticationHelper` for bearer-token request handling.
- Added user registration and user-list endpoints through `UserController`.
- Refreshed the README to position the project as a JWT-secured hotel management API.

### Notes

This version improves the project’s learning value by introducing stateless authentication while keeping the hotel-management workflow compact.

## [v3.0.0] - 2026-04-09

### Summary
Third version of the Hotel Management API that restructures the project around JPA persistence and secured role-based access.

### Highlights

- Replaced in-memory hotel storage with a `HotelRepository` backed by JPA.
- Added MySQL configuration through `application.yml`.
- Added HTTP Basic authentication with `NORMAL` and `ADMIN` users.
- Added `@PreAuthorize` rules to separate read access from management actions.
- Introduced `HotelRequest` DTO for create requests and removed the earlier rating-service communicator flow.

### Notes

This version significantly improves the project’s architectural learning value by combining persistence layering and security without changing its core hotel-management purpose.

## [v2.0.0] - 2026-04-02

### Summary
Second version of the Hotel Management API that introduces rating-service synchronization through a dedicated RestTemplate communicator.

### Highlights

- Added `RatingServiceCommunicator` for external rating-service requests.
- Updated hotel creation to save rating data in the rating service.
- Updated hotel lookup to refresh the hotel rating from the rating service.
- Updated hotel update and delete flows to synchronize rating changes externally.
- Added `HttpRatingServiceNotFound` for rating-service delete failures.

### Notes

This version improves the project’s learning value by extending a local CRUD API into a simple cross-service integration example.

## [v1.0.0] - 2026-04-01

### Summary
Initial publication of the Hotel Management API as a clean, portfolio-ready Spring Boot REST project.

### Highlights

- Added a recruiter-friendly README with API overview, features, run steps, and project structure.
- Added a changelog to track future versions clearly.
- Cleaned editor files, build artifacts, and system clutter before publishing.
- Preserved the original hotel CRUD logic, validation, and exception handling.

### Notes

This version establishes the project as a compact learning showcase for building a REST API with Spring Boot and in-memory data storage.
