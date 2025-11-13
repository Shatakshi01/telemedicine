-- Initialize the telemedicine database with patient and appointment schemas
-- Create patient schema
CREATE SCHEMA IF NOT EXISTS patient;
-- Create appointment schema  
CREATE SCHEMA IF NOT EXISTS appointment;
-- Grant all privileges on patient schema to telemedicine_user
GRANT ALL PRIVILEGES ON SCHEMA patient TO telemedicine_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA patient TO telemedicine_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA patient TO telemedicine_user;
-- Grant all privileges on appointment schema to telemedicine_user
GRANT ALL PRIVILEGES ON SCHEMA appointment TO telemedicine_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA appointment TO telemedicine_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA appointment TO telemedicine_user;
-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA patient
GRANT ALL ON TABLES TO telemedicine_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA patient
GRANT ALL ON SEQUENCES TO telemedicine_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA appointment
GRANT ALL ON TABLES TO telemedicine_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA appointment
GRANT ALL ON SEQUENCES TO telemedicine_user;