pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'Java17'
    }

    environment {
        DOCKER_IMAGE = "safiakhoulaid/sdms-backend"
        DOCKER_CREDS = 'docker-hub-creds' // الـ ID ديال الـ Credentials فـ Jenkins
    }

    stages {
        stage('1. Checkout Code') {
            steps {
                // كايجيب الكود من GitHub
                checkout scm
            }
        }

       /* stage('2. Unit Tests & JaCoCo') {
            steps {
                // تنفيذ التستات وكايخرج التقرير ديال JaCoCo
                sh 'mvn clean verify jacoco:report'
            }
        }*/

        stage('3. Build Jar') {
            steps {
                // كايصاوب الـ JAR اللي غايتحط فـ Docker Image
                sh 'mvn package -DskipTests'
            }
        }

        stage('4. Docker Build & Tag') {
            steps {
                script {
                    // بناء الـ Image باستعمال الـ Dockerfile ديالك
                    sh "docker build -t ${DOCKER_IMAGE}:${env.BUILD_NUMBER} ."
                    sh "docker tag ${DOCKER_IMAGE}:${env.BUILD_NUMBER} ${DOCKER_IMAGE}:latest"
                }
            }
        }

        stage('5. Docker Push') {
            steps {
                script {
                    // كايصيفط الـ Image لـ Docker Hub
                    docker.withRegistry('', DOCKER_CREDS) {
                        sh "docker push ${DOCKER_IMAGE}:${env.BUILD_NUMBER}"
                        sh "docker push ${DOCKER_IMAGE}:latest"
                    }
                }
            }
        }
    }

    post {
        always {
            // كاينقي الـ Workspace مورا ما يسالي
            cleanWs()
        }
    }
}