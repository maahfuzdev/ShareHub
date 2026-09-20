# ShareHub

ShareHub is a Spring Boot application designed to encourage reuse and reduce waste by helping people share useful and reusable resources with others in their community.

This project is currently in its initial setup stage, with the core Spring Boot structure in place and dependencies for web, security, validation, JPA, and PostgreSQL configured.

## Project Overview

The idea behind ShareHub is to create a platform where users can:

- share reusable items or resources
- browse available listings
- connect people who need something with people who can offer it
- promote sustainable and community-driven consumption

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
└── HELP.md
```

## Prerequisites

Before running the application, make sure you have:

- JDK 17 or newer
- Maven
- PostgreSQL installed and running

## Configuration

The application configuration is currently minimal and can be found in:

- src/main/resources/application.properties

At the moment it only contains the app name:

```properties
spring.application.name=sharehub
```

For a real database setup, you will likely add PostgreSQL datasource settings such as:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sharehub
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Run the Application

From the project root, run:

```bash
./mvnw spring-boot:run
```

On Windows, you can also use:

```bash
mvnw.cmd spring-boot:run
```

## Build the Project

```bash
./mvnw clean package
```

## Current Status

This project is in an early development stage. The base application skeleton is ready, but feature modules, controllers, entities, and database integration still need to be implemented according to the final product requirements.

## Next Steps

Possible next steps for the project include:

- creating user authentication and roles
- designing item listing and request flows
- adding database entities for users, products, and transactions
- building frontend pages with Thymeleaf or a React/Vue frontend
- implementing search, filters, and messaging features

## License

This project does not yet specify a license in the project configuration.

