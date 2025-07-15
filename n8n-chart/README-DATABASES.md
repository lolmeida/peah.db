# 🗄️ **N8N com Bancos de Dados**

## 📊 **Bancos Disponíveis**

O chart N8N agora inclui suporte para múltiplos bancos de dados:

### **🐘 PostgreSQL**
- **Imagem**: `postgres:15-alpine`
- **Porta**: 5432
- **Banco**: `n8n`
- **Usuário**: `n8n`
- **Storage**: 5Gi

### **🐬 MySQL**
- **Imagem**: `mysql:8.0`
- **Porta**: 3306
- **Banco**: `n8n`
- **Usuário**: `n8n`
- **Storage**: 5Gi

### **🔴 Redis**
- **Imagem**: `redis:7-alpine`
- **Porta**: 6379
- **Uso**: Cache e queues
- **Storage**: 2Gi

## 🔧 **Configuração**

### **Habilitar/Desabilitar Bancos:**
```yaml
postgresql:
  enabled: true    # Use PostgreSQL como banco principal

mysql:
  enabled: true    # Use MySQL como banco principal

redis:
  enabled: true    # Use Redis para cache/queues
```

### **Senhas Seguras (OBRIGATÓRIO):**
```yaml
postgresql:
  auth:
    password: "sua-senha-postgres-segura"

mysql:
  auth:
    password: "sua-senha-mysql-segura"
    rootPassword: "sua-senha-root-mysql-segura"

redis:
  auth:
    password: "sua-senha-redis-segura"
```

## 🎯 **Prioridade de Bancos**

O N8N usará os bancos na seguinte ordem:
1. **PostgreSQL** (se habilitado)
2. **MySQL** (se PostgreSQL desabilitado)
3. **SQLite** (padrão, se ambos desabilitados)

## 🚀 **Deploy com Bancos**

### **1. Editar Configuração:**
```bash
vim custom-values.yaml
```

### **2. Deploy/Upgrade do N8N:**
```bash
# Instala ou atualiza automaticamente
./deploy-n8n.sh
```

O script detecta automaticamente se é uma primeira instalação ou um upgrade.

### **3. Verificar Deployment:**
```bash
ssh n8n "microk8s kubectl get pods -l app.kubernetes.io/name=n8n"
```

## 📊 **Recursos Criados**

### **Deployments:**
- `n8n-n8n` - Aplicação N8N
- `n8n-postgres` - PostgreSQL (se habilitado)
- `n8n-mysql` - MySQL (se habilitado)
- `n8n-redis` - Redis (se habilitado)

### **Services:**
- `n8n-n8n:5678` - N8N Web Interface
- `n8n-postgres:5432` - PostgreSQL
- `n8n-mysql:3306` - MySQL
- `n8n-redis:6379` - Redis

### **PVCs:**
- `n8n-n8n-pvc` - Dados do N8N (5Gi)
- `n8n-postgres-pvc` - Dados PostgreSQL (5Gi)
- `n8n-mysql-pvc` - Dados MySQL (5Gi)
- `n8n-redis-pvc` - Dados Redis (2Gi)

## 🔐 **Acesso aos Bancos**

### **PostgreSQL:**
```bash
# Port-forward
kubectl port-forward svc/n8n-postgres 5432:5432

# Conectar
psql -h localhost -U n8n -d n8n
```

### **MySQL:**
```bash
# Port-forward
kubectl port-forward svc/n8n-mysql 3306:3306

# Conectar
mysql -h localhost -u n8n -p n8n
```

### **Redis:**
```bash
# Port-forward
kubectl port-forward svc/n8n-redis 6379:6379

# Conectar
redis-cli -h localhost -p 6379
```

## 🛠️ **Troubleshooting**

### **Verificar Logs:**
```bash
# PostgreSQL
kubectl logs deployment/n8n-postgres -f

# MySQL
kubectl logs deployment/n8n-mysql -f

# Redis
kubectl logs deployment/n8n-redis -f
```

### **Verificar Conexões:**
```bash
# Status dos bancos
kubectl get pods -l app.kubernetes.io/name=n8n

# Verificar services
kubectl get svc -l app.kubernetes.io/name=n8n
```

### **Problemas Comuns:**

1. **Pod não inicia**: Verificar logs e senhas
2. **Conexão falha**: Verificar services e endpoints
3. **Storage**: Verificar PVCs e storage class

## 📈 **Performance**

### **Recursos Recomendados:**

**PostgreSQL:**
- CPU: 250m-500m
- Memory: 256Mi-512Mi
- Storage: 5Gi+

**MySQL:**
- CPU: 250m-500m
- Memory: 256Mi-512Mi
- Storage: 5Gi+

**Redis:**
- CPU: 100m-250m
- Memory: 128Mi-256Mi
- Storage: 2Gi+

## 🔄 **Backup e Restore**

### **PostgreSQL:**
```bash
# Backup
kubectl exec deployment/n8n-postgres -- pg_dump -U n8n n8n > backup.sql

# Restore
kubectl exec -i deployment/n8n-postgres -- psql -U n8n -d n8n < backup.sql
```

### **MySQL:**
```bash
# Backup
kubectl exec deployment/n8n-mysql -- mysqldump -u n8n -p n8n > backup.sql

# Restore
kubectl exec -i deployment/n8n-mysql -- mysql -u n8n -p n8n < backup.sql
```

---

**✨ N8N com bancos de dados robustos para produção! 🚀** 