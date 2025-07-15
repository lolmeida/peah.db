# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Helm chart for deploying n8n** (workflow automation tool) on Kubernetes with:
- Multiple database options (PostgreSQL, MySQL, SQLite)
- Redis for caching and queues
- Automatic SSL/TLS via cert-manager and Let's Encrypt
- Persistent storage for all data
- Basic authentication for the n8n interface

## Essential Commands

### Deployment
```bash
# Automated deployment (recommended)
./deploy-n8n.sh

# Manual deployment
helm upgrade --install n8n . -f custom-values.yaml --namespace default

# Deploy with dry-run to validate
helm install n8n . -f custom-values.yaml --dry-run --debug

# Uninstall
helm uninstall n8n
```

### Verification
```bash
# Check deployment status
kubectl get all -l app.kubernetes.io/name=n8n

# View pod logs
kubectl logs -f deployment/n8n-n8n

# Check ingress and certificate
kubectl describe ingress n8n-ingress
kubectl describe certificate n8n-tls

# Test health endpoint
curl -k https://n8n.lolmeida.com/healthz
```

### Troubleshooting
```bash
# Get events for debugging
kubectl get events --sort-by='.lastTimestamp'

# Check PVC status
kubectl get pvc

# Describe failing pod
kubectl describe pod <pod-name>

# Access n8n pod shell
kubectl exec -it deployment/n8n-n8n -- /bin/sh
```

## Architecture & Configuration

### Chart Structure
```
n8n-chart/
├── Chart.yaml           # Chart metadata
├── values.yaml          # Default values (test config)
├── custom-values.yaml   # Production values (use this!)
├── deploy-n8n.sh       # Automated deployment script
├── templates/
│   ├── _helpers.tpl            # Helm template helpers
│   ├── n8n-deployment.yaml     # Main n8n application
│   ├── n8n-service.yaml        # n8n service (port 5678)
│   ├── postgres-deployment.yaml # PostgreSQL database
│   ├── postgres-service.yaml   # PostgreSQL service
│   ├── mysql-deployment.yaml   # MySQL database
│   ├── mysql-service.yaml      # MySQL service
│   ├── redis-deployment.yaml   # Redis cache
│   ├── redis-service.yaml      # Redis service
│   ├── ingress.yaml           # Ingress with TLS
│   └── secret.yaml            # All passwords
```

### Key Configuration Patterns

1. **Database Selection Logic**:
   - PostgreSQL takes precedence if enabled
   - Falls back to MySQL if PostgreSQL disabled
   - Uses SQLite if both disabled
   - Only one database type runs at a time

2. **Environment Variables**:
   - Database connection dynamically set based on enabled database
   - All sensitive data stored in Kubernetes secret
   - Environment variables reference secret keys

3. **Persistent Storage**:
   - Each component has its own PVC
   - Data survives pod restarts
   - Sizes: n8n (5Gi), databases (5Gi), Redis (2Gi)

### Configuration Reference

**Critical values to modify in custom-values.yaml**:
```yaml
n8n:
  auth:
    username: "admin"      # Change this
    password: "changeme"   # Must change!
  config:
    domain: "n8n.yourdomain.com"  # Your domain
    
postgresql:
  password: "changeme"     # Must change!
  
mysql:
  rootPassword: "changeme" # Must change!
  
redis:
  password: "changeme"     # Must change!
```

### Deployment Workflow

The `deploy-n8n.sh` script:
1. Validates custom-values.yaml exists
2. Checks DNS resolution for configured domain
3. Validates Helm templates
4. Copies files to server via SSH (n8n.lolmeida.com)
5. Runs helm upgrade remotely
6. Monitors deployment progress
7. Tests health endpoint

### Important Considerations

1. **DNS Setup**: Ensure domain points to cluster IP before deployment
2. **Prerequisites**: Requires nginx-ingress and cert-manager in cluster
3. **Passwords**: Never use default passwords in production
4. **Database Choice**: PostgreSQL recommended for production
5. **Monitoring**: Check all pods are running and PVCs are bound

## Common Tasks

### Update Configuration
```bash
# Edit custom-values.yaml, then:
helm upgrade n8n . -f custom-values.yaml

# Or use automated script:
./deploy-n8n.sh
```

### Change Passwords
1. Update passwords in `custom-values.yaml`
2. Run `helm upgrade` to apply changes
3. Pods will restart with new credentials

### Enable/Disable Database
```yaml
# In custom-values.yaml:
postgresql:
  enabled: true   # or false
mysql:
  enabled: false  # or true
```

### Scale Resources
```yaml
# Adjust in custom-values.yaml:
n8n:
  resources:
    limits:
      cpu: 1000m
      memory: 1Gi
```

### Debug Database Connection
```bash
# Check n8n environment variables
kubectl exec deployment/n8n-n8n -- env | grep DB_

# Test database connectivity
kubectl exec deployment/n8n-n8n -- nc -zv n8n-postgres 5432
```

## Status Report Context

The existing CLAUDE.md contains a detailed Kubernetes cluster status report showing:
- MicroK8s cluster operational for 26 days
- All core components working (DNS, networking, ingress, cert-manager)
- Previous `peah-db` application deployed but pods not running
- Certificate and ingress properly configured
- Recommendation to deploy n8n using this chart

This context helps understand the target environment is properly configured and ready for n8n deployment.