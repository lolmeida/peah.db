# 📊 **Stack de Monitoramento - Prometheus + Grafana**

## 🎯 **Visão Geral**

A stack de monitoramento inclui:
- **Prometheus**: Coleta de métricas e alertas
- **Grafana**: Visualização e dashboards
- **Ingress**: Acesso externo com SSL

## 🔧 **Configuração**

### **1. Habilitar Monitoramento**

No arquivo `custom-values.yaml`:

```yaml
monitoring:
  prometheus:
    enabled: true
  grafana:
    enabled: true
  ingress:
    enabled: true
```

### **2. Configurar Senhas**

⚠️ **IMPORTANTE**: Altere as senhas antes do deploy!

```yaml
monitoring:
  grafana:
    auth:
      adminUser: "admin"
      adminPassword: "sua-senha-grafana-segura"  # ALTERE ISSO!
```

### **3. Configurar Domínios**

```yaml
monitoring:
  grafana:
    config:
      rootUrl: "https://grafana.lolmeida.com"
  
  ingress:
    hosts:
      - host: grafana.lolmeida.com
        paths:
          - path: /
            pathType: Prefix
            service: grafana
      - host: prometheus.lolmeida.com
        paths:
          - path: /
            pathType: Prefix
            service: prometheus
```

## 📊 **Dashboards Pré-configurados**

### **🎯 5 Dashboards Automáticos:**

1. **🎯 N8N Overview** (`n8n-overview.json`)
   - ✅ Status do serviço N8N
   - ⏱️ Tempo de resposta
   - 📊 Métricas de performance
   - 🔄 Workflow executions

2. **🐘 PostgreSQL Monitoring** (`postgresql-dashboard.json`)
   - ✅ Status da base de dados
   - 🔗 Conexões ativas
   - 📁 Tamanho da base de dados
   - 🚀 Performance de queries

3. **🐬 MySQL Monitoring** (`mysql-dashboard.json`)
   - ✅ Status da base de dados
   - 🔗 Conexões ativas
   - 📊 Taxa de queries por segundo
   - 📈 Performance metrics

4. **🔴 Redis Monitoring** (`redis-dashboard.json`)
   - ✅ Status do cache
   - 💾 Uso de memória
   - 🎯 Hit rate do cache
   - ⚡ Comandos por segundo

5. **☸️ Kubernetes Cluster** (`kubernetes-dashboard.json`)
   - ✅ Status dos pods
   - 🔧 Uso de CPU
   - 💾 Uso de memória
   - 🌐 Recursos do cluster

### **🏠 Dashboard Padrão**
- O dashboard **N8N Overview** é definido como homepage
- Todos os dashboards ficam organizados na pasta **"N8N Stack"**
- Atualização automática a cada 30 segundos

## 🚀 **Deploy**

### **1. Configurar DNS**

Adicione entradas DNS ou `/etc/hosts`:
```bash
# Exemplo para /etc/hosts
IP_DO_SERVIDOR grafana.lolmeida.com
IP_DO_SERVIDOR prometheus.lolmeida.com
```

### **2. Executar Deploy**

```bash
# Atualizar senhas no custom-values.yaml
vim custom-values.yaml

# Deploy/upgrade
./deploy-n8n.sh
```

## 📊 **Recursos Deployados**

### **Prometheus**
- **URL**: https://prometheus.lolmeida.com
- **Porta**: 9090
- **Storage**: 10Gi
- **Retenção**: 15 dias
- **Função**: Coleta de métricas

### **Grafana**
- **URL**: https://grafana.lolmeida.com
- **Porta**: 3000
- **Storage**: 5Gi
- **Login**: admin / (senha configurada)
- **Função**: Visualização e dashboards

### **Persistent Volumes**
- `n8n-prometheus-pvc` (10Gi)
- `n8n-grafana-pvc` (5Gi)

### **Certificates**
- `monitoring-tls` (Let's Encrypt)

## 📈 **Métricas Coletadas**

### **N8N**
- Status da aplicação
- Execuções de workflows
- Tempo de resposta
- Uso de recursos

### **Bancos de Dados**
- PostgreSQL: Status e conexões
- MySQL: Status e conexões
- Redis: Status e uso de memória

### **Kubernetes**
- Status dos pods
- Uso de CPU e memória
- Volumes persistentes
- Networking

## 🎨 **Dashboards Incluídos**

### **1. N8N Overview**
- Status geral do N8N
- Execuções de workflows
- Conexões com bancos

### **2. Kubernetes Overview**
- Status dos pods
- Uso de CPU e memória
- Recursos do cluster

### **3. Dashboards Personalizados**
Você pode criar dashboards personalizados no Grafana.

## 🔍 **Acesso às Interfaces**

### **Grafana**
```bash
# URL
https://grafana.lolmeida.com

# Login
User: admin
Pass: (conforme configurado)
```

### **Prometheus**
```bash
# URL
https://prometheus.lolmeida.com

# Targets
/targets - Ver todos os serviços monitorados
/graph - Consultas manuais
```

## 🛠️ **Troubleshooting**

### **Verificar Deployments**
```bash
# Status dos pods
kubectl get pods -l app.kubernetes.io/component=prometheus
kubectl get pods -l app.kubernetes.io/component=grafana

# Logs
kubectl logs deployment/n8n-prometheus -f
kubectl logs deployment/n8n-grafana -f
```

### **Verificar Serviços**
```bash
# Services
kubectl get svc -l app.kubernetes.io/name=n8n

# Ingress
kubectl get ingress
```

### **Verificar Métricas**
```bash
# Port-forward para teste local
kubectl port-forward svc/n8n-prometheus 9090:9090
kubectl port-forward svc/n8n-grafana 3000:3000

# Testar métricas
curl http://localhost:9090/metrics
curl http://localhost:3000/api/health
```

## 🔔 **Alertas (Configuração Futura)**

### **Alertas Recomendados**
- N8N indisponível
- Workflows falhando
- Uso alto de CPU/memória
- Espaço em disco baixo
- Certificados expirando

### **Canais de Notificação**
- Email
- Slack
- Discord
- Webhook

## 📊 **Queries Prometheus Úteis**

### **Status do N8N**
```promql
up{job="n8n"}
```

### **Uso de CPU**
```promql
rate(container_cpu_usage_seconds_total[5m])
```

### **Uso de Memória**
```promql
container_memory_usage_bytes
```

### **Conexões de Banco**
```promql
up{job="postgres"}
up{job="mysql"}
up{job="redis"}
```

## 🎯 **Próximos Passos**

### **Melhorias Futuras**
1. **Alertmanager**: Configurar alertas avançados
2. **Loki**: Logs centralizados
3. **Jaeger**: Distributed tracing
4. **Exporters**: Métricas de sistema (node-exporter)

### **Dashboards Adicionais**
1. **Database Performance**: Métricas detalhadas de DB
2. **N8N Workflows**: Análise de execuções
3. **Infrastructure**: Métricas de sistema

## 📋 **Checklist de Deploy**

- [ ] Senhas alteradas no `custom-values.yaml`
- [ ] DNS configurado para `grafana.lolmeida.com`
- [ ] DNS configurado para `prometheus.lolmeida.com`
- [ ] Deploy executado com `./deploy-n8n.sh`
- [ ] Pods running: `kubectl get pods`
- [ ] Ingress configurado: `kubectl get ingress`
- [ ] Certificados válidos: `kubectl get certificate`
- [ ] Acesso via browser: `https://grafana.lolmeida.com`
- [ ] Login funcionando no Grafana
- [ ] Dashboards carregados
- [ ] Métricas sendo coletadas

---

**🎉 Stack de monitoramento profissional para N8N pronta!**

- **Grafana**: https://grafana.lolmeida.com
- **Prometheus**: https://prometheus.lolmeida.com
- **N8N**: https://n8n.lolmeida.com 