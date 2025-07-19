# 📚 IMPLEMENTATION GUIDE - K8s Configuration System

## 🎯 Overview Atualizado

**Sistema completo de configuração Kubernetes** com:
- ✅ **Auth settings condicionais** por serviço
- ✅ **Manifestos K8s defaults** por categoria 
- ✅ **Base de Dados Configurável** 🆕 - Migração completa de hardcoded → BD
- ✅ **Cache inteligente** para performance
- ✅ **REST API completa** para gestão de configurações

---

## 🗄️ **NOVIDADE: Database-Driven Configuration**

### **Migration V2.2.0 - Schema Completo**

#### **Tabelas Principais:**
```sql
-- Categorias de serviços (database, monitoring, automation, api, etc.)
config_service_categories
├── id, name, display_name, description
├── icon, color (visual metadata)
└── is_active, created_at, updated_at

-- Manifestos defaults por categoria  
config_manifest_defaults
├── category_id → config_service_categories(id)
├── manifest_type, required, creation_priority
├── description, creation_condition
├── default_config (JSON)
└── is_active, timestamps

-- Auth defaults por categoria e tipo
config_auth_defaults  
├── category_id → config_service_categories(id)
├── auth_type, display_name, description
├── default_config (JSON com configurações completas)
└── is_active, timestamps
```

#### **Dados Seed Incluídos:**
```yaml
🗄️ Database: password, certificate
📊 Monitoring: basic, oauth2  
🤖 Automation: email, ldap
🚀 API: jwt, oauth2, basic, api-key
📡 MessageQueue: password (nova categoria)
🔧 Default: fallback básico
```

### **Entidades JPA Implementadas**

#### **ServiceCategory.java**
```java
@Entity
@Table(name = "config_service_categories") 
public class ServiceCategory {
    private String name;           // database, monitoring, api, etc.
    private String displayName;    // "Database Services"
    private String icon;          // "🗄️" 
    private String color;         // "primary"
    
    // Utility methods
    public boolean isDatabase() { return "database".equals(name); }
    public boolean isApi() { return "api".equals(name); }
}
```

#### **ManifestDefault.java** 
```java
@Entity
@Table(name = "config_manifest_defaults")
public class ManifestDefault {
    private ServiceCategory category;
    private AppManifest.ManifestType manifestType;  // DEPLOYMENT, SERVICE, etc.
    private Boolean required;
    private Integer creationPriority;
    private String creationCondition;               // "auth.enabled", "persistence.enabled"
    private JsonNode defaultConfig;                 // Configurações específicas
}
```

#### **AuthDefault.java**
```java
@Entity  
@Table(name = "config_auth_defaults")
public class AuthDefault {
    private ServiceCategory category;
    private String authType;                       // password, jwt, oauth2, etc.
    private String displayName;                   // "JWT Authentication"
    private JsonNode defaultConfig;               // Config completa do auth
}
```

### **ManifestDefaultsRepository - CRUD Completo**

```java
@ApplicationScoped
public class ManifestDefaultsRepository {
    
    // === CATEGORIES ===
    public List<ServiceCategory> findAllActiveCategories();
    public Optional<ServiceCategory> findCategoryByName(String name);
    
    // === MANIFEST DEFAULTS ===
    public List<ManifestDefault> findManifestDefaultsByCategory(String categoryName);
    public Optional<ManifestDefault> findManifestDefault(String category, String manifestType);
    public List<String> getAllManifestTypes();
    
    // === AUTH DEFAULTS ===  
    public List<AuthDefault> findAuthDefaultsByCategory(String categoryName);
    public Optional<AuthDefault> findAuthDefault(String category, String authType);
    public List<String> getAuthTypesForCategory(String categoryName);
    
    // === UTILITY ===
    public List<Object[]> getCategoryStats();     // Categories com contagens
    public boolean categoryExists(String categoryName);
}
```

### **K8sManifestDefaultsService - Refatorado para BD**

#### **Antes (Hardcoded):**
```java
private static final Map<String, List<ManifestDefault>> MANIFEST_DEFAULTS = new HashMap<>();

static {
    initializeDefaults();  // Hardcoded configurations
}
```

#### **Depois (Database-Driven):**
```java
@Inject
ManifestDefaultsRepository repository;

// Cache para performance
private final Map<String, List<ManifestDefaultEntry>> manifestCache = new HashMap<>();

public List<ManifestDefaultEntry> getDefaultManifestsForCategory(String category) {
    // 1. Check cache
    if (manifestCache.containsKey(category)) {
        return manifestCache.get(category);
    }
    
    // 2. Load from database
    List<ManifestDefault> dbDefaults = repository.findManifestDefaultsByCategory(category);
    List<ManifestDefaultEntry> entries = new ArrayList<>();
    
    // 3. Convert & cache
    for (ManifestDefault dbDefault : dbDefaults) {
        entries.add(new ManifestDefaultEntry(dbDefault, objectMapper));
    }
    manifestCache.put(category, entries);
    
    return entries;
}
```

---

## 🚀 **Novos Endpoints REST API**

### **Manifest Defaults - Database Driven**
```http
GET /api/k8s/manifests/defaults/{category}
# Retorna manifestos para categoria específica da BD

GET /api/k8s/manifests/categories  
# Retorna todas as categorias ativas com metadata visual

GET /api/k8s/manifests/types
# Retorna todos os tipos de manifestos disponíveis
```

### **Auth Defaults - Novos Endpoints**
```http  
GET /api/k8s/auth/defaults/{category}
# Retorna todos os auth types disponíveis para categoria

GET /api/k8s/auth/defaults/{category}/{authType}  
# Retorna configuração específica de auth

# Exemplo: GET /api/k8s/auth/defaults/database/password
{
  "category": "database",
  "authType": "password", 
  "defaultConfig": {
    "enabled": true,
    "type": "password",
    "username": "admin",
    "existingSecret": "database-secret",
    "allowEmptyPassword": false
  }
}
```

### **Cache Management - Novos**
```http
POST /api/k8s/cache/clear
# Limpa cache para forçar reload da BD

GET /api/k8s/cache/stats  
# Estatísticas de cache (to be implemented)
```

---

## 🎯 **Como Usar a Nova Arquitetura**

### **1. Adicionar Nova Categoria**
```sql
-- 1. Inserir categoria
INSERT INTO config_service_categories (name, display_name, description, icon, color) 
VALUES ('blockchain', 'Blockchain Services', 'Blockchain nodes and services', '⛓️', 'warning');

-- 2. Inserir manifestos defaults
INSERT INTO config_manifest_defaults (category_id, manifest_type, required, creation_priority, description, default_config) 
VALUES (
  (SELECT id FROM config_service_categories WHERE name = 'blockchain'),
  'DEPLOYMENT', true, 10, 'Blockchain node deployment',
  '{"replicaCount": 1, "resources": {"limits": {"memory": "2Gi", "cpu": "1000m"}}}'
);

-- 3. Inserir auth defaults
INSERT INTO config_auth_defaults (category_id, auth_type, display_name, description, default_config)
VALUES (
  (SELECT id FROM config_service_categories WHERE name = 'blockchain'),
  'certificate', 'Certificate Authentication', 'TLS certificate based auth',
  '{"enabled": true, "type": "certificate", "certFile": "/etc/certs/node.crt"}'
);
```

### **2. Atualizar Configurações Existentes**
```sql
-- Atualizar manifest default
UPDATE config_manifest_defaults 
SET default_config = '{"replicaCount": 3, "strategy": {"type": "RollingUpdate"}}'
WHERE category_id = (SELECT id FROM config_service_categories WHERE name = 'api')
  AND manifest_type = 'DEPLOYMENT';

-- Atualizar auth default
UPDATE config_auth_defaults
SET default_config = '{"enabled": true, "type": "jwt", "expirationTime": "12h"}'
WHERE category_id = (SELECT id FROM config_service_categories WHERE name = 'api')
  AND auth_type = 'jwt';
```

### **3. Cache Management**
```bash
# Depois de mudanças na BD, limpar cache
curl -X POST http://localhost:8080/api/k8s/cache/clear

# Verificar mudanças
curl GET http://localhost:8080/api/k8s/manifests/defaults/api
curl GET http://localhost:8080/api/k8s/auth/defaults/api/jwt
```

---

## 🔄 **Migration Path - Backward Compatibility**

### **Compatibilidade Mantida:**
- ✅ `getDefaultManifestsForCategory()` - Funciona igual, mas usa BD
- ✅ `evaluateManifestCondition()` - Sem mudanças
- ✅ Frontend `k8sManifestDefaults.ts` - Continue funcionando
- ✅ Todos os endpoints existentes - Sem breaking changes

### **ManifestDefaultEntry Wrapper:**
```java
// Wrapper para manter compatibilidade com código existente
public static class ManifestDefaultEntry {
    public AppManifest.ManifestType manifestType;
    public boolean required;
    public int creationPriority;
    public String description;
    public String creationCondition;
    public Map<String, Object> defaultConfig;

    // Constructor from database entity
    public ManifestDefaultEntry(ManifestDefault entity, ObjectMapper mapper) {
        this.manifestType = entity.getManifestType();
        this.required = entity.isRequired();
        // ... convert JsonNode to Map for existing code
        this.defaultConfig = mapper.convertValue(entity.getDefaultConfig(), Map.class);
    }
}
```

---

## ⚡ **Performance & Cache**

### **Cache Strategy:**
```java
// 1. Manifest Cache
private final Map<String, List<ManifestDefaultEntry>> manifestCache = new HashMap<>();

// 2. Auth Cache  
private final Map<String, List<AuthDefault>> authCache = new HashMap<>();

// 3. Cache Keys
String manifestCacheKey = category.toLowerCase();
String authCacheKey = category.toLowerCase() + "_" + authType.toLowerCase();
```

### **Cache Benefits:**
- ⚡ **1st call**: Database query + cache store
- ⚡ **Subsequent calls**: Cache hit (muito mais rápido)
- 🔄 **Cache invalidation**: Via endpoint `/cache/clear`
- 📊 **Future**: Cache statistics e TTL

---

## 🎨 **Frontend Impact**

### **Sem Mudanças Necessárias:**
```typescript
// Continua funcionando exatamente igual!
const defaultManifests = getDefaultManifestsForCategory(app.category);
const manifestsToCreate = defaultManifests.filter(manifest => 
  shouldCreateManifest(manifest, defaultConfig)
);
```

### **Novas Possibilidades:**
```typescript
// Buscar categorias da BD com metadata visual
const categories = await api.get('/api/k8s/manifests/categories');
categories.forEach(cat => {
  console.log(`${cat.icon} ${cat.displayName} (${cat.color})`);
});

// Buscar auth types específicos por categoria
const authTypes = await api.get('/api/k8s/auth/defaults/database');
authTypes.authTypes.forEach(auth => {
  console.log(`${auth.authType}: ${auth.defaultConfig}`);
});
```

---

## 🔧 **Development Workflow**

### **1. Desenvolvimento Local:**
```bash
# 1. Iniciar Quarkus dev
./mvnw quarkus:dev

# 2. Migration automática V2.2.0 roda ao iniciar
# 3. Seed data carregado automaticamente
# 4. Endpoints disponíveis em http://localhost:8080/api/k8s/
```

### **2. Testes:**
```bash
# Testar manifestos por categoria
curl GET http://localhost:8080/api/k8s/manifests/defaults/database

# Testar auth configs
curl GET http://localhost:8080/api/k8s/auth/defaults/api/jwt

# Testar categorias
curl GET http://localhost:8080/api/k8s/manifests/categories

# Limpar cache
curl -X POST http://localhost:8080/api/k8s/cache/clear
```

### **3. Customização:**
```sql
-- Exemplo: Adicionar novo auth type para APIs
INSERT INTO config_auth_defaults (category_id, auth_type, display_name, description, default_config)
VALUES (
  (SELECT id FROM config_service_categories WHERE name = 'api'),
  'saml', 'SAML Authentication', 'SAML 2.0 SSO authentication',
  '{"enabled": true, "type": "saml", "entityId": "api-service", "ssoUrl": "https://idp.example.com/sso"}'
);
```

---

## 🎯 **Benefits da Nova Arquitetura**

### **✅ Flexibility:**
- Configurações editáveis sem redeploy
- Adição de categorias/auth types via SQL
- Configurações específicas por ambiente (dev/staging/prod)

### **✅ Performance:**
- Cache inteligente reduz consultas à BD
- Fallback automático para categoria "default"
- Queries otimizadas com índices

### **✅ Maintainability:**
- Configurações versionadas via migrations
- Auditoria completa com timestamps
- Separação clara: código vs. configuração

### **✅ Extensibility:**
- Adição fácil de novos tipos de auth
- Suporte a configurações complexas via JSON
- Metadata visual (icons, colors) para UI

### **✅ Compatibility:**
- Zero breaking changes
- Wrapper classes mantêm interface existente  
- Migration path suave

---

## 🔮 **Próximos Passos**

- [ ] **Frontend integration** - Usar novos endpoints de categorias/auth
- [ ] **Cache statistics** - Implementar métricas de cache hit/miss
- [ ] **Admin UI** - Interface para editar configurações na BD
- [ ] **Environment overrides** - Configurações específicas por ambiente
- [ ] **Validation rules** - Validação de configurações via JSON Schema
- [ ] **Audit logging** - Log de mudanças nas configurações
- [ ] **Import/Export** - Backup/restore de configurações

---

**🎉 A migração está completa! Sistema agora 100% configurável via base de dados com total backward compatibility!** 