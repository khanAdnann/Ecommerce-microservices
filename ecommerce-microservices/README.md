# 🛍️ E-Commerce Microservices Architecture

A production-ready distributed system similar to Netflix/Amazon architecture using Spring Boot, Kafka, and React.

## 🏗️ Architecture Overview

```
├── infrastructure/          # Infrastructure Services
│   ├── eureka-server/       # Service Discovery (8761)
│   ├── config-server/       # Centralized Config (8888)
│   └── api-gateway/         # API Gateway (8080)
├── microservices/           # Business Microservices
│   ├── user-service/        # Authentication & Users (8081)
│   ├── product-service/     # Product Catalog (8082)
│   ├── order-service/       # Order Management (8083)
│   ├── payment-service/     # Payment Processing (8084)
│   ├── cart-service/        # Shopping Cart (8085)
│   ├── inventory-service/   # Stock Management (8086)
│   ├── notification-service/# Notifications (8087)
│   ├── review-service/      # Ratings & Reviews (8088)
│   └── analytics-service/   # Analytics & Reports (8089)
├── frontend/                # React SPA
├── docker-compose.yml       # Complete System Setup
└── docs/                    # Documentation
```

## 🚀 Quick Start

```bash
# Start entire system
docker-compose up -d

# Access services
# API Gateway: http://localhost:8080
# Eureka Dashboard: http://localhost:8761
# Config Server: http://localhost:8888
# Frontend: http://localhost:3000
```

## 📊 Services & Ports

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Entry point, routing, auth |
| Eureka Server | 8761 | Service discovery |
| Config Server | 8888 | Centralized configuration |
| User Service | 8081 | Authentication, user management |
| Product Service | 8082 | Product catalog, search |
| Order Service | 8083 | Order lifecycle management |
| Payment Service | 8084 | Payment processing |
| Cart Service | 8085 | Shopping cart operations |
| Inventory Service | 8086 | Stock management |
| Notification Service | 8087 | Email, WebSocket notifications |
| Review Service | 8088 | Product ratings and reviews |
| Analytics Service | 8089 | Metrics and reporting |

## 🔐 Security Features

- JWT Authentication with Access/Refresh tokens
- OAuth2 integration (Google login)
- Role-based access control (ADMIN, USER)
- Gateway-level token validation
- API rate limiting

## ⚡ Event-Driven Architecture

Apache Kafka for asynchronous communication:
- Order events (created, updated, cancelled)
- Payment events (completed, failed, refunded)
- Inventory events (stock updated, low stock)
- Notification events (email, push, websocket)

## 🛠️ Tech Stack

**Backend:**
- Java 11+, Spring Boot 3.x
- Spring Security, Spring Data JPA
- MySQL (database per service)
- Apache Kafka, Zookeeper
- Redis (caching)
- Docker & Docker Compose

**Frontend:**
- React 18+, Redux Toolkit
- Axios, Formik, Yup
- Material-UI / TailwindCSS
- WebSocket integration

## 📖 API Documentation

Each service exposes REST APIs following OpenAPI 3.0 specification. Access Swagger UI at:
`http://localhost:{service-port}/swagger-ui.html`

## 🔧 Development

```bash
# Build all services
./mvnw clean install

# Run specific service
cd microservices/user-service
./mvnw spring-boot:run

# Run tests
./mvnw test
```

## 📈 Monitoring & Observability

- Distributed logging with ELK stack
- Metrics collection with Prometheus
- Health checks and circuit breakers
- Distributed tracing (optional)
