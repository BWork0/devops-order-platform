pipeline {
    agent any

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

        stage('Deploy to Nexus') {
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
    }
}