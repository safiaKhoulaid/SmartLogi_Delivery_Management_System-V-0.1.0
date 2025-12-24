# SmartLogi Delivery Management System (SDMS)

## 📋 Overview

SmartLogi Delivery Management System (SDMS) is a comprehensive logistics management solution designed to streamline delivery operations, optimize route planning, and enhance customer satisfaction. Built with Spring Boot and following clean architecture principles(DDD), SDMS provides a robust platform for managing the entire delivery lifecycle.

## 🚀 Features

### Core Functionalities
- **User Management**
    - Multi-role support (Expediteur, Destinataire, Livreur, Gestionnaire)
    - Secure authentication and authorization
    - Profile management

- **Package Management**
    - Package tracking and status updates
    - Priority-based handling (HAUTE, NORMALE, BASSE)
    - Weight and size tracking
    - Historical delivery data

- **Delivery Management**
    - Real-time delivery tracking
    - Zone-based delivery assignments
    - Route optimization
    - Delivery status updates (CREE, COLLECTE, EN_STOCK, EN_TRANSIT, LIVRE)

- **Zone Management**
    - Geographic zone definition
    - Driver assignment to zones
    - Postal code-based organization

### Technical Features
- Clean Architecture implementation(DDD)
- RESTful API design (Spring boot)
- Database migrations with Liquibase
- Comprehensive logging system(ElasticSearch-logBack-Kibana)
- Monitoring with Prometheus (visualisation avec grafana)
- Containerization with Docker
- Authentification et Authosisation avec Spring Security

## 🛠 Technology Stack

### Backend & Core Technologies
- **Framework**: Spring Boot 3.x
- **Language**: Java 21 (LTS)
- **Build Tool**: Maven 3.8+
- **Architecture**: Clean Architecture (DDD - Domain Driven Design)
- **ORM**: JPA/Hibernate
- **Database**: PostgreSQL 15+
- **Migration**: Liquibase
- **Version Control**: Git

### Security & Authentication
- **Framework**: Spring Security
- **Authentication**: JWT (JSON Web Tokens)
- **Authorization**: Role-Based Access Control (RBAC)
- **OAuth 2.0**: OAuth2 Client & Resource Server
- **Token Management**: Refresh Token mechanism
- **Password Encoding**: BCrypt

### API & Documentation
- **API Style**: RESTful API
- **Documentation**: Swagger/OpenAPI 3.0
- **API Gateway**: Spring Cloud Gateway (optional)
- **Validation**: Hibernate Validator

### Monitoring & Observability
- **Metrics Collection**: Prometheus
- **Visualization**: Grafana
- **Log Management**: ELK Stack
  - **Elasticsearch**: Log storage and indexing
  - **Logstash**: Log aggregation and processing
  - **Kibana**: Log visualization and analysis
- **Application Logging**: Logback with Spring Boot
- **Distributed Tracing**: Spring Cloud Sleuth (optional)

### Containerization & Orchestration
- **Container Runtime**: Docker
- **Container Orchestration**: Docker Compose
- **Container Registry**: Docker Hub / AWS ECR
- **Base Images**: OpenJDK 21 Alpine

### Cloud & Deployment
- **Cloud Provider**: AWS (Amazon Web Services)
- **Compute**: AWS EC2 / AWS ECS / AWS EKS
- **Container Registry**: AWS ECR (Elastic Container Registry)
- **Load Balancer**: AWS ALB (Application Load Balancer)
- **Database**: AWS RDS PostgreSQL
- **Object Storage**: AWS S3
- **Messaging**: AWS SQS / AWS SNS
- **Secrets Management**: AWS Secrets Manager
- **CDN**: AWS CloudFront (optional)

### Testing & Quality
- **Unit Testing**: JUnit 5
- **Integration Testing**: Spring Boot Test
- **Mocking**: Mockito
- **Code Coverage**: JaCoCo
- **Code Quality**: SonarQube / SonarCloud
- **Performance Testing**: JMeter (optional)

### Development Tools
- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **Code Quality**: SonarLint
- **API Testing**: Postman / Insomnia
- **Database Tools**: DBeaver / pgAdmin

### Messaging & Notifications
- **Email Service**: Spring Mail / AWS SES
- **SMS Gateway**: Twilio / AWS SNS
- **Push Notifications**: Firebase Cloud Messaging (FCM)
- **Message Queue**: RabbitMQ / AWS SQS (optional)

### Environments
- **Development (dev)**: Local development with Docker Compose
- **Production (prod)**: AWS Cloud deployment with auto-scaling
- **Docker**: Containerized environment for consistent deployment

## 🌍 Deployment Environments

### Development Environment (dev)
- **Purpose**: Local development and testing
- **Database**: PostgreSQL in Docker container
- **Configuration**: `application-dev.yml`
- **Features**:
  - Hot reload enabled
  - Debug mode active
  - H2 Console accessible
  - Detailed logging
  - Mock external services

### Docker Environment
- **Purpose**: Containerized local testing and CI/CD
- **Configuration**: `application-docker.yml` + `docker-compose.yml`
- **Services**:
  - Application container (Spring Boot)
  - PostgreSQL database
  - Elasticsearch
  - Logstash
  - Kibana
  - Prometheus
  - Grafana
- **Networking**: Docker bridge network
- **Volumes**: Persistent data storage

### Production Environment (prod)
- **Purpose**: Live production deployment
- **Cloud Provider**: AWS
- **Configuration**: `application-prod.yml`
- **Infrastructure**:
  - **Compute**: AWS ECS/EKS with auto-scaling
  - **Database**: AWS RDS PostgreSQL (Multi-AZ)
  - **Load Balancer**: AWS Application Load Balancer
  - **CDN**: AWS CloudFront
  - **Storage**: AWS S3
  - **Monitoring**: CloudWatch + Prometheus + Grafana
  - **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Security**:
  - VPC with private subnets
  - Security Groups
  - SSL/TLS certificates (AWS ACM)
  - Secrets Manager for sensitive data
  - WAF for application protection

## 📊 Monitoring & Logging Architecture

### Prometheus + Grafana
- **Prometheus**: Collects metrics from application endpoints (`/actuator/prometheus`)
- **Grafana**: Visualizes metrics with custom dashboards
- **Metrics Tracked**:
  - Application performance (response times, throughput)
  - JVM metrics (heap, threads, GC)
  - Database connection pool
  - Custom business metrics

### ELK Stack (Elasticsearch, Logstash, Kibana)
- **Logback**: Application logging framework
- **Logstash**: Aggregates and processes logs
- **Elasticsearch**: Stores and indexes logs
- **Kibana**: Visualizes and analyzes logs
- **Log Levels**: INFO, WARN, ERROR, DEBUG
- **Log Format**: JSON structured logging

### AWS CloudWatch (Production)
- Application logs
- Infrastructure metrics
- Alarms and notifications
- Log retention policies

## 📦 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/smartlogi/sdms/
│   │       ├── application/      # Application services and DTOs
│   │       ├── domain/          # Business logic and entities
│   │       ├── infrastructure/  # External services implementation
│   │       └── presentation/    # Controllers and API endpoints
│   └── resources/
│       ├── application.yml     # Application configuration
│       └── db/changelog/      # Database migrations
```

## 🚀 Getting Started

### Prerequisites
- JDK 21
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/safiaKhoulaid/SmartLogi_Delivery_Management_System-V-0.1.0.git
cd SmartLogi_Delivery_Management_System-V-0.1.0
```

2. Configure database connection in `application.yml`

3. Build the project:
```bash
mvn clean install
```

4. Run with Docker:
```bash
docker-compose up
```

### Running Tests

#### Unit Tests
Run all unit tests:
```bash
mvn test
```

#### Integration Tests
Run integration tests:
```bash
mvn verify
```

#### Code Coverage with JaCoCo
Generate code coverage report:
```bash
mvn clean test jacoco:report
```

View coverage report:
- Open `target/site/jacoco/index.html` in your browser

JaCoCo Maven Plugin configuration (add to `pom.xml` if not present):
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Code Quality with SonarQube

Run SonarQube analysis locally:
```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=smartlogi-sdms \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=YOUR_SONAR_TOKEN
```

Run SonarCloud analysis (CI/CD):
```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=smartlogi-sdms \
  -Dsonar.organization=YOUR_ORG \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.login=$SONAR_TOKEN
```

SonarQube coverage configuration (`sonar-project.properties`):
```properties
sonar.projectKey=smartlogi-sdms
sonar.projectName=SmartLogi Delivery Management System
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.junit.reportPaths=target/surefire-reports
```

#### CI/CD Pipeline Testing
```bash
# Complete CI/CD test suite
mvn clean verify jacoco:report sonar:sonar \
  -Dsonar.login=$SONAR_TOKEN \
  -DskipTests=false
```

## 📊 Domain Model
 📐 UML Diagrams

### Class Diagram

<!-- Embedded image: filename has spaces and parentheses, URL-encode them for reliable rendering -->
![SDMS Class Diagram](https://raw.githubusercontent.com/safiaKhoulaid/SmartLogi_Delivery_Management_System-V-0.1.0/main/docs/diagrams/images/sdms.drawio%20(1).jpg)

If the image does not render in your preview, open the file directly at `docs/diagrams/images/sdms.drawio (1).png`.

It shows:
- All entities and their relationships
- Attributes and methods
- Inheritance hierarchies
- Value objects
- Enums

##
### Core Entities
1. **Users**
    - BaseUser (Abstract)
        - ClientExpediteur
        - Destinataire
        - Livreur
        - Gestionnaire

2. **Logistics**
    - Colis (Package)
    - Zone
    - Mission
    - HistoriqueLivraison

### Value Objects
- Adresse (Address)
- Telephone
- Poids (Weight)

### Enums
- StatusColis
- PriorityColis
- StatusLivreur
- TypeVehicule
- UnitePoids

## 🔐 Security

- JWT-based authentication
- Role-based access control
- Secure password handling
- API request validation
- Input sanitization

## 🌟 Key Features

1. **Smart Package Tracking**
    - Real-time status updates
    - Location tracking
    - Delivery history
    - Priority-based handling

2. **Zone Management**
    - Geographic optimization
    - Driver assignment
    - Workload distribution

3. **User Management**
    - Multiple user roles
    - Profile management
    - Access control

4. **Reporting & Analytics**
    - Delivery performance metrics
    - Driver efficiency tracking
    - Zone-based analytics

## 📱 API Documentation

API documentation is available at `/swagger-ui.html` when running the application.

### Endpoints
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Actuator Health: `http://localhost:8080/actuator/health`
- Prometheus Metrics: `http://localhost:8080/actuator/prometheus`

## ☁️ AWS Deployment

### Prerequisites
- AWS CLI configured
- Docker installed
- AWS account with appropriate permissions
- ECR repository created

### Deployment Steps

#### 1. Build Docker Image
```bash
# Build the application
mvn clean package -DskipTests

# Build Docker image
docker build -t smartlogi-sdms:latest .
```

#### 2. Push to AWS ECR
```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# Tag the image
docker tag smartlogi-sdms:latest YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/smartlogi-sdms:latest

# Push to ECR
docker push YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/smartlogi-sdms:latest
```

#### 3. Deploy to ECS/EKS
```bash
# Update ECS service
aws ecs update-service --cluster smartlogi-cluster --service smartlogi-service --force-new-deployment

# Or deploy to EKS
kubectl apply -f k8s/deployment.yaml
kubectl rollout restart deployment/smartlogi-sdms
```

### Environment Variables (AWS)
```bash
# Database
DB_HOST=your-rds-endpoint.rds.amazonaws.com
DB_PORT=5432
DB_NAME=smartlogi
DB_USERNAME=stored-in-secrets-manager
DB_PASSWORD=stored-in-secrets-manager

# AWS Services
AWS_REGION=us-east-1
AWS_S3_BUCKET=smartlogi-uploads
AWS_SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/ACCOUNT/smartlogi-queue

# Security
JWT_SECRET=stored-in-secrets-manager
OAUTH2_CLIENT_ID=stored-in-secrets-manager
OAUTH2_CLIENT_SECRET=stored-in-secrets-manager

# Monitoring
PROMETHEUS_ENDPOINT=http://prometheus:9090
ELASTICSEARCH_HOST=elasticsearch.us-east-1.es.amazonaws.com
```

### AWS Services Configuration

#### RDS PostgreSQL
- Multi-AZ deployment for high availability
- Automated backups with 7-day retention
- Encryption at rest enabled
- SSL/TLS connections required

#### ECS/EKS
- Auto-scaling based on CPU/Memory
- Health checks configured
- Rolling updates for zero-downtime deployment
- Service discovery enabled

#### Application Load Balancer
- HTTPS termination
- Health check endpoint: `/actuator/health`
- Sticky sessions enabled
- Cross-zone load balancing

#### S3 Bucket
- Versioning enabled
- Lifecycle policies configured
- Server-side encryption (SSE-S3)
- CORS configuration for web access

### Monitoring in AWS
- **CloudWatch Logs**: Application and infrastructure logs
- **CloudWatch Metrics**: Custom application metrics
- **CloudWatch Alarms**: Alert on critical metrics
- **X-Ray**: Distributed tracing (optional)
- **Prometheus**: Custom metrics collection
- **Grafana**: Metrics visualization

## 🔐 OAuth2 & JWT Configuration

### OAuth2 Setup
The application supports OAuth2 authentication with the following providers:
- Google OAuth2
- GitHub OAuth2
- Custom OAuth2 providers

#### OAuth2 Configuration (`application.yml`)
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: profile, email
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: read:user, user:email
```

### JWT Token Management

#### Access Token
- **Expiration**: 15 minutes
- **Usage**: API authentication
- **Storage**: HTTP-only cookie or Authorization header

#### Refresh Token
- **Expiration**: 7 days
- **Usage**: Obtain new access tokens
- **Storage**: HTTP-only secure cookie
- **Rotation**: Automatic rotation on use

#### Token Endpoints
```bash
# Login
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password"
}

# Refresh Token
POST /api/auth/refresh
{
  "refreshToken": "your-refresh-token"
}

# Logout
POST /api/auth/logout
```

#### Security Best Practices
- Tokens stored in HTTP-only cookies
- CSRF protection enabled
- Refresh token rotation
- Token blacklist for logout
- Rate limiting on auth endpoints

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- Safia Khoulaid - Initial work and maintenance

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- The open-source community for various tools and libraries

- Contributors and testers of the project





