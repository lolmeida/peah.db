#!/bin/bash

# N8N Deploy/Upgrade Script
# Instala ou atualiza n8n em n8n.lolmeida.com

set -e

NAMESPACE="lolmeida"
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
ssh n8n << EOF
set -e

cd /tmp/n8n-chart

NAMESPACE="$NAMESPACE"
RELEASE_NAME="$RELEASE_NAME"

# Verificar se o namespace existe
microk8s kubectl get namespace \$NAMESPACE 2>/dev/null || microk8s kubectl create namespace \$NAMESPACE

# Verificar estado do release
RELEASE_STATUS=\$(microk8s helm3 list -n \$NAMESPACE -q | grep "^\$RELEASE_NAME$" || echo "")

if [ -n "\$RELEASE_STATUS" ]; then
    echo "🔍 Release \$RELEASE_NAME encontrado no namespace \$NAMESPACE"
    
    # Verificar se o release está em estado failed
    RELEASE_FULL_STATUS=\$(microk8s helm3 list -n \$NAMESPACE | grep "\$RELEASE_NAME" || echo "")
    
    if echo "\$RELEASE_FULL_STATUS" | grep -q "failed\|pending"; then
        echo "⚠️  Release em estado problemático. Fazendo cleanup..."
        microk8s helm3 uninstall \$RELEASE_NAME -n \$NAMESPACE --wait || true
        echo "🧹 Cleanup concluído"
    fi
fi

# Usar upgrade --install que funciona para ambos os casos
echo "🚀 Executando helm upgrade --install..."
microk8s helm3 upgrade \$RELEASE_NAME . -f custom-values.yaml \
    --namespace \$NAMESPACE \
    --install \
    --create-namespace \
    --wait --timeout=600s

echo "✅ Verificando deployment..."
microk8s kubectl get pods -n \$NAMESPACE -l app.kubernetes.io/name=\$RELEASE_NAME

echo "🗃️ Verificando bancos de dados..."
echo "PostgreSQL:"
microk8s kubectl get pods -n \$NAMESPACE -l app.kubernetes.io/component=postgres 2>/dev/null || echo "  Nenhum pod PostgreSQL encontrado"
echo "MySQL:"
microk8s kubectl get pods -n \$NAMESPACE -l app.kubernetes.io/component=mysql 2>/dev/null || echo "  Nenhum pod MySQL encontrado"
echo "Redis:"
microk8s kubectl get pods -n \$NAMESPACE -l app.kubernetes.io/component=redis 2>/dev/null || echo "  Nenhum pod Redis encontrado"

echo "📊 Verificando services..."
microk8s kubectl get svc -n \$NAMESPACE -l app.kubernetes.io/name=\$RELEASE_NAME

echo "💾 Verificando PVCs..."
microk8s kubectl get pvc -n \$NAMESPACE -l app.kubernetes.io/name=\$RELEASE_NAME 2>/dev/null || echo "  Nenhum PVC encontrado"

echo "🌐 Verificando ingress..."
microk8s kubectl get ingress -n \$NAMESPACE

echo "🔒 Verificando certificado..."
microk8s kubectl get certificate -n \$NAMESPACE

echo "🏥 Testando conectividade..."
sleep 20
curl -f https://n8n.lolmeida.com/healthz && echo -e "\n✨ N8N deploy/upgrade successful!"
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