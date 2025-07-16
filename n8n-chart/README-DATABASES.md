# 🗄️ **N8N Database Architecture**

## 📊 **Supported Databases**

The n8n chart provides comprehensive database support through a unified deployment architecture:

### **🐘 PostgreSQL** (Recommended for Production)
- **Image**: `postgres:15-alpine`
- **Port**: 5432
- **Database**: `n8n`
- **User**: `n8n`
- **Storage**: 5Gi
- **Generated via**: `templates/deployments.yaml` (unified template)

### **🐬 MySQL** (Alternative Database)
- **Image**: `mysql:8.0`
- **Port**: 3306
- **Database**: `n8n`
- **User**: `n8n`
- **Storage**: 5Gi
- **Generated via**: `templates/deployments.yaml` (unified template)

### **🔴 Redis** (Performance Cache)
- **Image**: `redis:7-alpine`
- **Port**: 6379
- **Usage**: Caching and job queues for n8n
- **Storage**: 2Gi
- **Generated via**: `templates/deployments.yaml` (unified template)

## 🔧 **Configuration Architecture**

### **Dynamic Database Selection** (`templates/deployments.yaml:15-29`)
The chart uses intelligent database priority logic:

```yaml
# In custom-values.yaml
postgresql:
  enabled: true    # Primary choice - takes precedence

mysql:
  enabled: false   # Fallback if PostgreSQL disabled

redis:
  enabled: true    # Independent - enhances performance
```

### **Automatic Environment Variable Configuration**
N8N environment variables are set dynamically based on enabled databases:

- **PostgreSQL enabled**: `DB_TYPE=postgresdb`, connection to `n8n-postgres:5432`
- **MySQL enabled**: `DB_TYPE=mysqldb`, connection to `n8n-mysql:3306`
- **Both disabled**: `DB_TYPE=sqlite` (file-based storage)
- **Redis enabled**: Queue configuration with `n8n-redis:6379`

### **Unified Secret Management** (`templates/secret.yaml`)
All database passwords stored in single Kubernetes secret:
```yaml
# Secret keys generated automatically
- n8n-auth-password
- postgres-password  
- mysql-password
- mysql-root-password
- redis-password
```

## 🎯 **Database Priority Logic**

**Automatic Selection Order**:
1. **PostgreSQL** (if `postgresql.enabled: true`)
2. **MySQL** (if PostgreSQL disabled and `mysql.enabled: true`)
3. **SQLite** (if both disabled - local file storage)

**Redis**: Independent of primary database choice, used for caching when enabled.

## 🚀 **Deployment with Databases**

### **1. Configure Database Selection:**
```bash
# Edit production values
vim custom-values.yaml

# Example production config:
postgresql:
  enabled: true
  auth:
    password: "strong-postgres-password"

mysql:
  enabled: false  # Not needed if PostgreSQL enabled

redis:
  enabled: true
  auth:
    password: "strong-redis-password"
```

### **2. Automated Deployment:**
```bash
# Deploy with validation
./deploy-n8n.sh

# The script automatically:
# - Validates database configuration
# - Deploys only enabled components
# - Verifies database connectivity
# - Tests n8n startup with selected database
```

### **3. Verification Commands:**
```bash
# Check all database pods
kubectl get pods -l app.kubernetes.io/name=n8n

# Check specific database components  
kubectl get pods -l app.kubernetes.io/component=postgres
kubectl get pods -l app.kubernetes.io/component=mysql
kubectl get pods -l app.kubernetes.io/component=redis

# Verify n8n database connection
kubectl logs deployment/n8n-n8n | grep -i "database\|connection"
```

## 📊 **Generated Resources**

### **Dynamic Deployments** (via `templates/deployments.yaml`)
Components are generated only if enabled:
- `n8n-n8n` - Always deployed (main application)
- `n8n-postgres` - Only if `postgresql.enabled: true`
- `n8n-mysql` - Only if `mysql.enabled: true` and PostgreSQL disabled
- `n8n-redis` - Only if `redis.enabled: true`

### **Unified Services** (via `templates/services.yaml`)
- `n8n-n8n:5678` - Main n8n interface
- `n8n-postgres:5432` - PostgreSQL (conditional)
- `n8n-mysql:3306` - MySQL (conditional)  
- `n8n-redis:6379` - Redis (conditional)

### **Persistent Storage**
All components get dedicated PVCs when enabled:
- `n8n-n8n-pvc` - n8n workflows and data (5Gi)
- `n8n-postgres-pvc` - PostgreSQL data (5Gi)
- `n8n-mysql-pvc` - MySQL data (5Gi)
- `n8n-redis-pvc` - Redis persistence (2Gi)

## 🔐 **Database Access & Management**

### **PostgreSQL Connection:**
```bash
# Port-forward for external access
kubectl port-forward svc/n8n-postgres 5432:5432

# Connect with psql
psql -h localhost -U n8n -d n8n

# Check connection from within n8n pod
kubectl exec deployment/n8n-n8n -- nc -zv n8n-postgres 5432
```

### **MySQL Connection:**
```bash
# Port-forward for external access
kubectl port-forward svc/n8n-mysql 3306:3306

# Connect with mysql client
mysql -h localhost -u n8n -p n8n

# Check connection from within n8n pod
kubectl exec deployment/n8n-n8n -- nc -zv n8n-mysql 3306
```

### **Redis Management:**
```bash
# Port-forward for external access
kubectl port-forward svc/n8n-redis 6379:6379

# Connect with redis-cli
redis-cli -h localhost -p 6379

# Check cache performance from n8n
kubectl exec deployment/n8n-n8n -- env | grep QUEUE_BULL_REDIS
```

## 🛠️ **Troubleshooting & Diagnostics**

### **Component Status Verification:**
```bash
# Check all database deployments
kubectl get deployments -l app.kubernetes.io/name=n8n

# Check database-specific pods
kubectl get pods -l app.kubernetes.io/component=postgres
kubectl get pods -l app.kubernetes.io/component=mysql  
kubectl get pods -l app.kubernetes.io/component=redis

# Verify PVC binding
kubectl get pvc -l app.kubernetes.io/name=n8n
```

### **Database Connection Debugging:**
```bash
# Check n8n environment variables
kubectl exec deployment/n8n-n8n -- env | grep DB_

# Test database connectivity from n8n
kubectl exec deployment/n8n-n8n -- nc -zv n8n-postgres 5432
kubectl exec deployment/n8n-n8n -- nc -zv n8n-mysql 3306

# View n8n startup logs for database connection
kubectl logs deployment/n8n-n8n | grep -i "database\|connection\|migrate"
```

### **Component Logs Analysis:**
```bash
# PostgreSQL logs
kubectl logs deployment/n8n-postgres -f

# MySQL logs  
kubectl logs deployment/n8n-mysql -f

# Redis logs
kubectl logs deployment/n8n-redis -f

# n8n logs with database context
kubectl logs deployment/n8n-n8n -f | grep -i "db\|database\|sql"
```

### **Common Issues & Solutions:**

1. **Pod Startup Failures**:
   - Check secret passwords match between components
   - Verify PVC storage class is available
   - Check resource limits vs cluster capacity

2. **Connection Failures**:
   - Verify service endpoints: `kubectl get endpoints`
   - Check network policies if implemented
   - Confirm database selection logic in n8n environment

3. **Performance Issues**:
   - Monitor resource usage: `kubectl top pods`
   - Check Redis hit rate if enabled
   - Review database query performance

## 📈 **Performance & Resource Management**

### **Resource Specifications** (from `values.yaml`)
```yaml
# Current default allocations
postgresql:
  resources:
    limits: { cpu: "500m", memory: "512Mi" }
    requests: { cpu: "250m", memory: "256Mi" }

mysql:
  resources:
    limits: { cpu: "500m", memory: "512Mi" }
    requests: { cpu: "250m", memory: "256Mi" }

redis:
  resources:
    limits: { cpu: "250m", memory: "256Mi" }
    requests: { cpu: "100m", memory: "128Mi" }
```

### **Production Scaling Recommendations:**
- **PostgreSQL**: Scale to 1-2 CPU, 1-2Gi memory for high workflow volume
- **MySQL**: Similar scaling profile to PostgreSQL
- **Redis**: Increase memory to 512Mi-1Gi for large workflow caches
- **Storage**: Consider 20Gi+ for production database volumes

## 🔄 **Backup & Data Management**

### **PostgreSQL Backup Strategy:**
```bash
# Create backup
kubectl exec deployment/n8n-postgres -- pg_dump -U n8n -d n8n --clean --if-exists > n8n-backup-$(date +%Y%m%d).sql

# Restore from backup
kubectl exec -i deployment/n8n-postgres -- psql -U n8n -d n8n < n8n-backup-20240101.sql

# Check database size
kubectl exec deployment/n8n-postgres -- psql -U n8n -d n8n -c "SELECT pg_size_pretty(pg_database_size('n8n'));"
```

### **MySQL Backup Strategy:**
```bash
# Create backup with password
kubectl exec deployment/n8n-mysql -- mysqldump -u n8n -p$(kubectl get secret n8n-secret -o jsonpath='{.data.mysql-password}' | base64 -d) n8n > n8n-backup-$(date +%Y%m%d).sql

# Restore from backup
kubectl exec -i deployment/n8n-mysql -- mysql -u n8n -p n8n < n8n-backup-20240101.sql
```

### **Redis Data Persistence:**
```bash
# Force Redis save
kubectl exec deployment/n8n-redis -- redis-cli BGSAVE

# Check Redis memory usage
kubectl exec deployment/n8n-redis -- redis-cli INFO memory
```

---

**🚀 Production-ready database architecture for n8n workflows!** 