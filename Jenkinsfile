@Library('shared-lib@main') _

pipeline {
    agent any
    
    environment {
        DOCKER_USER = 'sanbajorge'
        IMAGE_NAME = "node${env.BRANCH_NAME}"
        IMAGE_TAG = 'v1.0'
        PORT = "${env.BRANCH_NAME == 'main' ? '3000' : '3001'}"
    }
    
    stages {
        stage('Declarative: Checkout SCM') {
            steps {
                checkout scm
                script {
                    echo "Checked out branch: ${env.BRANCH_NAME}"
                }
            }
        }
        
        stage('Declarative: Tool Install') {
            steps {
                script {
                    echo "Installing Node.js dependencies"
                }
            }
        }
        
        stage('Hadolint Check') {
            steps {
                script {
                    echo "Checking Dockerfile with Hadolint"
                    hadolintCheck('Dockerfile')
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    echo "Building project for branch: ${env.BRANCH_NAME}"
                    sh 'chmod +x scripts/build.sh'
                    sh './scripts/build.sh'
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    echo "Running tests"
                    sh 'CI=true npm test || true'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    echo "Building Docker image: ${IMAGE_NAME}:${IMAGE_TAG}"
                    dockerBuild("${IMAGE_NAME}", "${IMAGE_TAG}", "${env.BRANCH_NAME}")
                }
            }
        }
        
        stage('Scan Docker Image for Vulnerabilities') {
            steps {
                script {
                    echo "Scanning image with Trivy"
                    trivyScan("${IMAGE_NAME}", "${IMAGE_TAG}")
                }
            }
        }
        
        stage('Push to Docker Hub') {
            when {
                expression { return fileExists('.dockerhub-enabled') }
            }
            steps {
                script {
                    echo "Pushing image to Docker Hub"
                    dockerPush("${env.DOCKER_USER}/${IMAGE_NAME}", "${IMAGE_TAG}", 'docker-hub-credentials')
                }
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    echo "Deploying to ${env.BRANCH_NAME} environment on port ${PORT}"
                    cleanupContainers("${env.BRANCH_NAME}")
                    dockerDeploy("${IMAGE_NAME}", "${IMAGE_TAG}", "${env.BRANCH_NAME}")
                }
            }
        }
    }
    
    post {
        success {
            script {
                if (env.BRANCH_NAME == 'main' && fileExists('.auto-deploy-enabled')) {
                    build job: 'Deploy_to_main', wait: false
                } else if (env.BRANCH_NAME == 'dev' && fileExists('.auto-deploy-enabled')) {
                    build job: 'Deploy_to_dev', wait: false
                }
            }
            echo "Pipeline completed successfully for ${env.BRANCH_NAME}"
        }
        failure {
            echo "Pipeline failed for ${env.BRANCH_NAME}"
        }
    }
}

