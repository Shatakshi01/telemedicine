-- Create appointment schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS appointment;
-- Create appointments table
CREATE TABLE IF NOT EXISTS appointment.appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    reason VARCHAR(500),
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- Create patients table for eligibility tracking
CREATE TABLE IF NOT EXISTS appointment.patients (
    patient_id BIGINT PRIMARY KEY,
    mobile_number VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_appointments_patient_id ON appointment.appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_id ON appointment.appointments(doctor_id);
CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointment.appointments(appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointment.appointments(status);
CREATE INDEX IF NOT EXISTS idx_patients_created_at ON appointment.patients(created_at);
-- Add triggers for updating updated_at timestamp
CREATE OR REPLACE FUNCTION appointment.update_updated_at_column() RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';
CREATE TRIGGER update_appointments_updated_at BEFORE
UPDATE ON appointment.appointments FOR EACH ROW EXECUTE FUNCTION appointment.update_updated_at_column();