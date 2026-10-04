pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Order Service') {
            steps {
                sh './gradlew :order-service:clean :order-service:build -x test'
            }
        }
    }

    post {
        success {
            echo 'PR build PASSED'
        }

        failure {
            echo 'PR build FAILED'
        }
    }
}