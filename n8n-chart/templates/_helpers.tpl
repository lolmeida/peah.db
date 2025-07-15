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
      "expr": "{{ $panel.expr }}",
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
 