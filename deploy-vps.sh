#!/bin/bash

# Deploy script for peah.db to be executed on VPS
# Usage: ./deploy-vps.sh [image-tag]
# This script is designed to be called from GitHub Actions workflow with actions/checkout@v4

IMAGE_TAG=${1:-latest}
NAMESPACE="lolmeida"

echo "🚀 Deploying peah.db with image tag: $IMAGE_TAG"

# Check if k8s directory exists in expected location
if [ -d "/tmp/peah-db-k8s" ]; then
  # Files transferred via SCP
  K8S_DIR="/tmp/peah-db-k8s"
  echo "📁 Using k8s directory from SCP transfer: $K8S_DIR"
elif [ -d "k8s" ]; then
  # Running from project root
  K8S_DIR="k8s"
  echo "📁 Using k8s directory from current location: $K8S_DIR"
else
  echo "❌ Error: k8s directory not found in /tmp/peah-db-k8s or current directory."
  exit 1
fi

# Change to k8s directory
cd "$K8S_DIR"

# Run Helm upgrade
echo "🔧 Running Helm upgrade..."
microk8s helm3 upgrade --install peah-db . \
  --namespace $NAMESPACE \
  --create-namespace \
  --set image.tag=$IMAGE_TAG \
  --wait --timeout=300s

# Check deployment status
echo "✅ Checking deployment status..."
microk8s kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=k8s
microk8s kubectl rollout status deployment/peah-db-k8s -n $NAMESPACE --timeout=300s

# Test health endpoint
echo "🏥 Testing health endpoint..."
sleep 10
curl -f https://peah-db.lolmeida.com/q/health && echo -e "\n✨ Deployment successful!"

echo "🎉 Deployment completed successfully!" 