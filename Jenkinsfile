pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                sh './order-service/mvnw -q test'
                sh './product-service/mvnw -q test'
            }
        }
    }
}