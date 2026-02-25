# Push Notification API Service

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## 📋 Overview

A Spring Boot 3.4.1 implementation of the **Berlin Group openFinance API Framework v2.3 - Resource Status Notification Service**. This service acts as an API Client (receiver) that accepts push notifications from ASPSPs (Account Servicing Payment Service Providers) about resource status changes in the openFinance ecosystem.

The service implements the receiver side of the push notification architecture, where ASPSPs send real-time status updates about:
- Payment initiations (including Request to Pay)
- Consent establishment
- Subscription management
- Basket signing
- Mandate establishment
- Document submissions

### Key Features

- ✅ **Berlin Group oFA v2.3 Compliant** - Fully implements the Resource Status Notification specification
- ✅ **REST API Receiver** - Accepts push notifications via POST endpoint
- ✅ **Idempotency Support** - Tracks X-Request-ID headers to prevent duplicate processing
- ✅ **Event-Driven Architecture** - Publishes Spring Application Events for downstream processing
- ✅ **Push Client** - ASPSP-side client for sending notifications (using modern RestClient)
- ✅ **Java 21 Virtual Threads** - Leverages Project Loom for efficient concurrent processing
- ✅ **Security Ready** - OAuth2 Resource Server support (configurable)
- ✅ **OpenAPI/Swagger UI** - Interactive API documentation at `/psd2/swagger-ui.html`
- ✅ **H2 In-Memory Database** - Persistent notification storage with JPA
- ✅ **Comprehensive Testing** - Unit, integration, and repository tests

---

## 🏗️ Architecture

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.4.1 |
| **Build Tool** | Maven | 3.6+ |
| **Database** | H2 (in-memory) | Runtime |
| **Persistence** | Spring Data JPA | 3.4.1 |
| **Security** | Spring Security + OAuth2 Resource Server | 3.4.1 |
| **Validation** | Jakarta Bean Validation | 3.0+ |
| **API Docs** | SpringDoc OpenAPI | 2.7.0 |
| **HTTP Client** | RestClient (Spring 6.2+) | Built-in |
| **Testing** | JUnit 5 + Spring Boot Test | 3.4.1 |

### Project Structure

```
push-notification-api-service/
├── src/
│   ├── main/
│   │   ├── java/org/berlingroup/openfinance/push/
│   │   │   ├── PushNotificationApplication.java          # Spring Boot entry point
│   │   │   ├── controller/
│   │   │   │   └── ResourceStatusNotificationController.java  # REST endpoint
│   │   │   ├── service/
│   │   │   │   └── ResourceStatusNotificationService.java     # Business logic
│   │   │   ├── repository/
│   │   │   │   └── ResourceNotificationRepository.java        # JPA repository
│   │   │   ├── client/
│   │   │   │   └── PushNotificationClient.java               # ASPSP-side push client
│   │   │   ├── dto/
│   │   │   │   ├── PushResourceStatusRequest.java            # Request DTO (record)
│   │   │   │   ├── Amount.java
│   │   │   │   └── HrefType.java
│   │   │   ├── model/
│   │   │   │   ├── entity/ResourceNotification.java          # JPA entity
│   │   │   │   └── enums/                                    # Status enums
│   │   │   │       ├── TransactionStatus.java
│   │   │   │       ├── ConsentStatus.java
│   │   │   │       ├── SubscriptionStatus.java
│   │   │   │       ├── SubscriptionEntryStatus.java
│   │   │   │       ├── MandateStatus.java
│   │   │   │       ├── DocumentStatus.java
│   │   │   │       ├── SCAStatus.java
│   │   │   │       ├── RequestStatus.java
│   │   │   │       └── StatusReasonCode.java
│   │   │   ├── event/
│   │   │   │   └── ResourceNotificationEvent.java            # Spring event
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java               # REST error handler
│   │   │       ├── DuplicateRequestException.java
│   │   │       ├── InvalidNotificationException.java
│   │   │       └── PushNotificationClientException.java
│   │   └── resources/
│   │       └── application.yml                               # Configuration
│   └── test/
│       └── java/org/berlingroup/openfinance/push/
│           ├── PushNotificationIntegrationTest.java          # Integration tests
│           ├── controller/ResourceStatusNotificationControllerTest.java
│           ├── service/ResourceStatusNotificationServiceTest.java
│           ├── repository/ResourceNotificationRepositoryTest.java
│           ├── client/PushNotificationClientTest.java
│           └── dto/DtoSerializationTest.java
├── BG_oFA_PUSH_Version_2.3_20260204.openapi.yaml            # OpenAPI specification
├── pom.xml                                                   # Maven dependencies
└── README.md

```

### Design Patterns

- **Controller-Service-Repository Pattern** - Clean separation of concerns
- **Record DTOs** - Immutable Java 17+ records for request/response objects
- **Event Publishing** - Decoupled processing via Spring ApplicationEvent
- **Repository Pattern** - Spring Data JPA abstracts database operations
- **Global Exception Handling** - Centralized error responses via @RestControllerAdvice
- **Builder Pattern** - RestClient.Builder for HTTP client configuration

---

## 🚀 API Endpoints

### Base URL
```
http://localhost:8080/psd2
```

### POST /psd2/Client-Notification-URL

**Receives push resource status notifications from ASPSPs.**

#### Request Headers
| Header | Type | Required | Description |
|--------|------|----------|-------------|
| `X-Request-ID` | UUID | ✅ Yes | Unique request identifier for idempotency |
| `Digest` | String | ❌ No | Hash of the message body |
| `x-jws-signature` | String | ❌ No | JSON Web Signature for body verification |
| `Body-Sig-Profile` | String | ❌ No | Signature profile used for signing |
| `Body-Enc-Profile` | String | ❌ No | Encryption profile used |
| `Body-Enc-List` | String | ❌ No | List of encrypted body elements |
| `Content-Type` | String | ✅ Yes | Must be `application/json` |

#### Request Body (PushResourceStatusRequest)

At least one resource identifier must be present:

```json
{
  "paymentId": "3dc3d5b3-7023-4848-9853-f5400a64e80f",
  "consentId": null,
  "subscriptionId": null,
  "basketId": null,
  "mandateResourceId": null,
  "documentResourceId": null,
  "entryId": null,
  "subscriptionEntryId": null,
  "authorisationId": "463318a0-2c5d-4f42-af67-24a2e6b26f9c",
  "cancellationId": null,
  "transactionStatus": "ACCP",
  "consentStatus": null,
  "subscriptionStatus": null,
  "subscriptionEntryStatus": null,
  "mandateStatus": null,
  "documentStatus": null,
  "scaStatus": "finalised",
  "requestStatus": null,
  "reasonCode": null,
  "reasonProprietary": null,
  "debtorDecisionDateTime": null,
  "acceptedAmount": null,
  "acceptanceDateTime": null,
  "acceptedPaymentInstrument": "SCT",
  "statusIdentification": "REF-12345",
  "_links": {
    "scaStatus": {
      "href": "/v1/payments/sepa-credit-transfers/3dc3d5b3-7023-4848-9853-f5400a64e80f/authorisations/463318a0-2c5d-4f42-af67-24a2e6b26f9c"
    }
  }
}
```

#### Response
**200 OK**
```
Headers:
  X-Request-ID: 99391c7e-ad88-49ec-a2ad-99ddcb1f7721
Body: (empty)
```

#### Error Responses
| Status Code | Description | Example Cause |
|-------------|-------------|---------------|
| `400 Bad Request` | Invalid request body | No resource identifier present |
| `409 Conflict` | Duplicate X-Request-ID | Request already processed |
| `415 Unsupported Media Type` | Wrong Content-Type | Not `application/json` |
| `500 Internal Server Error` | Server error | Database failure |

---

## 📊 Data Models

### Core Entity: ResourceNotification

**Table:** `resource_notification`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Database ID |
| `request_id` | UUID | UNIQUE, NOT NULL, INDEXED | X-Request-ID for idempotency |
| `payment_id` | VARCHAR(70) | | Payment/RTP resource identifier |
| `consent_id` | VARCHAR(70) | | Consent resource identifier |
| `subscription_id` | VARCHAR(70) | | Subscription resource identifier |
| `basket_id` | VARCHAR(70) | | Basket resource identifier |
| `mandate_resource_id` | UUID | | Mandate resource identifier |
| `document_resource_id` | UUID | | Document resource identifier |
| `entry_id` | VARCHAR | | RTP bulk entry identifier |
| `subscription_entry_id` | VARCHAR(70) | | Subscription entry identifier |
| `authorisation_id` | VARCHAR(70) | | SCA authorisation ID |
| `cancellation_id` | VARCHAR(70) | | Cancellation authorisation ID |
| `transaction_status` | VARCHAR(4) | ENUM | e.g., ACCP, ACSC, ACSP, etc. |
| `consent_status` | VARCHAR(20) | ENUM | e.g., received, valid, rejected, etc. |
| `subscription_status` | VARCHAR(20) | ENUM | Subscription status |
| `subscription_entry_status` | VARCHAR(20) | ENUM | Subscription entry status |
| `mandate_status` | VARCHAR(20) | ENUM | Mandate status |
| `document_status` | VARCHAR(20) | ENUM | Document status |
| `sca_status` | VARCHAR(20) | ENUM | e.g., received, psuAuthenticated, finalised, etc. |
| `request_status` | VARCHAR(20) | ENUM | Request to Pay status |
| `reason_code` | VARCHAR(50) | ENUM | Status reason code |
| `reason_proprietary` | VARCHAR | | Proprietary reason text |
| `debtor_decision_date_time` | VARCHAR | | ISO 8601 datetime |
| `accepted_amount_currency` | VARCHAR(3) | | ISO 4217 currency code |
| `accepted_amount_value` | VARCHAR | | Decimal amount |
| `acceptance_date_time` | VARCHAR | | ISO 8601 datetime |
| `accepted_payment_instrument` | VARCHAR | | e.g., "SCT", "SCT inst" |
| `status_identification` | VARCHAR | | Reference added by debtor |
| `received_at` | TIMESTAMP | NOT NULL, AUTO | Timestamp when received |

### Transaction Statuses (TransactionStatus enum)
```
ACCP - AcceptedCustomerProfile
ACSC - AcceptedSettlementCompleted
ACSP - AcceptedSettlementInProcess
ACTC - AcceptedTechnicalValidation
ACWC - AcceptedWithChange
ACWP - AcceptedWithoutPosting
PART - PartiallyAccepted
PATC - PartiallyAcceptedTechnicalCorrect
RCVD - Received
PDNG - Pending
RJCT - Rejected
CANC - Cancelled
```

### Consent Statuses (ConsentStatus enum)
```
received, valid, rejected, expired, revoked, terminatedByTpp, terminatedByAspsp
```

### SCA Statuses (SCAStatus enum)
```
received, psuIdentified, psuAuthenticated, scaMethodSelected, started, finalised, failed, exempted, unconfirmed
```

---

## ⚙️ Configuration

### application.yml

```yaml
server:
  port: 8080
  servlet:
    context-path: /psd2

spring:
  application:
    name: push-notification-api-service
  threads:
    virtual:
      enabled: true        # Java 21 Virtual Threads enabled
  datasource:
    url: jdbc:h2:mem:pushnotificationdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update     # Creates/updates schema automatically
    show-sql: false
  h2:
    console:
      enabled: true
      path: /h2-console    # Access at http://localhost:8080/psd2/h2-console

# OAuth2 Resource Server (uncomment for production)
#  security:
#    oauth2:
#      resourceserver:
#        jwt:
#          issuer-uri: https://your-auth-server.com/realms/openfinance

# Push notification client configuration
push-notification:
  client:
    connect-timeout-ms: 5000
    read-timeout-ms: 10000
    max-retries: 3
    retry-delay-ms: 1000

# Springdoc OpenAPI
springdoc:
  api-docs:
    path: /api-docs               # http://localhost:8080/psd2/api-docs
  swagger-ui:
    path: /swagger-ui.html        # http://localhost:8080/psd2/swagger-ui.html
    operationsSorter: method

logging:
  level:
    org.berlingroup.openfinance: DEBUG
    org.springframework.security: INFO
```

### Environment Variables

For production deployments, override these via environment variables:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://db.example.com:5432/pushnotifications
SPRING_DATASOURCE_USERNAME=dbuser
SPRING_DATASOURCE_PASSWORD=secretpassword

# OAuth2
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://auth.example.com

# Server
SERVER_PORT=8443
```

---

## 🔧 Build & Run

### Prerequisites

- **Java 21** (OpenJDK or Oracle JDK)
- **Maven 3.6+**

### Build the Project

```bash
# Clone the repository
git clone <repository-url>
cd push-notification-api-service

# Clean and build
mvn clean install

# Build without tests (faster)
mvn clean package -DskipTests
```

The compiled JAR will be in `target/push-notification-api-service-2.3.0.jar`

### Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Using the JAR
java -jar target/push-notification-api-service-2.3.0.jar

# With custom profile
java -jar target/push-notification-api-service-2.3.0.jar --spring.profiles.active=prod
```

The service will start on **http://localhost:8080/psd2**

### Access Points

| URL | Description |
|-----|-------------|
| http://localhost:8080/psd2/Client-Notification-URL | Push notification endpoint |
| http://localhost:8080/psd2/swagger-ui.html | Swagger UI for API testing |
| http://localhost:8080/psd2/api-docs | OpenAPI JSON specification |
| http://localhost:8080/psd2/h2-console | H2 database console (dev only) |

---

## 🧪 Testing

### Test Structure

The project includes comprehensive test coverage:

| Test Class | Type | Coverage |
|------------|------|----------|
| `PushNotificationIntegrationTest` | Integration | End-to-end REST API tests |
| `ResourceStatusNotificationControllerTest` | Unit | Controller layer validation |
| `ResourceStatusNotificationServiceTest` | Unit | Service logic (happy path, validation, mapping) |
| `ResourceNotificationRepositoryTest` | Integration | JPA repository queries |
| `PushNotificationClientTest` | Unit | HTTP client behavior |
| `DtoSerializationTest` | Unit | JSON serialization/deserialization |

### Run All Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn clean test jacoco:report

# Run specific test class
mvn test -Dtest=ResourceStatusNotificationServiceTest

# Run specific test method
mvn test -Dtest=ResourceStatusNotificationServiceTest#shouldProcessValidNotification
```

### Test Reports

Test results are generated in:
- **Console output** - Real-time test execution
- **target/surefire-reports/** - XML and text reports
- **target/site/jacoco/** - Code coverage reports (if jacoco plugin is configured)

### Example Test: Integration Test

```java
@Test
void shouldAcceptValidNotification() throws Exception {
    UUID requestId = UUID.randomUUID();
    
    mockMvc.perform(post("/Client-Notification-URL")
            .header("X-Request-ID", requestId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "paymentId": "payment-123",
                  "transactionStatus": "ACCP"
                }
                """))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Request-ID"));

    // Verify notification was persisted
    assertTrue(repository.existsByRequestId(requestId));
}
```

---

## 📦 Dependencies

### Key Maven Dependencies

```xml
<!-- Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- OpenAPI/Swagger -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 🔒 Security

### OAuth2 Resource Server (Optional)

The service supports OAuth2 JWT validation. To enable:

1. **Remove the exclusion** in `PushNotificationApplication.java`:
   ```java
   @SpringBootApplication  // Remove the 'exclude' parameter
   public class PushNotificationApplication { ... }
   ```

2. **Configure JWT issuer** in `application.yml`:
   ```yaml
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: https://your-keycloak.com/realms/openfinance
   ```

3. **Restart the service** - Now all requests require a valid Bearer token

### Security Features

- **Request Validation** - Jakarta Bean Validation on all DTOs
- **Idempotency** - X-Request-ID tracking prevents duplicate processing
- **HTTPS Support** - Configure via `server.ssl.*` properties
- **Header Validation** - Optional Digest, JWS signature verification (implement as needed)
- **Global Exception Handling** - No sensitive data leakage in error responses

---

## 🔄 Business Logic Flow

### Notification Processing Flow

```
1. ASPSP → POST /Client-Notification-URL
   ├─ Headers: X-Request-ID (UUID)
   └─ Body: PushResourceStatusRequest (JSON)

2. ResourceStatusNotificationController
   ├─ Validates request headers
   ├─ Validates request body (@Valid)
   └─ Calls service layer

3. ResourceStatusNotificationService
   ├─ Validates at least one resource ID present
   ├─ Checks idempotency (X-Request-ID)
   │  └─ If duplicate → throw DuplicateRequestException → 409 Conflict
   ├─ Maps DTO to Entity
   ├─ Persists to database
   └─ Publishes ResourceNotificationEvent

4. ResourceNotificationEvent
   └─ (Downstream consumers can listen via @EventListener)

5. Response: 200 OK with X-Request-ID header
```

### Event-Driven Processing

The service publishes a `ResourceNotificationEvent` after successful persistence:

```java
@Component
public class NotificationEventListener {
    
    @EventListener
    public void handleNotificationEvent(ResourceNotificationEvent event) {
        UUID requestId = event.getRequestId();
        PushResourceStatusRequest request = event.getRequest();
        
        // Your business logic here:
        // - Update internal payment status
        // - Trigger webhooks to external systems
        // - Send email notifications
        // - Update dashboards
        log.info("Processing notification event: {}", requestId);
    }
}
```

---

## 🛠️ Development Guide

### Package Structure

```
org.berlingroup.openfinance.push/
├── controller/          REST endpoints
├── service/             Business logic
├── repository/          Data access layer
├── client/              External HTTP clients (ASPSP-side)
├── dto/                 Data Transfer Objects (records)
├── model/
│   ├── entity/          JPA entities
│   └── enums/           Status enumerations
├── event/               Spring Application Events
└── exception/           Custom exceptions & handlers
```

### Extending the Service

#### Add a New Resource Type

1. **Update `PushResourceStatusRequest` record** - Add new field
2. **Update `ResourceNotification` entity** - Add corresponding column
3. **Update `ResourceStatusNotificationService.mapToEntity()`** - Map new field
4. **Update `hasAtLeastOneResourceIdentifier()`** - Include new ID in validation
5. **Write tests** - Cover new field scenarios

#### Add Custom Validation

Implement a custom validator:

```java
@Component
public class NotificationValidator {
    
    public void validateBusinessRules(PushResourceStatusRequest request) {
        if (request.transactionStatus() == TransactionStatus.ACCP 
            && request.scaStatus() != SCAStatus.finalised) {
            throw new InvalidNotificationException(
                "ACCP status requires finalised SCA");
        }
    }
}
```

---

## 📝 OpenAPI Specification

The service implements the official Berlin Group specification:
- **File:** `BG_oFA_PUSH_Version_2.3_20260204.openapi.yaml`
- **Version:** 2.3
- **Date:** February 4, 2026

View the full specification at:
- https://www.berlin-group.org/openfinance-downloads

---

## 🚀 Deployment

### Docker Deployment (Recommended)

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/push-notification-api-service-2.3.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
docker build -t push-notification-api-service:2.3.0 .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/pushnotifications \
  push-notification-api-service:2.3.0
```

### Production Checklist

- [ ] Switch to PostgreSQL/MySQL database
- [ ] Enable OAuth2 JWT validation
- [ ] Configure SSL/TLS certificates
- [ ] Set up logging aggregation (ELK, Splunk)
- [ ] Configure monitoring (Prometheus, Grafana)
- [ ] Set up health checks (`/actuator/health`)
- [ ] Review and tune JVM settings for production
- [ ] Implement rate limiting
- [ ] Configure backup strategy for database

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## 📞 Support

For issues, questions, or contributions:
- **Berlin Group:** https://www.berlin-group.org
- **Email:** info@berlin-group.org
- **Documentation:** https://www.berlin-group.org/openfinance-downloads

---

## 📚 Additional Resources

- [Berlin Group openFinance API Framework](https://www.berlin-group.org/openfinance)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/index.html)
- [Java 21 Virtual Threads](https://openjdk.org/jeps/444)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [PSD2 Directive](https://ec.europa.eu/info/law/payment-services-psd-2-directive-eu-2015-2366_en)

---

**Version:** 2.3.0  
**Last Updated:** February 2026  
**Specification:** Berlin Group oFA v2.3