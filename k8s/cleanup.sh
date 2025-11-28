#!/bin/bash

# Telemedicine Kubernetes Cleanup Script
# This script removes all Kubernetes resources

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_message() {
    echo -e "${BLUE}==>${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Confirm deletion
read -p "Are you sure you want to delete all telemedicine resources? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    print_warning "Cleanup cancelled"
    exit 0
fi

print_message "Starting cleanup..."

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Delete microservices first
print_message "Deleting microservices..."
kubectl delete -f "$SCRIPT_DIR/session-service/" --ignore-not-found=true
kubectl delete -f "$SCRIPT_DIR/appointment-service/" --ignore-not-found=true
kubectl delete -f "$SCRIPT_DIR/patient-service/" --ignore-not-found=true
print_success "Microservices deleted"

print_message "Waiting for microservices to terminate..."
sleep 10

# Delete infrastructure
print_message "Deleting infrastructure..."
kubectl delete -f "$SCRIPT_DIR/kafka/" --ignore-not-found=true
kubectl delete -f "$SCRIPT_DIR/mongodb/" --ignore-not-found=true
kubectl delete -f "$SCRIPT_DIR/postgres/" --ignore-not-found=true
print_success "Infrastructure deleted"

print_message "Waiting for infrastructure to terminate..."
sleep 10

# Delete namespace (this will delete everything)
print_message "Deleting namespace..."
kubectl delete -f "$SCRIPT_DIR/namespace.yaml" --ignore-not-found=true
print_success "Namespace deleted"

# Wait for namespace to be fully deleted
print_message "Waiting for namespace to be fully deleted..."
kubectl wait --for=delete namespace/telemedicine --timeout=120s 2>/dev/null || true

print_success "Cleanup completed successfully!"

# Verify cleanup
print_message "Verifying cleanup..."
REMAINING=$(kubectl get all -n telemedicine 2>/dev/null | wc -l)
if [ "$REMAINING" -eq 0 ]; then
    print_success "All resources removed successfully"
else
    print_warning "Some resources may still be terminating"
    kubectl get all -n telemedicine 2>/dev/null || true
fi
