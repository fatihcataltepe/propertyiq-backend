# PropertyIQ Backend

Java Spring Boot microservices backend for PropertyIQ real estate investment analytics platform.

## Architecture

This is a multi-module Gradle project with the following microservices:

- **api-gateway**: API Gateway for routing and authentication
- **auth-service**: Authentication, user management, JWT token handling
- **portfolio-service**: Property management, mortgages, investments
- **expense-service**: Property expenses tracking
- **analytics-service**: ROI calculations and financial analytics
- **reporting-service**: Tax summaries and annual reports generation
- **notification-service**: Email notifications and alerts
- **common**: Shared utilities and common code

## Tech Stack

- Java 21
- Spring Boot 3.2.1
- Spring Data JPA
- PostgreSQL
- Gradle 8.x

## Prerequisites

- Java 21 or higher
- PostgreSQL 15+
- Docker (optional, for containerized databases)

## Build

```bash
./gradlew clean build
```

## Run Services

Each service can be run independently:

```bash
./gradlew :api-gateway-service:bootRun
./gradlew :auth-service:bootRun
./gradlew :portfolio-service:bootRun
./gradlew :expense-service:bootRun
./gradlew :analytics-service:bootRun
./gradlew :reporting-service:bootRun
./gradlew :notification-service:bootRun
```

## Development

The project structure follows a microservices architecture with each service being independently deployable.

Next steps:
- Implement service internals
- Configure service discovery
- Add observability stack (Prometheus, Grafana, Jaeger)
- Add Docker and Kubernetes deployment configs
