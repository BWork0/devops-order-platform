pipeline {
agent {
label 'docker'
}

environment {
    NEXUS_REGISTRY = '10.10.10.100:8081'
    MAVEN_REPOSITORY = 'devops-maven-releases'
    DOCKER_REPOSITORY = 'devops-docker'

    APP_VERSION = "0.0.1-${BUILD_NUMBER}"
    ORDER_IMAGE = "${NEXUS_REGISTRY}/${DOCKER_REPOSITORY}/order-service:${APP_VERSION}"
    PRODUCT_IMAGE = "${NEXUS_REGISTRY}/${DOCKER_REPOSITORY}/product-service:${APP_VERSION}"
}

stages {

    stage('Test') {
        steps {
            dir('order-service') {
                sh './mvnw -q -Drevision=${APP_VERSION} test'
            }

            dir('product-service') {
                sh './mvnw -q -Drevision=${APP_VERSION} test'
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
                    sh '''
                        ./mvnw -q \
                            -Drevision=${APP_VERSION} \
                            -s ../infrastructure/ci/settings.xml \
                            deploy
                    '''
                }

                dir('product-service') {
                    sh '''
                        ./mvnw -q \
                            -Drevision=${APP_VERSION} \
                            -s ../infrastructure/ci/settings.xml \
                            deploy
                    '''
                }
            }
        }
    }

    stage('Build Docker Images') {
        steps {
            sh '''
                docker build \
                    -t "$ORDER_IMAGE" \
                    order-service

                docker build \
                    -t "$PRODUCT_IMAGE" \
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

                    docker push "$ORDER_IMAGE"
                    docker push "$PRODUCT_IMAGE"

                    docker logout "$NEXUS_REGISTRY"
                '''
            }
        }
    }

    stage('Validate Helm Charts') {
    steps {
        sh '''
            helm lint infrastructure/helm/product-service
            helm lint infrastructure/helm/order-service
        '''
    }
}

stage('Deploy to K3s') {
    steps {
        withKubeConfig([
            credentialsId: 'k3s-jenkins-token',
            serverUrl: 'https://10.10.10.101:6443',
            namespace: 'devops',
            caCertificate: '''-----BEGIN CERTIFICATE-----
MIIBeDCCAR2gAwIBAgIBADAKBggqhkjOPQQDAjAjMSEwHwYDVQQDDBhrM3Mtc2Vy
dmVyLWNhQDE3OTExMDUwMTIwHhcNMjYxMDA0MDgxMDEyWhcNMzYxMDAxMDgxMDEy
WjAjMSEwHwYDVQQDDBhrM3Mtc2VydmVyLWNhQDE3OTExMDUwMTIwWTATBgcqhkjO
PQIBBggqhkjOPQMBBwNCAAQjg7NaWOuDMOhekyoLA1vKJKQJZcaXpe9NQKno/GCc
ickSDAcwV5tlN47bWMQCZfhxGAFUcSDIPrKSUZTuEoCjo0IwQDAOBgNVHQ8BAf8E
BAMCAqQwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUnLZUQ0GWxCJfqsAWNdGr
IzVBgi8wCgYIKoZIzj0EAwIDSQAwRgIhAM57cO7s57U5V2RIeeQEt4dgdJih6EiF
lFSgwd+EUoa7AiEA8kQwnXP8Poz3gEqHUzfuZbd1n9FYi20Wltuwn/OSceA=
-----END CERTIFICATE-----'''
        ]) {
            sh '''
                helm upgrade --install product-service \
                    infrastructure/helm/product-service \
                    --namespace devops \
                    --set-string image.tag="$APP_VERSION" \
                    --wait \
                    --rollback-on-failure \
                    --timeout 5m

                helm upgrade --install order-service \
                    infrastructure/helm/order-service \
                    --namespace devops \
                    --set-string image.tag="$APP_VERSION" \
                    --wait \
                    --rollback-on-failure \
                    --timeout 5m
            '''
        }
    }
}
}

post {
    always {
        sh '''
            docker logout "$NEXUS_REGISTRY" || true
        '''
    }
}

}
