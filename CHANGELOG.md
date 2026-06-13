# Changelog

All notable changes to this project are documented in this file.

## [v8.4.0] - 2026-06-13

### Summary
New version of the Hotel Management API that externalizes datasource credentials and security secrets into environment variables for safer and cleaner deployments.

### Highlights
- Replaced hardcoded database connection properties (username, password, URL) in `application.yml` with property placeholders.
- Replaced Keycloak client secret and OIDC URLs with configurable environment variables.
- Added a `.env.example` template file in the project root to help configure environment variables.

### Notes
This release improves security hygiene by preventing production credentials and identity-provider secrets from being committed directly to public source control.

## [v8.3.0] - 2026-06-02

### Summary
New version of the Hotel Management API that adds persistent audit logging for booking and security-sensitive actions, along with updated authentication and booking flow documentation.

### Highlights

- Added an `AuditLog` entity and repository for storing action, actor, resource, status, and timestamp details.
- Added audit logging for login page access, authenticated profile access, hotel CRUD, room creation, booking writes, and user registration/deletion.
- Added an admin-only audit log endpoint for reviewing recorded events.
- Refreshed the README with authentication flow and booking flow diagrams.
- Updated the GitHub metadata suggestions to include audit-log and observability keywords.

### Notes

The audit trail complements the existing Redis cache and concurrency-safe booking flow by making the security and booking paths easier to trace during reviews and demos.

## [v8.2.0] - 2026-06-01

### Summary
New version of the Hotel Management API that adds Redis-backed caching for room-availability searches with invalidation on room and booking changes.

### Highlights

- Added a Redis-backed cache for room-availability lookups by hotel and date range.
- Added cache eviction when rooms are created and when bookings are written.
- Added a dedicated room-availability endpoint to the README and API flow diagram.
- Updated the GitHub metadata suggestions to reflect Redis, caching, and cache invalidation.

### Notes

This release improves search responsiveness for repeated availability checks while keeping the booking logic concurrency-safe.

## [v8.1.0] - 2026-05-31

### Summary
Ninth version of the Hotel Management API that makes booking writes concurrency-safe so the same room cannot be double-booked under overlapping requests.

### Highlights

- Added a pessimistic write lock on room lookup during booking creation.
- Kept the date-overlap validation inside the same transaction so concurrent requests are serialized per room.
- Preserved the existing booking statuses and rejected-booking response path.
- Refreshed the README to explain the locked booking flow and updated the GitHub metadata topics.

### Notes

This release improves the reliability of the room-booking workflow under concurrent traffic without changing the public booking API.

## [v8.0.0] - 2026-05-31

### Summary
Eighth version of the Hotel Management API that introduces room inventory and booking lifecycle management on top of the existing hotel security workflow.

### Highlights

- Added `Room` as a first-class hotel child entity with room number, type, capacity, nightly rate, and status.
- Added `Booking` as a first-class domain entity tied to hotel, room, and user records.
- Added booking statuses for request, confirmation, cancellation, and rejection flows.
- Added room management endpoints for creating and listing rooms by hotel.
- Added booking endpoints for creating and querying bookings by hotel or user.
- Refreshed the README architecture, flow diagram, endpoint list, and GitHub metadata suggestions.

### Notes

The new booking flow validates hotel-room ownership, checks date overlap, and records rejected bookings instead of failing silently.

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
