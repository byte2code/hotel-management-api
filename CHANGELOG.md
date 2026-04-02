# Changelog

All notable changes to this project are documented in this file.

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
