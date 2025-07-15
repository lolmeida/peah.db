#!/bin/bash

# Deploy script for n8n
# Usage: ./n8n-deploy.sh

NAMESPACE="lolmeida"

echo "🚀 Deploying n8n with Redis..."

# Execute deployment on VPS
ssh n8n << 'EOF'
  echo "📦 Updating Helm chart..."
  cd /tmp
  rm -rf peah-db-n8n-helm
  git clone https://github.com/lolmeida/peah.db.git peah-db-n8n-helm
  cd peah-db-n8n-helm/n8n-chart
  
  echo "🔧 Running Helm upgrade..."
  microk8s helm3 upgrade --install n8n . \
    --namespace lolmeida \
    --create-namespace \
    --wait --timeout=300s
  
  echo "✅ Checking deployment status..."
  microk8s kubectl get pods -n lolmeida -l app.kubernetes.io/name=n8n
  microk8s kubectl rollout status deployment/n8n-n8n -n lolmeida --timeout=300s
  microk8s kubectl rollout status deployment/n8n-redis -n lolmeida --timeout=300s
  
  echo "🌐 Checking ingress..."
  microk8s kubectl get ingress -n lolmeida
  
  echo "🏥 Testing n8n endpoint..."
  sleep 10
  curl -f https://n8n.lolmeida.com/healthz && echo -e "\n✨ N8N deployment successful!"
EOF