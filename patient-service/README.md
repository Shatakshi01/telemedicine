# Patient Service - Telemedicine System

A Spring Boot microservice for managing patient registration and information in a telemedicine system.

## Features

- **Patient Registration**: Register new patients with validation
- **Patient Information Retrieval**: Get patient details by ID
- **Kafka Integration**: Publishes `patient.registered` events
- **PostgreSQL Database**: Persistent storage for patient data
- **Swagger API Documentation**: Interactive API documentation
- **Docker Compose**: Easy local development setup

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL**
- **Apache Kafka**
- **Swagger/OpenAPI**
- **Docker & Docker Compose**
- **Gradle 8.14.1**

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── patient/
│   │           ├── PatientServiceApplication.java
│   │           ├── config/
│   │           │   ├── KafkaConfig.java
│   │           │   └── SwaggerConfig.java
│   │           ├── controller/
│   │           │   └── PatientController.java
│   │           ├── dto/
│   │           │   ├── PatientRegistrationDto.java
│   │           │   └── PatientResponseDto.java
│   │           ├── entity/
│   │           │   └── Patient.java
│   │           ├── event/
│   │           │   └── PatientRegisteredEvent.java
│   │           ├── exception/
│   │           │   ├── ErrorResponse.java
│   │           │   ├── GlobalExceptionHandler.java
│   │           │   ├── PatientAlreadyExistsException.java
│   │           │   └── PatientNotFoundException.java
│   │           ├── kafka/
│   │           │   └── PatientEventProducer.java
│   │           ├── mapper/
│   │           │   └── PatientMapper.java
│   │           ├── repository/
│   │           │   └── PatientRepository.java
│   │           └── service/
│   │               └── PatientService.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── patient/
                └── PatientServiceApplicationTests.java
```

## API Endpoints

### 1. Register Patient
- **POST** `/patients`
- **Description**: Register a new patient
- **Request Body**: `PatientRegistrationDto`
- **Response**: `PatientResponseDto` (201 Created)

### 2. Get Patient Details
- **GET** `/patients/{id}`
- **Description**: Get patient details by ID
- **Path Parameter**: `id` (Long)
- **Response**: `PatientResponseDto` (200 OK)

## Getting Started

### Prerequisites

- Java 17
- Docker & Docker Compose
- Gradle 8.14.1

### 1. Start Infrastructure Services

```bash
docker-compose up -d
```

This will start:
- PostgreSQL (port 5432)
- Kafka (port 9092)
- Zookeeper (port 2181)
- Kafka UI (port 8080)

### 2. Build the Application

```bash
./gradlew build
```

### 3. Run the Application

```bash
./gradlew bootRun
```

The application will start on port 8081.

### 4. Access API Documentation

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API Docs**: http://localhost:8081/api-docs

### 5. Monitor Kafka

- **Kafka UI**: http://localhost:8080

## Cross-Service Communication & Kafka Configuration

This patient service is designed to work with other microservices (like appointment-service). To prevent deserialization errors when consuming events across services, the Kafka configuration has been optimized:

### Error Resolution
If you encounter the error:
```
ClassNotFoundException: com.patient.event.PatientRegisteredEvent
```

This has been resolved by:

1. **Using ErrorHandlingDeserializer**: Gracefully handles deserialization errors
2. **Flexible JSON Configuration**: Allows cross-package event consumption
3. **Disabled Type Headers**: Prevents package-specific issues

### For Other Services (e.g., appointment-service)

Use this Kafka consumer configuration:

```properties
spring.kafka.consumer.key-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
spring.kafka.consumer.properties.spring.deserializer.key.delegate.class=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.properties.spring.deserializer.value.delegate.class=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.consumer.properties.spring.json.use.type.headers=false
spring.kafka.consumer.group-id=appointment-service-group
```

### Updated Docker Compose

The docker-compose.yml now supports multiple services:
- **Patient PostgreSQL**: localhost:5432
- **Appointment PostgreSQL**: localhost:5433  
- **Shared Kafka**: localhost:9092
- **Kafka UI**: localhost:8080
- **Redis**: localhost:6379

## Configuration

### Database Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/patient_db
spring.datasource.username=patient_user
spring.datasource.password=patient_password
```

### Kafka Configuration
```properties
spring.kafka.bootstrap-servers=localhost:9092
```

## Sample API Requests

### Register a Patient

```bash
curl -X POST http://localhost:8081/patients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE",
    "address": "123 Main St, City, State",
    "medicalHistory": "No known allergies"
  }'
```

### Get Patient Details

```bash
curl -X GET http://localhost:8081/patients/1
```

## Kafka Events

### Patient Registered Event

When a patient is successfully registered, a `patient.registered` event is published to Kafka:

```json
{
  "patientId": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "address": "123 Main St, City, State",
  "registeredAt": "2023-12-16T10:30:00"
}
```

## Testing

```bash
./gradlew test
```

## Health Checks

The application includes Spring Boot Actuator endpoints for monitoring:

- **Health**: http://localhost:8081/actuator/health
- **Info**: http://localhost:8081/actuator/info

## Error Handling

The application includes comprehensive error handling with appropriate HTTP status codes:

- **400 Bad Request**: Validation errors
- **404 Not Found**: Patient not found
- **409 Conflict**: Patient already exists
- **500 Internal Server Error**: Unexpected errors

## Development

### Code Style
- Follow standard Java naming conventions
- Use Lombok annotations to reduce boilerplate code
- Implement proper validation using Bean Validation annotations
- Use appropriate logging levels

### Adding New Features
1. Create/update entity classes
2. Update DTOs and mappers
3. Extend repository interfaces
4. Implement service layer logic
5. Create/update controller endpoints
6. Add appropriate tests
7. Update API documentation

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.
