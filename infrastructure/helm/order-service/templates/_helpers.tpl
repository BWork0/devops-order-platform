{{- define "order-service.name" -}}
order-service
{{- end }}

{{- define "order-service.fullname" -}}
{{ include "order-service.name" . }}
{{- end }}

{{- define "order-service.labels" -}}
app.kubernetes.io/name: {{ include "order-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
{{- end }}