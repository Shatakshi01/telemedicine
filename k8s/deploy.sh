#!/bin/bash

# Telemedicine Kubernetes Deployment Script
# This script builds Docker images and deploys all Kubernetes resources

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
print_message() {
    echo -e "${BLUE}==>${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
print_message "Checking prerequisites..."
if ! command_exists docker; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

if ! command_exists kubectl; then
    print_error "kubectl is not installed or not in PATH"
    exit 1
fi

print_success "All prerequisites met"

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

print_message "Project root: $PROJECT_ROOT"

# Step 1: Build JAR files (if needed)
print_message "Step 1: Building Java applications..."

# Check if gradle/maven exists and build
for service in patient-service appointment-service session-service; do
    SERVICE_DIR="$PROJECT_ROOT/$service"
    if [ -d "$SERVICE_DIR" ]; then
        cd "$SERVICE_DIR"
        
        if [ -f "build.gradle" ] || [ -f "build.gradle.kts" ]; then
            print_message "Building $service with Gradle..."
            if [ -f "gradle" ]; then
                gradle clean build -x test
            else
                gradle clean build -x test
            fi
            print_success "$service built successfully"
        elif [ -f "pom.xml" ]; then
            print_message "Building $service with Maven..."
            mvn clean package -DskipTests
            print_success "$service built successfully"
        else
            print_warning "No build file found for $service, assuming JAR exists"
        fi
    else
        print_warning "Directory $SERVICE_DIR not found"
    fi
done

cd "$PROJECT_ROOT"

# Step 2: Build Docker images
print_message "Step 2: Building Docker images..."

build_image() {
    local service=$1
    local service_dir="$PROJECT_ROOT/$service"
    
    if [ -d "$service_dir" ]; then
        print_message "Building Docker image for $service..."
        cd "$service_dir"
        
        if [ -f "Dockerfile" ]; then
            docker build -t "$service:latest" .
            print_success "Docker image $service:latest built successfully"
        else
            print_error "Dockerfile not found in $service_dir"
            return 1
        fi
    else
        print_error "Service directory $service_dir not found"
        return 1
    fi
}

build_image "patient-service"
build_image "appointment-service"
build_image "session-service"

cd "$PROJECT_ROOT"

# Verify images
print_message "Verifying Docker images..."
docker images | grep -E "patient-service|appointment-service|session-service"

# Step 3: Deploy Kubernetes resources
print_message "Step 3: Deploying Kubernetes resources..."

K8S_DIR="$SCRIPT_DIR"

# Create namespace
print_message "Creating namespace..."
kubectl apply -f "$K8S_DIR/namespace.yaml"
print_success "Namespace created"

# Deploy infrastructure (PostgreSQL, MongoDB, Kafka)
print_message "Step 4: Deploying infrastructure components..."

print_message "Deploying PostgreSQL..."
kubectl apply -f "$K8S_DIR/postgres/"
print_success "PostgreSQL resources applied"

print_message "Deploying MongoDB..."
kubectl apply -f "$K8S_DIR/mongodb/"
print_success "MongoDB resources applied"

print_message "Deploying Kafka and Zookeeper..."
kubectl apply -f "$K8S_DIR/kafka/"
print_success "Kafka resources applied"

# Wait for infrastructure to be ready
print_message "Step 5: Waiting for infrastructure to be ready (60 seconds)..."
echo -n "Waiting: "
for i in {1..60}; do
    echo -n "."
    sleep 1
done
echo ""
print_success "Wait completed"

# Check infrastructure status
print_message "Checking infrastructure pod status..."
kubectl get pods -n telemedicine | grep -E "postgres|mongodb|kafka|zookeeper"

# Wait for pods to be ready
print_message "Waiting for infrastructure pods to be in Running state..."
kubectl wait --for=condition=ready pod -l app=postgres -n telemedicine --timeout=120s || print_warning "PostgreSQL not ready yet"
kubectl wait --for=condition=ready pod -l app=mongodb -n telemedicine --timeout=120s || print_warning "MongoDB not ready yet"
kubectl wait --for=condition=ready pod -l app=zookeeper -n telemedicine --timeout=120s || print_warning "Zookeeper not ready yet"
kubectl wait --for=condition=ready pod -l app=kafka -n telemedicine --timeout=120s || print_warning "Kafka not ready yet"

print_success "Infrastructure is ready"

# Step 6: Deploy microservices
print_message "Step 6: Deploying microservices..."

print_message "Deploying patient-service..."
kubectl apply -f "$K8S_DIR/patient-service/"
print_success "Patient service resources applied"

print_message "Deploying appointment-service..."
kubectl apply -f "$K8S_DIR/appointment-service/"
print_success "Appointment service resources applied"

print_message "Deploying session-service..."
kubectl apply -f "$K8S_DIR/session-service/"
print_success "Session service resources applied"

# Wait for microservices to start
print_message "Waiting for microservices to initialize (30 seconds)..."
sleep 30

# Step 7: Verify deployment
print_message "Step 7: Verifying deployment..."

echo ""
print_message "===== POD STATUS ====="
kubectl get pods -n telemedicine

echo ""
print_message "===== SERVICE STATUS ====="
kubectl get svc -n telemedicine

echo ""
print_message "===== HPA STATUS ====="
kubectl get hpa -n telemedicine

echo ""
print_message "===== PVC STATUS ====="
kubectl get pvc -n telemedicine

echo ""
print_success "Deployment completed!"

echo ""
print_message "===== USEFUL COMMANDS ====="
echo "Watch pods:          kubectl get pods -n telemedicine -w"
echo "View logs:           kubectl logs -n telemedicine -l app=patient-service"
echo "Port forward:        kubectl port-forward -n telemedicine svc/patient-service 8080:8080"
echo "Delete all:          kubectl delete namespace telemedicine"
echo ""

print_message "===== SERVICE ENDPOINTS ====="
echo "Patient Service:     http://localhost:8080 (after port-forward)"
echo "Appointment Service: http://localhost:8081 (after port-forward)"
echo "Session Service:     http://localhost:8082 (after port-forward)"
echo ""

# Check for any pods that are not running
print_message "Checking for issues..."
FAILED_PODS=$(kubectl get pods -n telemedicine --field-selector=status.phase!=Running --no-headers 2>/dev/null | wc -l)
if [ "$FAILED_PODS" -gt 0 ]; then
    print_warning "Some pods are not in Running state. Check logs with:"
    echo "kubectl logs -n telemedicine <pod-name>"
    echo "kubectl describe pod -n telemedicine <pod-name>"
else
    print_success "All pods are running!"
fi

print_success "Deployment script completed successfully!"
