# 🚀 Estratégias de Deployment por Ambiente

Esta implementação fornece diferentes estratégias de deployment baseadas no ambiente selecionado.

## 📋 Visão Geral

| Ambiente | Estratégia | Descrição | Duração |
|----------|------------|-----------|---------|
| **DEV** | 🔄 Mock | Simula deployment sem mudanças reais | ~800ms |
| **STAGING** | 🚀 Local K8s | Deploy real no cluster local (docker-desktop) | ~2s |
| **PROD** | ⚡ Production | Deploy no cluster de produção | ~3s |

## 🔧 Implementação Backend

### DeploymentService
Localizado em `src/main/java/com/lolmeida/peahdb/service/DeploymentService.java`

Responsável por executar as diferentes estratégias:

```java
public DeploymentResult deployStack(Environment env, Stack stack) {
    switch (env.name.toLowerCase()) {
        case "dev":    return handleDevDeploy(env, stack);
        case "staging": return handleStagingDeploy(env, stack);
        case "prod":   return handleProdDeploy(env, stack);
    }
}
```

### Configurações
No arquivo `application.properties`:

```properties
# Configurações padrão
app.deployment.local.helm-path=helm
app.deployment.prod.context=prod-cluster
app.deployment.staging.context=docker-desktop

# Desenvolvimento  
%dev.app.deployment.staging.context=docker-desktop
%dev.app.deployment.prod.context=minikube

# Produção
%prod.app.deployment.prod.context=prod-k8s-cluster
%prod.app.deployment.staging.context=staging-k8s-cluster
```

## 🎨 Frontend

### Indicadores Visuais
- **DEV**: Botão azul (info) - "Simulate Deploy" 
- **STAGING**: Botão laranja (warning) - "Deploy Local"
- **PROD**: Botão vermelho (error) - "Deploy Production" ⚠️

### Mensagens Diferenciadas
```typescript
switch (envName.toLowerCase()) {
  case 'dev':     showMessage('🔄 Simulating deployment...', 'info');
  case 'staging': showMessage('🚀 Deploying to local Kubernetes...', 'info');  
  case 'prod':    showMessage('⚡ Deploying to production cluster...', 'info');
}
```

## 🛠️ Comandos Helm Executados

### STAGING (Local)
```bash
helm upgrade --install \
  staging-database \
  ./charts/database \
  --values /tmp/values-staging-database.yaml \
  --kube-context docker-desktop \
  --timeout 5m0s \
  --wait
```

### PROD (Production)
```bash
helm upgrade --install \
  prod-database \
  ./charts/database \
  --values /tmp/values-prod-database.yaml \
  --kube-context prod-k8s-cluster \
  --namespace production \
  --create-namespace \
  --timeout 10m0s \
  --wait \
  --atomic  # Rollback on failure
```

## 🧪 Como Testar

### 1. Pré-requisitos
- **Helm** instalado (`helm version`)
- **Docker Desktop** com Kubernetes habilitado (para STAGING)
- **kubectl** configurado com contextos apropriados

### 2. Verificar Contextos Kubernetes
```bash
kubectl config get-contexts
```

Deve mostrar:
- `docker-desktop` (para STAGING)  
- `prod-k8s-cluster` ou similar (para PROD)

### 3. Testar Deployment

#### DEV Environment
1. Selecione ambiente "dev" 
2. Clique "Simulate Deploy"
3. Deve mostrar: "Stack simulated successfully in dev environment"

#### STAGING Environment  
1. Selecione ambiente "staging"
2. Clique "Deploy Local" 
3. Verifica se executa helm no docker-desktop
4. Deve mostrar: "Stack deployed successfully to local Kubernetes"

#### PROD Environment
1. ⚠️ **CUIDADO**: Selecione ambiente "prod"
2. Clique "Deploy Production"
3. Verifica se executa helm no cluster de produção
4. Deve mostrar: "Stack deployed successfully to production cluster"

## 📁 Estrutura de Arquivos

```
apps/peah-be/
├── src/main/java/.../service/
│   └── DeploymentService.java          # 🆕 Estratégias de deployment
├── src/main/java/.../resource/
│   └── K8sConfigResource.java          # 🔄 Endpoint modificado  
└── src/main/resources/
    └── application.properties          # 🔄 Novas configurações

apps/peah-fe/
├── src/services/
│   ├── mockApi.ts                      # 🔄 Mock diferenciado
│   └── api.ts                          # API calls
└── src/components/
    └── StackManager.tsx                # 🔄 UI diferenciada
```

## 🚨 Considerações de Segurança

1. **Produção**: O botão PROD tem destaque visual vermelho e tooltip de aviso
2. **Contexts**: Verificar sempre se os contexts K8s estão corretos  
3. **Timeouts**: PROD tem timeout maior (10min) com rollback automático
4. **Logs**: Todos os deployments são logados com detalhes

## 🖥️ Funcionalidade "View Values"

### Botão "View Values"
Cada stack possui um botão "View Values" que permite visualizar os valores Helm gerados:

**🎯 Comportamento:**
- ✅ **Modal interativa** com valores formatados em JSON
- ✅ **Botão copy** para copiar valores para clipboard  
- ✅ **Context** claro (stack name + ambiente)
- ✅ **Syntax highlighting** com fonte monospace
- ✅ **Scroll interno** para valores extensos

**🔗 Endpoints:**
- `GET /api/config/environments/{envId}/stacks/{stackName}/values` - JSON format
- `GET /api/config/environments/{envId}/stacks/{stackName}/values.yaml` - YAML format

**💡 Para que serve:**
- Preview dos valores antes do deployment
- Debug de configurações de apps
- Validação de estruturas Helm
- Documentação de valores por ambiente

### Interface da Modal
```
┌─────────────────────────────────────────────────────────┐
│ 🖥️ Values for "database" stack (PROD)           📋 ✖️ │
├─────────────────────────────────────────────────────────┤
│  {                                                      │
│    "global": { "namespace": "prod", ... },             │
│    "databaseStack": { "enabled": true, ... },          │
│    "postgresql": { "image": {...}, "ports": [...] }    │
│  }                                                      │
├─────────────────────────────────────────────────────────┤
│ 💡 These values will be used during Helm deployment    │
│                                            [Close]     │
└─────────────────────────────────────────────────────────┘
```

## 🔐 Configurações de Autenticação por Serviço

### Sistema de Auth Individual
Cada serviço agora possui configurações de autenticação específicas baseadas em sua categoria:

#### **🗄️ Database Services (PostgreSQL, Redis)**
```json
{
  "auth": {
    "enabled": true,
    "type": "password", // password, certificate, ldap, oauth
    "username": "postgres",
    "database": "postgres", 
    "existingSecret": "postgresql-secret",
    "secretKeys": {
      "adminPassword": "postgres-password",
      "userPassword": "user-password"
    },
    "allowEmptyPassword": false
  }
}
```

#### **📊 Monitoring Services (Prometheus, Grafana)**
```json
{
  "auth": {
    "enabled": true,
    "type": "basic", // basic, oauth2, tls, proxy
    "adminUser": "admin",
    "adminPassword": "secret",
    "existingSecret": "grafana-secret",
    "autoAssignOrgRole": "Viewer",
    "allowSignUp": false
  }
}
```

#### **🤖 Automation Services (N8N)**
```json
{
  "auth": {
    "enabled": true,
    "type": "email", // email, ldap, saml
    "defaultUser": {
      "email": "admin@n8n.local",
      "firstName": "Admin",
      "lastName": "User",
      "password": "n8n-admin-password"
    },
    "jwtSecret": "jwt-secret-key",
    "enablePublicAPI": true
  }
}
```

#### **🚀 API Services (PeahDB)**
```json
{
  "auth": {
    "enabled": true,
    "type": "jwt", // jwt, oauth2, basic, api-key
    "jwt": {
      "secret": "api-jwt-secret",
      "issuer": "peahdb-api", 
      "expirationTime": "24h"
    },
    "cors": {
      "enabled": true,
      "allowedOrigins": ["*"]
    },
    "rateLimit": {
      "enabled": true,
      "requestsPerMinute": 100
    }
  }
}
```

### 🎨 Interface de Configuração Auth

#### AuthConfigEditor Component
- **Modal dedicada** para configuração de auth por serviço
- **Campos específicos** baseados na categoria do serviço 
- **Preview de secrets** que serão criados
- **Validação** de configurações por tipo de auth
- **Suporte visual** com ícones e tooltips

#### Integração no AppEditor
- **Botão "🔐 Authentication Settings"** em cada app
- **Chip de status** mostrando Auth ON/OFF
- **Preview em tempo real** das configurações
- **Salvamento automático** com as outras configurações

#### Indicadores Visuais
```
🔐 Auth ON  - Auth habilitada (chip verde)
🔓 Auth OFF - Auth desabilitada (chip outlined)
```

### 🎯 Tipos de Auth Suportados

| Categoria | Tipos Disponíveis | Ícones |
|-----------|------------------|--------|
| **Database** | Password, Certificate, LDAP, OAuth | 🔐📜🏢🔗 |
| **Monitoring** | Basic, OAuth2, TLS, Proxy | 🔐🔗📜🔄 |
| **Automation** | Email, LDAP, SAML | 📧🏢🎫 |
| **API** | JWT, OAuth2, Basic, API-Key | 🎟️🔗🔐🗝️ |

### 🔧 Como Usar

1. **Editar App**: Clique no app desejado
2. **Auth Settings**: Clique em "🔐 Authentication Settings"
3. **Enable Auth**: Ative o switch "Enable Authentication"
4. **Choose Type**: Selecione o tipo de auth apropriado
5. **Configure**: Preencha os campos específicos
6. **Save**: Clique em "Save Auth Settings"
7. **Deploy**: Use o deploy normal - auth será aplicada automaticamente

## 📋 Sistema de Manifestos K8s Defaults

### Manifestos Automáticos por Categoria
Ao criar uma nova aplicação, os manifestos Kubernetes são automaticamente definidos baseados na categoria do serviço:

#### **🗄️ Database Services** 
```yaml
✓ DEPLOYMENT (Priority 10) - Database deployment with persistence 
✓ SERVICE (Priority 20) - Internal service for database access
✓ PERSISTENT_VOLUME_CLAIM (Priority 5) - Storage [IF persistence.enabled]
✓ SECRET (Priority 1) - Database credentials [IF auth.enabled]  
○ CONFIG_MAP (Priority 15) - Database configuration files
```

#### **📊 Monitoring Services**
```yaml  
✓ SERVICE_ACCOUNT (Priority 1) - Monitoring permissions
✓ CLUSTER_ROLE (Priority 2) - Cluster-wide monitoring access
✓ DEPLOYMENT (Priority 10) - Monitoring service deployment
✓ SERVICE (Priority 20) - Service for monitoring access
○ INGRESS (Priority 30) - External dashboard access [IF ingress.enabled]
○ PERSISTENT_VOLUME_CLAIM (Priority 5) - Data retention storage [IF persistence.enabled]
○ SECRET (Priority 3) - Auth credentials [IF auth.enabled]
✓ CONFIG_MAP (Priority 8) - Monitoring configuration files
```

#### **🤖 Automation Services**
```yaml
✓ DEPLOYMENT (Priority 10) - Automation platform deployment
✓ SERVICE (Priority 20) - Internal automation service  
✓ INGRESS (Priority 30) - External automation interface
○ PERSISTENT_VOLUME_CLAIM (Priority 5) - Workflow storage [IF persistence.enabled]
✓ SECRET (Priority 1) - Authentication and encryption keys
○ CONFIG_MAP (Priority 15) - Platform configuration
```

#### **🚀 API Services** 
```yaml
✓ DEPLOYMENT (Priority 10) - API deployment (2 replicas + rolling update)
✓ SERVICE (Priority 20) - Internal API service
✓ INGRESS (Priority 30) - External API access with rate limiting
○ HPA (Priority 40) - Horizontal autoscaler [IF hpa.enabled] 
✓ SECRET (Priority 1) - API keys, JWT secrets, DB credentials
✓ CONFIG_MAP (Priority 8) - API configuration and env vars
○ SERVICE_ACCOUNT (Priority 2) - API permissions [IF serviceAccount.create]
```

### 🎨 Preview dos Manifestos na UI

#### **Accordion "📋 Kubernetes Manifests Preview"**
- **Visual Real-time**: Mostra quais manifestos serão criados 
- **Cards Coloridos**: Verde = Será criado, Cinza = Será pulado
- **Condições**: Mostra as condições para criação (ex: `auth.enabled`)
- **Descrições**: Explicação de cada manifesto
- **Contadores**: "✓ Will Create" vs "⏸ Skipped"

#### **Exemplo Visual**
```
┌─────────────────────────────────────────────────────────┐
│  📋 Kubernetes Manifests Preview                        │
├─────────────────────────────────────────────────────────┤
│  [DEPLOYMENT]     [✓ Will Create]                      │  
│  Database deployment with persistence                   │
│                                                         │
│  [SECRET]         [✓ Will Create]                      │
│  Database credentials  Condition: auth.enabled         │
│                                                         │  
│  [INGRESS]        [⏸ Skipped]                          │
│  External access   Condition: ingress.enabled          │
└─────────────────────────────────────────────────────────┘
```

### 🔧 Lógica de Criação Condicional

#### **Condições Suportadas**
- `auth.enabled` - Criar SECRET se autenticação estiver habilitada
- `persistence.enabled` - Criar PVC se persistência estiver habilitada  
- `ingress.enabled` - Criar INGRESS se acesso externo estiver habilitado
- `hpa.enabled` - Criar HPA se autoscaling estiver habilitado
- `serviceAccount.create` - Criar ServiceAccount se necessário

#### **Prioridades de Criação**
1. **Priority 1-5**: Secrets, PVCs, ServiceAccounts (base infrastructure)
2. **Priority 8-15**: ConfigMaps, Deployments (application layer)  
3. **Priority 20-30**: Services, Ingresses (networking layer)
4. **Priority 40+**: HPA, advanced features (scaling layer)

### 💡 Benefícios

✅ **Consistência**: Manifestos padronizados por categoria  
✅ **Automação**: Criação automática baseada na configuração  
✅ **Flexibilidade**: Condições permitem customização  
✅ **Visibilidade**: Preview em tempo real dos manifestos  
✅ **Best Practices**: Configurações padrão seguem boas práticas K8s

## ⚙️ Backend Integration - Java Services

### K8sManifestDefaultsService
Serviço Java equivalente ao frontend `k8sManifestDefaults.ts`:

#### **Funcionalidades Principais:**
```java
// Obter manifestos defaults para uma categoria
List<ManifestDefault> getDefaultManifestsForCategory(String category)

// Avaliar condições de criação de manifestos
boolean evaluateManifestCondition(String condition, JsonNode appConfig)

// Gerar manifestos automáticos para uma app
List<AppManifest> generateAppManifests(App app, JsonNode appConfig) 

// Obter configuração de auth default por categoria/tipo
JsonNode getDefaultAuthConfig(String category, String authType)
```

#### **Manifestos Suportados por Categoria:**
```yaml
🗄️ Database: [DEPLOYMENT, SERVICE, PVC, SECRET, CONFIG_MAP]
📊 Monitoring: [SERVICE_ACCOUNT, CLUSTER_ROLE, DEPLOYMENT, SERVICE, INGRESS, PVC, SECRET, CONFIG_MAP]  
🤖 Automation: [DEPLOYMENT, SERVICE, INGRESS, PVC, SECRET, CONFIG_MAP]
🚀 API: [DEPLOYMENT, SERVICE, INGRESS, HPA, SECRET, CONFIG_MAP, SERVICE_ACCOUNT]
🔧 Default: [DEPLOYMENT, SERVICE]
```

### K8sService Melhorias
Integração completa com o sistema de manifestos defaults:

- **generateAppManifestConfigurations()**: Usa manifestos defaults + manifestos customizados
- **shouldCreateManifest()**: Avaliação avançada de condições via `K8sManifestDefaultsService`  
- **addManifestDefaults()**: Configurações específicas por categoria de app

### Novos Endpoints REST
```http
GET /api/k8s/manifests/defaults/{category}     # Manifestos defaults por categoria
GET /api/k8s/manifests/categories              # Categorias disponíveis  
GET /api/k8s/manifests/types                   # Tipos de manifestos suportados
```

**Exemplos de uso:**
```bash
# Obter manifestos defaults para categoria database
curl GET /api/k8s/manifests/defaults/database

# Listar todas as categorias disponíveis
curl GET /api/k8s/manifests/categories

# Obter todos os tipos de manifestos
curl GET /api/k8s/manifests/types
```

## 🔄 Próximos Passos

- [x] Modal para visualização de Values na interface ✅
- [x] Sistema de Auth individual por serviço ✅
- [x] AuthConfigEditor component com suporte a múltiplos tipos ✅
- [x] Manifestos K8s defaults por categoria de serviço ✅
- [ ] Export Values como arquivo YAML
- [ ] LDAP/SAML integration para serviços que suportam
- [ ] OAuth2 flow completo para APIs
- [ ] Certificate management para TLS auth
- [ ] Adicionar logs de deployment em tempo real
- [ ] Implementar rollback automático  
- [ ] Status tracking de deployments
- [ ] Integração com GitOps (ArgoCD)
- [ ] Notificações Slack/Teams para deployments PROD 