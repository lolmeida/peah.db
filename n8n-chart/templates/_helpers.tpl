{{/*
Expand the name of the chart.
*/}}
{{- define "n8n.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "n8n.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "n8n.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "n8n.labels" -}}
helm.sh/chart: {{ include "n8n.chart" . }}
{{ include "n8n.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "n8n.selectorLabels" -}}
app.kubernetes.io/name: {{ include "n8n.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "n8n.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "n8n.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}



{{/*
Generate a single panel based on configuration
*/}}
{{- define "grafana.panel" -}}
{{- $panel := . -}}
{
  "id": {{ $panel.id }},
  "title": "{{ $panel.title }}",
  "type": "{{ $panel.type }}",
  "targets": [
    {
      "expr": {{ $panel.expr | quote }},
      "legendFormat": "{{ $panel.legend }}"
    }
  ],
  {{- if eq $panel.type "stat" }}
  "fieldConfig": {
    "defaults": {
      "color": {
        "mode": "thresholds"
      },
      "thresholds": {
        "steps": [
          {
            "color": "red",
            "value": 0
          },
          {
            "color": "green",
            "value": 1
          }
        ]
      }
    }
  },
  {{- end }}
  "gridPos": {
    "h": {{ $panel.height | default 8 }},
    "w": {{ $panel.width | default 8 }},
    "x": {{ $panel.x | default 0 }},
    "y": {{ $panel.y | default 0 }}
  }
}
{{- end }}

{{/*
Generate complete dashboard JSON from configuration
*/}}
{{- define "grafana.dashboard" -}}
{{- $dashConfig := . -}}
{
  "id": null,
  "title": "{{ $dashConfig.title }}",
  "tags": {{ $dashConfig.tags | toJson }},
  "style": "dark",
  "timezone": "browser",
  "refresh": "30s",
  "time": {
    "from": "now-1h",
    "to": "now"
  },
  "panels": [
    {{- range $index, $panel := $dashConfig.panels }}
    {{- if $index }},{{ end }}
    {{- include "grafana.panel" $panel }}
    {{- end }}
  ]
}
{{- end }}

{{/*
Generate all dashboards from configuration
*/}}
{{- define "grafana.dashboards" -}}
{{- $dashConfigFile := .Files.Get "dash-config-values.yaml" -}}
{{- if $dashConfigFile -}}
{{- $dashConfig := fromYaml $dashConfigFile -}}
{{- range $key, $config := $dashConfig.dashboards }}
  {{ $key }}.json: |
    {{- include "grafana.dashboard" $config | nindent 4 }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Define ingress configurations as a map
*/}}
{{- define "n8n.ingressConfigs" -}}
n8n:
  enabled: {{ .Values.ingress.n8n.enabled }}
  name: "ingress"
  config: {{ toYaml .Values.ingress.n8n | nindent 4 }}
  component: ""
  serviceName: "n8n"
monitoring:
  enabled: {{ .Values.ingress.monitoring.enabled }}
  name: "monitoring-ingress"
  config: {{ toYaml .Values.ingress.monitoring | nindent 4 }}
  component: "monitoring"
  serviceName: ""
{{- end }}

{{/*
Define deployment configurations as a map
*/}}
{{- define "n8n.deploymentConfigs" -}}
{{- $n8nEnv := list
  (dict "name" "TZ" "value" .Values.global.timezone)
  (dict "name" "GENERIC_TIMEZONE" "value" .Values.global.timezone)
  (dict "name" "N8N_BASIC_AUTH_ACTIVE" "value" (.Values.n8n.auth.enabled | toString))
  (dict "name" "N8N_BASIC_AUTH_USER" "valueFrom" (dict "secretKeyRef" (dict "name" (printf "%s-secret" (include "n8n.fullname" .)) "key" "n8n-auth-username")))
  (dict "name" "N8N_BASIC_AUTH_PASSWORD" "valueFrom" (dict "secretKeyRef" (dict "name" (printf "%s-secret" (include "n8n.fullname" .)) "key" "n8n-auth-password")))
  (dict "name" "WEBHOOK_URL" "value" .Values.n8n.config.webhookUrl)
  (dict "name" "N8N_EDITOR_BASE_URL" "value" .Values.n8n.config.editorBaseUrl)
  (dict "name" "N8N_PROTOCOL" "value" .Values.n8n.config.protocol)
  (dict "name" "N8N_HOST" "value" .Values.n8n.config.domain)
-}}
{{- if .Values.postgresql.enabled -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_TYPE" "value" "postgresdb") -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_POSTGRESDB_HOST" "value" (printf "%s-postgres" (include "n8n.fullname" .))) -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_POSTGRESDB_PORT" "value" "5432") -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_POSTGRESDB_DATABASE" "value" .Values.postgresql.auth.database) -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_POSTGRESDB_USER" "value" .Values.postgresql.auth.username) -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_POSTGRESDB_PASSWORD" "valueFrom" (dict "secretKeyRef" (dict "name" (printf "%s-secret" (include "n8n.fullname" .)) "key" "postgres-password"))) -}}
{{- else if .Values.mysql.enabled -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_TYPE" "value" "mysqldb") -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_MYSQLDB_HOST" "value" (printf "%s-mysql" (include "n8n.fullname" .))) -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_MYSQLDB_PORT" "value" "3306") -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_MYSQLDB_DATABASE" "value" .Values.mysql.auth.database) -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_MYSQLDB_USER" "value" .Values.mysql.auth.username) -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "DB_MYSQLDB_PASSWORD" "valueFrom" (dict "secretKeyRef" (dict "name" (printf "%s-secret" (include "n8n.fullname" .)) "key" "mysql-password"))) -}}
{{- end -}}
{{- if .Values.redis.enabled -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "QUEUE_BULL_REDIS_HOST" "value" (printf "%s-redis" (include "n8n.fullname" .))) -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "QUEUE_BULL_REDIS_PORT" "value" "6379") -}}
{{- $n8nEnv = append $n8nEnv (dict "name" "QUEUE_BULL_REDIS_PASSWORD" "valueFrom" (dict "secretKeyRef" (dict "name" (printf "%s-secret" (include "n8n.fullname" .)) "key" "redis-password"))) -}}
{{- end -}}
n8n:
  enabled: {{ and .Values.n8n.enabled .Values.deployments.n8n.enabled }}
  name: "n8n"
  component: "n8n"
  replicaCount: {{ .Values.deployments.n8n.replicaCount }}
  image: {{ toYaml .Values.deployments.n8n.image | nindent 4 }}
  port:
    name: "http"
    containerPort: 5678
  resources: {{ toYaml .Values.deployments.n8n.resources | nindent 4 }}
  persistence: {{ toYaml .Values.persistence.n8n | nindent 4 }}
  volumeMount:
    name: "n8n-data"
    mountPath: "/home/node/.n8n"
  env: {{ toYaml $n8nEnv | nindent 4 }}
  livenessProbe:
    httpGet:
      path: "/healthz"
      port: "http"
    initialDelaySeconds: 30
    periodSeconds: 10
  readinessProbe:
    httpGet:
      path: "/healthz"
      port: "http"
    initialDelaySeconds: 15
    periodSeconds: 5
mysql:
  enabled: {{ and .Values.mysql.enabled .Values.deployments.mysql.enabled }}
  name: "mysql"
  component: "mysql"
  replicaCount: {{ .Values.deployments.mysql.replicaCount }}
  image: {{ toYaml .Values.deployments.mysql.image | nindent 4 }}
  port:
    name: "mysql"
    containerPort: 3306
  resources: {{ toYaml .Values.deployments.mysql.resources | nindent 4 }}
  persistence: {{ toYaml .Values.persistence.mysql | nindent 4 }}
  volumeMount:
    name: "mysql-data"
    mountPath: "/var/lib/mysql"
  env:
    - name: "TZ"
      value: {{ .Values.global.timezone | quote }}
    - name: "MYSQL_DATABASE"
      value: {{ .Values.mysql.auth.database | quote }}
    - name: "MYSQL_USER"
      value: {{ .Values.mysql.auth.username | quote }}
    - name: "MYSQL_PASSWORD"
      valueFrom:
        secretKeyRef:
          name: {{ printf "%s-secret" (include "n8n.fullname" .) }}
          key: "mysql-password"
    - name: "MYSQL_ROOT_PASSWORD"
      valueFrom:
        secretKeyRef:
          name: {{ printf "%s-secret" (include "n8n.fullname" .) }}
          key: "mysql-root-password"
  livenessProbe:
    exec:
      command:
        - "/bin/sh"
        - "-c"
        - "exec mysqladmin ping -h localhost -u root -p$MYSQL_ROOT_PASSWORD"
    initialDelaySeconds: 30
    periodSeconds: 10
  readinessProbe:
    exec:
      command:
        - "/bin/sh"
        - "-c"
        - "-e"
        - "exec mysqladmin ping -h localhost -u root -p$MYSQL_ROOT_PASSWORD"
    initialDelaySeconds: 5
    periodSeconds: 5
postgresql:
  enabled: {{ and .Values.postgresql.enabled .Values.deployments.postgresql.enabled }}
  name: "postgres"
  component: "postgres"
  replicaCount: {{ .Values.deployments.postgresql.replicaCount }}
  image: {{ toYaml .Values.deployments.postgresql.image | nindent 4 }}
  port:
    name: "postgres"
    containerPort: 5432
  resources: {{ toYaml .Values.deployments.postgresql.resources | nindent 4 }}
  persistence: {{ toYaml .Values.persistence.postgresql | nindent 4 }}
  volumeMount:
    name: "postgres-data"
    mountPath: "/var/lib/postgresql/data"
  env:
    - name: "TZ"
      value: {{ .Values.global.timezone | quote }}
    - name: "POSTGRES_DB"
      value: {{ .Values.postgresql.auth.database | quote }}
    - name: "POSTGRES_USER"
      value: {{ .Values.postgresql.auth.username | quote }}
    - name: "POSTGRES_PASSWORD"
      valueFrom:
        secretKeyRef:
          name: {{ printf "%s-secret" (include "n8n.fullname" .) }}
          key: "postgres-password"
    - name: "PGDATA"
      value: "/var/lib/postgresql/data/pgdata"
  livenessProbe:
    exec:
      command:
        - "/bin/sh"
        - "-c"
        - {{ printf "exec pg_isready -U %s -d %s" .Values.postgresql.auth.username .Values.postgresql.auth.database | quote }}
    initialDelaySeconds: 30
    periodSeconds: 10
  readinessProbe:
    exec:
      command:
        - "/bin/sh"
        - "-c"
        - "-e"
        - {{ printf "exec pg_isready -U %s -d %s" .Values.postgresql.auth.username .Values.postgresql.auth.database | quote }}
    initialDelaySeconds: 5
    periodSeconds: 5
redis:
  enabled: {{ and .Values.redis.enabled .Values.deployments.redis.enabled }}
  name: "redis"
  component: "redis"
  replicaCount: {{ .Values.deployments.redis.replicaCount }}
  image: {{ toYaml .Values.deployments.redis.image | nindent 4 }}
  port:
    name: "redis"
    containerPort: 6379
  resources: {{ toYaml .Values.deployments.redis.resources | nindent 4 }}
  persistence: {{ toYaml .Values.persistence.redis | nindent 4 }}
  volumeMount:
    name: "redis-data"
    mountPath: "/data"
  command:
    - "redis-server"
    - "--requirepass"
    - "$(REDIS_PASSWORD)"
    - "--appendonly"
    - "yes"
  env:
    - name: "TZ"
      value: {{ .Values.global.timezone | quote }}
    - name: "REDIS_PASSWORD"
      valueFrom:
        secretKeyRef:
          name: {{ printf "%s-secret" (include "n8n.fullname" .) }}
          key: "redis-password"
  livenessProbe:
    exec:
      command:
        - "redis-cli"
        - "ping"
    initialDelaySeconds: 30
    periodSeconds: 10
  readinessProbe:
    exec:
      command:
        - "redis-cli"
        - "ping"
    initialDelaySeconds: 5
    periodSeconds: 5
grafana:
  enabled: {{ and .Values.monitoring.grafana.enabled .Values.deployments.grafana.enabled }}
  name: "grafana"
  component: "grafana"
  replicaCount: {{ .Values.deployments.grafana.replicaCount }}
  image: {{ toYaml .Values.deployments.grafana.image | nindent 4 }}
  port:
    name: "http"
    containerPort: 3000
  resources: {{ toYaml .Values.deployments.grafana.resources | nindent 4 }}
  persistence: {{ toYaml .Values.persistence.grafana | nindent 4 }}
  volumeMount:
    name: "grafana-data"
    mountPath: "/var/lib/grafana"
  extraVolumeMounts:
    - name: "grafana-config"
      mountPath: "/etc/grafana/grafana.ini"
      subPath: "grafana.ini"
    - name: "grafana-config"
      mountPath: "/etc/grafana/provisioning/datasources/datasources.yaml"
      subPath: "datasources.yaml"
    - name: "grafana-config"
      mountPath: "/etc/grafana/provisioning/dashboards/dashboards.yaml"
      subPath: "dashboards.yaml"
    - name: "grafana-dashboards"
      mountPath: "/var/lib/grafana/dashboards"
  extraVolumes:
    - name: "grafana-config"
      configMap:
        name: {{ printf "%s-grafana-config" (include "n8n.fullname" .) }}
    - name: "grafana-dashboards"
      configMap:
        name: {{ printf "%s-grafana-dashboards" (include "n8n.fullname" .) }}
  env:
    - name: "TZ"
      value: {{ .Values.global.timezone | quote }}
    - name: "GF_SECURITY_ADMIN_USER"
      value: {{ .Values.monitoring.grafana.auth.adminUser | quote }}
    - name: "GF_SECURITY_ADMIN_PASSWORD"
      valueFrom:
        secretKeyRef:
          name: {{ printf "%s-secret" (include "n8n.fullname" .) }}
          key: "grafana-admin-password"
  livenessProbe:
    httpGet:
      path: "/api/health"
      port: "http"
    initialDelaySeconds: 30
    periodSeconds: 10
  readinessProbe:
    httpGet:
      path: "/api/health"
      port: "http"
    initialDelaySeconds: 15
    periodSeconds: 5
prometheus:
  enabled: {{ and .Values.monitoring.prometheus.enabled .Values.deployments.prometheus.enabled }}
  name: "prometheus"
  component: "prometheus"
  replicaCount: {{ .Values.deployments.prometheus.replicaCount }}
  image: {{ toYaml .Values.deployments.prometheus.image | nindent 4 }}
  port:
    name: "http"
    containerPort: 9090
  resources: {{ toYaml .Values.deployments.prometheus.resources | nindent 4 }}
  persistence: {{ toYaml .Values.persistence.prometheus | nindent 4 }}
  volumeMount:
    name: "prometheus-data"
    mountPath: "/prometheus"
  extraVolumeMounts:
    - name: "prometheus-config"
      mountPath: "/etc/prometheus/prometheus.yml"
      subPath: "prometheus.yml"
  extraVolumes:
    - name: "prometheus-config"
      configMap:
        name: {{ printf "%s-prometheus-config" (include "n8n.fullname" .) }}
  serviceAccountName: {{ printf "%s-prometheus" (include "n8n.fullname" .) }}
  args:
    - "--config.file=/etc/prometheus/prometheus.yml"
    - "--storage.tsdb.path=/prometheus"
    - "--web.console.libraries=/etc/prometheus/console_libraries"
    - "--web.console.templates=/etc/prometheus/consoles"
    - {{ printf "--storage.tsdb.retention.time=%s" .Values.monitoring.prometheus.retention | quote }}
    - "--web.enable-lifecycle"
  env:
    - name: "TZ"
      value: {{ .Values.global.timezone | quote }}
  livenessProbe:
    httpGet:
      path: "/-/healthy"
      port: "http"
    initialDelaySeconds: 30
    periodSeconds: 10
  readinessProbe:
    httpGet:
      path: "/-/ready"
      port: "http"
    initialDelaySeconds: 15
    periodSeconds: 5
{{- end }}

{{/*
Define service configurations as a map
*/}}
{{- define "n8n.serviceConfigs" -}}
n8n:
  enabled: {{ .Values.services.n8n.enabled }}
  name: "n8n"
  component: "n8n"
  port: {{ .Values.services.n8n.port }}
  targetPort: {{ .Values.services.n8n.targetPort | quote }}
  type: {{ .Values.services.n8n.type | quote }}
mysql:
  enabled: {{ and .Values.mysql.enabled .Values.services.mysql.enabled }}
  name: "mysql"
  component: "mysql"
  port: {{ .Values.services.mysql.port }}
  targetPort: {{ .Values.services.mysql.targetPort | quote }}
  type: {{ .Values.services.mysql.type | quote }}
postgresql:
  enabled: {{ and .Values.postgresql.enabled .Values.services.postgresql.enabled }}
  name: "postgres"
  component: "postgres"
  port: {{ .Values.services.postgresql.port }}
  targetPort: {{ .Values.services.postgresql.targetPort | quote }}
  type: {{ .Values.services.postgresql.type | quote }}
redis:
  enabled: {{ and .Values.redis.enabled .Values.services.redis.enabled }}
  name: "redis"
  component: "redis"
  port: {{ .Values.services.redis.port }}
  targetPort: {{ .Values.services.redis.targetPort | quote }}
  type: {{ .Values.services.redis.type | quote }}
grafana:
  enabled: {{ and .Values.monitoring.grafana.enabled .Values.services.grafana.enabled }}
  name: "grafana"
  component: "grafana"
  port: {{ .Values.services.grafana.port }}
  targetPort: {{ .Values.services.grafana.targetPort | quote }}
  type: {{ .Values.services.grafana.type | quote }}
prometheus:
  enabled: {{ and .Values.monitoring.prometheus.enabled .Values.services.prometheus.enabled }}
  name: "prometheus"
  component: "prometheus"
  port: {{ .Values.services.prometheus.port }}
  targetPort: {{ .Values.services.prometheus.targetPort | quote }}
  type: {{ .Values.services.prometheus.type | quote }}
{{- end }}

{{/*
Define configmap configurations as a map  
*/}}
{{- define "n8n.configmapConfigs" -}}
grafana-config:
  enabled: {{ and .Values.monitoring.grafana.enabled (default true .Values.monitoring.grafana.createConfigMap) }}
  name: "grafana-config"
  component: "grafana"
prometheus-config:
  enabled: {{ and .Values.monitoring.prometheus.enabled (default true .Values.monitoring.prometheus.createConfigMap) }}
  name: "prometheus-config"
  component: "prometheus"
grafana-dashboards:
  enabled: {{ and .Values.monitoring.grafana.enabled (default true .Values.monitoring.grafana.createConfigMap) }}
  name: "grafana-dashboards"
  component: "grafana"
  extraLabel: "grafana_dashboard"
  extraLabelValue: "1"
{{- end }}

{{/*
Define PVC configurations as a map
*/}}
{{- define "n8n.pvcConfigs" -}}
n8n:
  enabled: {{ and .Values.n8n.enabled .Values.persistence.n8n.enabled }}
  name: "n8n"
  component: "n8n"
  accessMode: {{ .Values.persistence.n8n.accessMode | quote }}
  size: {{ .Values.persistence.n8n.size | quote }}
  storageClass: {{ .Values.persistence.n8n.storageClass | quote }}
mysql:
  enabled: {{ and .Values.mysql.enabled .Values.persistence.mysql.enabled }}
  name: "mysql"
  component: "mysql"
  accessMode: {{ .Values.persistence.mysql.accessMode | quote }}
  size: {{ .Values.persistence.mysql.size | quote }}
  storageClass: {{ .Values.persistence.mysql.storageClass | quote }}
postgresql:
  enabled: {{ and .Values.postgresql.enabled .Values.persistence.postgresql.enabled }}
  name: "postgres"
  component: "postgres"
  accessMode: {{ .Values.persistence.postgresql.accessMode | quote }}
  size: {{ .Values.persistence.postgresql.size | quote }}
  storageClass: {{ .Values.persistence.postgresql.storageClass | quote }}
redis:
  enabled: {{ and .Values.redis.enabled .Values.persistence.redis.enabled }}
  name: "redis"
  component: "redis"
  accessMode: {{ .Values.persistence.redis.accessMode | quote }}
  size: {{ .Values.persistence.redis.size | quote }}
  storageClass: {{ .Values.persistence.redis.storageClass | quote }}
grafana:
  enabled: {{ and .Values.monitoring.grafana.enabled .Values.persistence.grafana.enabled }}
  name: "grafana"
  component: "grafana"
  accessMode: {{ .Values.persistence.grafana.accessMode | quote }}
  size: {{ .Values.persistence.grafana.size | quote }}
  storageClass: {{ .Values.persistence.grafana.storageClass | quote }}
prometheus:
  enabled: {{ and .Values.monitoring.prometheus.enabled .Values.persistence.prometheus.enabled }}
  name: "prometheus"
  component: "prometheus"
  accessMode: {{ .Values.persistence.prometheus.accessMode | quote }}
  size: {{ .Values.persistence.prometheus.size | quote }}
  storageClass: {{ .Values.persistence.prometheus.storageClass | quote }}
{{- end }}
 