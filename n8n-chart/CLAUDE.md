# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **comprehensive Helm chart for n8n workflow automation** with a complete monitoring stack:
- **n8n**: Workflow automation with multiple database support (PostgreSQL, MySQL, SQLite)
- **Redis**: Caching and queues for improved performance  
- **Prometheus**: Metrics collection and monitoring
- **Grafana**: Visualization with 5 pre-built dashboards
- **Ingress**: Automatic SSL/TLS via cert-manager and Let's Encrypt
- **Authentication**: Basic auth for all components

## Essential Commands

### Deployment
```bash
# Automated deployment (recommended)
./deploy-n8n.sh

# Manual deployment with custom values
helm upgrade --install n8n . -f custom-values.yaml --namespace lolmeida

# Validate before deployment
helm template n8n . -f custom-values.yaml > /dev/null
./check-dashboards.sh

# Uninstall completely
helm uninstall n8n --namespace lolmeida
```

### Monitoring & Verification
```bash
# Check all components
kubectl get all -l app.kubernetes.io/name=n8n

# Check specific components
kubectl get pods -l app.kubernetes.io/component=n8n
kubectl get pods -l app.kubernetes.io/component=postgres
kubectl get pods -l app.kubernetes.io/component=prometheus
kubectl get pods -l app.kubernetes.io/component=grafana

# View logs
kubectl logs deployment/n8n-n8n -f
kubectl logs deployment/n8n-prometheus -f

# Test services
curl -k https://n8n.lolmeida.com/healthz
curl -k https://grafana.lolmeida.com/api/health
```

### Dashboard Validation
```bash
# Check dashboard JSON structure
./check-dashboards.sh

# Validate dashboards in running Grafana
./check-dashboards.sh --check-grafana
```

## Architecture & Template Structure

### Unified Template Architecture
The chart uses a revolutionary **single-template approach** with dynamic component generation:

```
templates/
├── _helpers.tpl         # Template helpers + Grafana dashboard generation
├── deployments.yaml    # Unified deployment template for all components
├── services.yaml       # Unified service template
├── configmaps.yaml     # ConfigMaps (Prometheus + Grafana dashboards)
├── secret.yaml         # Unified secret for all passwords
├── ingresses.yaml      # Unified ingress template
├── serviceaccounts.yaml # Service accounts for monitoring
└── clusterroles.yaml   # RBAC for Prometheus
```

### Key Architectural Patterns

1. **Helper Template + Loop Architecture**: All major templates use centralized helper templates in `_helpers.tpl` with dynamic loops for component generation:
   - `n8n.deploymentConfigs` - All deployment configurations
   - `n8n.serviceConfigs` - All service configurations  
   - `n8n.ingressConfigs` - All ingress configurations
   - `n8n.configmapConfigs` - All ConfigMap configurations
   - `n8n.pvcConfigs` - All PVC configurations

2. **Database Priority Logic** (in deployments helper):
   - PostgreSQL takes precedence if enabled
   - Falls back to MySQL if PostgreSQL disabled  
   - Uses SQLite if both disabled
   - Environment variables set dynamically based on enabled database

3. **Template-Generated Dashboards**: Grafana dashboards are generated from YAML configuration using Helm template functions in `_helpers.tpl:69-148`.

4. **Conditional Resource Creation**: Components only deploy if enabled in values, with sophisticated conditional logic for monitoring stack.

5. **Unified Secret Management**: All passwords stored in single Kubernetes secret with component-specific keys.

### Component Configuration

The chart supports these major components (configured in values.yaml):

- **n8n**: Always enabled, main application
- **postgresql**: Database option (enabled: true/false)
- **mysql**: Alternative database (enabled: true/false)  
- **redis**: Caching layer (enabled: true/false)
- **monitoring.prometheus**: Metrics collection (enabled: true/false)
- **monitoring.grafana**: Visualization (enabled: true/false)
- **monitoring.ingress**: External access to monitoring (enabled: true/false)

### Critical Values Configuration

**Production checklist for custom-values.yaml**:
```yaml
# Change ALL passwords before deployment
n8n:
  auth:
    password: "strong-n8n-password"
postgresql:
  auth:
    password: "strong-postgres-password"
mysql:
  auth:
    password: "strong-mysql-password"
    rootPassword: "strong-mysql-root-password"
redis:
  auth:
    password: "strong-redis-password"
monitoring:
  grafana:
    auth:
      adminPassword: "strong-grafana-password"

# Configure domains
n8n:
  config:
    domain: "n8n.yourdomain.com"
monitoring:
  grafana:
    config:
      rootUrl: "https://grafana.yourdomain.com"
```

## Pre-built Monitoring Dashboards

The chart includes 5 auto-generated Grafana dashboards:

1. **n8n-overview.json**: n8n service status, workflow metrics, response times
2. **postgresql-dashboard.json**: Database status, connections, performance  
3. **mysql-dashboard.json**: MySQL metrics, query performance
4. **redis-dashboard.json**: Cache performance, memory usage, hit rates
5. **kubernetes-overview.json**: Pod status, resource usage, cluster health

### Dashboard Generation Process
- Dashboards defined in `dash-config-values.yaml` 
- Generated via template helpers in `_helpers.tpl:114-148`
- Injected into ConfigMap in `templates/configmaps.yaml`
- Auto-loaded by Grafana on startup

## Deployment Workflow

The `deploy-n8n.sh` script provides full automation:

1. **Validation Phase**:
   - Checks custom-values.yaml exists
   - Validates DNS resolution
   - Validates Helm templates
   - Validates dashboard JSON structure

2. **Remote Deployment Phase**:
   - Copies files to target server via SSH
   - Detects if install or upgrade needed
   - Executes helm upgrade --install with wait
   - Verifies all component deployments

3. **Health Check Phase**:
   - Tests n8n health endpoint
   - Displays deployment summary with URLs
   - Shows credential information

## Common Development Tasks

### Adding New Components
1. Add component configuration to `values.yaml`
2. Add component to appropriate helper template in `_helpers.tpl`:
   - For deployments: Add to `n8n.deploymentConfigs`
   - For services: Add to `n8n.serviceConfigs`
   - For ingresses: Add to `n8n.ingressConfigs`
   - For ConfigMaps: Add to `n8n.configmapConfigs`
   - For PVCs: Add to `n8n.pvcConfigs`
3. Test with `helm template` and deploy

### Modifying Dashboard Configuration
1. Edit dashboard config in `dash-config-values.yaml`
2. Run `./check-dashboards.sh` to validate JSON structure
3. Deploy and verify in Grafana

### Database Connection Debugging
```bash
# Check environment variables in n8n pod
kubectl exec deployment/n8n-n8n -- env | grep DB_

# Test database connectivity
kubectl exec deployment/n8n-n8n -- nc -zv n8n-postgres 5432
kubectl exec deployment/n8n-n8n -- nc -zv n8n-mysql 3306

# Port-forward for local database access
kubectl port-forward svc/n8n-postgres 5432:5432
kubectl port-forward svc/n8n-mysql 3306:3306
```

### Monitoring Stack Troubleshooting
```bash
# Check Prometheus targets
kubectl port-forward svc/n8n-prometheus 9090:9090
# Visit http://localhost:9090/targets

# Check Grafana dashboard loading
kubectl logs deployment/n8n-grafana | grep -i dashboard

# Verify ConfigMaps
kubectl describe configmap n8n-prometheus-config
kubectl describe configmap n8n-grafana-dashboards
```

## Development Environment Setup

**Target Environment**: MicroK8s cluster with nginx-ingress and cert-manager
**Deployment Target**: n8n.lolmeida.com (SSH access required)

The chart assumes:
- Kubernetes cluster with ingress controller
- cert-manager for automatic TLS certificates  
- DNS pointing to cluster IP
- SSH access to deployment server for automation script

## SSH Access

- **SSH Command for Cluster**: `ssh n8n para aceder ao cluster`

## Template Development Principles

When modifying templates, follow these architectural patterns:

1. **Always use helper templates**: Never inline complex logic in main templates
2. **Follow the loop pattern**: Use `fromYaml (include "helper.name" .)` then `range $key, $item`
3. **Centralize configuration**: Add new components to helper templates in `_helpers.tpl`
4. **Maintain consistency**: Use same naming patterns and structure across components
5. **Test thoroughly**: Always run `helm template` and validate with `./check-dashboards.sh`

The chart's unified architecture ensures that adding new components requires minimal template changes - just configuration in the helper templates.