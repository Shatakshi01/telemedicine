# Database Initialization Fix

## Problem
Previously, when running `cleanup.sh` and then `deploy.sh`, you had to run `fix-patient-id.sh` every time to fix the auto-increment issue for the `patient_id` column. This was because:

1. The patient-service was using `spring.jpa.hibernate.ddl-auto=create-drop`, which meant Hibernate would create and drop tables on each restart
2. Hibernate's auto-generation doesn't always create proper BIGSERIAL sequences for PostgreSQL
3. The database had no initialization script to create tables with proper schemas

## Solution
The fix involves three main changes:

### 1. PostgreSQL Initialization Script
- **Created**: `k8s/postgres/init.sql` (also embedded in ConfigMap)
- **Purpose**: Automatically creates the `patients` and `appointments` tables with proper BIGSERIAL auto-increment when PostgreSQL first starts
- **Location**: Mounted as `/docker-entrypoint-initdb.d/init.sql` in the PostgreSQL container

### 2. Updated PostgreSQL Deployment
- **File**: `k8s/postgres/deployment.yaml`
- **Changes**: 
  - Added volume mount for the init script
  - PostgreSQL will automatically execute scripts in `/docker-entrypoint-initdb.d/` on first startup

### 3. Changed Hibernate DDL Mode
- **Files Updated**:
  - `patient-service/src/main/resources/application.properties`
  - `k8s/patient-service/configmap.yaml`
  - `k8s/appointment-service/configmap.yaml`
- **Changes**: Changed from `create-drop` to `validate`
  - `validate`: Hibernate only validates that the database schema matches the entity definitions
  - This prevents Hibernate from recreating tables and potentially losing the auto-increment configuration

## How It Works Now

1. When you run `cleanup.sh`:
   - All resources are deleted, including the PostgreSQL PVC (Persistent Volume Claim)
   - This ensures a fresh start

2. When you run `deploy.sh`:
   - PostgreSQL pod starts with a fresh database
   - The init script (`/docker-entrypoint-initdb.d/init.sql`) automatically runs
   - Tables are created with proper BIGSERIAL columns for auto-increment
   - Services start and validate the schema (not recreate it)

## Testing the Fix

After running `deploy.sh`, test the patient registration:

```bash
# Port forward the patient service
kubectl port-forward -n telemedicine svc/patient-service 8080:8080

# In another terminal, create a patient
curl -X POST http://localhost:8080/patients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "1234567890",
    "dateOfBirth": "1990-01-01",
    "gender": "MALE"
  }'
```

You should now see a response with an auto-generated `patient_id` without needing to run `fix-patient-id.sh`.

## Important Notes

- **Data Loss**: As requested, you're okay with losing data between deployments. The `cleanup.sh` script deletes the PVC, ensuring a fresh database each time.
- **No More Manual Fix**: You no longer need to run `fix-patient-id.sh` after deployment.
- **Schema Management**: The database schema is now managed by SQL scripts, not Hibernate auto-generation.

## Files Modified

1. `k8s/postgres/configmap.yaml` - Added init script ConfigMap
2. `k8s/postgres/deployment.yaml` - Added init script volume mount
3. `k8s/postgres/init.sql` - New initialization script
4. `patient-service/src/main/resources/application.properties` - Changed ddl-auto to validate
5. `k8s/patient-service/configmap.yaml` - Changed ddl-auto to validate
6. `k8s/appointment-service/configmap.yaml` - Changed ddl-auto to validate
