# Telemedicine Microservices - DevOps Automation
# Modern Kubernetes deployment with monitoring

.PHONY: help install deploy status clean logs monitor build test

NAMESPACE := telemedicine
HELM_RELEASE := telemedicine-stack
KUBECONFIG := ~/.kube/config
KUBECTL_CONTEXT := rancher-desktop

# Colors for output
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
NC := \033[0m

help: ## Show this help message
	@echo "🏥 Telemedicine Microservices - DevOps Commands"
	@echo "=============================================="
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}'

check-context: ## Verify kubectl context
	@echo "$(YELLOW)Checking kubectl context...$(NC)"
	@current_context=$$(kubectl config current-context 2>/dev/null || echo "none"); \
	if [ "$$current_context" != "$(KUBECTL_CONTEXT)" ]; then \
		echo "$(RED)Wrong context: $$current_context$(NC)"; \
		echo "$(YELLOW)Switching to $(KUBECTL_CONTEXT)...$(NC)"; \
		kubectl config use-context $(KUBECTL_CONTEXT); \
	fi
	@echo "$(GREEN)✓ Using correct context: $(KUBECTL_CONTEXT)$(NC)"

check-tools: ## Check required tools
	@echo "$(YELLOW)Checking required tools...$(NC)"
	@command -v kubectl >/dev/null 2>&1 || { echo "$(RED)kubectl is required$(NC)"; exit 1; }
	@command -v helm >/dev/null 2>&1 || { echo "$(RED)helm is required$(NC)"; exit 1; }
	@command -v docker >/dev/null 2>&1 || { echo "$(RED)docker is required$(NC)"; exit 1; }
	@echo "$(GREEN)✓ All tools available$(NC)"

install: check-context check-tools ## Install Helm charts and dependencies
	@echo "$(YELLOW)Installing Helm repositories...$(NC)"
	helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
	helm repo add grafana https://grafana.github.io/helm-charts
	helm repo add bitnami https://charts.bitnami.com/bitnami
	helm repo update
	@echo "$(GREEN)✓ Helm repositories updated$(NC)"

build: ## Build Docker images for services
	@echo "$(YELLOW)Building Docker images...$(NC)"
	@cd patient-service && docker build -t telemedicine/patient-service:latest .
	@cd appointment-service && docker build -t telemedicine/appointment-service:latest .
	@cd session-service && docker build -t telemedicine/session-service:latest .
	@echo "$(GREEN)✓ Docker images built$(NC)"

deploy: check-context install build ## Deploy complete telemedicine stack
	@echo "$(YELLOW)Creating namespace...$(NC)"
	kubectl create namespace $(NAMESPACE) --dry-run=client -o yaml | kubectl apply -f -
	
	@echo "$(YELLOW)Deploying infrastructure...$(NC)"
	kubectl apply -f k8s-modern/
	
	@echo "$(YELLOW)Waiting for infrastructure to be ready...$(NC)"
	kubectl wait --for=condition=available --timeout=300s deployment -l tier=infrastructure -n $(NAMESPACE)
	
	@echo "$(YELLOW)Deploying services...$(NC)"
	kubectl wait --for=condition=available --timeout=300s deployment -l tier=service -n $(NAMESPACE)
	
	@echo "$(GREEN)✓ Deployment complete!$(NC)"
	@$(MAKE) status

status: check-context ## Show deployment status
	@echo "$(YELLOW)Deployment Status:$(NC)"
	@echo "=================="
	@kubectl get pods -n $(NAMESPACE) -o wide
	@echo ""
	@echo "$(YELLOW)Services:$(NC)"
	@kubectl get svc -n $(NAMESPACE)
	@echo ""
	@echo "$(YELLOW)Access URLs:$(NC)"
	@echo "• Patient Service:     http://localhost:30081/swagger-ui.html"
	@echo "• Appointment Service: http://localhost:30082/swagger-ui.html" 
	@echo "• Session Service:     http://localhost:30083/swagger-ui.html"
	@echo "• Kafka UI:           http://localhost:30080"
	@echo "• Prometheus:         http://localhost:30090"
	@echo "• Grafana:            http://localhost:30030 (admin/admin)"

logs: ## Show logs for all services
	@echo "$(YELLOW)Recent logs from all services:$(NC)"
	@kubectl logs -n $(NAMESPACE) -l tier=service --tail=50

monitor: ## Open monitoring dashboard
	@echo "$(YELLOW)Opening monitoring dashboards...$(NC)"
	@if command -v open >/dev/null 2>&1; then \
		open http://localhost:30090 & \
		open http://localhost:30030 & \
	else \
		echo "Prometheus: http://localhost:30090"; \
		echo "Grafana:    http://localhost:30030 (admin/admin)"; \
	fi

test: check-context ## Run health checks
	@echo "$(YELLOW)Running health checks...$(NC)"
	@kubectl get pods -n $(NAMESPACE) --no-headers | awk '{if($$3!="Running") exit 1}' && echo "$(GREEN)✓ All pods running$(NC)" || echo "$(RED)✗ Some pods not running$(NC)"

clean: check-context ## Clean up all resources
	@echo "$(YELLOW)Cleaning up resources...$(NC)"
	@kubectl delete namespace $(NAMESPACE) --ignore-not-found=true
	@echo "$(GREEN)✓ Cleanup complete$(NC)"

restart: clean deploy ## Clean and redeploy everything

.DEFAULT_GOAL := help
