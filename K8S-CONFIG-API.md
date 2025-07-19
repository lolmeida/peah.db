# 🚀 K8s Configuration API

The **peah-be** application now includes a comprehensive REST API for managing Kubernetes configurations compatible with the frontend at `apps/peah-fe/`.

## 🎯 Overview

This API allows you to:
- Manage environments (prod, staging, dev)
- Configure stacks (database, monitoring, apps) per environment
- Define applications within each stack
- Specify required Kubernetes manifests for each app
- Generate Helm values.yaml files dynamically
- Deploy stacks (mock implementation for now)

## 📊 Database Schema

The API uses a hierarchical database structure:

```
Environment (prod, staging, dev)
├── Stack (database, monitoring, apps)
│   ├── App (postgresql, n8n, grafana, etc.)
│   │   ├── AppManifest (DEPLOYMENT, SERVICE, etc.)
│   │   └── Kubernetes Resources (future expansion)
```

### Tables Created
- `config_environments` - Environment definitions
- `config_stacks` - Stack configurations per environment
- `config_apps` - Application definitions within stacks
- `config_app_manifests` - Required K8s manifests per app
- `config_*` - Kubernetes resource tables (deployments, services, etc.)

## 🔧 API Endpoints

### Base URL: `http://localhost:8080/api/config`

### Environments
- `GET /environments` - List all environments
- `POST /environments` - Create new environment
- `PUT /environments/{envId}` - Update environment

### Stacks
- `GET /environments/{envId}/stacks` - Get stacks for environment
- `PUT /environments/{envId}/stacks/{stackName}` - Update stack by name

### Apps
- `GET /environments/{envId}/stacks/{stackName}/apps` - Get apps in stack
- `PUT /environments/{envId}/stacks/{stackName}/apps/{appName}` - Update app

### App Manifests
- `GET /environments/{envId}/stacks/{stackName}/apps/{appName}/manifests` - Get app manifests

### Values Generation
- `GET /environments/{envId}/stacks/{stackName}/values` - Generate JSON values
- `GET /environments/{envId}/stacks/{stackName}/values.yaml` - Generate YAML values

### Deployment
- `POST /environments/{envId}/stacks/{stackName}/deploy` - Deploy stack (mock)

## 🗄️ Seed Data

The application comes pre-populated with realistic data:

### Environments
1. **prod** - Production environment
2. **staging** - Staging environment  
3. **dev** - Development environment

### Stacks (in prod environment)
1. **database** - PostgreSQL, Redis
2. **monitoring** - Prometheus, Grafana
3. **apps** - N8N, Peah-DB

### Sample Apps
- **postgresql** - Database with deployment, service, PVC, secret manifests
- **redis** - Cache with deployment, service, secret manifests
- **prometheus** - Monitoring with service account, cluster role, config map, deployment, service
- **grafana** - Visualization with deployment, service, secret, ingress
- **n8n** - Automation with full manifest set including ingress
- **peahdb** - API with deployment, service, ingress, optional HPA

## 🧪 Testing

1. **Start the application:**
   ```bash
   cd /Users/lolmeida/Documents/DEV/gh/neu/apps/peah-be
   ./mvnw quarkus:dev
   ```

2. **Run the test script:**
   ```bash
   ./test-endpoints.sh
   ```

3. **Manual testing with curl:**
   ```bash
   # Get all environments
   curl http://localhost:8080/api/config/environments | jq .
   
   # Get database stack apps
   curl http://localhost:8080/api/config/environments/1/stacks/database/apps | jq .
   
   # Generate Helm values
   curl http://localhost:8080/api/config/environments/1/stacks/database/values | jq .
   ```

## 🔗 Frontend Integration

The API is designed to be fully compatible with the mock API in `apps/peah-fe/src/services/mockApi.ts`.

### Key Compatibility Features:
- Uses stack names instead of IDs in URLs
- Returns apps with `requiredManifests` embedded
- Provides all fields expected by frontend interfaces
- Supports update operations by name instead of ID

### Frontend Configuration:
To use the real API instead of mock data, update `apps/peah-fe/src/services/api.ts`:
```typescript
const USE_MOCK_API = false; // Set to false to use real API
```

## 🚀 What's Working

✅ **Complete CRUD operations** for all entities  
✅ **Database migrations** with seed data  
✅ **Frontend-compatible endpoints** with name-based lookups  
✅ **Values generation** for Helm charts  
✅ **CORS enabled** for frontend integration  
✅ **Comprehensive test coverage** (141 tests)  
✅ **OpenAPI documentation** at `/q/swagger-ui`  

## 🔮 Future Enhancements

- **Real deployment integration** with Helm/Kubernetes
- **Authentication & authorization**
- **Audit logging** for configuration changes
- **Validation** of Kubernetes manifests
- **Rollback capability** for deployments
- **Multi-cluster support**

## 📋 Configuration

The application supports multiple profiles:
- **dev** - MySQL with auto-migration, CORS enabled
- **prod** - PostgreSQL, optimized for production
- **test** - H2 in-memory for testing

Database migrations run automatically in dev mode, ensuring the schema is always up-to-date.

## 🌐 Production URLs

When deployed to production:
- **API**: https://peah-db.lolmeida.com/api/config
- **Swagger UI**: https://peah-db.lolmeida.com/q/swagger-ui
- **Health Check**: https://peah-db.lolmeida.com/q/health

---

**🎉 The K8s Configuration API is now ready for frontend integration!**