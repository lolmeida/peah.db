# 📊 **Comprehensive Monitoring Stack - Prometheus + Grafana**

## 🎯 **Architecture Overview**

The integrated monitoring stack provides complete observability through helper template + loop architecture:
- **Prometheus**: Metrics collection with service discovery  
- **Grafana**: Advanced visualization with auto-generated dashboards
- **Ingress**: Secure external access with automatic TLS
- **ConfigMaps**: Dynamic dashboard provisioning via template generation

## 🔧 **Configuration Architecture**

### **1. Enable Monitoring Stack** (`custom-values.yaml`)

```yaml
monitoring:
  prometheus:
    enabled: true           # Metrics collection
    retention: "15d"        # Data retention period
    createServiceAccount: true
    createClusterRole: true # For Kubernetes metrics
  
  grafana:
    enabled: true           # Dashboard visualization
    createConfigMap: true   # Auto-provision dashboards
    
  ingress:
    enabled: true           # External access
    className: "nginx"
    annotations:
      cert-manager.io/cluster-issuer: "letsencrypt-prod"
```

### **2. Security Configuration**

⚠️ **CRITICAL**: Change all default passwords before deployment!

```yaml
monitoring:
  grafana:
    auth:
      adminUser: "admin"
      adminPassword: "strong-grafana-password"  # CHANGE THIS!
    config:
      rootUrl: "https://grafana.yourdomain.com"
      dashboards:
        defaultHomeDashboardPath: "/var/lib/grafana/dashboards/n8n-overview.json"
```

### **3. Domain & Ingress Configuration**

```yaml
monitoring:
  ingress:
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
    tls:
      - secretName: monitoring-tls
        hosts:
          - grafana.yourdomain.com
          - prometheus.yourdomain.com
```

## 📊 **Auto-Generated Dashboard Suite**

### **Template-Generated Dashboards** (`_helpers.tpl:114-148`)
The chart automatically generates 5 production-ready dashboards:

1. **🎯 N8N Overview** (`n8n-overview.json`)
   - Application health status & uptime
   - Workflow execution metrics & performance
   - API response times & error rates
   - Resource utilization monitoring

2. **🐘 PostgreSQL Dashboard** (`postgresql-dashboard.json`)
   - Database connection status & pool utilization
   - Query performance & execution statistics  
   - Storage usage & transaction metrics
   - Lock analysis & slow query monitoring

3. **🐬 MySQL Dashboard** (`mysql-dashboard.json`)
   - Connection pool monitoring & thread usage
   - Query cache performance & hit ratios
   - InnoDB metrics & buffer pool analysis
   - Replication status & performance tuning

4. **🔴 Redis Dashboard** (`redis-dashboard.json`)
   - Memory usage & key eviction policies
   - Cache hit rates & performance metrics
   - Connection pool & command statistics
   - Persistence & AOF/RDB status

5. **☸️ Kubernetes Cluster** (`kubernetes-overview.json`)
   - Pod status & lifecycle monitoring
   - CPU/Memory utilization across nodes
   - Persistent volume usage & health
   - Network & service mesh metrics

### **Dashboard Generation Process**
- **Configuration**: Dashboards defined in `dash-config-values.yaml`
- **Generation**: Template helpers convert YAML to JSON (`_helpers.tpl:69-148`)
- **Deployment**: Injected into ConfigMap via `templates/configmaps.yaml`
- **Auto-Loading**: Grafana automatically provisions on startup
- **Validation**: `./check-dashboards.sh` validates JSON structure

### **Dashboard Features**
- **Auto-Refresh**: 30-second update interval
- **Time Range**: Default 1-hour window with customizable ranges
- **Navigation**: Organized in "N8N Stack" folder
- **Default Home**: N8N Overview dashboard as landing page

## 🚀 **Deployment & Management**

### **1. Prerequisites & DNS Configuration**

```bash
# Configure DNS records for monitoring domains
# Add to your DNS provider or /etc/hosts for testing:
YOUR_CLUSTER_IP grafana.yourdomain.com
YOUR_CLUSTER_IP prometheus.yourdomain.com

# Verify DNS resolution
nslookup grafana.yourdomain.com
nslookup prometheus.yourdomain.com
```

### **2. Configuration & Deployment**

```bash
# Configure monitoring in custom-values.yaml
vim custom-values.yaml

# Enable monitoring stack and set secure passwords
monitoring:
  prometheus:
    enabled: true
  grafana:
    enabled: true
    auth:
      adminPassword: "your-secure-grafana-password"
  ingress:
    enabled: true

# Deploy with automated validation
./deploy-n8n.sh

# Validate dashboard structure before deployment
./check-dashboards.sh
```

### **3. Post-Deployment Verification**

```bash
# Check monitoring component status
kubectl get pods -l app.kubernetes.io/component=prometheus
kubectl get pods -l app.kubernetes.io/component=grafana

# Verify ingress and certificates
kubectl get ingress | grep monitoring
kubectl get certificate monitoring-tls

# Test external access
curl -k https://grafana.yourdomain.com/api/health
curl -k https://prometheus.yourdomain.com/-/healthy
```

## 📊 **Deployed Infrastructure Components**

### **Prometheus** (Metrics Collection)
- **External URL**: `https://prometheus.yourdomain.com`
- **Internal Service**: `n8n-prometheus:9090`
- **Storage**: 10Gi persistent volume
- **Data Retention**: 15 days (configurable)
- **Generated via**: `_helpers.tpl:420-465` + `templates/deployments.yaml` (helper template + loop)
- **ConfigMap**: `n8n-prometheus-config` (scrape configurations)
- **RBAC**: ServiceAccount + ClusterRole for Kubernetes metrics

### **Grafana** (Visualization)
- **External URL**: `https://grafana.yourdomain.com`
- **Internal Service**: `n8n-grafana:3000`
- **Storage**: 5Gi persistent volume
- **Authentication**: admin / configured password
- **Generated via**: `_helpers.tpl:365-419` + `templates/deployments.yaml` (helper template + loop)
- **ConfigMap**: `n8n-grafana-dashboards` (auto-provisioned dashboards)

### **Resource Management**
```yaml
# Default resource allocations
prometheus:
  resources:
    limits: { cpu: "500m", memory: "512Mi" }
    requests: { cpu: "250m", memory: "256Mi" }

grafana:
  resources:
    limits: { cpu: "500m", memory: "512Mi" }
    requests: { cpu: "250m", memory: "256Mi" }
```

### **Persistent Storage**
- `n8n-prometheus-pvc` (10Gi) - Metrics data storage
- `n8n-grafana-pvc` (5Gi) - Dashboard configs & user data

### **TLS & Security**
- `monitoring-tls` certificate (Let's Encrypt via cert-manager)
- All passwords stored in unified `n8n-secret`
- Ingress-level SSL termination with automatic redirect

## 📈 **Metrics & Data Sources**

### **Automatic Service Discovery**
Prometheus automatically discovers and monitors:

- **n8n Application**: Health, performance, workflow metrics
- **PostgreSQL**: Connection pools, query performance, storage
- **MySQL**: Thread usage, query cache, InnoDB metrics  
- **Redis**: Memory usage, cache hit rates, command statistics
- **Kubernetes**: Pod status, resource utilization, networking

### **Key Metric Categories**
```promql
# Application Health
up{job="n8n"}                              # Service availability
http_request_duration_seconds{job="n8n"}   # Response times

# Database Performance  
pg_stat_database_tup_fetched                # PostgreSQL operations
mysql_global_status_queries                 # MySQL query rate
redis_commands_processed_total              # Redis throughput

# Infrastructure
container_cpu_usage_seconds_total           # CPU utilization
container_memory_usage_bytes                # Memory consumption
kube_pod_status_ready                       # Pod readiness
```

## 🔍 **Monitoring Interface Access**

### **Grafana Dashboard Portal**
```bash
# Access URL: https://grafana.yourdomain.com
# Default credentials:
Username: admin
Password: [configured in custom-values.yaml]

# Key features:
- 5 pre-built dashboards auto-loaded
- N8N Overview set as default homepage
- 30-second auto-refresh intervals
- Customizable time ranges and filters
```

### **Prometheus Metrics Explorer**
```bash
# Access URL: https://prometheus.yourdomain.com
# Key endpoints:
/targets          # View all monitored services
/graph           # PromQL query interface  
/rules           # Active recording/alerting rules
/status/config   # View Prometheus configuration
```

## 🛠️ **Troubleshooting & Diagnostics**

### **Component Health Verification**
```bash
# Check monitoring stack deployment
kubectl get deployments -l app.kubernetes.io/name=n8n | grep -E "(prometheus|grafana)"

# View component logs
kubectl logs deployment/n8n-prometheus -f --tail=100
kubectl logs deployment/n8n-grafana -f --tail=100

# Check ConfigMap provisioning
kubectl describe configmap n8n-prometheus-config
kubectl describe configmap n8n-grafana-dashboards
```

### **Dashboard Loading Issues**
```bash
# Validate dashboard JSON structure
./check-dashboards.sh

# Check Grafana dashboard provisioning
kubectl exec deployment/n8n-grafana -- ls -la /var/lib/grafana/dashboards/

# View Grafana provisioning logs
kubectl logs deployment/n8n-grafana | grep -i "dashboard\|provision"
```

### **Metrics Collection Debugging**
```bash
# Port-forward for direct access
kubectl port-forward svc/n8n-prometheus 9090:9090
kubectl port-forward svc/n8n-grafana 3000:3000

# Test metrics endpoints
curl http://localhost:9090/api/v1/targets         # Check target health
curl http://localhost:3000/api/health             # Grafana health
curl http://localhost:3000/api/datasources        # Configured data sources
```

### **Service Discovery Issues**
```bash
# Check Prometheus ServiceAccount permissions
kubectl describe serviceaccount n8n-prometheus-sa
kubectl describe clusterrole n8n-prometheus-clusterrole

# Verify service endpoints
kubectl get endpoints -l app.kubernetes.io/name=n8n

# Check network connectivity
kubectl exec deployment/n8n-prometheus -- nc -zv n8n-n8n 5678
```

## 📊 **Advanced PromQL Queries**

### **Application Performance**
```promql
# n8n uptime percentage (last 24h)
avg_over_time(up{job="n8n"}[24h]) * 100

# Workflow execution rate
rate(n8n_workflow_executions_total[5m])

# Average response time
rate(http_request_duration_seconds_sum{job="n8n"}[5m]) / 
rate(http_request_duration_seconds_count{job="n8n"}[5m])
```

### **Database Monitoring**
```promql
# PostgreSQL connection utilization
pg_stat_database_numbackends / pg_settings_max_connections * 100

# MySQL query cache hit rate
mysql_global_status_qcache_hits / 
(mysql_global_status_qcache_hits + mysql_global_status_qcache_inserts) * 100

# Redis memory usage percentage
redis_memory_used_bytes / redis_memory_max_bytes * 100
```

### **Infrastructure Health**
```promql
# Pod restart rate (last hour)
increase(kube_pod_container_status_restarts_total[1h])

# Persistent volume usage
(kubelet_volume_stats_used_bytes / kubelet_volume_stats_capacity_bytes) * 100

# Node resource pressure
kube_node_status_condition{condition="MemoryPressure",status="true"}
```

## 🎯 **Production Considerations**

### **Scaling & Performance**
- **Prometheus**: Increase retention for long-term analysis (30d+)
- **Grafana**: Enable plugin support for enhanced visualizations
- **Storage**: Consider separate storage classes for monitoring data
- **Network**: Implement service mesh for advanced observability

### **Security Enhancements**
- Configure Grafana LDAP/SAML integration for enterprise auth
- Implement Prometheus federation for multi-cluster monitoring
- Enable audit logging for dashboard changes
- Set up backup strategies for dashboard configurations

### **Future Roadmap**
1. **Alertmanager**: Advanced alerting with notification channels
2. **Loki**: Centralized log aggregation and analysis
3. **Jaeger**: Distributed tracing for workflow performance
4. **Custom Exporters**: Application-specific metrics collection

## ✅ **Production Deployment Checklist**

- [ ] **Security**: All default passwords changed in `custom-values.yaml`
- [ ] **DNS**: Records configured for `grafana.yourdomain.com` and `prometheus.yourdomain.com`
- [ ] **Deployment**: Executed via `./deploy-n8n.sh` with monitoring enabled
- [ ] **Validation**: `./check-dashboards.sh` passes without errors
- [ ] **Infrastructure**: All pods running (`kubectl get pods`)
- [ ] **Networking**: Ingress and certificates valid (`kubectl get certificate`)
- [ ] **Access**: External URLs accessible via HTTPS
- [ ] **Authentication**: Grafana login functional with configured credentials
- [ ] **Dashboards**: All 5 dashboards loaded and displaying data
- [ ] **Metrics**: Prometheus showing healthy targets (`/targets`)
- [ ] **Integration**: n8n metrics visible in monitoring dashboards

---

**🚀 Production-grade monitoring infrastructure for n8n automation platform!**

**Access Points:**
- **Main Application**: `https://n8n.yourdomain.com`
- **Monitoring Dashboard**: `https://grafana.yourdomain.com`  
- **Metrics Explorer**: `https://prometheus.yourdomain.com` 