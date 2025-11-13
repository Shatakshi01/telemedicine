# Refactored Session Service - Architecture Overview

## Overview
The Session Service has been completely refactored to support a more robust telemedicine workflow with proper MongoDB integration, file handling, and improved session management.

## Key Changes Made

### 1. New Database Schemas

#### AppointmentMapping Collection
- **Purpose**: Store appointment-doctor relationships separately before session creation
- **Collection**: `appointment_mappings`
- **Fields**:
  - `appointmentId`: Unique appointment identifier
  - `patientId`: Patient involved in the appointment
  - `doctorId`: Doctor assigned to the appointment
  - `appointmentTime`: Scheduled appointment time
  - `appointmentType`: Type of appointment (consultation, follow-up, etc.)
  - `status`: Current status (PENDING, CONFIRMED, SESSION_READY, COMPLETED, CANCELLED)

#### SessionFile Collection
- **Purpose**: Store metadata for files uploaded during sessions
- **Collection**: `session_files`
- **Fields**:
  - `sessionId`: Reference to the session
  - `fileName`: Unique file name on disk
  - `originalFileName`: Original name of uploaded file
  - `fileType`: File extension (pdf, jpg, png, etc.)
  - `fileSize`: Size of file in bytes
  - `filePath`: Full path to file on disk
  - `contentType`: MIME type of file
  - `category`: File category (MEDICAL_RECORD, PRESCRIPTION, LAB_REPORT, IMAGE, DOCUMENT, OTHER)
  - `uploadedBy`: Who uploaded (PATIENT or DOCTOR)
  - `uploadedById`: ID of the uploader
  - `description`: Optional description of the file

#### Enhanced Session Collection
- **Purpose**: Main session document with file tracking
- **Collection**: `sessions`
- **New Fields Added**:
  - `fileCount`: Total number of files in session
  - `hasPatientFiles`: Boolean indicating if patient has uploaded files
  - `hasDoctorFiles`: Boolean indicating if doctor has uploaded files

## Refactored User Flow

### 1. Appointment Booking Flow
```
1. Appointment Service publishes 'appointment.booked' event
2. Session Service receives event
3. AppointmentEventListener saves appointment mapping to 'appointment_mappings' collection
4. Mapping status set to CONFIRMED
5. Session creation is now available but not automatic
```

### 2. Session Creation Flow
```
1. User/System requests session creation via REST API
2. SessionService validates appointment mapping exists and is CONFIRMED
3. Session created and saved to 'sessions' collection
4. Appointment mapping status updated to SESSION_READY
5. Session files tracking initialized (fileCount=0, hasPatientFiles=false, hasDoctorFiles=false)
```

### 3. File Upload Flow
```
1. Patient/Doctor uploads files via REST API during session
2. Files saved to disk in configured directory
3. File metadata saved to 'session_files' collection
4. Session document updated with file counts and flags
5. Doctor can view all uploaded files during consultation
```

## New API Endpoints

### Appointment Mappings
- `GET /api/v1/appointments` - Get all appointment mappings (monitoring)
- `GET /api/v1/appointments/{appointmentId}` - Get appointment mapping
- `GET /api/v1/appointments/doctor/{doctorId}` - Get appointments by doctor
- `GET /api/v1/appointments/patient/{patientId}` - Get appointments by patient
- `GET /api/v1/appointments/status/{status}` - Get appointments by status
- `GET /api/v1/appointments/count` - Get appointment counts by status
- `POST /api/v1/appointments/{appointmentId}/status` - Update appointment status
- `GET /api/v1/appointments/{appointmentId}/can-create-session` - Check if session can be created

### Session File Management
- `POST /api/v1/sessions/{sessionId}/files/upload` - Upload file to session
- `GET /api/v1/sessions/{sessionId}/files` - Get all files for session
- `GET /api/v1/sessions/{sessionId}/files/category/{category}` - Get files by category
- `GET /api/v1/sessions/{sessionId}/files/uploader/{uploadedBy}` - Get files by uploader
- `GET /api/v1/sessions/{sessionId}/files/download/{fileId}` - Download specific file
- `DELETE /api/v1/sessions/{sessionId}/files/{fileId}` - Delete specific file

### Enhanced Session Endpoints
All existing session endpoints now return additional file information:
- `fileCount`: Number of uploaded files
- `hasPatientFiles`: Whether patient has uploaded files
- `hasDoctorFiles`: Whether doctor has uploaded files

## Configuration

### File Upload Settings
```properties
# File Upload Configuration
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
app.upload.dir=${user.home}/session-uploads
```

## How to Monitor Appointment Bookings

### 1. Check All Booked Appointments
```bash
# Get all appointment mappings (shows all received appointment bookings)
curl http://localhost:8083/api/v1/appointments

# Get appointment counts by status
curl http://localhost:8083/api/v1/appointments/count

# Get appointments by specific status
curl http://localhost:8083/api/v1/appointments/status/CONFIRMED
curl http://localhost:8083/api/v1/appointments/status/PENDING
curl http://localhost:8083/api/v1/appointments/status/SESSION_READY
```

### 2. Check Specific Appointment
```bash
# Check if a specific appointment was booked
curl http://localhost:8083/api/v1/appointments/123

# Check if session can be created for appointment
curl http://localhost:8083/api/v1/appointments/123/can-create-session
```

### 3. View Appointments by User
```bash
# Get all appointments for a specific doctor
curl http://localhost:8083/api/v1/appointments/doctor/1

# Get all appointments for a specific patient  
curl http://localhost:8083/api/v1/appointments/patient/5
```

## Sample Usage Scenarios

### 1. Create Session After Appointment Booking
```bash
# Check if session can be created
curl http://localhost:8083/api/v1/appointments/123/can-create-session

# Create session (only works if appointment mapping exists and is confirmed)
curl -X POST http://localhost:8083/api/v1/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 123,
    "patientId": 1,
    "doctorId": 1,
    "scheduledTime": "2025-10-15T10:00:00"
  }'
```

### 2. Upload Files to Session
```bash
# Patient uploads medical records
curl -X POST http://localhost:8083/api/v1/sessions/{sessionId}/files/upload \
  -F "file=@medical-report.pdf" \
  -F "category=MEDICAL_RECORD" \
  -F "uploadedBy=PATIENT" \
  -F "uploadedById=1" \
  -F "description=Recent blood test results"

# Doctor uploads prescription
curl -X POST http://localhost:8083/api/v1/sessions/{sessionId}/files/upload \
  -F "file=@prescription.pdf" \
  -F "category=PRESCRIPTION" \
  -F "uploadedBy=DOCTOR" \
  -F "uploadedById=1" \
  -F "description=Medication prescription"
```

### 3. View Session Files
```bash
# Get all files in session
curl http://localhost:8083/api/v1/sessions/{sessionId}/files

# Get only patient uploaded files
curl http://localhost:8083/api/v1/sessions/{sessionId}/files/uploader/PATIENT

# Get only medical records
curl http://localhost:8083/api/v1/sessions/{sessionId}/files/category/MEDICAL_RECORD
```

## Benefits of Refactored Architecture

### 1. Separation of Concerns
- **Appointment Mappings**: Dedicated schema for appointment-doctor relationships
- **Sessions**: Focus on session lifecycle and management  
- **Files**: Separate handling of file uploads and metadata

### 2. Better Data Integrity
- Appointment mappings must exist before session creation
- Status validation ensures proper workflow progression
- File metadata tracked separately from session data

### 3. Enhanced File Management
- Support for multiple file types (images, PDFs, documents)
- Categorization of files (medical records, prescriptions, etc.)
- Tracking of who uploaded what files
- Secure file storage with metadata separation

### 4. Improved User Experience
- Patients can upload medical records before/during session
- Doctors can review uploaded files during consultation
- File categorization helps organize medical information
- Download capabilities for record keeping

### 5. MongoDB Best Practices
- Proper document structure with embedded metadata
- Efficient querying with indexed fields
- Separate collections for different data types
- Audit trails with created/updated timestamps

## Security Considerations

1. **File Validation**: Only specific file types allowed
2. **Size Limits**: 50MB maximum file size configured
3. **Access Control**: Files linked to specific sessions and users
4. **Storage Security**: Files stored outside web root directory
5. **Audit Trail**: Complete tracking of file uploads and access

## Scalability Features

1. **File Storage**: Can be easily migrated to cloud storage (S3, etc.)
2. **Database Sharding**: Collections can be sharded by appointmentId/sessionId
3. **Caching**: File metadata can be cached for faster access
4. **Load Balancing**: Stateless design supports horizontal scaling

## Troubleshooting

### Date/Time Format Issues
If you encounter date parsing errors in Kafka messages, check the `AppointmentBookedEvent` class:
- `appointmentDate`: Uses format `"yyyy-MM-dd HH:mm"` (e.g., "2025-10-15 10:30")  
- `bookedAt`: Uses format `"yyyy-MM-dd HH:mm:ss"` (e.g., "2025-10-14 19:35:35")
- `appointmentTime`: Uses ISO format `"yyyy-MM-dd'T'HH:mm:ss"`

### Kafka Deserialization Issues
The system uses manual JSON parsing with StringDeserializer to avoid type header issues. If messages fail to deserialize:
1. Check the JSON format in Kafka logs
2. Verify field names match between producer and `AppointmentBookedEvent` class
3. Ensure date formats are consistent

### Monitoring Appointment Flow
```bash
# 1. Check if appointments are being received
curl http://localhost:8083/api/v1/appointments/count

# 2. View recent appointments  
curl http://localhost:8083/api/v1/appointments

# 3. Check specific appointment status
curl http://localhost:8083/api/v1/appointments/{appointmentId}

# 4. Verify session creation eligibility
curl http://localhost:8083/api/v1/appointments/{appointmentId}/can-create-session
```

## MongoDB Docker Container Access

Since you're using MongoDB in a Docker container, here are the specific commands to access your data:

### 1. Connect to MongoDB Docker Container

```bash
# Find your MongoDB container name/ID
docker ps | grep mongo

# Connect to MongoDB container (replace container_name with your actual container name)
docker exec -it <mongo-container-name> mongo

# Example if your container is named 'mongodb' or 'telemedicine-mongo'
docker exec -it mongodb mongo
docker exec -it telemedicine-mongo mongo

# Or connect directly to the database
docker exec -it <mongo-container-name> mongo telemedicine_sessions
```

### 2. Alternative: Use MongoDB from Host Machine

```bash
# If MongoDB port is exposed (usually 27017), connect from host
mongo mongodb://localhost:27017/telemedicine_sessions

# Or using mongosh (newer MongoDB shell)
mongosh mongodb://localhost:27017/telemedicine_sessions
```

### 3. Docker MongoDB Commands

```bash
# Check MongoDB container status
docker ps | grep mongo

# View MongoDB container logs
docker logs <mongo-container-name>

# Start MongoDB container (if stopped)
docker start <mongo-container-name>

# Stop MongoDB container
docker stop <mongo-container-name>

# Check container resource usage
docker stats <mongo-container-name>
```

### 4. Access MongoDB Data in Container

Once connected to the container:

```bash
# Switch to your database
use telemedicine_sessions

# List all collections
show collections

# View appointment mappings (booked appointments)
db.appointment_mappings.find().pretty()

# Count appointments by status
db.appointment_mappings.aggregate([
  {
    $group: {
      _id: "$status",
      count: { $sum: 1 }
    }
  }
])

# View all sessions
db.sessions.find().pretty()

# View session files
db.session_files.find().pretty()

# Find recent appointments (last 24 hours)
db.appointment_mappings.find({
  "createdAt": {
    $gte: new ISODate(new Date().getTime() - 24*60*60*1000)
  }
}).pretty()
```

### 5. Docker Volume Data Persistence

```bash
# Check where MongoDB data is stored (find volume mount)
docker inspect <mongo-container-name> | grep -A 10 "Mounts"

# If using named volume, list Docker volumes
docker volume ls

# Inspect MongoDB volume
docker volume inspect <volume-name>
```

### 6. Docker Compose MongoDB Access

If you're using Docker Compose:

```bash
# Connect via docker-compose
docker-compose exec mongodb mongo telemedicine_sessions

# View logs
docker-compose logs mongodb

# Restart MongoDB service
docker-compose restart mongodb
```

### 7. MongoDB Compass with Docker

Connect MongoDB Compass to your Docker container:
- **Connection String**: `mongodb://localhost:27017` (if port is exposed)
- **Host**: `localhost`
- **Port**: `27017`
- **Database**: `telemedicine_sessions`

### 8. Quick Docker MongoDB Health Check

```bash
# One-liner to check MongoDB in Docker
docker exec -it <mongo-container-name> mongo --eval "
  use telemedicine_sessions;
  print('=== DATABASE STATUS ===');
  print('Collections: ' + db.getCollectionNames());
  print('Appointments: ' + db.appointment_mappings.count());
  print('Sessions: ' + db.sessions.count());
  print('Files: ' + db.session_files.count());
"

# Check container health
docker exec -it <mongo-container-name> mongo --eval "db.adminCommand('ping')"
```

### 9. Container Network Access

If your app and MongoDB are in the same Docker network:

```bash
# List Docker networks
docker network ls

# Inspect network to see connected containers
docker network inspect <network-name>

# In this case, your app connects to MongoDB using the container name
# Connection string in app: mongodb://mongodb:27017/telemedicine_sessions
```
