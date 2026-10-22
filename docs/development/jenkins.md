# Jenkins (CI)

Jenkins para ejecutar la integración continua del monorepo sin depender de las
herramientas instaladas en el host. Es **independiente** de
`docker-compose.yml`: solo levanta Jenkins y reutiliza el daemon de Docker del
host.

## Qué incluye

| Herramienta | Uso |
| --- | --- |
| JDK 21 | `inventory-service` (Gradle 9 + Testcontainers). |
| Node 24 | `frontend` (Angular). |
| Python 3 | `ai-service` (pytest) y `mcp-server`. |
| Docker CLI | Testcontainers y construcción de imágenes. |
| Plugins | Configuration as Code, Pipeline, Git, JUnit, Docker Workflow, etc. |

La imagen se define en [`docker/jenkins/Dockerfile`](../../docker/jenkins/Dockerfile)
y los plugins en [`docker/jenkins/plugins.txt`](../../docker/jenkins/plugins.txt).

## Puesta en marcha

```bash
cp .env.jenkins.example .env.jenkins     # ajusta la contraseña y el token
docker compose -f docker-compose.jenkins.yml --env-file .env.jenkins up -d --build
```

La primera construcción descarga los plugins de Jenkins y puede tardar unos
minutos. Después, la configuración se aplica sola:

* Usuario administrador local (`JENKINS_ADMIN_ID` / `JENKINS_ADMIN_PASSWORD`).
* Job **`neuroinventory-ci`** apuntando a `GIT_REPO_URL` (`Jenkinsfile`).
* Credencial `dockerhub` para publicar imágenes.

Acceso: <http://localhost:8085>

> El setup wizard está deshabilitado: el primer arranque ya queda configurado
> por JCasC.

Para cambiar la contraseña o el repositorio, edita `.env.jenkins` y recrea el
contenedor:

```bash
docker compose -f docker-compose.jenkins.yml --env-file .env.jenkins up -d --force-recreate
```

## Pipeline

El [`Jenkinsfile`](../../Jenkinsfile) es declarativo y tiene estas etapas:

| Etapa | Comando | Resultado |
| --- | --- | --- |
| Checkout | `checkout scm` | Código + tag de imagen (`build-<n>`). |
| Backend | `./gradlew test` | Resultados JUnit + informe HTML. |
| Frontend | `npm ci --legacy-peer-deps && npm run build` | `frontend/dist` archivado. |
| AI Service | `pytest` | Resultados JUnit. |
| MCP Server | `compileall` | Validación de sintaxis. |
| Imágenes | `docker build` | Imágenes `neuroinventory/<servicio>:<tag>`. |
| Publicar | `docker push` | Solo con `PUSH_IMAGES=true`. |

Parámetros del job:

| Parámetro | Por defecto | Descripción |
| --- | --- | --- |
| `RUN_BACKEND_TESTS` | `true` | Pruebas de `inventory-service`. |
| `RUN_FRONTEND_BUILD` | `true` | Instalación y build del frontend. |
| `RUN_AI_TESTS` | `true` | Pruebas de `ai-service`. |
| `BUILD_IMAGES` | `true` | Construye las cuatro imágenes. |
| `PUSH_IMAGES` | `false` | Publica en Docker Hub. |
| `IMAGE_TAG` | vacío | Si está vacío, `build-<número>`. |

Hay un trigger `scm('H/10 * * * *')`; puedes desactivarlo desde la UI del job.

## Testcontainers

Las pruebas del backend crean contenedores PostgreSQL en el **daemon del host**,
no dentro de Jenkins. Para que el JVM de las pruebas los alcance:

* `/var/run/docker.sock` se monta en el contenedor.
* `TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal` y
  `extra_hosts: host.docker.internal:host-gateway` permiten llegar al puerto
  publicado en el host.
* `TESTCONTAINERS_RYUK_DISABLED=true` evita el contenedor de limpieza, que no
  funciona de forma fiable en este esquema. El `post` del pipeline elimina los
  contenedores `postgres:16-alpine` sobrantes.

Los contenedores efímeros viven en el host: si un build se cancela, pueden
quedar restos. Se limpian con:

```bash
docker ps -aq --filter ancestor=postgres:16-alpine | xargs -r docker rm -f
```

## Cachés

| Caché | Ruta |
| --- | --- |
| Gradle | `/var/jenkins_home/.cache/gradle` |
| npm | `/var/jenkins_home/.cache/npm` |
| pip | `/var/jenkins_home/.cache/pip` |

Todas viven en el volumen `jenkins_home`, por lo que sobreviven entre builds.

## Personalización

**Repositorio y rama.** Se toman de `GIT_REPO_URL` y `GIT_BRANCH` en
`.env.jenkins`. JCasC vuelve a aplicar el job en cada arranque, así que edita
esas variables y recrea el contenedor; los cambios hechos a mano en la UI del
job se pierden al reiniciar.

**Credenciales de Docker Hub.** La credencial `dockerhub` se rellena con
`DOCKERHUB_USERNAME` y `DOCKERHUB_TOKEN`. Usa un token de acceso, no la
contraseña. Si la rotas en la UI, ten en cuenta que JCasC la sobrescribe en el
siguiente arranque.

**Plugins.** Añade el nombre a `docker/jenkins/plugins.txt` y reconstruye la
imagen con `--build`.

**Ejecutar como usuario `jenkins`.** El contenedor se ejecuta como `root`
porque el socket de Docker ya concede acceso root-equivalente y así se evitan
problemas de GID. Si prefieres el usuario de la imagen, añade en el compose:

```yaml
user: jenkins
group_add:
  - "999"   # sustituye por: stat -c '%g' /var/run/docker.sock
```

## Mantenimiento

```bash
# Estado y logs
docker compose -f docker-compose.jenkins.yml ps
docker compose -f docker-compose.jenkins.yml logs -f jenkins

# Parar sin borrar datos
docker compose -f docker-compose.jenkins.yml down

# Borrar también el volumen (historial, jobs y cachés)
docker compose -f docker-compose.jenkins.yml down -v
```

## Problemas habituales

| Síntoma | Causa / solución |
| --- | --- |
| `permission denied` al usar Docker | El socket no es accesible. Mantén `user: root` o añade el GID correcto con `group_add`. |
| `Could not find a valid Docker environment` | Falta el montaje de `/var/run/docker.sock` o `DOCKER_HOST`. |
| Testcontainers no conecta a PostgreSQL | Falta `extra_hosts` o `TESTCONTAINERS_HOST_OVERRIDE`. |
| El contenedor queda `unhealthy` | Revisa `docker compose logs jenkins`; el primer arranque tarda ~1 min. |
| El puerto 8085 está ocupado | Cambia `JENKINS_PORT` en `.env.jenkins`. |
| El job clona código antiguo | El job lee del remoto y la rama configurados; haz push antes de lanzarlo. |

## Seguridad

El socket de Docker da control efectivo sobre el host. Este Jenkins está
pensado para **uso local o de red interna**:

* Cambia `JENKINS_ADMIN_PASSWORD` antes del primer arranque.
* No publiques el puerto 8085 en Internet.
* No concedas acceso al job a usuarios no confiables.
