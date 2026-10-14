# Finga API

Spring Boot REST API for managing shops and products with PostgreSQL, JPA, Liquibase, validation, Spring Security and OpenAPI.

## Requirements
- Java 21
- Maven 3.6.3+
- PostgreSQL

## Database
Create the database:

```sql
CREATE DATABASE shopdb;
```

Default connection values can be overridden with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

## Run

```bash
mvn clean spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui.html
OpenAPI: http://localhost:8080/v3/api-docs

Because Spring Security is included, the API currently uses HTTP Basic authentication with Spring Boot's generated development user. This is intentionally a first security layer; JWT authentication should be added before production deployment.

## Endpoints
- GET /api/shops
- GET /api/shops/{id}
- POST /api/shops
- PUT /api/shops/{id}
- DELETE /api/shops/{id}
- GET /api/shops/{shopId}/products
- GET /api/shops/{shopId}/products/{id}
- POST /api/shops/{shopId}/products
- PUT /api/shops/{shopId}/products/{id}
- DELETE /api/shops/{shopId}/products/{id}
