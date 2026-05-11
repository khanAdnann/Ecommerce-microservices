# 🚀 Deployment Guide - E-Commerce Microservices

## 📋 Prerequisites

- Docker & Docker Compose installed
- Java 11+ (for local development)
- Node.js 18+ (for frontend development)
- Git

## 🏗️ System Architecture Overview

This system consists of:
- **3 Infrastructure Services**: Eureka Server, Config Server, API Gateway
- **9 Business Microservices**: User, Product, Order, Payment, Cart, Inventory, Notification, Review, Analytics
- **1 Frontend**: React SPA with Redux Toolkit
- **Supporting Services**: Kafka, Zookeeper, Redis, MySQL (9 instances)

## 🐳 Quick Start with Docker Compose

### 1. Clone and Build
```bash
git clone <repository-url>
cd ecommerce-microservices

# Build all services
mvn clean package -DskipTests

# Build frontend
cd frontend
npm install
npm run build
cd ..
```

### 2. Start the Complete System
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### 3. Access Points
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888
- **Kafka**: localhost:9092

## 🗄️ Database Setup

### MySQL Databases
The system uses 9 separate MySQL databases (one per microservice):

| Service | Database | Port |
|---------|----------|------|
| User Service | user_service | 3306 |
| Product Service | product_service | 3307 |
| Order Service | order_service | 3308 |
| Payment Service | payment_service | 3309 |
| Cart Service | cart_service | 3310 |
| Inventory Service | inventory_service | 3311 |
| Notification Service | notification_service | 3312 |
| Review Service | review_service | 3313 |
| Analytics Service | analytics_service | 3314 |

### Database Initialization
Database schemas are automatically created through initialization scripts in `./scripts/`.

## 🔐 Security Configuration

### JWT Configuration
- **Secret Key**: Configurable via environment variable
- **Access Token Expiration**: 1 hour
- **Refresh Token Expiration**: 7 days

### Default Users
- **Admin**: admin@ecommerce.com / password
- **User**: user@ecommerce.com / password

## 📡 Kafka Event Flow

### Topics
- `order-events`: Order lifecycle events
- `payment-events`: Payment processing events
- `inventory-events`: Stock management events
- `notification-events`: Email and push notifications
- `user-events`: User activity events
- `product-events`: Product catalog events
- `cart-events`: Shopping cart events
- `analytics-events`: Analytics and metrics events
- `review-events`: Product review events

### Event Flow Example
```
Order Service → order-created → Inventory Service
Inventory Service → inventory-updated → Notification Service
Payment Service → payment-completed → Order Service
Notification Service → notification-event → Email Service
```

## 🔧 Configuration Management

### Environment Variables
Key environment variables for production:

```bash
# Database Configuration
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_USER=your_db_user
MYSQL_PASSWORD=your_db_password

# JWT Configuration
JWT_SECRET=your_very_long_secure_secret_key

# Email Configuration
EMAIL_USERNAME=your_email@gmail.com
EMAIL_PASSWORD=your_app_password

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
```

### Config Server
- **Git Repository**: https://github.com/ecommerce/config-repo
- **Branch**: main
- **Search Paths**: `{application}`

## 🚀 Production Deployment

### 1. Infrastructure Setup
```bash
# Production Docker Compose
docker-compose -f docker-compose.prod.yml up -d

# Scale services
docker-compose -f docker-compose.prod.yml up -d --scale user-service=3 --scale product-service=2
```

### 2. Load Balancer Configuration
Configure your load balancer to distribute traffic across API Gateway instances.

### 3. SSL/TLS Setup
- Configure SSL certificates for API Gateway
- Update frontend to use HTTPS
- Configure Kafka SSL if needed

### 4. Monitoring Setup
```bash
# Enable monitoring endpoints
management.endpoints.web.exposure.include=*

# Prometheus metrics
management.metrics.export.prometheus.enabled=true
```

## 🔍 Monitoring & Observability

### Health Checks
All services expose health endpoints:
- `/actuator/health` - Service health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

### Logging
- Structured logging with JSON format
- Centralized log aggregation (ELK stack optional)
- Log levels configurable per environment

### Metrics
- Response times
- Error rates
- Throughput metrics
- Database connection pool usage
- Kafka message throughput

## 🛠️ Development Setup

### Local Development
```bash
# Start infrastructure only
docker-compose up -d zookeeper kafka mysql-user mysql-product redis eureka-server config-server

# Run services locally
cd microservices/user-service
mvn spring-boot:run

cd microservices/product-service
mvn spring-boot:run

# Frontend development
cd frontend
npm start
```

### IDE Configuration
- Import as Maven project
- Configure Lombok annotation processing
- Set up code formatting standards

## 🧪 Testing

### Unit Tests
```bash
# Run all tests
mvn test

# Run tests for specific service
cd microservices/user-service
mvn test
```

### Integration Tests
```bash
# Run integration tests
mvn verify -P integration-test

# Run with test containers
mvn test -P test-containers
```

### API Testing
Use Swagger UI available at:
- `http://localhost:8081/swagger-ui.html` (User Service)
- `http://localhost:8082/swagger-ui.html` (Product Service)
- etc.

## 🔄 CI/CD Pipeline

### GitHub Actions Example
```yaml
name: Build and Deploy
on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Build with Maven
        run: mvn clean package -DskipTests
      - name: Build Docker images
        run: docker-compose build
      - name: Deploy to production
        run: docker-compose -f docker-compose.prod.yml up -d
```

## 🔧 Troubleshooting

### Common Issues

#### Service Discovery Issues
```bash
# Check Eureka dashboard
curl http://localhost:8761/eureka/apps

# Restart service registration
docker-compose restart user-service
```

#### Database Connection Issues
```bash
# Check database connectivity
docker-compose exec mysql-user mysql -u root -p

# Verify database schemas
mysql -h localhost -P 3306 -u root -p user_service
```

#### Kafka Issues
```bash
# Check Kafka topics
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Check consumer groups
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

#### Memory Issues
```bash
# Check container resource usage
docker stats

# Increase JVM heap size
JAVA_OPTS=-Xmx1g -Xms512m
```

### Performance Tuning

#### Database Optimization
- Configure connection pools
- Enable query caching
- Add database indexes
- Monitor slow queries

#### Kafka Optimization
- Adjust batch sizes
- Configure compression
- Optimize partition counts
- Monitor consumer lag

#### JVM Tuning
```bash
# Recommended JVM settings for production
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

## 📊 Scaling Guidelines

### Horizontal Scaling
- Stateless services can be scaled horizontally
- Use sticky sessions for WebSocket connections
- Configure database read replicas

### Vertical Scaling
- Increase memory for memory-intensive services
- CPU allocation for compute-intensive operations
- Storage scaling for analytics services

## 🔄 Backup & Recovery

### Database Backups
```bash
# Backup all databases
docker-compose exec mysql-user mysqldump -u root -p user_service > backup_user.sql

# Restore database
docker-compose exec -i mysql-user mysql -u root -p user_service < backup_user.sql
```

### Kafka Data Backup
```bash
# Backup Kafka topics
docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic order-events --from-beginning > order-events-backup.log
```

## 🛡️ Security Best Practices

### Production Security
- Use environment variables for secrets
- Enable HTTPS everywhere
- Implement rate limiting
- Use network segmentation
- Regular security updates

### Network Security
- Configure firewall rules
- Use private networks for inter-service communication
- Enable SSL/TLS for all external communications
- Implement API authentication

## 📚 Additional Resources

### Documentation
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [React Documentation](https://reactjs.org/docs/)

### Monitoring Tools
- Prometheus + Grafana
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Jaeger for distributed tracing
- New Relic / DataDog for APM

### Support
- Check logs for error details
- Use health endpoints for service status
- Monitor Kafka consumer lag
- Database performance metrics
