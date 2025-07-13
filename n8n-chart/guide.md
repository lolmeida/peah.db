# N8N Helm Chart Deployment Guide

## Prerequisites

1. **Kubernetes cluster** with kubectl configured
2. **Helm 3.x** installed
3. **nginx-ingress controller** installed in your cluster
4. **cert-manager** (optional, for automatic SSL certificates)

## Quick Start

### 1. Install nginx-ingress controller (if not already installed)
```bash
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace ingress-nginx --create-namespace
```

### 2. Install cert-manager (optional, for automatic SSL)
```bash
helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --set installCRDs=true
```

### 3. Create ClusterIssuer for Let's Encrypt (if using cert-manager)
```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: your-email@example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
```

### 4. Deploy N8N
```bash
# Create the chart directory structure first
mkdir -p n8n-chart/templates

# Copy all the template files to the templates directory
# Then run:
helm install n8n ./n8n-chart
```

## Configuration

### Custom Values
Create a `custom-values.yaml` file to override default values:

```yaml
n8n:
  auth:
    username: "myadmin"
    password: "mysecurepassword123"
  
  config:
    domain: "your-domain.com"
    webhookUrl: "https://your-domain.com/"
    editorBaseUrl: "https://your-domain.com/"

redis:
  auth:
    password: "myredispassword123"

ingress:
  hosts:
    - host: your-domain.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: n8n-tls
      hosts:
        - your-domain.com
```

Deploy with custom values:
```bash
helm install n8n ./n8n-chart -f custom-values.yaml
```

## Accessing N8N

1. **Ensure DNS is configured**: Point `m8n.lolmeida.com` (or your domain) to your cluster's ingress IP
2. **Get ingress IP**:
   ```bash
   kubectl get ingress
   ```
3. **Access the application**: Navigate to `https://m8n.lolmeida.com`
4. **Login**: Use the credentials from your values.yaml

## Useful Commands

### Check deployment status
```bash
kubectl get pods
kubectl get ingress
kubectl get services
```

### View logs
```bash
kubectl logs deployment/n8n-n8n
kubectl logs deployment/n8n-redis
```

### Update deployment
```bash
helm upgrade n8n ./n8n-chart -f custom-values.yaml
```

### Uninstall
```bash
helm uninstall n8n
```

### Debug
```bash
helm template n8n ./n8n-chart --debug
```

## Security Notes

1. **Change default passwords** in values.yaml before deploying
2. **Use strong passwords** for both N8N and Redis
3. **Enable SSL/TLS** through ingress configuration
4. **Consider network policies** to restrict traffic between pods
5. **Regular backups** of persistent volumes

## Troubleshooting

### Common Issues

1. **Ingress not working**: Check if nginx-ingress controller is running
2. **SSL certificate issues**: Verify cert-manager and ClusterIssuer configuration
3. **Pod not starting**: Check logs for configuration errors
4. **Storage issues**: Verify StorageClass is available in your cluster