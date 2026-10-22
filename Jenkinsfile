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
 *   6. Imágenes  -> docker build de los cuatro servicios (opcional)
 *   7. Publicar  -> docker push a Docker Hub (opcional, requiere credencial)
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
                        python -m pytest -q --junitxml=reports/junit.xml
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
