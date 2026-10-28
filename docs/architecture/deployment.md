# Despliegue

## Docker Compose

Es el entorno de desarrollo principal. Requiere Docker y Docker Compose; para
inferencia con GPU en Ollama, drivers NVIDIA y `nvidia-container-toolkit`:

```bash
docker compose up -d
```

Los detalles de variables, puertos y servicios están en
[`docs/development/env-setup.md`](../development/env-setup.md).

## Kubernetes (minikube)

El stack completo se puede desplegar en un clúster local de minikube con
Ingress:

```bash
minikube start --memory=12288 --cpus=6
./scripts/k8s-up.sh
```

La arquitectura, los manifiestos (`deploy/k8s/`), la configuración runtime del
frontend, los comandos de operación y las limitaciones están documentados en
[`kubernetes.md`](kubernetes.md).
