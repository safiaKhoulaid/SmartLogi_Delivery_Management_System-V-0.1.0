pipeline {
    agent any
	triggers {
		githubPush()
	}

    tools {
        maven 'Maven3'
        jdk 'Java17'
    }

    environment {

        DOCKER_IMAGE = "safiakhoulaid/smartlogi-backend"
        DOCKER_CREDS = 'docker-hub-creds'
        SSH_KEY_ID   = 'ec2-ssh-key'
        EC2_USER     = 'ec2-user'
        EC2_IP       = '51.21.186.55'
    }


    stages {
        stage('1. Checkout') {
            steps { checkout scm }
        }

        stage('2. Build Jar') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('3. Docker Build & Push') {
            steps {
                script {
                    docker.withRegistry('', DOCKER_CREDS) {
                        def app = docker.build("${DOCKER_IMAGE}:latest")
                        app.push()
                    }
                }
            }
        }

        stage('4. Deployment (CD)') {
                    steps {
                        sshagent([SSH_KEY_ID]) {
                            sh "scp -o StrictHostKeyChecking=no docker-compose.yml ${EC2_USER}@${EC2_IP}:/home/${EC2_USER}/"

                            sh """
                            ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_IP} "cd /home/${EC2_USER}/ && sudo docker-compose down && sudo docker-compose pull && sudo docker-compose up -d"
                            """
                        }
                    }
        }

        stage('5. AI Analysis (ReAct)') {
            steps {
                script {
                    sh 'mvn clean verify > build.log 2>&1 || true'

                    sh """
                    curl -X POST http://51.21.186.55:8080/api/v1/ai/analyze-cicd \
                         -H "Content-Type: text/plain" \
                         --data-binary "@build.log"
                    """
                }
            }
        }
    }
}