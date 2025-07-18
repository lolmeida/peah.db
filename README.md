# peah.db API

API REST moderna construída com Quarkus, demonstrando uma arquitetura cloud-native com sistema avançado de monitoramento e auditoria.

## 🚀 Funcionalidades Principais

### 🔐 API de Usuários
- **CRUD completo** com validação Bean Validation
- **Endpoints RESTful** (POST, GET, PUT, PATCH, DELETE)
- **MapStruct** para mapeamento DTO ↔ Entity
- **Hibernate ORM** com Panache
- **Flyway** para migrações de banco

### 📊 Sistema de Monitoramento Avançado
- **Auditoria automática** de todas as requisições
- **Detecção de dispositivos** (Mobile, Desktop, Tablet)
- **Headers automáticos** com informações do cliente
- **Performance analytics** com métricas de tempo
- **Logs estruturados** com emojis e cores
- **Dashboard de monitoramento** em tempo real

### 🔍 Detecção de Dispositivos
- **Browser detection** (Chrome, Firefox, Safari, Edge, Opera)
- **OS detection** (macOS, Windows, Linux, iOS, Android)
- **Device type** (Desktop, Mobile, Tablet, Bot)
- **IP extraction** com fallback para desenvolvimento local
- **User-Agent parsing** completo

### 📋 Headers Automáticos
Todas as respostas incluem headers informativos:
- `X-Request-ID`: ID único da requisição
- `X-Device-Type`: Tipo de dispositivo
- `X-Browser`: Browser e versão
- `X-OS`: Sistema operacional
- `X-User-Agent`: User-Agent completo
- `X-Response-Time`: Tempo de resposta
- `X-IP`: Endereço IP do cliente
- `X-Timestamp`: Timestamp da resposta

## 🛠️ Tecnologias Utilizadas

- **Java 21+** - Linguagem principal
- **Quarkus 3.24.3** - Framework supersônico
- **Maven 3.9+** - Gerenciamento de dependências
- **PostgreSQL** - Banco de dados de produção
- **MySQL** - Banco de dados de desenvolvimento (opcional)
- **Flyway** - Migrações de schema
- **MapStruct** - Mapeamento de DTOs
- **Lombok** - Redução de boilerplate
- **Bean Validation** - Validação declarativa
- **Docker** - Containerização
- **Kubernetes & Helm** - Orquestração
- **GitHub Actions** - CI/CD

## 🌐 Aplicação em Produção

### 📍 URLs de Produção
- **API Base**: https://peah-db.lolmeida.com
- **Swagger UI**: https://peah-db.lolmeida.com/api-docs/
- **Health Check**: https://peah-db.lolmeida.com/q/health
- **Metrics**: https://peah-db.lolmeida.com/q/metrics
- **Dashboard**: https://peah-db.lolmeida.com/logs/dashboard
- **OpenAPI Spec**: https://peah-db.lolmeida.com/q/openapi

### 🔧 Configuração de Produção
- **Database**: PostgreSQL no cluster Kubernetes
- **Namespace**: `lolmeida`
- **Deployment**: Helm chart com configuração dinâmica
- **Profile**: `prod` (ativado via `QUARKUS_PROFILE=prod`)
- **SSL**: Certificado automático via Let's Encrypt
- **Ingress**: Nginx com redirecionamento HTTPS

## 📁 Estrutura do Projeto

```
src/main/java/com/lolmeida/peahdb/
├── config/              # Configurações
├── dto/                 # Data Transfer Objects
│   ├── audit/          # DTOs de auditoria
│   ├── mapper/         # MapStruct mappers
│   ├── request/        # Request DTOs
│   └── response/       # Response DTOs
├── entity/             # Entidades JPA
├── health/             # Health checks
├── interceptor/        # Interceptadores JAX-RS
├── repository/         # Repositórios Panache
├── resource/           # Endpoints REST
├── service/            # Serviços de negócio
└── util/               # Utilitários

k8s/                     # Kubernetes & Helm
├── templates/           # Templates Helm
│   ├── deployment.yaml # Deployment da aplicação
│   ├── service.yaml    # Service do Kubernetes
│   ├── ingress.yaml    # Ingress para HTTPS
│   └── secret.yaml     # Secret para credenciais
├── values.yaml         # Valores de configuração
└── Chart.yaml          # Metadata do chart
```

## 🔧 Pré-requisitos

- **JDK 21+** (Java Development Kit)
- **Docker** (para banco de dados)
- **Maven 3.9+** (gerenciamento de dependências)
- **kubectl** (para Kubernetes)
- **helm** (para deploy)

## 🚀 Como Executar

### 1. Desenvolvimento Local

```bash
# Inicia a aplicação com hot-reload
./mvnw quarkus:dev
```

A aplicação usará **Testcontainers** para MySQL automaticamente.

### 2. Com Docker Compose

```bash
# Inicia MySQL local
docker run --rm --name peah-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=peahdb \
  -p 3306:3306 mysql:8.0

# Inicia a aplicação
./mvnw quarkus:dev
```

### 3. Com PostgreSQL

```bash
# Inicia PostgreSQL local
docker run --rm --name peah-postgres \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=peahdb \
  -p 5432:5432 postgres:15

# Configura para usar PostgreSQL
./mvnw quarkus:dev -Dquarkus.profile=prod
```

## ☸️ Deploy para Kubernetes

### 🛠️ Configuração do Helm

O projeto inclui um chart Helm completo para deploy em Kubernetes:

```yaml
# k8s/values.yaml
database:
  username: "n8n"
  password: "postgres-n8n-changeme123"
  url: "jdbc:postgresql://n8n-postgres:5432/n8n"
  host: "n8n-postgres"
  port: "5432"
  name: "n8n"

quarkus:
  profile: "prod"

image:
  repository: "lolmeida/peah-db"
  tag: "latest"
  pullPolicy: "Always"
```

### 🚀 Deploy Manual

```bash
# Executa script de deploy
./deploy.sh

# Ou deploy manual com Helm
helm upgrade --install peah-db ./k8s \
  --namespace lolmeida \
  --create-namespace \
  --wait --timeout=300s
```

### 📋 Variáveis de Ambiente

O deployment usa as seguintes variáveis de ambiente:

```bash
# Base de dados
DB_HOST=n8n-postgres
DB_PORT=5432  
DB_NAME=n8n
DB_USERNAME=n8n
DB_PASSWORD=postgres-n8n-changeme123
DB_URL=jdbc:postgresql://n8n-postgres:5432/n8n

# Quarkus
QUARKUS_PROFILE=prod
```

### 🔍 Verificação do Deploy

```bash
# Verifica status dos pods
kubectl get pods -n lolmeida -l app.kubernetes.io/name=k8s

# Verifica logs
kubectl logs -n lolmeida -l app.kubernetes.io/name=k8s

# Testa health check
curl https://peah-db.lolmeida.com/q/health

# Acessa Swagger UI
curl https://peah-db.lolmeida.com/api-docs/
```

## 📡 Endpoints da API

### 🔐 Usuários

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/users` | Lista todos os usuários |
| `GET` | `/users/{id}` | Busca usuário por ID |
| `POST` | `/users` | Cria novo usuário |
| `PUT` | `/users/{id}` | Atualiza usuário completo |
| `PATCH` | `/users/{id}` | Atualiza usuário parcial |
| `DELETE` | `/users/{id}` | Remove usuário |

### 🔐 Usuários com Metadata

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/users` | Lista usuários com metadata |
| `GET` | `/api/users/{id}` | Busca usuário com metadata |

### 📊 Monitoramento

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/monitoring/health` | Health check com request info |
| `GET` | `/monitoring/request-info` | Informações completas da requisição |
| `GET` | `/monitoring/request-summary` | Resumo da requisição |
| `GET` | `/monitoring/headers` | Headers da requisição |
| `GET` | `/monitoring/uri-info` | Informações do URI |

### 📋 Logs e Analytics

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/logs` | Todos os logs de requisições |
| `GET` | `/logs/recent?limit=50` | Logs mais recentes |
| `GET` | `/logs/slow?threshold=1000` | Requisições lentas |
| `GET` | `/logs/status/{status}` | Logs por status HTTP |
| `GET` | `/logs/endpoint/{endpoint}` | Logs por endpoint |
| `GET` | `/logs/statistics` | Estatísticas de uso |
| `GET` | `/logs/performance` | Métricas de performance |
| `GET` | `/logs/dashboard` | Dashboard completo |
| `DELETE` | `/logs/clear` | Limpa todos os logs |

### 🏥 Health Checks

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/q/health` | Health check geral |
| `GET` | `/q/health/live` | Liveness probe |
| `GET` | `/q/health/ready` | Readiness probe |

### 📊 Métricas

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/q/metrics` | Métricas do Quarkus (formato Prometheus) |

## 🔧 Configuração

### 📋 Profiles Disponíveis

| Profile | Descrição | Base de Dados |
|---------|-----------|---------------|
| `dev` | Desenvolvimento local | MySQL/Testcontainers |
| `prod` | Produção | PostgreSQL |
| `test` | Testes automatizados | H2 em memória |

### 🗄️ Configuração de Base de Dados

#### Development (MySQL)
```properties
quarkus.datasource.username=root
quarkus.datasource.password=admin
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/peahdb_dev
```

#### Production (PostgreSQL)
```properties
%prod.quarkus.datasource.db-kind=postgresql
%prod.quarkus.datasource.username=${DB_USERNAME}
%prod.quarkus.datasource.password=${DB_PASSWORD}
%prod.quarkus.datasource.jdbc.url=${DB_URL}
```

### 🎯 Configuração Avançada

```properties
# Swagger UI
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/api-docs

# Health checks
quarkus.smallrye-health.ui.always-include=true

# Flyway
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=classpath:db/migration
```

## 🔐 Dados de Exemplo

A aplicação inclui 8 usuários de exemplo:

1. **john_doe** - john.doe@email.com
2. **jane_smith** - jane.smith@email.com
3. **bob_wilson** - bob.wilson@email.com
4. **alice_johnson** - alice.johnson@email.com
5. **charlie_brown** - charlie.brown@email.com
6. **diana_prince** - diana.prince@email.com
7. **test_user** - test@example.com
8. **admin_user** - admin@peahdb.com

## 🚨 Troubleshooting

### 🔍 Problemas Comuns

#### Pod em CreateContainerConfigError
```bash
# Verifica se o secret existe
kubectl get secrets -n lolmeida | grep peah-db

# Verifica o deployment
kubectl describe deployment peah-db-k8s -n lolmeida
```

#### Conexão com Base de Dados
```bash
# Verifica se o PostgreSQL está acessível
kubectl exec -n lolmeida deployment/peah-db-k8s -- \
  curl -s http://localhost:8080/q/health
```

#### Swagger UI não carrega
```bash
# Verifica se o perfil prod está ativo
kubectl exec -n lolmeida deployment/peah-db-k8s -- \
  printenv | grep QUARKUS_PROFILE
```

### 📋 Logs de Debug

```bash
# Logs do pod
kubectl logs -n lolmeida -l app.kubernetes.io/name=k8s -f

# Logs do deployment
kubectl describe deployment peah-db-k8s -n lolmeida

# Eventos do namespace
kubectl get events -n lolmeida --sort-by='.lastTimestamp'
```

## 📝 Próximos Passos

### 🧪 Testes e Qualidade
- [x] **Testes unitários completos do UserService** (28 testes)
- [x] **Testes unitários do AuditService** (34 testes)
- [x] **Testes unitários do RequestLogService** (19 testes)
- [x] **Testes de repositório com Testcontainers** (29 testes)
- [x] **Testes de integração REST** (UserResource - 15 testes)
- [x] **Testes de MonitoringResource** (10 testes)
- [x] **Cobertura de cláusulas if/else** (100%)
- [x] **Testes de cenários de conflito** (uniqueness validation)
- [x] **Testes de campos nulos** (partial updates)
- [x] **Total de 141 testes** com build verde
- [ ] **Testes de performance** (JMeter/Gatling)
- [ ] **Testes de carga** (stress testing)
- [ ] **Mutation testing** (PIT)
- [ ] **Contract testing** (Pact)
- [ ] **Cobertura de código** (meta: >80%)

### 🔒 Segurança e Autenticação
- [ ] Autenticação JWT
- [ ] Rate limiting
- [ ] Validação de entrada avançada
- [ ] Audit logs de segurança

### 📊 Observabilidade
- [ ] Cache Redis
- [ ] Metrics com Prometheus
- [ ] Alertas automatizados
- [ ] Distributed tracing

### 🚀 Infraestrutura
- [x] **Deploy automatizado com Helm**
- [x] **Configuração dinâmica via values.yaml**
- [x] **Secrets management no Kubernetes**
- [x] **Health checks avançados**
- [x] **Ingress com SSL/TLS**
- [x] **Swagger UI em produção**
- [ ] Backup automatizado
- [ ] Blue-green deployment
- [ ] Scaling automático (HPA)

## 🤝 Contribuição

1. Fork o repositório
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🚀 Recursos Adicionais

### 🌐 Produção
- **Swagger UI**: https://peah-db.lolmeida.com/api-docs/
- **Health Check**: https://peah-db.lolmeida.com/q/health
- **Metrics**: https://peah-db.lolmeida.com/q/metrics
- **Dashboard**: https://peah-db.lolmeida.com/logs/dashboard
- **OpenAPI Spec**: https://peah-db.lolmeida.com/q/openapi

### 🖥️ Desenvolvimento Local
- **Swagger UI**: http://localhost:8080/api-docs/
- **Health Check**: http://localhost:8080/q/health
- **Metrics**: http://localhost:8080/q/metrics
- **Dashboard**: http://localhost:8080/logs/dashboard
- **OpenAPI Spec**: http://localhost:8080/q/openapi
- **Dev UI**: http://localhost:8080/q/dev (apenas em dev mode)

---

**Desenvolvido com ❤️ usando Quarkus e Java 21**

*Deploy em produção: https://peah-db.lolmeida.com*