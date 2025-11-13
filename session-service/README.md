# Session Service

## Overview
The Session Service manages telemedicine sessions in the telemedicine microservices architecture. It listens for appointment booking events and automatically creates corresponding telemedicine sessions, then publishes session started events.

## Features
- **Automatic Session Creation**: Listens for `appointment.booked` events and creates sessions
- **Session Management**: Start, track, and manage telemedicine sessions
- **Event Publishing**: Publishes `session.started` events when sessions begin
- **MongoDB Storage**: Uses MongoDB for flexible session data storage
- **REST API**: Complete CRUD operations for session management

## Technology Stack
- **Framework**: Spring Boot 3.1.4
- **Database**: MongoDB
- **Messaging**: Apache Kafka
- **Documentation**: OpenAPI/Swagger
- **Build Tool**: Gradle

## API Endpoints

### Session Management
- `POST /api/v1/sessions` - Create a new session
- `POST /api/v1/sessions/{sessionId}/start` - Start a session
- `GET /api/v1/sessions/{sessionId}` - Get session details
- `GET /api/v1/sessions/patient/{patientId}` - Get sessions by patient
- `GET /api/v1/sessions/doctor/{doctorId}` - Get sessions by doctor

## Event Flow

### Consumed Events
- **Topic**: `appointment.booked`
- **Event**: `AppointmentBookedEvent`
- **Action**: Automatically creates a session for the booked appointment

### Published Events
- **Topic**: `session.started`
- **Event**: `SessionStartedEvent`
- **Trigger**: When a session is started via REST API or automatically

## Session Lifecycle
1. **SCHEDULED** - Session created from appointment booking
2. **STARTED** - Session actively started (publishes event)
3. **IN_PROGRESS** - Session is ongoing
4. **COMPLETED** - Session finished successfully
5. **CANCELLED** - Session was cancelled
6. **NO_SHOW** - Patient/doctor didn't show up

## Configuration

### MongoDB
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/telemedicine_sessions
```

### Kafka
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=session-service-group
```

## Building and Running

### Prerequisites
- Java 17+
- MongoDB
- Kafka (shared infrastructure)

### Build
```bash
./gradlew clean build -x test
```

### Run Locally
```bash
# Start shared infrastructure first (from root directory)
docker-compose up -d

# Run the application
./gradlew bootRun
```

### Run with Docker
```bash
# Build and start the service
docker-compose up -d
```

## Sample Usage

### Create Session (REST)
```bash
curl -X POST http://localhost:8083/api/v1/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "patientId": 1,
    "doctorId": 1,
    "scheduledTime": "2025-10-10T10:00:00"
  }'
```

### Start Session
```bash
curl -X POST http://localhost:8083/api/v1/sessions/{sessionId}/start
```

### Get Session Details
```bash
curl http://localhost:8083/api/v1/sessions/{sessionId}
```

## Event Integration

The service automatically creates sessions when appointment booking events are received:

1. **Appointment Service** publishes `appointment.booked` event
2. **Session Service** consumes the event
3. **Session Service** creates a new session automatically
4. When session is started (via REST), publishes `session.started` event

## Access Points
- **Service**: http://localhost:8083
- **API Documentation**: http://localhost:8083/swagger-ui.html
- **Health Check**: http://localhost:8083/actuator/health

## MongoDB Collections
- **sessions**: Main collection storing session documents with embedded metadata
