#!/bin/bash

# Deploy script for peah.db
# Usage: ./deploy.sh [image-tag]

IMAGE_TAG=${1:-latest}
NAMESPACE="lolmeida"

echo "🚀 Deploying peah.db with image tag: $IMAGE_TAG"

# Execute deployment on VPS
ssh n8n << EOF
  echo "📦 Updating Helm chart..."
  cd /tmp
  rm -rf peah-db-helm
  git clone https://github.com/lolmeida/peah.db.git peah-db-helm
  cd peah-db-helm/k8s
  
  echo "🔧 Running Helm upgrade..."
  microk8s helm3 upgrade --install peah-db . \
    --namespace $NAMESPACE \
    --create-namespace \
    --set image.tag=$IMAGE_TAG \
    --wait --timeout=300s
  
  echo "✅ Checking deployment status..."
  microk8s kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=k8s
  microk8s kubectl rollout status deployment/peah-db-k8s -n $NAMESPACE --timeout=300s
  
  echo "🏥 Testing health endpoint..."
  sleep 10
  curl -f https://peah-db.lolmeida.com/q/health && echo -e "\n✨ Deployment successful!"
EOF