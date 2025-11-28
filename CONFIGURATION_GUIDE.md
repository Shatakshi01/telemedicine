# Configuration Guide - Telemedicine Platform

## 🎯 Configuration Priority in Spring Boot

Spring Boot follows this priority order (highest to lowest):

1. **Environment Variables** (from K8s ConfigMaps/Secrets) ← **HIGHEST PRIORITY**
2. Command-line arguments
3. Java System Properties
4. `application.properties` / `application.yml` files
5. Default values in `${VAR:default}` syntax

---

## 📝 How Configuration Works

### Local Development (Without Kubernetes)
When running locally, the application uses **default values** from `application.properties`:

```properties
# Example from patient-service
server.port=${SERVER_PORT:8080}
# Reads: Use env var SERVER_PORT if available, otherwise use 8080
```

**Local Run**: Uses `8080` (default value)

---

### Kubernetes Deployment
When deployed to K8s, **environment variables from ConfigMaps/Secrets override** the defaults:

```yaml
# k8s/patient-service/configmap.yaml
data:
  SERVER_PORT: '8080'
  SPRING_DATASOURCE_URL: 'jdbc:postgresql://postgres-service:5432/telemedicine'
```

**K8s Run**: Uses ConfigMap values (e.g., `postgres-service:5432`)

---

## 🔐 Current Configuration Mapping

### Patient Service (Port 8080)

| Property | K8s ConfigMap/Secret | Default (Local) |
|----------|---------------------|-----------------|
| `server.port` | `SERVER_PORT=8080` | `8080` |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/telemedicine` |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | `postgres` |
| `spring.datasource.password` | **Secret**: `SPRING_DATASOURCE_PASSWORD` | `postgres123` |
| `spring.kafka.bootstrap-servers` | `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |

---

### Appointment Service (Port 8081)

| Property | K8s ConfigMap/Secret | Default (Local) |
|----------|---------------------|-----------------|
| `server.port` | `SERVER_PORT=8081` | `8081` |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/telemedicine` |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | `postgres` |
| `spring.datasource.password` | **Secret**: `SPRING_DATASOURCE_PASSWORD` | `postgres123` |
| `spring.kafka.bootstrap-servers` | `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |

---

### Session Service (Port 8082)

| Property | K8s ConfigMap/Secret | Default (Local) |
|----------|---------------------|-----------------|
| `server.port` | `SERVER_PORT=8082` | `8082` |
| `spring.data.mongodb.host` | `SPRING_DATA_MONGODB_HOST` | `localhost` |
| `spring.data.mongodb.port` | `SPRING_DATA_MONGODB_PORT` | `27017` |
| `spring.data.mongodb.database` | `SPRING_DATA_MONGODB_DATABASE` | `telemedicine` |
| `spring.data.mongodb.username` | `SPRING_DATA_MONGODB_USERNAME` | `admin` |
| `spring.data.mongodb.password` | **Secret**: `SPRING_DATA_MONGODB_PASSWORD` | `admin123` |
| `spring.kafka.bootstrap-servers` | `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |

---

## 🔒 Kubernetes Secrets

Secrets are base64 encoded in K8s:

### PostgreSQL Secret (`k8s/postgres/secret.yaml`)
```yaml
data:
  POSTGRES_USER: cG9zdGdyZXM=           # postgres
  POSTGRES_PASSWORD: cG9zdGdyZXMxMjM=  # postgres123
```

### MongoDB Secret (`k8s/mongodb/secret.yaml`)
```yaml
data:
  MONGO_INITDB_ROOT_USERNAME: YWRtaW4=        # admin
  MONGO_INITDB_ROOT_PASSWORD: YWRtaW4xMjM=    # admin123
```

### Service Secrets
```yaml
# patient-service-secret.yaml
data:
  SPRING_DATASOURCE_PASSWORD: cG9zdGdyZXMxMjM=  # postgres123

# session-service-secret.yaml
data:
  SPRING_DATA_MONGODB_PASSWORD: YWRtaW4xMjM=    # admin123
```

---

## 🛠️ How to Update Configuration

### Option 1: Update Kubernetes ConfigMaps/Secrets (Recommended for K8s)

```bash
# Edit ConfigMap
kubectl edit configmap patient-service-config -n telemedicine

# Or update from file
kubectl apply -f k8s/patient-service/configmap.yaml

# Restart deployment to pick up changes
kubectl rollout restart deployment patient-service -n telemedicine
```

### Option 2: Update application.properties (For local development)

Edit the default values:
```properties
# patient-service/src/main/resources/application.properties
server.port=${SERVER_PORT:8080}  # Change 8080 to your preferred default
```

Then rebuild:
```bash
./gradlew clean build
docker build -t patient-service:latest .
```

---

## 🔍 Verify Configuration

### Check Current Environment Variables in Pod

```bash
# Check patient-service environment
kubectl exec -it -n telemedicine deployment/patient-service -- env | grep SPRING

# Check all config
kubectl exec -it -n telemedicine deployment/patient-service -- env | sort
```

### Check ConfigMap Contents

```bash
# View ConfigMap
kubectl get configmap patient-service-config -n telemedicine -o yaml

# View Secret (decoded)
kubectl get secret patient-service-secret -n telemedicine -o jsonpath='{.data.SPRING_DATASOURCE_PASSWORD}' | base64 -d
```

### Check Application Logs

```bash
# Check if configuration loaded correctly
kubectl logs -n telemedicine -l app=patient-service --tail=50 | grep -i "datasource\|port\|started"
```

---

## 📋 Configuration Checklist

When updating configuration:

- [ ] Update K8s ConfigMap YAML file
- [ ] Apply ConfigMap: `kubectl apply -f k8s/<service>/configmap.yaml`
- [ ] Update Secret if needed: `kubectl apply -f k8s/<service>/secret.yaml`
- [ ] Restart deployment: `kubectl rollout restart deployment <service> -n telemedicine`
- [ ] Verify pods restarted: `kubectl get pods -n telemedicine`
- [ ] Check logs: `kubectl logs -n telemedicine -l app=<service> --tail=50`
- [ ] Test endpoint: `curl http://localhost:<port>/actuator/health`

---

## 🎨 Best Practices

### 1. Use Environment Variables in Properties Files
✅ **Good**:
```properties
server.port=${SERVER_PORT:8080}
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/db}
```

❌ **Bad**:
```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/db
```

### 2. Keep Secrets in Kubernetes Secrets
✅ Passwords, API keys, tokens → K8s Secrets
❌ Never commit secrets to Git

### 3. Use Meaningful Defaults
Set defaults that work for local development:
```properties
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
# Works locally with default postgres, overridden in K8s
```

### 4. Document Configuration Changes
Always document in this file when adding new config properties.

---

## 🚀 Quick Commands

### Rebuild After Config Changes
```bash
cd k8s
./rebuild.sh
```

### View All Service Configurations
```bash
# List all ConfigMaps
kubectl get configmap -n telemedicine

# List all Secrets
kubectl get secret -n telemedicine

# View specific ConfigMap
kubectl describe configmap patient-service-config -n telemedicine
```

### Test Configuration
```bash
# Start port-forward
./k8s/port-forward.sh

# Test each service
curl http://localhost:8080/actuator/health  # Patient Service
curl http://localhost:8081/actuator/health  # Appointment Service
curl http://localhost:8082/actuator/health  # Session Service
```

---

## 📞 Troubleshooting

### Issue: Configuration not updating
**Solution**: Restart the deployment
```bash
kubectl rollout restart deployment <service-name> -n telemedicine
```

### Issue: Wrong database connection
**Solution**: Check ConfigMap and Secret values
```bash
kubectl get configmap <service>-config -n telemedicine -o yaml
kubectl get secret <service>-secret -n telemedicine -o yaml
```

### Issue: Application using wrong port
**Solution**: Verify SERVER_PORT in ConfigMap and restart
```bash
kubectl get configmap <service>-config -n telemedicine -o jsonpath='{.data.SERVER_PORT}'
```

---

## 📚 References

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Kubernetes ConfigMaps](https://kubernetes.io/docs/concepts/configuration/configmap/)
- [Kubernetes Secrets](https://kubernetes.io/docs/concepts/configuration/secret/)

---

**Last Updated**: 2025-11-18
