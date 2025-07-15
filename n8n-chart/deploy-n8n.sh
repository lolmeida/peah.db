#!/bin/bash

# N8N Deploy/Upgrade Script
# Instala ou atualiza n8n em n8n.lolmeida.com

set -e

NAMESPACE="default"
RELEASE_NAME="n8n"
DOMAIN="n8n.lolmeida.com"

echo "🚀 Deploying/Upgrading N8N to $DOMAIN..."

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if custom-values.yaml exists
if [ ! -f "custom-values.yaml" ]; then
    echo -e "${RED}❌ custom-values.yaml não encontrado!${NC}"
    echo "Execute: cp values.yaml custom-values.yaml e edite as senhas"
    exit 1
fi

echo -e "${YELLOW}⚠️  Verificando DNS...${NC}"
if ! nslookup $DOMAIN > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  DNS para $DOMAIN não configurado${NC}"
    echo "Configure o DNS ou adicione ao /etc/hosts"
    read -p "Continuar assim mesmo? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo -e "${YELLOW}📋 Validando template...${NC}"
helm template $RELEASE_NAME . -f custom-values.yaml > /dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Template válido${NC}"
else
    echo -e "${RED}❌ Template inválido${NC}"
    exit 1
fi

echo -e "${YELLOW}📦 Copiando arquivos para servidor...${NC}"
ssh n8n "mkdir -p /tmp/n8n-chart"
scp -r . n8n:/tmp/n8n-chart/

echo -e "${YELLOW}🚀 Executando deploy/upgrade no servidor...${NC}"
ssh n8n << 'EOF'
set -e

cd /tmp/n8n-chart

# Verificar se o release já existe
RELEASE_EXISTS=$(microk8s helm3 list -q | grep -c "^n8n$" || echo "0")

if [ "$RELEASE_EXISTS" = "0" ]; then
    echo "🔧 Executando helm install (primeira instalação)..."
    ACTION="install"
else
    echo "🔄 Executando helm upgrade (atualizando release existente)..."
    ACTION="upgrade"
fi

# Usar upgrade --install que funciona para ambos os casos
microk8s helm3 upgrade n8n . -f custom-values.yaml \
    --namespace default \
    --install \
    --wait --timeout=600s

echo "✅ Verificando deployment..."
microk8s kubectl get pods -l app.kubernetes.io/name=n8n

echo "🗃️ Verificando bancos de dados..."
echo "PostgreSQL:"
microk8s kubectl get pods -l app.kubernetes.io/component=postgres 2>/dev/null || echo "  Nenhum pod PostgreSQL encontrado"
echo "MySQL:"
microk8s kubectl get pods -l app.kubernetes.io/component=mysql 2>/dev/null || echo "  Nenhum pod MySQL encontrado"
echo "Redis:"
microk8s kubectl get pods -l app.kubernetes.io/component=redis 2>/dev/null || echo "  Nenhum pod Redis encontrado"

echo "📊 Verificando services..."
microk8s kubectl get svc -l app.kubernetes.io/name=n8n

echo "💾 Verificando PVCs..."
microk8s kubectl get pvc -l app.kubernetes.io/name=n8n 2>/dev/null || echo "  Nenhum PVC encontrado"

echo "🌐 Verificando ingress..."
microk8s kubectl get ingress

echo "🔒 Verificando certificado..."
microk8s kubectl get certificate

echo "🏥 Testando conectividade..."
sleep 20
curl -f https://n8n.lolmeida.com/healthz && echo -e "\n✨ N8N $ACTION successful!"
EOF

echo -e "${GREEN}🎉 Deploy/Upgrade concluído!${NC}"
echo ""
echo -e "${GREEN}📊 Recursos disponíveis:${NC}"
echo -e "${GREEN}• N8N: https://$DOMAIN${NC}"
echo -e "${GREEN}• PostgreSQL: n8n-postgres:5432 (se habilitado)${NC}"
echo -e "${GREEN}• MySQL: n8n-mysql:3306 (se habilitado)${NC}"
echo -e "${GREEN}• Redis: n8n-redis:6379 (se habilitado)${NC}"
echo ""
echo -e "${GREEN}🔐 Credenciais:${NC}"
echo -e "${GREEN}• N8N: admin / (senha configurada)${NC}"
echo -e "${GREEN}• PostgreSQL: n8n / (senha configurada)${NC}"
echo -e "${GREEN}• MySQL: n8n / (senha configurada)${NC}"
echo -e "${GREEN}• Redis: (senha configurada)${NC}" 