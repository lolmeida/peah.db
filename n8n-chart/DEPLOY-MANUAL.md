# 🚀 **Manual Deployment Guide - N8N with Monitoring Stack**

**Production Domain:** `n8n.lolmeida.com`  
**Monitoring Domains:** `grafana.lolmeida.com`, `prometheus.lolmeida.com`  
**Target Environment:** MicroK8s cluster with unified template architecture  
**Chart Version:** Advanced n8n with integrated monitoring stack  

---

## ✅ **Infrastructure Prerequisites**

### **Verified Cluster Components:**
- ✅ **MicroK8s**: Operational cluster environment
- ✅ **Nginx Ingress**: Installed and configured for external access
- ✅ **Cert-Manager**: Deployed with Let's Encrypt cluster issuer
- ✅ **Storage Provisioner**: Hostpath provisioner for persistent volumes
- ✅ **RBAC**: Service accounts and cluster roles for monitoring

### **Network Configuration Requirements:**
- ⚠️ **DNS Resolution**: Verify all domains point to cluster ingress IP
  - `n8n.lolmeida.com` → Main application
  - `grafana.lolmeida.com` → Monitoring dashboard  
  - `prometheus.lolmeida.com` → Metrics collection
- ⚠️ **Firewall Rules**: Ensure ports 80/443 are accessible externally

---

## 🔧 **Comprehensive Deployment Process**

### **1. Security Configuration** (CRITICAL)

```bash
# Edit production configuration
vim custom-values.yaml

# Configure ALL passwords (NEVER use defaults in production):
n8n:
  auth:
    password: "strong-n8n-production-password"

postgresql:  # or mysql
  auth:
    password: "strong-database-password"
    rootPassword: "strong-root-password"  # mysql only

redis:
  auth:
    password: "strong-redis-password"

monitoring:
  grafana:
    auth:
      adminPassword: "strong-grafana-password"
```

### **2. Component Selection & Validation**

```bash
# Configure enabled components based on requirements
vim custom-values.yaml

# Example production configuration:
postgresql:
  enabled: true     # Primary database
mysql:
  enabled: false    # Disable alternative
redis:
  enabled: true     # Enable caching
monitoring:
  prometheus:
    enabled: true   # Metrics collection
  grafana:
    enabled: true   # Dashboard visualization
  ingress:
    enabled: true   # External monitoring access

# Validate complete template structure
helm template n8n . -f custom-values.yaml --debug > /tmp/n8n-template-output.yaml

# Validate dashboard generation
./check-dashboards.sh
```

### **3. Remote Deployment Process**

```bash
# Transfer chart to target server
scp -r . n8n:/tmp/n8n-chart/

# Execute deployment on target cluster
ssh n8n
```

### **4. Cluster Deployment Execution**

```bash
# On target server (n8n.lolmeida.com):
cd /tmp/n8n-chart

# Validate template generation on target
microk8s helm3 template n8n . -f custom-values.yaml > /dev/null

# Deploy with comprehensive monitoring stack
microk8s helm3 install n8n . -f custom-values.yaml \
  --namespace default \
  --wait --timeout=600s \
  --create-namespace

# Alternative: namespace isolation (recommended for production)
# microk8s helm3 install n8n . -f custom-values.yaml \
#   --namespace n8n-production \
#   --create-namespace \
#   --wait --timeout=600s
```

### **5. Comprehensive Verification**

```bash
# Verify all component deployments
microk8s kubectl get deployments -l app.kubernetes.io/name=n8n

# Check component-specific pods
microk8s kubectl get pods -l app.kubernetes.io/component=n8n
microk8s kubectl get pods -l app.kubernetes.io/component=postgres
microk8s kubectl get pods -l app.kubernetes.io/component=redis
microk8s kubectl get pods -l app.kubernetes.io/component=prometheus
microk8s kubectl get pods -l app.kubernetes.io/component=grafana

# Verify unified services
microk8s kubectl get svc -l app.kubernetes.io/name=n8n

# Check ingress configuration
microk8s kubectl get ingress

# Verify TLS certificates
microk8s kubectl get certificate
microk8s kubectl describe certificate n8n-tls
microk8s kubectl describe certificate monitoring-tls

# Check persistent volume claims
microk8s kubectl get pvc -l app.kubernetes.io/name=n8n
```

### **6. Component Monitoring & Health Checks**

```bash
# Application logs
microk8s kubectl logs deployment/n8n-n8n -f --tail=50

# Database logs (if enabled)
microk8s kubectl logs deployment/n8n-postgres -f --tail=50

# Redis cache logs
microk8s kubectl logs deployment/n8n-redis -f --tail=50

# Monitoring stack logs
microk8s kubectl logs deployment/n8n-prometheus -f --tail=50
microk8s kubectl logs deployment/n8n-grafana -f --tail=50

# Ingress controller logs (if issues)
microk8s kubectl logs -n ingress daemonset/nginx-ingress-microk8s-controller --tail=50
```

---

## 🌐 **DNS & Network Configuration**

### **Multi-Domain DNS Verification:**
```bash
# Verify all required domains resolve correctly
nslookup n8n.lolmeida.com          # Main application
nslookup grafana.lolmeida.com      # Monitoring dashboard
nslookup prometheus.lolmeida.com   # Metrics collection

# Test from target server
ssh n8n "nslookup n8n.lolmeida.com && nslookup grafana.lolmeida.com"
```

### **Network Connectivity Testing:**
```bash
# Test external access to cluster ingress
CLUSTER_IP=$(microk8s kubectl get ingress -o jsonpath='{.items[0].status.loadBalancer.ingress[0].ip}')
curl -I $CLUSTER_IP

# Check if ports are accessible
nc -zv $CLUSTER_IP 80
nc -zv $CLUSTER_IP 443
```

---

## 🔒 **TLS Certificate Management**

Automatic SSL certificate provisioning via cert-manager for all domains:

```bash
# Check certificate status for main application
microk8s kubectl describe certificate n8n-tls

# Check monitoring stack certificates
microk8s kubectl describe certificate monitoring-tls

# Verify cluster issuer configuration
microk8s kubectl describe clusterissuer letsencrypt-prod

# Monitor certificate issuance process
microk8s kubectl logs -n cert-manager deployment/cert-manager --tail=100
```

---

## ✅ **Comprehensive Functionality Testing**

### **1. Application Connectivity Testing**
```bash
# Test HTTP to HTTPS redirection
curl -I http://n8n.lolmeida.com

# Test HTTPS connectivity
curl -I https://n8n.lolmeida.com

# Test application health endpoint
curl -f https://n8n.lolmeida.com/healthz

# Test monitoring stack endpoints
curl -f https://grafana.lolmeida.com/api/health
curl -f https://prometheus.lolmeida.com/-/healthy
```

### **2. Monitoring Stack Verification**
```bash
# Check Grafana dashboard loading
curl -u admin:your-grafana-password https://grafana.lolmeida.com/api/search

# Verify Prometheus metrics collection
curl https://prometheus.lolmeida.com/api/v1/targets

# Test dashboard JSON validation
./check-dashboards.sh --check-grafana
```

### **3. Database Connectivity Testing**
```bash
# Test database connectivity from n8n
microk8s kubectl exec deployment/n8n-n8n -- nc -zv n8n-postgres 5432
microk8s kubectl exec deployment/n8n-n8n -- nc -zv n8n-redis 6379

# Check database environment variables
microk8s kubectl exec deployment/n8n-n8n -- env | grep DB_
```

### **4. Web Interface Access**
```bash
# Open main application
open https://n8n.lolmeida.com

# Open monitoring dashboard
open https://grafana.lolmeida.com

# Credentials for all interfaces:
# N8N: admin / [configured in custom-values.yaml]
# Grafana: admin / [configured monitoring password]
```

---

## 🛠️ **Advanced Troubleshooting**

### **Component Deployment Issues:**
```bash
# Check specific component status
microk8s kubectl describe deployment n8n-n8n
microk8s kubectl describe deployment n8n-prometheus
microk8s kubectl describe deployment n8n-grafana

# Investigate pod startup failures
microk8s kubectl describe pod <failing-pod-name>
microk8s kubectl logs <failing-pod-name> --previous

# Check ConfigMap generation
microk8s kubectl describe configmap n8n-prometheus-config
microk8s kubectl describe configmap n8n-grafana-dashboards
```

### **Ingress & Networking Issues:**
```bash
# Check ingress controller status
microk8s kubectl get ingress -A
microk8s kubectl describe ingress n8n-ingress

# Verify service endpoints
microk8s kubectl get endpoints -l app.kubernetes.io/name=n8n

# Test internal service connectivity
microk8s kubectl run debug --image=busybox --rm -it --restart=Never -- nc -zv n8n-n8n 5678
```

### **Certificate & TLS Issues:**
```bash
# Detailed certificate investigation
microk8s kubectl describe certificate n8n-tls
microk8s kubectl describe certificate monitoring-tls

# Check certificate challenge process
microk8s kubectl get challenges

# Monitor cert-manager logs
microk8s kubectl logs -n cert-manager deployment/cert-manager -f
```

### **Storage & Persistence Issues:**
```bash
# Check persistent volume status
microk8s kubectl get pv
microk8s kubectl get pvc -l app.kubernetes.io/name=n8n

# Investigate storage class issues
microk8s kubectl describe storageclass

# Check volume mounting
microk8s kubectl describe pod <pod-name> | grep -A5 "Mounts:"
```

---

## 🔄 **Operational Management Commands**

### **Deployment Updates:**
```bash
# Upgrade with new configuration
microk8s helm3 upgrade n8n . -f custom-values.yaml --wait

# Rollback to previous version
microk8s helm3 rollback n8n 1

# Check deployment history
microk8s helm3 history n8n
```

### **Component Management:**
```bash
# Restart individual components
microk8s kubectl rollout restart deployment/n8n-n8n
microk8s kubectl rollout restart deployment/n8n-prometheus
microk8s kubectl rollout restart deployment/n8n-grafana

# Scale components (if needed)
microk8s kubectl scale deployment/n8n-n8n --replicas=2

# Update resource limits
microk8s kubectl patch deployment n8n-n8n -p '{"spec":{"template":{"spec":{"containers":[{"name":"n8n","resources":{"limits":{"cpu":"1000m","memory":"1Gi"}}}]}}}}'
```

### **Monitoring & Maintenance:**
```bash
# Monitor resource usage
microk8s kubectl top pods -l app.kubernetes.io/name=n8n
microk8s kubectl top nodes

# Backup persistent volumes
microk8s kubectl exec deployment/n8n-postgres -- pg_dump -U n8n n8n > n8n-backup-$(date +%Y%m%d).sql

# Clean up completed jobs and old pods
microk8s kubectl delete pods --field-selector=status.phase=Succeeded
```

### **Complete Uninstallation:**
```bash
# Remove Helm release
microk8s helm3 uninstall n8n

# Clean up persistent volumes (CAUTION: destroys data)
microk8s kubectl delete pvc -l app.kubernetes.io/name=n8n

# Remove certificates
microk8s kubectl delete certificate n8n-tls monitoring-tls

# Clean up ConfigMaps
microk8s kubectl delete configmap -l app.kubernetes.io/name=n8n
```

---

## 📋 **Production Deployment Checklist**

### **Pre-Deployment Security:**
- [ ] **Passwords**: All default passwords changed in `custom-values.yaml`
- [ ] **Components**: Required components enabled (databases, monitoring)
- [ ] **Domains**: DNS configured for all required domains
- [ ] **Templates**: Validated with `helm template` and `./check-dashboards.sh`

### **Deployment Execution:**
- [ ] **Transfer**: Chart files transferred to target server
- [ ] **Installation**: `helm install` executed with `--wait` flag
- [ ] **Timeout**: Sufficient timeout configured (600s recommended)

### **Post-Deployment Verification:**
- [ ] **Infrastructure**: All pods running (`kubectl get pods`)
- [ ] **Services**: All services created (`kubectl get svc`)
- [ ] **Ingress**: Ingress rules configured (`kubectl get ingress`)
- [ ] **Certificates**: TLS certificates issued (`kubectl get certificate`)
- [ ] **Storage**: PVCs bound successfully (`kubectl get pvc`)

### **Functional Testing:**
- [ ] **Main Application**: `https://n8n.lolmeida.com` accessible and login works
- [ ] **Monitoring**: `https://grafana.lolmeida.com` accessible with dashboards loaded
- [ ] **Metrics**: `https://prometheus.lolmeida.com` showing healthy targets
- [ ] **Database**: Database connectivity verified from n8n pod
- [ ] **Health Checks**: All health endpoints responding correctly

### **Monitoring Integration:**
- [ ] **Dashboards**: All 5 dashboards loaded in Grafana
- [ ] **Metrics**: Prometheus collecting metrics from all components
- [ ] **Alerts**: Basic health monitoring operational
- [ ] **Data Flow**: End-to-end data flow from metrics to visualization confirmed

---

**🚀 Complete production deployment of n8n with comprehensive monitoring stack!**

**Access Points:**
- **Main Application**: `https://n8n.lolmeida.com`
- **Monitoring Dashboard**: `https://grafana.lolmeida.com`  
- **Metrics Collection**: `https://prometheus.lolmeida.com`

**Credentials**: As configured in `custom-values.yaml` security section 