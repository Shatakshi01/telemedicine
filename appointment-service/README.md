# Appointment Service

A Spring Boot microservice for managing patient appointments in a telemedicine system.

## Features

- **RESTful API** for appointment management
- **Kafka integration** for event-driven communication
- **PostgreSQL** database with dedicated schema
- **Swagger/OpenAPI** documentation
- **Docker support** with docker-compose

## Tech Stack

- **Java 17**
- **Spring Boot 3.1.4**
- **Gradle** for build management
- **PostgreSQL** for data persistence
- **Apache Kafka** for messaging
- **Docker & Docker Compose** for containerization
- **Swagger/OpenAPI** for API documentation

## API Endpoints

### Appointment Management
- `POST /appointments` - Book new appointment
- `GET /appointments/{id}` - Get appointment details
- `GET /appointments/patient/{patientId}` - Get appointments by patient
- `PUT /appointments/{id}/status` - Update appointment status

### Documentation
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **API Docs**: http://localhost:8082/api-docs

## Kafka Integration

### Consumer
- **Topic**: `patient.registered`
- **Purpose**: Listen for patient registration events to enable appointment booking eligibility

### Producer
- **Topic**: `appointment.booked`
- **Purpose**: Publish events when appointments are successfully booked

## Database Schema

The service uses PostgreSQL with a dedicated `appointment` schema:

```sql
-- Appointments table
CREATE TABLE appointment.appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    reason VARCHAR(500),
    notes VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- **Patient Service** running (provides shared Kafka and PostgreSQL)

### Setup Instructions

1. **Start Patient Service First** (provides shared infrastructure):
   ```bash
   # In patient-service directory
   docker-compose up -d
   ```

2. **Build Appointment Service**:
   ```bash
   ./gradlew build
   ```

3. **Start Appointment Service**:
   ```bash
   docker-compose up -d
   ```

4. **Access the services**:
   - **Appointment API**: http://localhost:8082
   - **Swagger UI**: http://localhost:8082/swagger-ui.html
   - **Kafka UI**: http://localhost:8080 (from patient service)
   - **Health Check**: http://localhost:8082/actuator/health

### Running Locally (Development)

1. **Start patient service infrastructure**:
   ```bash
   # In patient-service directory  
   docker-compose up -d
   ```

2. **Run appointment service locally**:
   ```bash
   ./gradlew bootRun
   ```

### Build the project

```bash
./gradlew build
```

## Configuration

Key configuration properties in `application.yml`:

```yaml
server:
  port: 8084

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/patient_db  # Shared with patient service
    username: patient_user
    password: patient_password
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        default_schema: appointment  # Separate schema in shared database

  kafka:
    bootstrap-servers: localhost:9092  # Shared Kafka from patient service
    consumer:
      group-id: appointment-service
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

## Usage Examples

### Book an Appointment

```bash
curl -X POST http://localhost:8082/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "doctorId": 2,
    "appointmentDate": "2024-12-25 10:30",
    "reason": "Regular checkup",
    "notes": "Annual physical examination"
  }'
```

### Get Appointment Details

```bash
curl http://localhost:8082/appointments/1
```

### Get Patient Appointments

```bash
curl http://localhost:8082/appointments/patient/1
```

## Testing

Run tests:
```bash
./gradlew test
```

## Monitoring

The service includes Spring Boot Actuator endpoints:
- `/actuator/health` - Health check
- `/actuator/info` - Application info
- `/actuator/metrics` - Metrics
- `/actuator/prometheus` - Prometheus metrics

## Architecture

The service follows a layered architecture:

```
├── controller/     # REST API endpoints
├── service/        # Business logic
├── repository/     # Data access layer
├── model/          # Entity classes
├── dto/            # Data transfer objects
├── event/          # Kafka event classes
├── config/         # Configuration classes
├── exception/      # Exception handling
└── listener/       # Kafka event listeners
```

## Development

### Project Structure
```
src/
├── main/
│   ├── java/com/appointment/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── dto/
│   │   ├── event/
│   │   ├── config/
│   │   ├── exception/
│   │   └── listener/
│   └── resources/
│       └── application.yml
└── test/
```

### Adding New Features

1. Create DTOs in `dto/` package
2. Add business logic in `service/` package
3. Create REST endpoints in `controller/` package
4. Add tests in `src/test/`

## Docker

### Build Docker Image
```bash
./gradlew build
docker build -t appointment-service .
```

### Run with Docker
```bash
docker run -p 8082:8082 appointment-service
```
