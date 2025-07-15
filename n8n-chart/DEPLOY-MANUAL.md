# 🚀 **Deploy Manual do N8N**

**Domínio:** `n8n.lolmeida.com`  
**Namespace:** `default` (ou criar `n8n` se preferir)  
**Chart:** `./n8n-chart`  

---

## ✅ **Pré-requisitos Verificados**

### **Cluster Kubernetes:**
- ✅ **MicroK8s**: Rodando há 26 dias
- ✅ **Nginx Ingress**: Instalado e funcionando
- ✅ **Cert-Manager**: Instalado com Let's Encrypt
- ✅ **Storage**: Hostpath provisioner disponível

### **Configuração DNS:**
- ⚠️ **DNS**: Verificar se `n8n.lolmeida.com` aponta para o servidor
- ⚠️ **Firewall**: Verificar se portas 80/443 estão abertas

---

## 🔧 **Passos para Deploy Manual**

### **1. Preparar Configuração**

```bash
# Editar senhas no arquivo custom-values.yaml
vim custom-values.yaml

# Alterar estas linhas:
# n8n.auth.password: "sua-senha-segura"
# redis.auth.password: "sua-senha-redis-segura"
```

### **2. Validar Template**

```bash
# Validar se o template está correto
helm template n8n . -f custom-values.yaml --debug

# Ou testar dry-run
helm install n8n . -f custom-values.yaml --dry-run
```

### **3. Deploy no Servidor**

```bash
# Copiar arquivos para o servidor
scp -r . n8n:/tmp/n8n-chart/

# Conectar ao servidor
ssh n8n
```

### **4. Executar Deploy no Servidor**

```bash
# No servidor n8n:
cd /tmp/n8n-chart

# Deploy com Helm
microk8s helm3 install n8n . -f custom-values.yaml \
  --namespace default \
  --wait --timeout=300s

# Ou se preferir namespace separado:
# microk8s helm3 install n8n . -f custom-values.yaml \
#   --namespace n8n \
#   --create-namespace \
#   --wait --timeout=300s
```

### **5. Verificar Deploy**

```bash
# Verificar pods
microk8s kubectl get pods -l app.kubernetes.io/name=n8n

# Verificar services
microk8s kubectl get svc -l app.kubernetes.io/name=n8n

# Verificar ingress
microk8s kubectl get ingress

# Verificar certificado
microk8s kubectl get certificate
```

### **6. Verificar Logs**

```bash
# Logs do n8n
microk8s kubectl logs deployment/n8n-n8n -f

# Logs do redis
microk8s kubectl logs deployment/n8n-redis -f

# Logs do ingress (se houver problemas)
microk8s kubectl logs -n ingress daemonset/nginx-ingress-microk8s-controller
```

---

## 🌐 **Configuração DNS (Se Necessário)**

### **Verificar DNS atual:**
```bash
# Local
nslookup n8n.lolmeida.com

# No servidor
ssh n8n "nslookup n8n.lolmeida.com"
```

### **Se DNS não estiver configurado:**
```bash
# Adicionar entrada DNS ou hosts file temporário
# echo "IP_DO_SERVIDOR n8n.lolmeida.com" >> /etc/hosts
```

---

## 🔒 **Certificado SSL**

O certificado será gerado automaticamente pelo cert-manager:

```bash
# Verificar status do certificado
microk8s kubectl describe certificate n8n-tls

# Verificar issuer
microk8s kubectl get clusterissuer letsencrypt-prod
```

---

## ✅ **Verificar Funcionamento**

### **1. Testar Conectividade**
```bash
# Testar HTTP (deve redirecionar para HTTPS)
curl -I http://n8n.lolmeida.com

# Testar HTTPS
curl -I https://n8n.lolmeida.com

# Testar endpoint específico
curl -f https://n8n.lolmeida.com/healthz
```

### **2. Acessar Interface**
```bash
# Abrir no browser
open https://n8n.lolmeida.com
```

### **3. Login**
```
Username: admin
Password: (conforme configurado em custom-values.yaml)
```

---

## 🛠️ **Troubleshooting**

### **Pod não inicia:**
```bash
microk8s kubectl describe pod <pod-name>
microk8s kubectl logs <pod-name>
```

### **Ingress não funciona:**
```bash
microk8s kubectl describe ingress n8n
microk8s kubectl get endpoints
```

### **Certificado não é gerado:**
```bash
microk8s kubectl describe certificate n8n-tls
microk8s kubectl logs -n cert-manager deployment/cert-manager
```

### **Problemas de storage:**
```bash
microk8s kubectl get pvc
microk8s kubectl describe pvc <pvc-name>
```

---

## 🔄 **Comandos Úteis**

### **Atualizar deployment:**
```bash
microk8s helm3 upgrade n8n . -f custom-values.yaml
```

### **Reiniciar pods:**
```bash
microk8s kubectl rollout restart deployment/n8n-n8n
microk8s kubectl rollout restart deployment/n8n-redis
```

### **Desinstalar:**
```bash
microk8s helm3 uninstall n8n
microk8s kubectl delete pvc -l app.kubernetes.io/name=n8n
```

---

## 📋 **Checklist Final**

- [ ] Arquivo `custom-values.yaml` configurado com senhas seguras
- [ ] DNS para `n8n.lolmeida.com` configurado
- [ ] Templates validados com `helm template`
- [ ] Deploy executado com `helm install`
- [ ] Pods rodando: `kubectl get pods`
- [ ] Services criados: `kubectl get svc`
- [ ] Ingress configurado: `kubectl get ingress`
- [ ] Certificado válido: `kubectl get certificate`
- [ ] Acesso via browser: `https://n8n.lolmeida.com`
- [ ] Login funcionando com credenciais configuradas

---

**✨ Deploy completo! N8N rodando em `https://n8n.lolmeida.com`** 