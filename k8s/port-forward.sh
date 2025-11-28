#!/bin/bash

# Port Forward Script for Telemedicine Services
# This script starts all port-forwards in the background

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}Starting port-forward for all services...${NC}\n"

# Kill any existing port-forwards
pkill -f "kubectl port-forward" 2>/dev/null

# Start port forwards
echo -e "${GREEN}Patient Service:${NC} http://localhost:8080/swagger-ui.html"
kubectl port-forward -n telemedicine svc/patient-service 8080:8080 > /dev/null 2>&1 &

echo -e "${GREEN}Appointment Service:${NC} http://localhost:8081/swagger-ui.html"
kubectl port-forward -n telemedicine svc/appointment-service 8081:8081 > /dev/null 2>&1 &

echo -e "${GREEN}Session Service:${NC} http://localhost:8082/swagger-ui.html"
kubectl port-forward -n telemedicine svc/session-service 8082:8082 > /dev/null 2>&1 &

echo -e "${GREEN}Kafka UI:${NC} http://localhost:9090"
kubectl port-forward -n telemedicine svc/kafka-ui-service 9090:8080 > /dev/null 2>&1 &

echo -e "\n${YELLOW}Optional database connections:${NC}"
echo "PostgreSQL: localhost:5432 (user: postgres, pass: postgres123)"
echo "MongoDB: localhost:27017 (user: admin, pass: mongodb123)"
echo ""
echo "To enable database port-forwards, uncomment lines in this script"
echo ""

# Uncomment these if you need database access
kubectl port-forward -n telemedicine svc/postgres-service 5432:5432 > /dev/null 2>&1 &
kubectl port-forward -n telemedicine svc/mongodb-service 27017:27017 > /dev/null 2>&1 &

echo -e "${BLUE}All port-forwards started!${NC}"
echo -e "${YELLOW}Press Ctrl+C to stop all port-forwards${NC}\n"

# Wait for interrupt
trap "echo '\nStopping all port-forwards...'; pkill -f 'kubectl port-forward'; exit 0" INT
wait
