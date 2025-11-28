#!/bin/bash

# Telemedicine Kubernetes Status Check Script
# This script checks the status of all deployed resources

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

print_header() {
    echo ""
    echo -e "${CYAN}=====================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}=====================================${NC}"
}

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

# Check if namespace exists
if ! kubectl get namespace telemedicine &>/dev/null; then
    print_error "Namespace 'telemedicine' does not exist. Run deploy.sh first."
    exit 1
fi

# Pod Status
print_header "POD STATUS"
kubectl get pods -n telemedicine -o wide

# Check pod health
print_header "POD HEALTH CHECK"
TOTAL_PODS=$(kubectl get pods -n telemedicine --no-headers | wc -l)
RUNNING_PODS=$(kubectl get pods -n telemedicine --field-selector=status.phase=Running --no-headers | wc -l)
PENDING_PODS=$(kubectl get pods -n telemedicine --field-selector=status.phase=Pending --no-headers | wc -l)
FAILED_PODS=$(kubectl get pods -n telemedicine --field-selector=status.phase=Failed --no-headers | wc -l)

echo "Total Pods:   $TOTAL_PODS"
echo "Running:      $RUNNING_PODS"
echo "Pending:      $PENDING_PODS"
echo "Failed:       $FAILED_PODS"

if [ "$RUNNING_PODS" -eq "$TOTAL_PODS" ] && [ "$TOTAL_PODS" -gt 0 ]; then
    print_success "All pods are running!"
else
    print_warning "Some pods are not in Running state"
fi

# Service Status
print_header "SERVICE STATUS"
kubectl get svc -n telemedicine

# HPA Status
print_header "HPA STATUS"
kubectl get hpa -n telemedicine

# PVC Status
print_header "PVC STATUS"
kubectl get pvc -n telemedicine

# ConfigMap Status
print_header "CONFIGMAP STATUS"
kubectl get configmap -n telemedicine

# Secret Status
print_header "SECRET STATUS"
kubectl get secret -n telemedicine

# Recent Events
print_header "RECENT EVENTS (Last 10)"
kubectl get events -n telemedicine --sort-by='.lastTimestamp' | tail -10

# Check for problematic pods
print_header "PROBLEMATIC PODS CHECK"
PROBLEM_PODS=$(kubectl get pods -n telemedicine --field-selector=status.phase!=Running --no-headers 2>/dev/null)
if [ -z "$PROBLEM_PODS" ]; then
    print_success "No problematic pods found"
else
    print_warning "Found pods with issues:"
    echo "$PROBLEM_PODS"
    echo ""
    print_message "To check logs, run:"
    echo "kubectl logs -n telemedicine <pod-name>"
    echo "kubectl describe pod -n telemedicine <pod-name>"
fi

# Service Endpoints
print_header "SERVICE ENDPOINTS"
kubectl get endpoints -n telemedicine

# Resource Usage (if metrics-server is installed)
print_header "RESOURCE USAGE (if metrics-server available)"
kubectl top pods -n telemedicine 2>/dev/null || print_warning "Metrics server not available"

# Deployment Status
print_header "DEPLOYMENT STATUS"
kubectl get deployments -n telemedicine

# Quick access commands
print_header "QUICK ACCESS COMMANDS"
echo "Port Forward Services:"
echo "  kubectl port-forward -n telemedicine svc/patient-service 8080:8080"
echo "  kubectl port-forward -n telemedicine svc/appointment-service 8081:8081"
echo "  kubectl port-forward -n telemedicine svc/session-service 8082:8082"
echo ""
echo "View Logs:"
echo "  kubectl logs -n telemedicine -l app=patient-service --tail=50"
echo "  kubectl logs -n telemedicine -l app=appointment-service --tail=50"
echo "  kubectl logs -n telemedicine -l app=session-service --tail=50"
echo ""
echo "Watch Pods:"
echo "  kubectl get pods -n telemedicine -w"
echo ""
echo "Delete Everything:"
echo "  ./cleanup.sh"
echo ""
