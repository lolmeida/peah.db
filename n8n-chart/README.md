# 🚀 N8N Workflow Automation - Production Helm Chart

[![Helm Chart](https://img.shields.io/badge/Helm-v3.x-blue)](https://helm.sh/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-v1.20+-blue)](https://kubernetes.io/)
[![N8N](https://img.shields.io/badge/N8N-latest-orange)](https://n8n.io/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

**Production-ready Helm chart for n8n workflow automation with comprehensive monitoring stack**

## ✨ Features

- 🎯 **N8N Workflow Automation** - Latest version with full feature support
- 📊 **Integrated Monitoring** - Prometheus + Grafana with 5 pre-built dashboards  
- 🗄️ **Multiple Database Support** - PostgreSQL, MySQL, or SQLite with automatic selection
- ⚡ **Redis Caching** - Enhanced performance with distributed caching
- 🔒 **Automatic SSL/TLS** - Let's Encrypt integration via cert-manager
- 🏗️ **Unified Architecture** - Helper template + loop system for scalability
- 🛡️ **Production Security** - Basic authentication and secret management

## 🏗️ Architecture

### Helper Template + Loop Design
Revolutionary template architecture using centralized configuration with dynamic loops:

```
_helpers.tpl (582 lines)          templates/ (336 lines total)
├── n8n.deploymentConfigs    →    deployments.yaml (107 lines)
├── n8n.serviceConfigs       →    services.yaml (23 lines) 
├── n8n.ingressConfigs       →    ingresses.yaml (60 lines)
├── n8n.configmapConfigs     →    configmaps.yaml (120 lines)
└── n8n.pvcConfigs           →    persistentvolumeclaims.yaml (26 lines)
```

**Benefits:**
- 🔄 **52% code reduction** in main templates  
- 🎯 **Centralized configuration** in helper templates
- 📈 **Easy scaling** - add components via configuration
- 🔧 **Consistent patterns** across all resources

## 🚀 Quick Start

### Prerequisites
- Kubernetes cluster (v1.20+)
- Helm 3.x
- nginx-ingress controller
- cert-manager for SSL certificates

### 1. Clone & Configure
```bash
git clone <repository-url>
cd n8n-chart

# Create production configuration
cp values.yaml custom-values.yaml
vim custom-values.yaml  # Configure passwords and domains
```

### 2. Deploy
```bash
# Automated deployment with validation
./deploy-n8n.sh

# Manual deployment
helm install n8n . -f custom-values.yaml --namespace n8n --create-namespace
```

### 3. Access
- **N8N Application**: `https://n8n.yourdomain.com`
- **Grafana Dashboards**: `https://grafana.yourdomain.com` 
- **Prometheus Metrics**: `https://prometheus.yourdomain.com`

## 📊 Monitoring Stack

### Auto-Generated Dashboards
5 production-ready Grafana dashboards automatically provisioned:

1. **🎯 N8N Overview** - Application health, workflow metrics, response times
2. **🐘 PostgreSQL** - Database performance, connections, query analysis  
3. **🐬 MySQL** - Connection pools, cache hit rates, InnoDB metrics
4. **🔴 Redis** - Memory usage, cache performance, command statistics
5. **☸️ Kubernetes** - Pod status, resource utilization, cluster health

### Dashboard Generation Process
```yaml
# Configuration in dash-config-values.yaml
dashboards:
  n8n-overview:
    title: "N8N Overview"
    panels: [...]

# ↓ Helper template processing (_helpers.tpl:114-148)
# ↓ JSON generation and validation  
# ↓ ConfigMap injection (templates/configmaps.yaml)
# ↓ Auto-loading in Grafana
```

## 🗄️ Database Support

### Automatic Database Selection
Intelligent priority logic implemented in helper templates:

1. **PostgreSQL** (if enabled) - Recommended for production
2. **MySQL** (if PostgreSQL disabled) - Alternative database  
3. **SQLite** (if both disabled) - File-based storage

```yaml
# Example configuration
postgresql:
  enabled: true           # Primary choice
  auth:
    password: "secure-postgres-password"
    
mysql:
  enabled: false          # Disabled when PostgreSQL enabled
  
redis:
  enabled: true           # Independent caching layer
  auth:
    password: "secure-redis-password"
```

## 🔧 Configuration

### Production Configuration Template
```yaml
# Global settings
global:
  timezone: "Europe/Lisbon"

# Application settings  
n8n:
  auth:
    enabled: true
    username: "admin"
    password: "secure-n8n-password"  # CHANGE THIS!
  config:
    domain: "n8n.yourdomain.com"
    protocol: "https"

# Database configuration
postgresql:
  enabled: true
  auth:
    password: "secure-postgres-password"  # CHANGE THIS!

# Monitoring stack
monitoring:
  prometheus:
    enabled: true
    retention: "15d"
  grafana:
    enabled: true
    auth:
      adminPassword: "secure-grafana-password"  # CHANGE THIS!
    config:
      rootUrl: "https://grafana.yourdomain.com"

# Ingress configuration
ingress:
  n8n:
    enabled: true
    className: "nginx"
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

## 🛠️ Management Commands

### Deployment & Updates
```bash
# Deploy or upgrade
./deploy-n8n.sh

# Manual deployment
helm upgrade --install n8n . -f custom-values.yaml --namespace n8n

# Validate configuration
helm template n8n . -f custom-values.yaml > /dev/null
./check-dashboards.sh
```

### Monitoring & Debugging
```bash
# Check all components
kubectl get all -l app.kubernetes.io/name=n8n

# Component-specific status
kubectl get pods -l app.kubernetes.io/component=n8n
kubectl get pods -l app.kubernetes.io/component=postgres
kubectl get pods -l app.kubernetes.io/component=grafana

# View logs
kubectl logs deployment/n8n-n8n -f
kubectl logs deployment/n8n-grafana -f

# Test connectivity
curl -f https://n8n.yourdomain.com/healthz
curl -f https://grafana.yourdomain.com/api/health
```

### Troubleshooting
```bash
# Debug template generation
helm template n8n . -f custom-values.yaml --debug

# Check dashboard validation
./check-dashboards.sh

# Database connectivity
kubectl exec deployment/n8n-n8n -- nc -zv n8n-postgres 5432

# Certificate status
kubectl describe certificate n8n-tls
```

## 📈 Scaling & Production

### Resource Scaling
```bash
# Scale application instances
kubectl scale deployment/n8n-n8n --replicas=2

# Update resource limits
kubectl patch deployment n8n-n8n -p '{
  "spec": {
    "template": {
      "spec": {
        "containers": [{
          "name": "n8n", 
          "resources": {
            "limits": {"cpu": "1000m", "memory": "1Gi"}
          }
        }]
      }
    }
  }
}'
```

### Production Hardening
```bash
# Deploy with namespace isolation
helm install n8n . -f custom-values.yaml \
  --namespace n8n-production \
  --create-namespace

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
EOF
```

## 📚 Documentation

### Detailed Guides
- **[Database Architecture](README-DATABASES.md)** - Database configuration and management
- **[Monitoring Stack](README-MONITORING.md)** - Prometheus + Grafana setup and usage  
- **[Manual Deployment](DEPLOY-MANUAL.md)** - Step-by-step deployment procedures
- **[Comprehensive Guide](guide.md)** - Complete setup and operational guide
- **[Developer Guide](CLAUDE.md)** - Template architecture and development patterns

### Key Files
- `values.yaml` - Default configuration values
- `custom-values.yaml` - Production configuration (create from values.yaml)
- `dash-config-values.yaml` - Dashboard configuration
- `deploy-n8n.sh` - Automated deployment script
- `check-dashboards.sh` - Dashboard validation utility

## 🔒 Security

### Essential Security Measures
1. **Change ALL default passwords** in `custom-values.yaml`
2. **Configure proper domains** with valid DNS records
3. **Enable HTTPS-only** access via ingress SSL redirect
4. **Use dedicated namespace** for production deployments
5. **Regular updates** of chart and container images

### Secret Management
All sensitive data stored in Kubernetes secrets:
```bash
# View configured secrets
kubectl get secret n8n-secret -o yaml

# Secret keys automatically generated:
# - n8n-auth-password
# - postgres-password  
# - mysql-password
# - redis-password
# - grafana-admin-password
```

## 🗂️ Generated Resources

When fully deployed, the chart creates:
- **6 Deployments** (n8n, postgres, mysql, redis, prometheus, grafana)
- **6 Services** (ClusterIP for internal communication)
- **2 Ingresses** (n8n + monitoring with automatic TLS)
- **6 PersistentVolumeClaims** (data persistence)
- **3 ConfigMaps** (prometheus config, grafana config + dashboards)
- **1 Secret** (unified password management)
- **1 ServiceAccount + ClusterRole** (RBAC for monitoring)

## 🤝 Contributing

### Development Principles
1. **Always use helper templates** - Never inline complex logic
2. **Follow loop patterns** - Use `fromYaml (include "helper.name" .)` then `range`
3. **Centralize configuration** - Add components to `_helpers.tpl`
4. **Test thoroughly** - Validate with `helm template` and `./check-dashboards.sh`
5. **Maintain consistency** - Follow existing naming and structure patterns

### Adding New Components
1. Add configuration to `values.yaml`
2. Add to appropriate helper template in `_helpers.tpl`
3. Test template generation
4. Update documentation

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [N8N](https://n8n.io/) - Workflow automation platform
- [Prometheus](https://prometheus.io/) - Monitoring system
- [Grafana](https://grafana.com/) - Visualization platform
- [Helm](https://helm.sh/) - Kubernetes package manager

---

**🚀 Ready to deploy production-grade n8n workflow automation with comprehensive monitoring!**

For support and detailed documentation, visit the individual guide files or check the [CLAUDE.md](CLAUDE.md) for development patterns.