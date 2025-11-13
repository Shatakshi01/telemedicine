#!/bin/bash

# Telemedicine DevOps Automation Script
# Modern Kubernetes deployment with best practices

set -euo pipefail

# Configuration
readonly NAMESPACE="telemedicine"
readonly KUBECTL_CONTEXT="rancher-desktop"
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Colors for output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

# Check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Verify prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    local missing_tools=()
    
    if ! command_exists kubectl; then
        missing_tools+=("kubectl")
    fi
    
    if ! command_exists docker; then
        missing_tools+=("docker")
    fi
    
    if [ ${#missing_tools[@]} -ne 0 ]; then
        log_error "Missing required tools: ${missing_tools[*]}"
        log_error "Please install the missing tools and try again."
        exit 1
    fi
    
    log_success "All prerequisites satisfied"
}

# Check and set kubectl context
check_kubectl_context() {
    log_info "Checking kubectl context..."
    
    local current_context
    current_context=$(kubectl config current-context 2>/dev/null || echo "none")
    
    if [[ "$current_context" != "$KUBECTL_CONTEXT" ]]; then
        log_warning "Current context: $current_context"
        log_info "Switching to $KUBECTL_CONTEXT context..."
        
        if kubectl config get-contexts "$KUBECTL_CONTEXT" &> /dev/null; then
            kubectl config use-context "$KUBECTL_CONTEXT"
            log_success "Switched to $KUBECTL_CONTEXT context"
        else
            log_error "$KUBECTL_CONTEXT context not found!"
            log_error "Please ensure Rancher Desktop is installed and Kubernetes is enabled."
            exit 1
        fi
    else
        log_success "Using correct context: $KUBECTL_CONTEXT"
    fi
}

# Wait for deployment to be ready
wait_for_deployment() {
    local deployment="$1"
    local timeout="${2:-300}"
    
    log_info "Waiting for deployment $deployment to be ready..."
    
    if kubectl wait --for=condition=available --timeout="${timeout}s" deployment/"$deployment" -n "$NAMESPACE" 2>/dev/null; then
        log_success "Deployment $deployment is ready"
    else
        log_warning "Deployment $deployment is not ready yet, continuing..."
    fi
}

# Wait for pod to be ready
wait_for_pods() {
    local label_selector="$1"
    local timeout="${2:-300}"
    
    log_info "Waiting for pods with selector '$label_selector' to be ready..."
    
    if kubectl wait --for=condition=ready pod -l "$label_selector" -n "$NAMESPACE" --timeout="${timeout}s" 2>/dev/null; then
        log_success "Pods are ready"
    else
        log_warning "Some pods are not ready yet, continuing..."
    fi
}

# Build JAR files
build_jars() {
    log_info "Building JAR files..."
    
    local services=("patient-service" "appointment-service" "session-service")
    
    for service in "${services[@]}"; do
        if [ -d "$SCRIPT_DIR/$service" ]; then
            log_info "Building JAR for $service..."
            cd "$SCRIPT_DIR/$service"
            
            # Make gradlew executable
            chmod +x gradle
            
            # Build JAR file
            gradle clean build -x test || {
                log_error "Failed to build JAR for $service"
                exit 1
            }
            cd "$SCRIPT_DIR"
        else
            log_warning "Directory $service not found, skipping..."
        fi
    done
    
    log_success "All JAR files built successfully"
}

# Build Docker images
build_images() {
    log_info "Building Docker images..."
    
    local services=("patient-service" "appointment-service" "session-service")
    
    for service in "${services[@]}"; do
        if [ -d "$SCRIPT_DIR/$service" ]; then
            log_info "Building Docker image for $service..."
            cd "$SCRIPT_DIR/$service"
            
            # Check if JAR file exists
            if ! ls build/libs/*.jar 1> /dev/null 2>&1; then
                log_error "No JAR file found for $service. Run build_jars first."
                exit 1
            fi
            
            docker build -t "telemedicine/$service:latest" . || {
                log_error "Failed to build Docker image for $service"
                exit 1
            }
            cd "$SCRIPT_DIR"
        else
            log_warning "Directory $service not found, skipping..."
        fi
    done
    
    log_success "All Docker images built successfully"
}

# Deploy infrastructure components
deploy_infrastructure() {
    log_info "Deploying infrastructure components..."
    
    # Create namespace
    kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
    
    # Apply infrastructure manifests in order
    local infrastructure_files=(
        "01-database.yaml"
        "02-mongodb.yaml"
        "03-kafka.yaml"
    )
    
    for file in "${infrastructure_files[@]}"; do
        if [ -f "$SCRIPT_DIR/k8s-modern/$file" ]; then
            log_info "Applying $file..."
            kubectl apply -f "$SCRIPT_DIR/k8s-modern/$file"
        fi
    done
    
    # Wait for infrastructure to be ready
    log_info "Waiting for infrastructure to be ready..."
    wait_for_deployment "postgres"
    wait_for_deployment "mongodb"
    wait_for_deployment "zookeeper"
    wait_for_deployment "kafka"
    wait_for_deployment "kafka-ui"
    
    log_success "Infrastructure deployed successfully"
}

# Deploy services
deploy_services() {
    log_info "Deploying application services..."
    
    local service_files=(
        "04-patient-service.yaml"
        "05-appointment-service.yaml"
        "06-session-service.yaml"
    )
    
    for file in "${service_files[@]}"; do
        if [ -f "$SCRIPT_DIR/k8s-modern/$file" ]; then
            log_info "Applying $file..."
            kubectl apply -f "$SCRIPT_DIR/k8s-modern/$file"
        fi
    done
    
    # Wait for services to be ready
    wait_for_deployment "patient-service"
    wait_for_deployment "appointment-service"
    wait_for_deployment "session-service"
    
    log_success "Services deployed successfully"
}

# Deploy monitoring
deploy_monitoring() {
    log_info "Deploying monitoring stack..."
    
    local monitoring_files=(
        "07-prometheus.yaml"
        "08-grafana.yaml"
    )
    
    for file in "${monitoring_files[@]}"; do
        if [ -f "$SCRIPT_DIR/k8s-modern/$file" ]; then
            log_info "Applying $file..."
            kubectl apply -f "$SCRIPT_DIR/k8s-modern/$file"
        fi
    done
    
    # Wait for monitoring to be ready
    wait_for_deployment "prometheus"
    wait_for_deployment "grafana"
    
    log_success "Monitoring stack deployed successfully"
}

# Deploy autoscaling
deploy_autoscaling() {
    log_info "Deploying autoscaling configuration..."
    
    if [ -f "$SCRIPT_DIR/k8s-modern/09-autoscaling.yaml" ]; then
        kubectl apply -f "$SCRIPT_DIR/k8s-modern/09-autoscaling.yaml"
        log_success "Autoscaling configuration applied"
    fi
}

# Show deployment status
show_status() {
    log_info "Deployment Status:"
    echo "=================="
    
    echo -e "\n${YELLOW}Pods:${NC}"
    kubectl get pods -n "$NAMESPACE" -o wide
    
    echo -e "\n${YELLOW}Services:${NC}"
    kubectl get svc -n "$NAMESPACE"
    
    echo -e "\n${YELLOW}HPA Status:${NC}"
    kubectl get hpa -n "$NAMESPACE" 2>/dev/null || echo "No HPA configured"
    
    echo -e "\n${YELLOW}Access URLs:${NC}"
    echo "• Patient Service:     http://localhost:30081/swagger-ui.html"
    echo "• Appointment Service: http://localhost:30082/swagger-ui.html"
    echo "• Session Service:     http://localhost:30083/swagger-ui.html"
    echo "• Kafka UI:           http://localhost:30080"
    echo "• Prometheus:         http://localhost:30090"
    echo "• Grafana:            http://localhost:30030 (admin/admin)"
}

# Health check
health_check() {
    log_info "Running health checks..."
    
    local failed_pods
    failed_pods=$(kubectl get pods -n "$NAMESPACE" --no-headers | awk '$3!="Running" {print $1}' | wc -l)
    
    if [ "$failed_pods" -eq 0 ]; then
        log_success "All pods are running"
    else
        log_warning "$failed_pods pod(s) are not running"
        kubectl get pods -n "$NAMESPACE" --no-headers | awk '$3!="Running" {print $1}'
    fi
}

# Clean up resources
cleanup() {
    log_info "Cleaning up resources..."
    
    if kubectl get namespace "$NAMESPACE" &> /dev/null; then
        kubectl delete namespace "$NAMESPACE" --timeout=60s
        log_success "Cleanup completed"
    else
        log_info "Namespace $NAMESPACE does not exist"
    fi
}

# Show logs
show_logs() {
    local service="${1:-}"
    
    if [ -n "$service" ]; then
        log_info "Showing logs for $service..."
        kubectl logs -n "$NAMESPACE" -l "app=$service" --tail=100 -f
    else
        log_info "Showing recent logs from all services..."
        kubectl logs -n "$NAMESPACE" -l "tier=service" --tail=50
    fi
}

# Main function
main() {
    case "${1:-help}" in
        "deploy"|"up")
            check_prerequisites
            check_kubectl_context
            build_jars
            build_images
            deploy_infrastructure
            deploy_services
            deploy_monitoring
            deploy_autoscaling
            log_success "🏥 Telemedicine platform deployed successfully!"
            show_status
            ;;
        "build")
            check_prerequisites
            check_kubectl_context
            build_jars
            build_images
            ;;
        "build-jars")
            check_prerequisites
            build_jars
            ;;
        "status"|"ps")
            check_kubectl_context
            show_status
            ;;
        "health"|"check")
            check_kubectl_context
            health_check
            ;;
        "logs")
            check_kubectl_context
            show_logs "${2:-}"
            ;;
        "clean"|"down")
            check_kubectl_context
            cleanup
            ;;
        "restart")
            check_kubectl_context
            cleanup
            sleep 5
            "$0" deploy
            ;;
        "monitor")
            log_info "Opening monitoring dashboards..."
            if command_exists open; then
                open "http://localhost:30090" &
                open "http://localhost:30030" &
            else
                echo "Prometheus: http://localhost:30090"
                echo "Grafana:    http://localhost:30030 (admin/admin)"
            fi
            ;;
        *)
            cat << EOF
🏥 Telemedicine DevOps Automation

Usage: $0 <command> [options]

Commands:
  deploy, up      Deploy the complete telemedicine platform
  build          Build JAR files and Docker images
  build-jars     Build JAR files only
  status, ps     Show deployment status
  health, check  Run health checks
  logs [service] Show logs (optionally for specific service)
  clean, down    Clean up all resources
  restart        Clean up and redeploy
  monitor        Open monitoring dashboards

Examples:
  $0 deploy              # Full deployment (builds JARs + images + deploys)
  $0 build               # Build JAR files and Docker images
  $0 build-jars          # Build JAR files only
  $0 status              # Check status
  $0 logs patient-service # Show logs for patient service
  $0 clean               # Clean up everything

Access URLs (after deployment):
• Patient Service:     http://localhost:30081/swagger-ui.html
• Appointment Service: http://localhost:30082/swagger-ui.html
• Session Service:     http://localhost:30083/swagger-ui.html
• Kafka UI:           http://localhost:30080
• Prometheus:         http://localhost:30090
• Grafana:            http://localhost:30030 (admin/admin)

EOF
            ;;
    esac
}

main "$@"
