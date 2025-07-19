# 🚀 Developer Guide - K8s Configuration System

## 📋 Overview
Sistema completo de configuração Kubernetes com autenticação individual por serviço e geração automática de manifestos baseados em categorias.

## 🏗️ Architecture

### Frontend (React + TypeScript)
```
src/
├── services/
│   ├── k8sManifestDefaults.ts    # Manifestos defaults por categoria
│   ├── mockData.ts               # Auth configs por serviço  
│   └── api.ts                    # API calls
├── components/
│   ├── AuthConfigEditor.tsx      # Editor de configurações auth
│   ├── AppEditor.tsx             # Editor principal de apps
│   └── StackManager.tsx          # Gerenciador de stacks
```

### Backend (Java + Quarkus)
```
src/main/java/com/lolmeida/peahdb/
├── service/
│   ├── K8sManifestDefaultsService.java   # Equivalente do frontend
│   └── K8sService.java                   # Geração de values integrada
├── resource/
│   └── K8sConfigResource.java            # Endpoints REST
└── entity/k8s/
    └── AppManifest.java                  # Entidade de manifestos
```

## 🔐 Authentication System

### 1. Configurações por Categoria

#### Database Services
```typescript
auth: {
  enabled: true,
  type: 'password',           // password, certificate, ldap, oauth
  username: 'postgres',
  database: 'postgres',
  existingSecret: 'postgresql-secret',
  secretKeys: {
    adminPassword: 'postgres-password',
    userPassword: 'user-password'
  },
  enableSuperuserAccess: true,
  createUserDB: true,
  allowEmptyPassword: false
}
```

#### API Services
```typescript
auth: {
  enabled: true,
  type: 'jwt',                // jwt, oauth2, basic, api-key
  jwt: {
    secret: 'api-jwt-secret',
    issuer: 'peahdb-api',
    expirationTime: '24h'
  },
  existingSecret: 'peahdb-auth-secret',
  cors: {
    enabled: true,
    allowedOrigins: ['*'],
    allowCredentials: true
  },
  rateLimit: {
    enabled: true,
    requestsPerMinute: 100
  }
}
```

#### Monitoring Services
```typescript
auth: {
  enabled: false,             // Prometheus commonly runs without auth internally
  type: 'basic',              // basic, oauth2, tls
  basicAuth: {
    username: 'admin',
    existingSecret: 'prometheus-auth-secret'
  },
  webConfig: null,
  enableAdminAPI: false
}
```

### 2. Frontend Implementation

#### AuthConfigEditor Usage
```typescript
// Conditional Auth Settings button
{defaultConfig?.auth?.enabled ? (
  <Grid container spacing={2}>
    <Grid item xs={12} sm={6}>
      <Button onClick={() => setAuthConfigOpen(true)}>
        🔐 Authentication Settings
      </Button>
    </Grid>
  </Grid>
) : (
  <Button onClick={handleSave}>
    💾 Save Configuration  
  </Button>
)}

// Auth chip indicator  
<Chip 
  label={defaultConfig?.auth?.enabled ? "🔐 Auth ON" : "🔓 Auth OFF"}
  color={defaultConfig?.auth?.enabled ? "success" : "default"}
/>
```

#### Auth Config Save Handler
```typescript
const handleAuthConfigSave = async (authConfig: any) => {
  const updatedConfig = {
    ...defaultConfig,
    auth: authConfig
  };
  setDefaultConfig(updatedConfig);
  onUpdate({
    enabled,
    defaultImageTag: imageTag,
    deploymentPriority: deploymentPriority || 100,
    defaultConfig: updatedConfig,
    defaultResources: resources
  });
};
```

### 3. Backend Implementation

#### Condition Evaluation
```java
public boolean evaluateManifestCondition(String condition, JsonNode appConfig) {
    switch (condition) {
        case "auth.enabled":
            return appConfig.path("auth").path("enabled").asBoolean(false);
        case "persistence.enabled": 
            return appConfig.path("persistence").path("enabled").asBoolean(false);
        case "ingress.enabled":
            return appConfig.path("ingress").path("enabled").asBoolean(true);
        case "hpa.enabled":
            return appConfig.path("hpa").path("enabled").asBoolean(false);
        // Generic path evaluation...
    }
}
```

## 📋 Manifest Defaults System

### 1. Category-Based Defaults

#### Database Category Manifests
```java
List<ManifestDefault> databaseDefaults = Arrays.asList(
    new ManifestDefault(DEPLOYMENT, true, 10, 
        "Database deployment with persistence and health checks"),
    new ManifestDefault(SERVICE, true, 20,
        "Internal service for database access"),
    new ManifestDefault(PERSISTENT_VOLUME_CLAIM, true, 5,
        "Persistent storage for database data", 
        "persistence.enabled"),              // <-- Condition
    new ManifestDefault(SECRET, true, 1,
        "Database credentials and configuration secrets",
        "auth.enabled"),                     // <-- Condition
    new ManifestDefault(CONFIG_MAP, false, 15,
        "Database configuration files")
);
```

#### API Category Manifests
```java
List<ManifestDefault> apiDefaults = Arrays.asList(
    new ManifestDefault(DEPLOYMENT, true, 10, "API service deployment",
        null, Map.of("replicaCount", 2)),    // <-- APIs get 2 replicas by default
    new ManifestDefault(HPA, false, 40, "Horizontal Pod Autoscaler",
        "hpa.enabled", Map.of(
            "minReplicas", 2,
            "maxReplicas", 10,
            "targetCPUUtilizationPercentage", 70
        )),
    new ManifestDefault(SERVICE_ACCOUNT, false, 2,
        "Service account for API permissions",
        "serviceAccount.create")
);
```

### 2. Frontend Usage

#### Manifest Preview Component
```typescript
const defaultManifests = getDefaultManifestsForCategory(app.category);
const manifestsToCreate = defaultManifests.filter(manifest => 
  shouldCreateManifest(manifest, defaultConfig)
);

return (
  <Accordion>
    <AccordionSummary>
      <Typography>📋 Kubernetes Manifests Preview</Typography>
    </AccordionSummary>
    <AccordionDetails>
      {manifestsToCreate.map(manifest => (
        <Card key={manifest.manifestType}>
          <Chip label="✅ Will Create" color="success" />
          <Typography variant="h6">
            {manifest.manifestType}
          </Typography>
          <Typography variant="body2">
            {manifest.description}
          </Typography>
        </Card>
      ))}
    </AccordionDetails>
  </Accordion>
);
```

#### Conditional Manifest Creation
```typescript
export const shouldCreateManifest = (manifest: ManifestDefault, config: any): boolean => {
  if (!manifest.creationCondition) return manifest.required;
  
  const condition = manifest.creationCondition;
  
  // Evaluate nested conditions like "auth.enabled" or "persistence.enabled"
  if (condition.includes('.')) {
    const [parent, child] = condition.split('.');
    return config?.[parent]?.[child] === true;
  }
  
  return config?.[condition] === true;
};
```

### 3. Backend Integration

#### Enhanced K8sService
```java
private void generateAppManifestConfigurations(App app, ObjectNode appConfig) {
    // Get default manifests for category
    List<K8sManifestDefaultsService.ManifestDefault> defaultManifests = 
        manifestDefaultsService.getDefaultManifestsForCategory(app.category);
    
    // Generate configurations for default manifests
    for (K8sManifestDefaultsService.ManifestDefault defaultManifest : defaultManifests) {
        if (manifestDefaultsService.evaluateManifestCondition(
                defaultManifest.creationCondition, appConfig)) {
            
            String manifestKey = defaultManifest.manifestType.name().toLowerCase();
            ObjectNode manifestConfig = appConfig.putObject(manifestKey);
            manifestConfig.put("enabled", true);
            
            // Add default configuration
            if (!defaultManifest.defaultConfig.isEmpty()) {
                ObjectNode defaultConfigNode = objectMapper.valueToTree(
                    defaultManifest.defaultConfig);
                manifestConfig.setAll(defaultConfigNode);
            }
        }
    }
}
```

## 🛠️ Development Workflow

### 1. Adding a New Service Category

#### Step 1: Frontend - Add to k8sManifestDefaults.ts
```typescript
export const K8S_MANIFEST_DEFAULTS: Record<string, ManifestDefault[]> = {
  // ... existing categories ...
  
  // 🔥 New Category: Message Queue
  messagequeue: [
    {
      manifestType: 'DEPLOYMENT',
      required: true,
      creationPriority: 10,
      description: 'Message queue deployment with clustering',
      defaultConfig: {
        replicaCount: 3,
        clustering: { enabled: true }
      }
    },
    {
      manifestType: 'SERVICE',
      required: true,
      creationPriority: 20,
      description: 'Internal service for queue access'
    },
    {
      manifestType: 'PERSISTENT_VOLUME_CLAIM',
      required: true,
      creationPriority: 5,
      description: 'Storage for message persistence',
      creationCondition: 'persistence.enabled',
      defaultConfig: {
        accessMode: 'ReadWriteOnce',
        size: '50Gi'
      }
    }
  ]
};
```

#### Step 2: Backend - Add to K8sManifestDefaultsService.java
```java
private static void initializeDefaults() {
    // ... existing categories ...
    
    // 🔥 Message Queue Services  
    List<ManifestDefault> messageQueueDefaults = Arrays.asList(
        new ManifestDefault(AppManifest.ManifestType.DEPLOYMENT, true, 10,
            "Message queue deployment with clustering",
            null, Map.of(
                "replicaCount", 3,
                "clustering", Map.of("enabled", true)
            )),
        new ManifestDefault(AppManifest.ManifestType.SERVICE, true, 20,
            "Internal service for queue access"),
        new ManifestDefault(AppManifest.ManifestType.PERSISTENT_VOLUME_CLAIM, true, 5,
            "Storage for message persistence",
            "persistence.enabled", Map.of(
                "accessMode", "ReadWriteOnce", 
                "size", "50Gi"
            ))
    );
    
    MANIFEST_DEFAULTS.put("messagequeue", messageQueueDefaults);
}
```

#### Step 3: Update Mock Data
```typescript
// In mockData.ts
{
  id: 7,
  name: 'rabbitmq',
  displayName: 'RabbitMQ',
  category: 'messagequeue',           // <-- New category
  defaultConfig: {
    clustering: { enabled: true },
    auth: {
      enabled: true,
      type: 'password',
      username: 'admin',
      existingSecret: 'rabbitmq-secret'
    }
  }
}
```

### 2. Adding a New Auth Type

#### Step 1: Frontend AuthConfigEditor.tsx
```typescript
// Add new auth type to interface
type AuthType = 'password' | 'jwt' | 'basic' | 'email' | 'oauth2' | 'saml';

// Add to auth type selection
<Select value={authConfig.type} onChange={handleTypeChange}>
  <MenuItem value="oauth2">OAuth2</MenuItem>
  <MenuItem value="saml">SAML</MenuItem>
</Select>

// Add specific configuration UI
{authConfig.type === 'oauth2' && (
  <Grid container spacing={2}>
    <Grid item xs={12} sm={6}>
      <TextField 
        label="Client ID"
        value={authConfig.clientId || ''}
        onChange={(e) => handleConfigChange('clientId', e.target.value)}
      />
    </Grid>
    <Grid item xs={12} sm={6}>
      <TextField 
        label="Client Secret"
        value={authConfig.clientSecret || ''}
        onChange={(e) => handleConfigChange('clientSecret', e.target.value)}
      />
    </Grid>
  </Grid>
)}
```

#### Step 2: Backend - Update K8sManifestDefaultsService.java
```java
public JsonNode getDefaultAuthConfig(String category, String authType) {
    ObjectNode authConfig = objectMapper.createObjectNode();
    authConfig.put("enabled", true);
    authConfig.put("type", authType);

    switch (authType) {
        case "oauth2":
            authConfig.put("clientId", "app-client-id");
            authConfig.put("clientSecret", "app-client-secret");
            authConfig.put("authorizeUrl", "https://auth.provider.com/oauth2/authorize");
            authConfig.put("tokenUrl", "https://auth.provider.com/oauth2/token");
            authConfig.put("scope", "read write");
            break;
            
        case "saml":
            authConfig.put("entityId", "app-entity-id");
            authConfig.put("ssoUrl", "https://idp.provider.com/sso");
            authConfig.put("certificateFile", "/etc/saml/cert.pem");
            break;
    }

    return authConfig;
}
```

## 🧪 Testing

### Frontend Testing
```bash
cd apps/peah-fe

# Run the development server
npm start

# Test auth settings conditionally appear
# 1. Create new app with auth.enabled: false → No auth button
# 2. Enable auth → Auth button appears  
# 3. Configure auth → Settings save correctly

# Test manifest preview
# 1. Select database category → Shows PVC, Secret if conditions met
# 2. Disable persistence → PVC disappears from preview
# 3. Enable auth → Secret appears in preview
```

### Backend Testing  
```bash
cd apps/peah-be

# Compile and run
./mvnw quarkus:dev

# Test new endpoints
curl http://localhost:8080/api/k8s/manifests/defaults/database
curl http://localhost:8080/api/k8s/manifests/categories  
curl http://localhost:8080/api/k8s/manifests/types

# Test values generation with new auth configs
curl http://localhost:8080/api/k8s/environments/1/stacks/dBase/values
```

## 📚 References

- **Frontend**: `apps/peah-fe/src/services/k8sManifestDefaults.ts`
- **Backend**: `apps/peah-be/src/main/java/com/lolmeida/peahdb/service/K8sManifestDefaultsService.java`
- **Auth Editor**: `apps/peah-fe/src/components/AuthConfigEditor.tsx`
- **Documentation**: `apps/peah-be/DEPLOYMENT-STRATEGIES.md`
- **REST Endpoints**: `apps/peah-be/src/main/java/com/lolmeida/peahdb/resource/K8sConfigResource.java`

## 🎯 Best Practices

1. **Always check conditions** before creating manifests
2. **Use existing secrets** quando possível para evitar duplicação
3. **Category-specific defaults** devem seguir as boas práticas do serviço
4. **Test auth configurations** em ambiente de desenvolvimento antes de prod
5. **Validate manifest generation** com `kubectl apply --dry-run`

## 🐛 Common Issues

### Issue: Auth button not appearing
**Solution**: Check if `defaultConfig.auth.enabled` is `true`

### Issue: Manifest not generated
**Solution**: Verify condition evaluation in `shouldCreateManifest()`

### Issue: Values generation error  
**Solution**: Check JSON structure and ObjectMapper configuration 