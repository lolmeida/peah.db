# N8N Comprehensive Deployment Guide

## Overview

This guide covers deployment of the advanced n8n Helm chart featuring:
- **Helper template + loop architecture** with dynamic component generation
- **Integrated monitoring stack** (Prometheus + Grafana)
- **Multiple database support** (PostgreSQL, MySQL, SQLite)
- **Redis caching layer** for enhanced performance
- **Automatic SSL/TLS** with cert-manager integration
- **5 pre-built Grafana dashboards** for complete observability

## Prerequisites

### Required Infrastructure
1. **Kubernetes cluster** (v1.20+) with kubectl configured
2. **Helm 3.x** installed and functional
3. **nginx-ingress controller** for external access
4. **cert-manager** for automatic SSL certificate management
5. **DNS configuration** for all required domains

### Cluster Setup Commands

```bash
# Install nginx-ingress controller
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace ingress-nginx --create-namespace

# Install cert-manager with CRDs
helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --set installCRDs=true

# Create production Let's Encrypt issuer
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: your-email@example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

## Quick Deployment

### Automated Deployment (Recommended)
```bash
# Configure custom-values.yaml with your settings
cp values.yaml custom-values.yaml
vim custom-values.yaml  # Configure passwords and domains

# Deploy using automation script
./deploy-n8n.sh

# Validate dashboard structure
./check-dashboards.sh
```

### Manual Deployment
```bash
# Validate configuration and templates
helm template n8n . -f custom-values.yaml > /dev/null
./check-dashboards.sh

# Deploy to cluster
helm install n8n . -f custom-values.yaml --wait --timeout=600s

# Verify deployment
kubectl get all -l app.kubernetes.io/name=n8n
```

## Configuration Guide

### Production Configuration Template

Create and customize `custom-values.yaml`:

```yaml
# Global settings
global:
  timezone: "Europe/Lisbon"  # Set your timezone

# N8N application configuration
n8n:
  auth:
    enabled: true
    username: "admin"
    password: "change-this-secure-password"  # REQUIRED: Change this!
  
  config:
    domain: "n8n.yourdomain.com"  # REQUIRED: Your domain
    protocol: "https"
    webhookUrl: "https://n8n.yourdomain.com/"
    editorBaseUrl: "https://n8n.yourdomain.com/"

# Database selection (choose one primary database)
postgresql:
  enabled: true  # Recommended for production
  auth:
    password: "secure-postgres-password"  # REQUIRED: Change this!

mysql:
  enabled: false  # Alternative to PostgreSQL
  auth:
    password: "secure-mysql-password"
    rootPassword: "secure-mysql-root-password"

# Redis for performance enhancement
redis:
  enabled: true
  auth:
    password: "secure-redis-password"  # REQUIRED: Change this!

# Monitoring stack (optional but recommended)
monitoring:
  prometheus:
    enabled: true
    retention: "15d"
  
  grafana:
    enabled: true
    auth:
      adminPassword: "secure-grafana-password"  # REQUIRED: Change this!
    config:
      rootUrl: "https://grafana.yourdomain.com"
  
  ingress:
    enabled: true
    hosts:
      - host: grafana.yourdomain.com
        paths:
          - path: /
            pathType: Prefix
            service: grafana
      - host: prometheus.yourdomain.com
        paths:
          - path: /
            pathType: Prefix
            service: prometheus

# Ingress configuration
ingress:
  enabled: true
  className: "nginx"
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
  hosts:
    - host: n8n.yourdomain.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: n8n-tls
      hosts:
        - n8n.yourdomain.com
```

## Advanced Features

### Database Architecture (via `_helpers.tpl:183-197`)
The chart automatically configures database connections based on enabled components using helper template logic:

```yaml
# Priority order (automatic selection):
# 1. PostgreSQL (if enabled)
# 2. MySQL (if PostgreSQL disabled and MySQL enabled)  
# 3. SQLite (if both disabled)

# Environment variables set automatically:
# PostgreSQL: DB_TYPE=postgresdb, DB_POSTGRESDB_HOST=n8n-postgres
# MySQL: DB_TYPE=mysqldb, DB_MYSQLDB_HOST=n8n-mysql
# SQLite: DB_TYPE=sqlite (file-based)
```

### Monitoring Stack Features
- **5 Auto-Generated Dashboards**: n8n overview, PostgreSQL, MySQL, Redis, Kubernetes
- **Prometheus Metrics**: Automatic service discovery and scraping
- **Grafana Integration**: Pre-configured data sources and dashboard provisioning
- **External Access**: Secure ingress with automatic TLS certificates

### Component Management
```bash
# Enable/disable components dynamically
helm upgrade n8n . -f custom-values.yaml \
  --set postgresql.enabled=true \
  --set mysql.enabled=false \
  --set monitoring.grafana.enabled=true

# Scale application instances
kubectl scale deployment/n8n-n8n --replicas=2

# Update resource allocations
kubectl patch deployment n8n-n8n -p '{
  "spec": {
    "template": {
      "spec": {
        "containers": [{
          "name": "n8n",
          "resources": {
            "limits": {"cpu": "1000m", "memory": "1Gi"},
            "requests": {"cpu": "500m", "memory": "512Mi"}
          }
        }]
      }
    }
  }
}'
```

## Access & Usage

### Application Access Points
```bash
# Main n8n application
open https://n8n.yourdomain.com
# Credentials: admin / [your configured password]

# Monitoring dashboard (if enabled)
open https://grafana.yourdomain.com  
# Credentials: admin / [your configured grafana password]

# Metrics collection (if enabled)
open https://prometheus.yourdomain.com
# No authentication required
```

### Verification Commands
```bash
# Check all components
kubectl get all -l app.kubernetes.io/name=n8n

# Component-specific status
kubectl get pods -l app.kubernetes.io/component=n8n
kubectl get pods -l app.kubernetes.io/component=postgres
kubectl get pods -l app.kubernetes.io/component=grafana

# Test connectivity
curl -f https://n8n.yourdomain.com/healthz
curl -f https://grafana.yourdomain.com/api/health
```

## Operational Management

### Routine Operations
```bash
# View application logs
kubectl logs deployment/n8n-n8n -f --tail=100

# View monitoring logs
kubectl logs deployment/n8n-grafana -f --tail=50

# Update deployment
helm upgrade n8n . -f custom-values.yaml --wait

# Backup database (PostgreSQL example)
kubectl exec deployment/n8n-postgres -- pg_dump -U n8n n8n > backup.sql

# Restart components
kubectl rollout restart deployment/n8n-n8n
kubectl rollout restart deployment/n8n-prometheus
```

### Troubleshooting Commands
```bash
# Debug template generation
helm template n8n . -f custom-values.yaml --debug

# Validate dashboard structure
./check-dashboards.sh

# Check ConfigMaps
kubectl describe configmap n8n-prometheus-config
kubectl describe configmap n8n-grafana-dashboards

# Investigate networking
kubectl get endpoints -l app.kubernetes.io/name=n8n
kubectl describe ingress

# Certificate debugging
kubectl describe certificate n8n-tls
kubectl describe certificate monitoring-tls
```

## Security Best Practices

### Essential Security Measures
1. **Password Management**: Change ALL default passwords before deployment
2. **TLS Configuration**: Enable HTTPS-only access via ingress
3. **Network Isolation**: Use Kubernetes network policies if required
4. **Secret Management**: Store sensitive data in Kubernetes secrets only
5. **Regular Updates**: Keep chart and container images updated

### Production Hardening
```bash
# Create dedicated namespace for isolation
kubectl create namespace n8n-production

# Deploy with namespace isolation
helm install n8n . -f custom-values.yaml \
  --namespace n8n-production \
  --wait --timeout=600s

# Configure resource quotas
kubectl apply -f - <<EOF
apiVersion: v1
kind: ResourceQuota
metadata:
  name: n8n-quota
  namespace: n8n-production
spec:
  hard:
    requests.cpu: "2"
    requests.memory: 4Gi
    limits.cpu: "4"
    limits.memory: 8Gi
    persistentvolumeclaims: "10"
EOF
```

## Maintenance & Monitoring

### Health Monitoring
- **Application Health**: `/healthz` endpoint monitoring
- **Database Health**: Connection pool and performance metrics
- **Cache Performance**: Redis hit rates and memory usage
- **Infrastructure**: Pod status, resource utilization, certificate expiry

### Backup Strategy
```bash
# Automated backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)

# Backup PostgreSQL
kubectl exec deployment/n8n-postgres -- pg_dump -U n8n n8n | \
  gzip > "n8n_backup_${DATE}.sql.gz"

# Backup Grafana dashboards
kubectl get configmap n8n-grafana-dashboards -o yaml > \
  "grafana_dashboards_${DATE}.yaml"

# Backup application configuration
helm get values n8n > "n8n_values_${DATE}.yaml"
```

## Uninstallation

### Complete Removal
```bash
# Remove Helm release
helm uninstall n8n

# Clean up persistent volumes (CAUTION: destroys data!)
kubectl delete pvc -l app.kubernetes.io/name=n8n

# Remove certificates
kubectl delete certificate n8n-tls monitoring-tls

# Clean up ConfigMaps and secrets
kubectl delete configmap -l app.kubernetes.io/name=n8n
kubectl delete secret -l app.kubernetes.io/name=n8n
```

---

**🚀 Ready to deploy your production-grade n8n automation platform with comprehensive monitoring!**

For detailed component-specific information, see:
- `README-DATABASES.md` - Database architecture and management
- `README-MONITORING.md` - Monitoring stack configuration and usage
- `DEPLOY-MANUAL.md` - Step-by-step manual deployment procedures