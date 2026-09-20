# ShareHub

ShareHub is a Spring Boot-based platform focused on reducing waste by making it easier for people to share useful and reusable resources with others in their community.

The application aims to connect owners of underused items with people who need them, encouraging a more sustainable and community-driven way of consuming.

## Overview

ShareHub is designed to support:

- sharing reusable items and resources
- discovering available listings
- connecting borrowers or requesters with providers
- promoting sustainability and community cooperation

## Tech Stack

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Thymeleaf
- PostgreSQL
- Maven

## Project Structure

```text
sharehub/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/sharehub/sharehub/
│   │   │       └── SharehubApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       └── java/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
├── HELP.md
└── target/
```

## Prerequisites

Before running the app, ensure you have:

- JDK 17 or newer
- Maven installed
- PostgreSQL database running locally or remotely

## Configuration

The current configuration is minimal and located in:

- `src/main/resources/application.properties`

Current default setting:

```properties
spring.application.name=sharehub
```

For a working database setup, you can add PostgreSQL configuration like this:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sharehub
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Running the Application

From the project root, run:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

## Building the Project

```bash
./mvnw clean package
```

## Current Status

This project is in its early development stage. The base Spring Boot application is set up, and the project already includes dependencies for web, validation, security, JPA, and database support.

Key next steps include:

- creating user accounts and roles
- designing the item-sharing workflow
- implementing listings, requests, and ownership logic
- adding database entities and repositories
- building UI pages for browsing and posting items
- adding search, filters, and messaging features

## License

No explicit license has been set for this project yet.

## Contributing

Contributions are welcome as the project grows. If you want to help, you can:

- improve the platform design
- add backend features
- build frontend pages
- fix bugs and improve performance

