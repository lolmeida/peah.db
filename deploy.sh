#!/bin/bash
#
# Script para deploy manual/local no servidor 'n8n'.
# O deploy automatizado para produção é feito via GitHub Actions (.github/workflows/deploy.yml).
#

# Deploy script for peah.db
# Usage: ./deploy.sh [image-tag]

# Enable verbose logging
set -eo pipefail

# Color codes for better visibility
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to log with timestamp
log() {
    echo -e "${CYAN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

# Function to log errors
error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" >&2
}

# Function to log success
success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS:${NC} $1"
}

# Function to log warnings
warn() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1"
}

IMAGE_TAG=${1:-latest}
NAMESPACE="lolmeida"
VPS_HOST="n8n"
REPO_URL="https://github.com/lolmeida/peah.db.git"
TEMP_DIR="/tmp/peah-be-helm"
DEPLOYMENT_NAME="peah-be-k8s"
APP_URL="https://peah-be.lolmeida.com"

log "🚀 Starting deployment process for peah.db"
log "📋 Deployment parameters:"
log "   - Image tag: ${BLUE}$IMAGE_TAG${NC}"
log "   - Namespace: ${BLUE}$NAMESPACE${NC}"
log "   - VPS Host: ${BLUE}$VPS_HOST${NC}"
log "   - Repository: ${BLUE}$REPO_URL${NC}"
log "   - Application URL: ${BLUE}$APP_URL${NC}"
echo "----------------------------------------"

# Check SSH connectivity
log "🔗 Checking SSH connectivity to $VPS_HOST..."
if ssh -o ConnectTimeout=5 $VPS_HOST "echo 'SSH connection successful'" > /dev/null 2>&1; then
    success "SSH connection to $VPS_HOST established"
else
    error "Failed to connect to $VPS_HOST via SSH"
    exit 1
fi

# Execute deployment on VPS
ssh $VPS_HOST << EOF
    set -eo pipefail
    
    # Redefine color codes and functions inside SSH session
    RED='\033[0;31m'
    GREEN='\033[0;32m'
    YELLOW='\033[1;33m'
    BLUE='\033[0;34m'
    CYAN='\033[0;36m'
    NC='\033[0m'
    
    log() { echo -e "${CYAN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} ${1:-}"; }
    error() { echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} ${1:-}" >&2; }
    success() { echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS:${NC} ${1:-}"; }
    warn() { echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} ${1:-}"; }
    
    IMAGE_TAG='$IMAGE_TAG'
    NAMESPACE='$NAMESPACE'
    REPO_URL='$REPO_URL'
    TEMP_DIR='$TEMP_DIR'
    DEPLOYMENT_NAME='$DEPLOYMENT_NAME'
    APP_URL='$APP_URL'
    
    log "📍 Connected to VPS - $(hostname)"
    log "📊 System info: $(uname -a)"
    echo "----------------------------------------"
    
    # Check MicroK8s status
    log "🔍 Checking MicroK8s status..."
    if microk8s status --wait-ready > /dev/null 2>&1; then
        success "MicroK8s is running and ready"
        log "   - Kubernetes version: $(microk8s kubectl version --short 2>/dev/null | grep Server | cut -d: -f2 | xargs)"
    else
        error "MicroK8s is not ready"
        exit 1
    fi
    
    # Check Helm installation
    log "🔍 Checking Helm installation..."
    if microk8s helm3 version > /dev/null 2>&1; then
        HELM_VERSION=$(microk8s helm3 version --short 2>/dev/null || echo "unknown")
        success "Helm is installed: $HELM_VERSION"
    else
        error "Helm is not installed or not accessible"
        exit 1
    fi
    
    # Clean and prepare deployment directory
    log "🧹 Cleaning temporary directory..."
    if [ -d "$TEMP_DIR" ]; then
        log "   - Removing existing directory: $TEMP_DIR"
        rm -rf "$TEMP_DIR"
        success "Temporary directory cleaned"
    else
        log "   - No existing directory to clean"
    fi
    
    # Clone repository
    log "📦 Cloning repository..."
    log "   - Source: $REPO_URL"
    log "   - Destination: $TEMP_DIR"
    
    if git clone --quiet "$REPO_URL" "$TEMP_DIR" 2>&1; then
        success "Repository cloned successfully"
        cd "$TEMP_DIR"
        log "   - Current commit: $(git rev-parse --short HEAD)"
        log "   - Branch: $(git branch --show-current)"
    else
        error "Failed to clone repository"
        exit 1
    fi
    
    # Navigate to Helm chart directory
    log "📂 Navigating to Helm chart directory..."
    cd "$TEMP_DIR/k8s"
    if [ -f "Chart.yaml" ]; then
        success "Found Helm chart"
        log "   - Chart name: $(grep '^name:' Chart.yaml | cut -d' ' -f2)"
        log "   - Chart version: $(grep '^version:' Chart.yaml | cut -d' ' -f2)"
        log "   - App version: $(grep '^appVersion:' Chart.yaml | cut -d' ' -f2)"
    else
        error "Chart.yaml not found in k8s directory"
        exit 1
    fi
    
    # Check current deployment status
    log "📊 Checking current deployment status..."
    if microk8s kubectl get deployment "$DEPLOYMENT_NAME" -n "$NAMESPACE" > /dev/null 2>&1; then
        warn "Existing deployment found"
        CURRENT_IMAGE=$(microk8s kubectl get deployment "$DEPLOYMENT_NAME" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].image}')
        log "   - Current image: $CURRENT_IMAGE"
        log "   - Current replicas: $(microk8s kubectl get deployment "$DEPLOYMENT_NAME" -n "$NAMESPACE" -o jsonpath='{.spec.replicas}')"
    else
        log "   - No existing deployment found (will create new)"
    fi
    
    # Check namespace
    log "🏷️  Checking namespace..."
    if microk8s kubectl get namespace "$NAMESPACE" > /dev/null 2>&1; then
        success "Namespace '$NAMESPACE' exists"
    else
        warn "Namespace '$NAMESPACE' does not exist (will be created)"
    fi
    
    # Validate Helm chart
    log "✅ Validating Helm chart..."
    if microk8s helm3 lint . > /dev/null 2>&1; then
        success "Helm chart validation passed"
    else
        warn "Helm chart has warnings (continuing anyway)"
    fi
    
    # Dry run to preview changes
    log "🔍 Running Helm dry-run to preview changes..."
    echo "----------------------------------------"
    microk8s helm3 upgrade --install peah-be . \
        --namespace "$NAMESPACE" \
        --create-namespace \
        --set image.tag="$IMAGE_TAG" \
        --dry-run --debug 2>&1 | grep -E "(REVISION|DEPLOYED|MANIFEST|image:)" || true
    echo "----------------------------------------"
    
    # Execute Helm upgrade
    log "🔧 Running Helm upgrade..."
    log "   - Release name: peah-be"
    log "   - Namespace: $NAMESPACE"
    log "   - Image tag: $IMAGE_TAG"
    log "   - Timeout: 300s"
    
    if microk8s helm3 upgrade --install peah-be . \
        --namespace "$NAMESPACE" \
        --create-namespace \
        --set image.tag="$IMAGE_TAG" \
        --wait --timeout=300s 2>&1; then
        success "Helm upgrade completed successfully"
    else
        error "Helm upgrade failed"
        exit 1
    fi
    
    # Check Helm release status
    log "📋 Checking Helm release status..."
    microk8s helm3 status peah-be -n "$NAMESPACE" | grep -E "(STATUS|REVISION|DEPLOYED)"
    
    # Check deployment rollout status
    log "⏳ Waiting for deployment rollout..."
    START_TIME=$(date +%s)
    
    if microk8s kubectl rollout status deployment/"$DEPLOYMENT_NAME" -n "$NAMESPACE" --timeout=300s; then
        END_TIME=$(date +%s)
        DURATION=$((END_TIME - START_TIME))
        success "Deployment rollout completed in ${DURATION}s"
    else
        error "Deployment rollout failed or timed out"
        
        # Show pod events for debugging
        log "🔍 Recent pod events:"
        microk8s kubectl get events -n "$NAMESPACE" --sort-by='.lastTimestamp' | tail -10
        exit 1
    fi
    
    # Get detailed pod status
    log "🔍 Checking pod status..."
    echo "----------------------------------------"
    microk8s kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/name=k8s -o wide
    echo "----------------------------------------"
    
    # Show pod details
    POD_NAME=$(microk8s kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/name=k8s -o jsonpath='{.items[0].metadata.name}')
    if [ -n "$POD_NAME" ]; then
        log "📝 Pod details for: $POD_NAME"
        log "   - Image: $(microk8s kubectl get pod "$POD_NAME" -n "$NAMESPACE" -o jsonpath='{.spec.containers[0].image}')"
        log "   - Status: $(microk8s kubectl get pod "$POD_NAME" -n "$NAMESPACE" -o jsonpath='{.status.phase}')"
        log "   - Ready: $(microk8s kubectl get pod "$POD_NAME" -n "$NAMESPACE" -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}')"
        log "   - Restarts: $(microk8s kubectl get pod "$POD_NAME" -n "$NAMESPACE" -o jsonpath='{.status.containerStatuses[0].restartCount}')"
        
        # Check recent logs
        log "📜 Recent pod logs (last 20 lines):"
        echo "----------------------------------------"
        microk8s kubectl logs "$POD_NAME" -n "$NAMESPACE" --tail=20 || warn "Could not fetch logs"
        echo "----------------------------------------"
    fi
    
    # Check service status
    log "🌐 Checking service status..."
    if microk8s kubectl get service peah-be-k8s -n "$NAMESPACE" > /dev/null 2>&1; then
        success "Service is configured"
        SERVICE_INFO=$(microk8s kubectl get service peah-be-k8s -n "$NAMESPACE" -o wide)
        echo "$SERVICE_INFO"
    else
        warn "Service not found"
    fi
    
    # Check ingress status
    log "🔗 Checking ingress status..."
    if microk8s kubectl get ingress -n "$NAMESPACE" > /dev/null 2>&1; then
        INGRESS_INFO=$(microk8s kubectl get ingress -n "$NAMESPACE" -o wide)
        if [ -n "$INGRESS_INFO" ]; then
            success "Ingress is configured"
            echo "$INGRESS_INFO"
        else
            warn "No ingress found"
        fi
    fi
    
    # Test application health
    log "🏥 Testing application health endpoint..."
    log "   - Waiting 10 seconds for application to stabilize..."
    sleep 10
    
    log "   - Testing URL: $APP_URL/q/health"
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 10 --max-time 30 "$APP_URL/q/health" || echo "000")
    
    if [ "$HTTP_CODE" = "200" ]; then
        success "Health check passed (HTTP $HTTP_CODE)"
        log "   - Health response:"
        curl -s "$APP_URL/q/health" | jq . 2>/dev/null || curl -s "$APP_URL/q/health"
    else
        error "Health check failed (HTTP $HTTP_CODE)"
        
        # Additional diagnostics
        log "🔍 Running additional diagnostics..."
        log "   - Testing pod-to-pod connectivity..."
        microk8s kubectl run curl-test --rm -i --restart=Never --image=curlimages/curl -- curl -s http://peah-be-k8s."$NAMESPACE".svc.cluster.local:8080/q/health || warn "Pod-to-pod test failed"
        
        exit 1
    fi
    
    # Summary
    echo "----------------------------------------"
    success "✨ Deployment completed successfully!"
    log "📊 Deployment summary:"
    log "   - Release: peah-be"
    log "   - Namespace: $NAMESPACE"
    log "   - Image tag: $IMAGE_TAG"
    log "   - Application URL: $APP_URL"
    log "   - Health endpoint: $APP_URL/q/health"
    echo "----------------------------------------"
EOF

# Check exit status
DEPLOYMENT_STATUS=$?
if [ $DEPLOYMENT_STATUS -eq 0 ]; then
    success "🎉 Deployment script completed successfully!"
else
    error "❌ Deployment script failed with exit code: $DEPLOYMENT_STATUS"
    exit $DEPLOYMENT_STATUS
fi