# Read4U - Online Library

Spring Boot application for online library management.

## Tech Stack

- Java 17
- Spring Boot 3.2
- MySQL
- Lombok

## Prerequisites

- JDK 17+
- Maven 3.8+
- MySQL (optional)

## Setup

```bash
./mvnw spring-boot:run
```

## Docker

```bash
docker build -t read4u .
docker run -p 8080:8080 read4u
```

## Configuration

Create `application-local.properties` for local settings:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/read4u
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Run with local profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Health check |
