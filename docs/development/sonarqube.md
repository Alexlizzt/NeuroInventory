# SonarQube Community (calidad de código)

SonarQube Community analiza **todo el monorepo** (backend Spring Boot, frontend
Angular, `ai-service` y `mcp-server`) y se levanta junto a Jenkins con el mismo
`docker-compose.jenkins.yml`. Es independiente de `docker-compose.yml`, igual
que el CI.

| Servicio | Contenedor | Puerto | Datos |
| --- | --- | --- | --- |
| `sonarqube` | `neuroinventory-sonarqube` | `9000` | `sonar_data`, `sonar_extensions`, `sonar_logs` |
| `sonar-db` | `neuroinventory-sonar-db` | — | `sonar_db` (PostgreSQL 16) |

Acceso: <http://localhost:9000>

## Requisito del host

SonarQube usa Elasticsearch y necesita más descriptores de archivo que el valor
por defecto de Linux:

```bash
sysctl vm.max_map_count          # debe ser >= 524288
sudo sysctl -w vm.max_map_count=524288

# Persistente entre reinicios:
echo 'vm.max_map_count=524288' | sudo tee /etc/sysctl.d/99-sonarqube.conf
```

El compose ya aplica `ulimits.nofile=131072` al contenedor.

## Puesta en marcha

```bash
cp .env.jenkins.example .env.jenkins   # define SONAR_ADMIN_PASSWORD (>= 12 caracteres)
docker compose -f docker-compose.jenkins.yml --env-file .env.jenkins up -d --build
```

Espera a que ambos servicios estén `healthy`:

```bash
docker compose -f docker-compose.jenkins.yml --env-file .env.jenkins ps
```

### Inicialización (una sola vez)

SonarQube arranca con `admin` / `admin`. El script de bootstrap espera al
servidor, cambia la contraseña y genera el token de análisis, que escribe en
`.env.jenkins`:

```bash
./scripts/sonar-init.sh

# Recarga Jenkins para que JCasC lea la nueva credencial `sonarqube-token`:
docker compose -f docker-compose.jenkins.yml --env-file .env.jenkins \
    up -d --force-recreate jenkins
```

El script es idempotente: si el admin ya tiene `SONAR_ADMIN_PASSWORD`, no la
cambia; el token `jenkins` se regenera en cada ejecución.

| Variable | Uso |
| --- | --- |
| `SONAR_PORT` | Puerto publicado (por defecto `9000`). |
| `SONAR_DB_*` | Credenciales de PostgreSQL para SonarQube. |
| `SONAR_ADMIN_PASSWORD` | Contraseña final del usuario `admin`. |
| `SONAR_TOKEN` | Token de análisis; lo rellena el script. |

## Análisis en el pipeline

El [`Jenkinsfile`](../../Jenkinsfile) tiene dos etapas nuevas:

1. **SonarQube · análisis**: ejecuta el escáner oficial
   (`sonarsource/sonar-scanner-cli:12.3`) en un contenedor hermano del daemon
   del host. El contenedor se une a la red de Compose
   (`neuroinventory-jenkins_default`) y monta el workspace de Jenkins con
   `--volumes-from`, por lo que no hace falta instalar el escáner en la imagen
   de Jenkins. La configuración está en
   [`sonar-project.properties`](../../sonar-project.properties).
2. **SonarQube · quality gate**: espera a que termine la tarea en el *Compute
   Engine* y consulta `/api/qualitygates/project_status` con
   [`scripts/sonar-quality-gate.py`](../../scripts/sonar-quality-gate.py).

El quality gate es **informativo por defecto**: la etapa imprime `OK`, `WARN`,
`ERROR` o `NONE` y no rompe el build. Para que lo haga, lanza el job con
`ENFORCE_QUALITY_GATE=true`.

| Parámetro | Por defecto | Descripción |
| --- | --- | --- |
| `RUN_SONAR_ANALYSIS` | `true` | Ejecuta análisis + consulta del quality gate. |
| `ENFORCE_QUALITY_GATE` | `false` | Falla el build si el gate no es `OK`. |

## Qué se analiza

| Módulo | Fuentes | Cobertura |
| --- | --- | --- |
| `inventory-service` | `src/main/java` (Java 21) | JaCoCo XML (`jacocoTestReport` ya se ejecuta en las pruebas). |
| `frontend` | `src/**/*.ts`, HTML y SCSS; los `*.spec.ts` se tratan como tests. | Pendiente: requiere `ng test` con cobertura (Chrome/Chromium). |
| `ai-service` | `app/` y `main.py` (Python 3.13) | `pytest-cov` → `ai-service/reports/coverage.xml`. |
| `mcp-server` | `app/` y `main.py` | Sin pruebas propias todavía. |

Se excluyen `node_modules`, `dist`, `build`, `.venv`, `__pycache__`,
`coverage`, minificados, declaraciones `.d.ts` y `frontend/src/assets`.

## Quality gate

El **Sonar way** por defecto exige, entre otras cosas, cobertura ≥ 80 % en
código nuevo. En un proyecto recién analizado es habitual que el gate salga
`ERROR`; por eso el pipeline es informativo hasta que se estabilicen las
métricas. Puedes ajustar el gate en *Quality Gates* de la UI o crear uno propio
más laxo para las primeras iteraciones.

No se registran webhooks: SonarQube valida las URLs de destino
(`sonar.validateWebhooks`) y rechaza las direcciones internas del stack de
Compose. Por eso el pipeline consulta el estado por API. Si en el futuro
necesitas usar `waitForQualityGate()`, tendrás que desactivar esa validación
en SonarQube (`sonar.validateWebhooks=false`) asumiendo el riesgo de SSRF.

## Uso manual

```bash
# Análisis desde la raíz del repositorio
docker run --rm \
  --network neuroinventory-jenkins_default \
  -e SONAR_HOST_URL=http://sonarqube:9000 \
  -e SONAR_TOKEN=<token> \
  -v "$PWD:/usr/src" \
  sonarsource/sonar-scanner-cli:12.3

# Estado del quality gate
SONAR_HOST_URL=http://localhost:9000 SONAR_AUTH_TOKEN=<token> \
  python3 scripts/sonar-quality-gate.py
```

## Mantenimiento

```bash
# Estado y logs
docker compose -f docker-compose.jenkins.yml ps
docker compose -f docker-compose.jenkins.yml logs -f sonarqube

# Parar sin borrar datos
docker compose -f docker-compose.jenkins.yml down

# Borrar también los datos (proyectos, historial y base de datos)
docker compose -f docker-compose.jenkins.yml down -v
```

## Problemas habituales

| Síntoma | Causa / solución |
| --- | --- |
| `max virtual memory areas vm.max_map_count [65530] is too low` | Aplica el `sysctl` indicado arriba. |
| El contenedor queda `unhealthy` | El primer arranque tarda 1–2 min; revisa `logs sonarqube`. |
| El puerto 9000 está ocupado | Cambia `SONAR_PORT` en `.env.jenkins`. |
| `Not authorized` en el análisis | Ejecuta `scripts/sonar-init.sh` y recrea Jenkins. |
| Quality gate en `NONE` | El proyecto aún no tiene análisis procesado; revisa la etapa anterior. |
| `Please provide compiled classes` | El análisis de Java necesita `inventory-service/build/classes/java/main`; ejecuta la etapa de backend o `./gradlew compileJava`. |
