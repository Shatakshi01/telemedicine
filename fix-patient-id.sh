#!/bin/bash

# Quick fix script for patient_id auto-increment issue
# This script will fix the patients table to properly auto-generate patient_id

echo "🔧 Fixing patient_id auto-increment issue..."

# Get the postgres pod name
POSTGRES_POD=$(kubectl get pods -n telemedicine -l app=postgres -o jsonpath='{.items[0].metadata.name}')

if [ -z "$POSTGRES_POD" ]; then
    echo "❌ Error: PostgreSQL pod not found in telemedicine namespace"
    exit 1
fi

echo "📊 Found PostgreSQL pod: $POSTGRES_POD"

# Run the fix
echo "🔄 Applying database fix..."
kubectl exec -n telemedicine -i $POSTGRES_POD -- psql -U postgres -d telemedicine << 'EOF'
-- Drop the existing patients table
DROP TABLE IF EXISTS patients CASCADE;

-- Recreate with proper auto-increment
CREATE TABLE patients (
    patient_id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    address VARCHAR(255),
    medical_history TEXT,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6)
);

-- Verify the fix
SELECT 
    column_name, 
    column_default, 
    is_nullable,
    data_type 
FROM information_schema.columns 
WHERE table_name = 'patients' AND column_name = 'patient_id';

-- Check sequence was created
SELECT sequence_name FROM information_schema.sequences WHERE sequence_name LIKE '%patient%';
EOF

if [ $? -eq 0 ]; then
    echo "✅ Database fix applied successfully!"
    
    # Restart patient service
    echo "🔄 Restarting patient-service..."
    kubectl rollout restart deployment/patient-service -n telemedicine
    
    echo "⏳ Waiting for patient-service to be ready..."
    kubectl rollout status deployment/patient-service -n telemedicine
    
    echo "✅ Fix completed! You can now register patients."
    echo ""
    echo "🧪 Test the fix with:"
    echo "kubectl port-forward -n telemedicine svc/patient-service 8080:8080"
    echo ""
    echo "Then in another terminal:"
    echo 'curl -X POST http://localhost:8080/patients -H "Content-Type: application/json" -d '"'"'{"firstName":"Test","lastName":"User","email":"test@example.com","phoneNumber":"1234567890","dateOfBirth":"1990-01-01","gender":"MALE"}'"'"
else
    echo "❌ Error applying database fix"
    exit 1
fi
