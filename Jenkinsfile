pipeline {
    agent any
    stages {

        stage('Compile') {
            steps {
                echo "Compiling..."
                sh "sbt compile -J-Xmx1280M"
            }
        }

        stage('Test') {
            steps {
                echo "Testing..."
                sh "sbt compile  -J-Xmx1280M"
            }
        }

        stage('Package') {
            steps {
                echo "Packaging..."
                sh "sbt package -J-Xmx1280M"
            }
        }

    }
}
