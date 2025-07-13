# Deployment Setup

Este documento descreve como configurar o deploy automático da aplicação.

## GitHub Secrets Necessários

Configure os seguintes secrets no repositório GitHub (Settings > Secrets and variables > Actions):

### Docker Hub
- `DOCKER_USERNAME`: seu username do Docker Hub
- `DOCKER_PASSWORD`: sua senha ou token do Docker Hub

### SSH e VPS
- `SSH_PRIVATE_KEY`: chave SSH privada para acesso à VPS
- `VPS_HOST`: IP da VPS (31.97.53.64)
- `VPS_USER`: usuário SSH (provavelmente 'root')

### Kubernetes
- `KUBECONFIG`: configuração do kubectl em base64

Para obter o KUBECONFIG:
```bash
ssh n8n "microk8s config" | base64
```

O valor já foi obtido e está disponível para configuração.

## Como funciona

1. **Push/Merge para main** → Triggera o workflow
2. **Tests** → Executa testes Maven
3. **Build & Push** → Gera imagem Docker com Jib e faz push
4. **Deploy** → Atualiza o deployment no Kubernetes via Helm

## URLs da aplicação

- **Produção**: https://peah-db.lolmeida.com
- **Health Check**: https://peah-db.lolmeida.com/q/health
- **API Docs**: https://peah-db.lolmeida.com/api-docs

## Comandos manuais

### Build e push local
```bash
mvn clean package -Dquarkus.container-image.push=true
```

### Deploy manual
```bash
helm upgrade --install peah-db ./k8s --namespace lolmeida --create-namespace
```

### Verificar deployment
```bash
kubectl get pods -n lolmeida
kubectl logs -f deployment/peah-db-k8s -n lolmeida
```