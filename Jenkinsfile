pipeline {
agent {
label 'docker'
}

```
environment {
    NEXUS_REGISTRY = '10.10.10.100:8081'
    DOCKER_REPOSITORY = 'devops-docker'
    IMAGE_TAG = "0.0.1-${BUILD_NUMBER}"
}

stages {
    stage('Test') {
        steps {
            dir('order-service') {
                sh './mvnw -q test'
            }

            dir('product-service') {
                sh './mvnw -q test'
            }
        }
    }

    stage('Deploy Maven Artifacts') {
        steps {
            withCredentials([
                usernamePassword(
                    credentialsId: 'nexus-jenkins',
                    usernameVariable: 'NEXUS_USERNAME',
                    passwordVariable: 'NEXUS_PASSWORD'
                )
            ]) {
                dir('order-service') {
                    sh './mvnw -q -s ../infrastructure/ci/settings.xml deploy'
                }

                dir('product-service') {
                    sh './mvnw -q -s ../infrastructure/ci/settings.xml deploy'
                }
            }
        }
    }

    stage('Build Docker Images') {
        steps {
            sh '''
                docker build \
                    -t ${NEXUS_REGISTRY}/${DOCKER_REPOSITORY}/order-service:${IMAGE_TAG} \
                    order-service

                docker build \
                    -t ${NEXUS_REGISTRY}/${DOCKER_REPOSITORY}/product-service:${IMAGE_TAG} \
                    product-service
            '''
        }
    }

    stage('Push Docker Images') {
        steps {
            withCredentials([
                usernamePassword(
                    credentialsId: 'nexus-jenkins',
                    usernameVariable: 'NEXUS_USERNAME',
                    passwordVariable: 'NEXUS_PASSWORD'
                )
            ]) {
                sh '''
                    set +x

                    echo "$NEXUS_PASSWORD" | docker login \
                        "$NEXUS_REGISTRY" \
                        --username "$NEXUS_USERNAME" \
                        --password-stdin

                    docker push \
                        ${NEXUS_REGISTRY}/${DOCKER_REPOSITORY}/order-service:${IMAGE_TAG}

                    docker push \
                        ${NEXUS_REGISTRY}/${DOCKER_REPOSITORY}/product-service:${IMAGE_TAG}

                    docker logout "$NEXUS_REGISTRY"
                '''
            }
        }
    }
}
```

}
