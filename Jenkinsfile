#!/usr/bin/env groovy

/**
 * Pipeline CI de NeuroInventory.
 *
 * Pensada para el Jenkins de docker-compose.jenkins.yml, que ya incluye
 * JDK 21, Node 24, Python 3 y el CLI de Docker con acceso al socket del host.
 *
 * Etapas:
 *   1. Checkout
 *   2. Backend   -> ./gradlew test (Testcontainers sobre el daemon del host)
 *   3. Frontend  -> npm ci + build de producción
 *   4. AI        -> pytest del ai-service
 *   5. MCP       -> comprobación de sintaxis
 *   6. SonarQube -> análisis del monorepo y consulta del quality gate
 *   7. Imágenes  -> docker build de los cuatro servicios (opcional)
 *   8. Publicar  -> docker push a Docker Hub (opcional, requiere credencial)
 */

pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10'))
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    parameters {
        booleanParam(
            name: 'RUN_BACKEND_TESTS',
            defaultValue: true,
            description: 'Ejecuta las pruebas de inventory-service (Gradle + Testcontainers).'
        )
        booleanParam(
            name: 'RUN_FRONTEND_BUILD',
            defaultValue: true,
            description: 'Instala dependencias y compila el frontend Angular.'
        )
        booleanParam(
            name: 'RUN_AI_TESTS',
            defaultValue: true,
            description: 'Ejecuta las pruebas del ai-service (pytest).'
        )
        booleanParam(
            name: 'RUN_SONAR_ANALYSIS',
            defaultValue: true,
            description: 'Ejecuta el análisis de SonarQube y consulta el quality gate.'
        )
        booleanParam(
            name: 'ENFORCE_QUALITY_GATE',
            defaultValue: false,
            description: 'Falla el build si el quality gate de SonarQube no es OK. Por defecto solo informa.'
        )
        booleanParam(
            name: 'BUILD_IMAGES',
            defaultValue: true,
            description: 'Construye las imágenes Docker de los cuatro servicios.'
        )
        booleanParam(
            name: 'PUSH_IMAGES',
            defaultValue: false,
            description: 'Publica las imágenes en Docker Hub con la credencial `dockerhub`.'
        )
        string(
            name: 'IMAGE_TAG',
            defaultValue: '',
            description: 'Tag de las imágenes. Si está vacío se usa build-<número de build>.'
        )
    }

    environment {
        CI = 'true'
        NG_CLI_ANALYTICS = 'false'

        // Cachés persistentes dentro de JENKINS_HOME (volumen de Jenkins).
        GRADLE_USER_HOME = '/var/jenkins_home/.cache/gradle'
        npm_config_cache = '/var/jenkins_home/.cache/npm'
        PIP_CACHE_DIR = '/var/jenkins_home/.cache/pip'

        // Testcontainers contra el daemon de Docker del host.
        TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
        TESTCONTAINERS_RYUK_DISABLED = 'true'

        DOCKERHUB_CREDENTIALS = 'dockerhub'

        // SonarQube: servidor y credencial declarados en JCasC. El escáner se
        // ejecuta en un contenedor hermano unido a la red de Compose.
        SONAR_SERVER = 'SonarQube'
        SONAR_SCANNER_IMAGE = 'sonarsource/sonar-scanner-cli:12.3'
        SONAR_NETWORK = 'neuroinventory-jenkins_default'
        SONAR_PROJECT_KEY = 'neuroinventory'
        JENKINS_CONTAINER = 'neuroinventory-jenkins'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                script {
                    def requestedTag = params.IMAGE_TAG?.trim()
                    env.IMAGE_TAG_RESOLVED = requestedTag ? requestedTag : "build-${env.BUILD_NUMBER}"
                    env.GIT_COMMIT_SHORT = sh(
                        returnStdout: true,
                        script: 'git rev-parse --short HEAD'
                    ).trim()
                }
                echo "Commit ${env.GIT_COMMIT_SHORT} · imágenes con tag ${env.IMAGE_TAG_RESOLVED}"
            }
        }

        stage('Backend · inventory-service') {
            when { expression { params.RUN_BACKEND_TESTS } }
            steps {
                dir('inventory-service') {
                    sh './gradlew test --no-daemon'
                }
            }
            post {
                always {
                    junit(
                        testResults: 'inventory-service/build/test-results/test/TEST-*.xml',
                        allowEmptyResults: true
                    )
                    archiveArtifacts(
                        artifacts: 'inventory-service/build/reports/tests/test/**',
                        allowEmptyArchive: true
                    )
                }
            }
        }

        stage('Frontend · Angular') {
            when { expression { params.RUN_FRONTEND_BUILD } }
            steps {
                dir('frontend') {
                    sh 'npm ci --legacy-peer-deps'
                    sh 'npm run build'
                }
            }
            post {
                always {
                    archiveArtifacts(
                        artifacts: 'frontend/dist/**',
                        allowEmptyArchive: true
                    )
                }
            }
        }

        stage('AI Service · pytest') {
            when { expression { params.RUN_AI_TESTS } }
            steps {
                dir('ai-service') {
                    sh '''
                        set -e
                        python3 -m venv .venv
                        . .venv/bin/activate
                        python -m pip install --quiet --upgrade pip
                        python -m pip install --quiet -r requirements.txt -r requirements-dev.txt
                        mkdir -p reports
                        python -m pytest -q --junitxml=reports/junit.xml \
                            --cov=app --cov-report=xml:reports/coverage.xml
                    '''
                }
            }
            post {
                always {
                    junit(testResults: 'ai-service/reports/junit.xml', allowEmptyResults: true)
                }
            }
        }

        stage('MCP Server · sintaxis') {
            steps {
                dir('mcp-server') {
                    // El módulo aún no tiene pruebas propias (solo tests/__init__.py):
                    // se valida que todo el código compile.
                    sh 'python3 -m compileall -q app main.py'
                }
            }
        }

        stage('SonarQube · análisis') {
            when { expression { params.RUN_SONAR_ANALYSIS } }
            steps {
                // `withSonarQubeEnv` inyecta SONAR_HOST_URL y SONAR_AUTH_TOKEN
                // desde el servidor configurado en JCasC.
                withSonarQubeEnv(env.SONAR_SERVER) {
                    sh '''
                        set -e
                        # El análisis de Java necesita las clases compiladas. Si
                        # se desactivaron las pruebas del backend, se compilan
                        # aquí para no analizar a medias.
                        if [ ! -d inventory-service/build/classes/java/main ]; then
                            (cd inventory-service && ./gradlew compileJava --no-daemon)
                        fi
                        # El escáner se ejecuta en un contenedor hermano del
                        # daemon del host. --volumes-from monta el volumen de
                        # Jenkins en la misma ruta para poder leer el workspace.
                        docker run --rm --user root \
                            --volumes-from "$JENKINS_CONTAINER" \
                            --workdir "$WORKSPACE" \
                            --network "$SONAR_NETWORK" \
                            -e SONAR_HOST_URL="$SONAR_HOST_URL" \
                            -e SONAR_TOKEN="$SONAR_AUTH_TOKEN" \
                            "$SONAR_SCANNER_IMAGE"
                    '''
                }
            }
        }

        stage('SonarQube · quality gate') {
            when { expression { params.RUN_SONAR_ANALYSIS } }
            steps {
                withSonarQubeEnv(env.SONAR_SERVER) {
                    script {
                        // SonarQube rechaza webhooks hacia URLs internas, así
                        // que se consulta el estado del gate por API.
                        def gateStatus = sh(
                            returnStdout: true,
                            script: 'python3 scripts/sonar-quality-gate.py'
                        ).trim()

                        echo "Quality gate de SonarQube: ${gateStatus}"
                        if (gateStatus != 'OK' && params.ENFORCE_QUALITY_GATE) {
                            error("Quality gate ${gateStatus}. Revisa ${env.SONAR_HOST_URL}/dashboard?id=${env.SONAR_PROJECT_KEY}")
                        }
                    }
                }
            }
        }

        stage('Imágenes Docker') {
            when { expression { params.BUILD_IMAGES } }
            steps {
                script {
                    ['inventory-service', 'frontend', 'ai-service', 'mcp-server'].each { service ->
                        sh "docker build -t neuroinventory/${service}:${env.IMAGE_TAG_RESOLVED} ./${service}"
                    }
                }
            }
        }

        stage('Publicar imágenes') {
            when { expression { params.PUSH_IMAGES } }
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', env.DOCKERHUB_CREDENTIALS) {
                        ['inventory-service', 'frontend', 'ai-service', 'mcp-server'].each { service ->
                            def image = docker.image("neuroinventory/${service}:${env.IMAGE_TAG_RESOLVED}")
                            image.push()
                            image.push('latest')
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            // Ryuk está deshabilitado: se limpian los contenedores efímeros que
            // dejan las pruebas de Testcontainers en el daemon del host.
            sh 'docker rm -f $(docker ps -aq --filter ancestor=postgres:16-alpine) >/dev/null 2>&1 || true'
            cleanWs(cleanWhenNotBuilt: false, deleteDirs: true, notFailBuild: true)
        }
        success {
            echo "Build ${env.BUILD_NUMBER} completado correctamente."
        }
        failure {
            echo "Build ${env.BUILD_NUMBER} fallido. Revisa los informes archivados."
        }
    }
}
