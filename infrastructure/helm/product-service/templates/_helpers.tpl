{{- define "product-service.name" -}}
product-service
{{- end }}

{{- define "product-service.fullname" -}}
{{ include "product-service.name" . }}
{{- end }}

{{- define "product-service.labels" -}}
app.kubernetes.io/name: {{ include "product-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
{{- end }}