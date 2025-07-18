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
- **MySQL/PostgreSQL** - Banco de dados relacional
- **Flyway** - Migrações de schema
- **MapStruct** - Mapeamento de DTOs
- **Lombok** - Redução de boilerplate
- **Bean Validation** - Validação declarativa
- **Docker** - Containerização
- **Kubernetes & Helm** - Orquestração
- **GitHub Actions** - CI/CD

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

### 📋 Logs e Analytics

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/logs/recent` | Logs recentes |
| `GET` | `/logs/statistics` | Estatísticas de uso |
| `GET` | `/logs/dashboard` | Dashboard completo |
| `DELETE` | `/logs/clear` | Limpa logs |
| `GET` | `/logs/export` | Exporta logs em JSON |

### 🔍 Saúde e Documentação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/q/health` | Health check padrão |
| `GET` | `/q/swagger-ui` | Documentação Swagger |
| `GET` | `/q/openapi` | Especificação OpenAPI |

## 🧪 Exemplos de Uso

### Criar Usuário

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "novo_usuario",
    "email": "novo@example.com",
    "passwordHash": "$2a$10$exemplo123456789"
  }'
```

### Atualizar Parcialmente

```bash
curl -X PATCH http://localhost:8080/users/1 \
  -H "Content-Type: application/json" \
  -d '{"email": "novo_email@example.com"}'
```

### Ver Headers de Monitoramento

```bash
curl -i http://localhost:8080/users/1
```

**Resposta:**
```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Request-ID: req_abc123
X-Device-Type: Desktop
X-Browser: Chrome 138.0.0.0
X-OS: macOS 10.15.7
X-User-Agent: Mozilla/5.0 (Macintosh...)
X-Response-Time: 15ms
X-IP: 127.0.0.1
X-Timestamp: 2025-07-18T02:42:43.123
```

### Monitoramento com Diferentes Devices

```bash
# Simular iPhone
curl -H "User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 14_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.2 Mobile/15E148 Safari/604.1" \
  http://localhost:8080/users/1

# Simular Android
curl -H "User-Agent: Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Mobile Safari/537.36" \
  http://localhost:8080/users/2
```

### Dashboard de Monitoramento

```bash
curl -s http://localhost:8080/logs/dashboard | jq .
```

## 📊 Logs Automáticos

Todas as requisições geram logs estruturados:

```
🔍 Request: GET /users/1 from 127.0.0.1 (Chrome) - macOS 10.15.7 Desktop [req_abc123]
📤 Response: GET /users/1 -> 200 (15ms) [req_abc123]
🔍 AUDIT - Request Details: ID=req_abc123, Method=GET, URI=/users/1, IP=127.0.0.1, UserAgent=Mozilla/5.0..., Browser=Chrome 138.0.0.0, OS=macOS 10.15.7, Device=Desktop, Status=200, Duration=15ms
⚡ PERFORMANCE - EXCELLENT: GET /users/1 took 15ms | IP=127.0.0.1, Device=Desktop
📈 USAGE - Browser: Chrome, OS: macOS 10.15.7, Device: Desktop, Language: pt-PT
```

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Executar testes específicos
./mvnw test -Dtest=UserResourceTest

# Executar com coverage
./mvnw test jacoco:report
```

## 🏗️ Build e Deploy

### Build Local

```bash
# Compilar aplicação
./mvnw clean package

# Build nativo (GraalVM)
./mvnw package -Pnative

# Build Docker
docker build -f src/main/docker/Dockerfile.jvm -t peah-db .
```

### Deploy Kubernetes

```bash
# Deploy com Helm
helm install peah-db ./k8s -n lolmeida

# Verificar deploy
kubectl get pods -n lolmeida
kubectl get svc -n lolmeida
```

## 🔧 Configuração

### Variáveis de Ambiente

```env
# Database
QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://localhost:3306/peahdb
QUARKUS_DATASOURCE_USERNAME=user
QUARKUS_DATASOURCE_PASSWORD=password

# Logging
QUARKUS_LOG_LEVEL=INFO
QUARKUS_LOG_CONSOLE_FORMAT=%d{HH:mm:ss} %-5p [%c{2.}] (%t) %s%e%n

# Monitoring
QUARKUS_SMALLRYE_OPENTRACING_ENABLED=true
```

### Perfis de Configuração

- **dev**: Desenvolvimento com H2/MySQL e hot-reload
- **prod**: Produção com PostgreSQL e otimizações
- **test**: Testes com H2 em memória

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

## 📝 Próximos Passos

- [ ] Autenticação JWT
- [ ] Rate limiting
- [ ] Cache Redis
- [ ] Metrics com Prometheus
- [ ] Alertas automatizados
- [ ] Backup automatizado
- [ ] Testes de carga
- [ ] Documentação OpenAPI detalhada

## 🤝 Contribuição

1. Fork o repositório
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🚀 Recursos Adicionais

- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Health Check**: http://localhost:8080/q/health
- **Dashboard**: http://localhost:8080/logs/dashboard
- **OpenAPI Spec**: http://localhost:8080/q/openapi
- **Dev UI**: http://localhost:8080/q/dev (apenas em dev mode)

---

**Desenvolvido com ❤️ usando Quarkus e Java 21**