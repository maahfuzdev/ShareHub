# Architecture Diagram

ShareHub is a Spring Boot application that serves both a server-rendered web experience and a session-based JSON API. Both entry points share the same security, business services, repositories, and PostgreSQL database.

## Application Architecture

<!-- mermaid-checked: no \n, no em-dash/en-dash, no {} in labels, subgraphs are id["label"], arrows are -->|"label"|, all subgraphs closed by end, ids unique -->
~~~mermaid
flowchart TD
    subgraph Clients["Client Layer"]
        Browser["Web Browser"]
        ApiClient["External API Client"]
    end
    subgraph App["Application Layer - Spring Boot 3.4.7"]
        Web["Spring MVC and Thymeleaf"]
        Api["Session-based JSON API"]
        Security["Spring Security and sessions"]
        Services["Authentication and domain services"]
        Repositories["Spring Data JPA repositories"]
    end
    subgraph DatabaseLayer["Database Layer"]
        Flyway["Flyway SQL migrations"]
        Pg[("PostgreSQL 15")]
    end

    Browser -->|"HTML requests"| Web
    ApiClient -->|"JSON requests and session cookie"| Api
    Web -->|"authentication and authorization"| Security
    Api -->|"authentication and authorization"| Security
    Security -->|"authorized requests"| Services
    Services -->|"persistence operations"| Repositories
    Repositories -->|"SQL through Hibernate"| Pg
    Flyway -->|"versioned schema changes"| Pg
~~~

### Technology Stack Summary

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Runtime | Java | 17 | Application runtime and source language |
| Application | Spring Boot | 3.4.7 | Application configuration and lifecycle |
| Web | Spring MVC and Thymeleaf | Managed by Spring Boot | HTML pages and web/API routing |
| Security | Spring Security | 6 | Authentication, sessions, and role-based authorization |
| Persistence | Spring Data JPA and Hibernate | Managed by Spring Boot | Repository-based data access and ORM |
| Database | PostgreSQL | 15 in Docker Compose | Persistent relational data |
| Schema management | Flyway | Managed by Spring Boot | Versioned database migrations |
| API docs | springdoc OpenAPI | 2.2.0 | OpenAPI and Swagger UI |
| Packaging | Maven and Docker | Maven Wrapper; Docker Compose | Build and local container orchestration |

### Data Storage & External Services

PostgreSQL stores user accounts, donations, recipient requests, and donation image bytes. Flyway applies SQL migrations at startup; the default application configuration asks Hibernate to validate the schema. No cache, message broker, object-storage provider, or third-party API integration was identified. Docker Compose supplies PostgreSQL for local development.

### Key Architectural Decisions

- Web pages and JSON endpoints share application services and persistence rather than duplicating business rules.
- Authentication uses server-side HTTP sessions and a configurable `SHAREHUB_SESSION` cookie, not JWTs.
- Donation and recipient-request operations are represented by separate entities and services, with role and ownership checks in the application.

## Component Relationships

<!-- mermaid-checked: no \n, no em-dash/en-dash, no {} in labels, subgraphs are id["label"], arrows are -->|"label"|, all subgraphs closed by end, ids unique -->
~~~mermaid
flowchart LR
    subgraph PresentationLayer["Presentation"]
        cWeb["Web controllers"]
        cAuthApi["AuthController"]
        cDonationApi["DonationApiController"]
        cTemplates["Thymeleaf templates"]
    end
    subgraph BusinessLayer["Business Logic"]
        cAuthService["AuthService"]
        cDonationService["DonationService"]
        cRequestService["ResourceRequestService"]
        cUserDetails["CustomUserDetailsService"]
    end
    subgraph DataAccessLayer["Data Access"]
        cUserRepo["UserRepository"]
        cDonationRepo["DonationRepository"]
        cImageRepo["DonationImageRepository"]
        cRequestRepo["ResourceRequestRepository"]
        cEntities["User, Donation, Image, Request"]
        cSchema["PostgreSQL schema"]
    end
    subgraph CrossCutting["Cross-cutting"]
        cSecurity["SecurityConfig"]
        cErrors["GlobalExceptionHandler"]
        cFlyway["Flyway migrations"]
    end

    cWeb -->|"renders and handles forms"| cTemplates
    cWeb -->|"uses"| cDonationService
    cWeb -->|"uses"| cRequestService
    cAuthApi -->|"delegates"| cAuthService
    cDonationApi -->|"delegates"| cDonationService
    cAuthService -->|"reads and writes users"| cUserRepo
    cAuthService -->|"loads principals"| cUserDetails
    cUserDetails -->|"loads accounts"| cUserRepo
    cDonationService -->|"reads and writes donations"| cDonationRepo
    cDonationService -->|"reads and writes images"| cImageRepo
    cDonationService -->|"loads donor"| cUserRepo
    cRequestService -->|"reads and writes requests"| cRequestRepo
    cRequestService -->|"checks donations and recipients"| cDonationRepo
    cRequestService -->|"loads recipient"| cUserRepo
    cUserRepo -->|"maps"| cEntities
    cDonationRepo -->|"maps"| cEntities
    cImageRepo -->|"maps"| cEntities
    cRequestRepo -->|"maps"| cEntities
    cEntities -->|"maps to"| cSchema
    cSecurity -.->|"protects routes"| cWeb
    cSecurity -.->|"protects routes"| cAuthApi
    cErrors -.->|"formats REST errors"| cAuthApi
    cErrors -.->|"formats REST errors"| cDonationApi
    cFlyway -.->|"creates and evolves"| cSchema
~~~

### Component Inventory

| Component | Layer | Type | Responsibility |
|---|---|---|---|
| `WebController` | Presentation | MVC controller | Serves login, registration, dashboard, and profile pages |
| `DonationWebController` | Presentation | MVC controller | Lists, creates, edits, and changes donation availability; serves images |
| `ResourceRequestWebController` | Presentation | MVC controller | Submits and cancels recipient requests |
| `AuthController` | Presentation | REST controller | Exposes registration, login, logout, and session-info endpoints |
| `DonationApiController` | Presentation | REST controller | Exposes authenticated donation listing and management endpoints |
| `HomeController` | Presentation | REST controller | Exposes the authenticated dashboard API |
| `Thymeleaf templates` | Presentation | Server-rendered views | Renders authentication, dashboard, profile, and donation pages |
| `AuthService` | Business Logic | Service | Registers users, authenticates credentials, and manages sessions |
| `DonationService` | Business Logic | Service | Applies donation ownership, availability, and image-handling rules |
| `ResourceRequestService` | Business Logic | Service | Applies recipient, donation availability, duplicate-request, and cancellation rules |
| `CustomUserDetailsService` | Business Logic | Security service | Loads user accounts for Spring Security authentication |
| `UserRepository` | Data Access | Spring Data repository | Loads and persists users |
| `DonationRepository` | Data Access | Spring Data repository | Loads and persists donations |
| `DonationImageRepository` | Data Access | Spring Data repository | Loads and persists donation image data |
| `ResourceRequestRepository` | Data Access | Spring Data repository | Loads and persists recipient requests |
| JPA entities | Data Access | Persistence model | Maps users, donations, donation images, and requests to relational tables |
| PostgreSQL schema | Data Access | Relational schema | Stores application records and donation images |
| `SecurityConfig` | Cross-cutting | Security configuration | Configures sessions, password encoding, request authorization, and logout |
| `GlobalExceptionHandler` | Cross-cutting | REST controller advice | Converts validation and application exceptions to REST error responses |
| Flyway migrations | Cross-cutting | Database migrations | Creates and evolves the PostgreSQL schema |
