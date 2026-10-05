# DevOps Order Platform

A production-style DevOps laboratory built around a simple Spring Boot order platform. The project focuses on automated CI/CD, Kubernetes deployment, infrastructure automation and full-stack observability.

## Architecture

```text
                        ┌──────────────┐
                        │    Git/Gitea │
                        └──────┬───────┘
                               │
                               ▼
                        ┌──────────────┐
                        │    Jenkins   │
                        └──────┬───────┘
                               │
                    ┌──────────┴──────────┐
                    ▼                     ▼
                 ┌───────┐           ┌─────────┐
                 │ Nexus │           │   K3s   │
                 └───────┘           │ Cluster │
                                     └────┬────┘
                                          │
                         ┌────────────────┼────────────────┐
                         ▼                ▼                ▼
                    Application       PostgreSQL      Observability
                         │                                 │
              ┌──────────┼──────────┐             ┌────────┼────────┐
              ▼          ▼          ▼             ▼        ▼        ▼
           Metrics      Logs      Traces       Grafana   Kibana   Jaeger
              │          │          │
              ▼          ▼          ▼
         Prometheus     ELK       OpenTelemetry
```

## Application

The application consists of two Spring Boot services:

* **Product Service** — REST API for product management
* **Order Service** — REST API for order management
* **PostgreSQL** — persistent database

The application is intentionally simple so the infrastructure and DevOps practices remain the main focus.

## Technology Stack

### Application

* Java 21
* Spring Boot
* Maven
* PostgreSQL
* JPA / Hibernate
* JUnit 5
* Testcontainers

### CI/CD

* Jenkins
* Gitea
* Nexus Repository
* Docker
* Helm

### Infrastructure

* Vagrant
* WSL2
* Ansible
* K3s
* Kubernetes
* Kubernetes RBAC
* Kubernetes Secrets

### Observability

* Prometheus
* Grafana
* Node Exporter
* kube-state-metrics
* cAdvisor
* Filebeat
* Logstash
* Elasticsearch
* Kibana
* OpenTelemetry
* Jaeger

## CI/CD Pipeline

The pipeline automates:

```text
Git Push
   ↓
Jenkins
   ↓
Tests
   ↓
Maven Build
   ↓
Maven Artifact → Nexus
   ↓
Docker Build
   ↓
Docker Image → Nexus
   ↓
Helm Validation
   ↓
K3s Deployment
```

Application images are stored in a private Nexus Docker registry and deployed to the K3s cluster using Helm.

## Observability

### Metrics

Spring Boot exposes Prometheus metrics through Actuator.

```text
Spring Boot
     ↓
Prometheus
     ↓
Grafana
```

Infrastructure metrics are collected using Node Exporter, kube-state-metrics and cAdvisor.

### Logging

Container logs are collected from Kubernetes nodes and processed through ELK:

```text
Kubernetes containers
        ↓
     Filebeat
        ↓
     Logstash
        ↓
   Elasticsearch
        ↓
      Kibana
```

### Distributed Tracing

Applications are instrumented with the OpenTelemetry Java Agent:

```text
Spring Boot
     ↓
OpenTelemetry
     ↓
OTel Collector
     ↓
Jaeger
     ↓
Elasticsearch
```

This provides distributed tracing without requiring tracing-specific application code.

## Infrastructure

The environment consists of:

| Node           | Address        | Purpose                  |
| -------------- | -------------- | ------------------------ |
| `cicd`         | `10.10.10.100` | Gitea, Jenkins, Nexus    |
| `k3s-server`   | `10.10.10.101` | Kubernetes control plane |
| `k3s-worker-1` | `10.10.10.102` | Kubernetes worker        |
| `k3s-worker-2` | `10.10.10.103` | Kubernetes worker        |
| `ci-agent`     | `10.10.10.104` | Jenkins build agent      |

Infrastructure is provisioned using Vagrant and Ansible.

## Repository Structure

```text
.
├── product-service/
├── order-service/
├── infrastructure/
│   ├── ansible/
│   ├── helm/
│   │   ├── product-service/
│   │   └── order-service/
│   └── kubernetes/
│       ├── monitoring/
│       ├── elastic/
│       └── tracing/
└── Jenkinsfile
```

## Key DevOps Practices

* Infrastructure as Code with Ansible
* Kubernetes-based application deployment
* Helm-based release management
* Automated CI/CD
* Private artifact and container registry
* Kubernetes RBAC
* Secrets management
* Health and readiness probes
* Prometheus ServiceMonitors
* Centralized logging
* Distributed tracing
* Reproducible local infrastructure

## Goal

The goal of this project is to demonstrate practical DevOps engineering skills by building and operating a complete software delivery platform rather than focusing solely on the application itself.
