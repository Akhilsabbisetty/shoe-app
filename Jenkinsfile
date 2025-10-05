pipeline {
    agent any
    tools {
        jdk 'JDK17'
        maven 'Maven'
    }
    environment {
        MAVEN_SETTINGS = "${env.WORKSPACE}/settings.xml"
        APP_NAME = "shoe-app"
        DOCKER_IMAGE_FRONTEND = "yourdockerhubusername/shoes-frontend"
        DOCKER_IMAGE_BACKEND = "yourdockerhubusername/shoes-backend"
        K8S_NAMESPACE = "shoes"
        JFROG_URL = 'https://artifactory.akhilsabbisetty.site'
        SONAR_URL = 'http://13.201.95.250:9000' // replace with your SonarQube IP
        MAIL_TO = 'sabbisettyakhil2414@gmail.com'
    }

    stages {

        stage('Checkout Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Akhilsabbisetty/shoe-app.git',
                    credentialsId: 'github-token'
            }
        }

        stage('Maven Build & Deploy to JFrog') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'jFrog-Cred',
                                                  usernameVariable: 'JFROG_USER',
                                                  passwordVariable: 'JFROG_PASS')]) {
                    sh """
                      mvn clean install -s $MAVEN_SETTINGS \
                        -DaltDeploymentRepository=jfrog::default::$JFROG_URL/artifactory/maven-release \
                        -Djfrog.user=$JFROG_USER \
                        -Djfrog.password=$JFROG_PASS
                    """
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh """
                      mvn sonar:sonar \
                        -Dsonar.projectKey=$APP_NAME \
                        -Dsonar.host.url=$SONAR_URL \
                        -Dsonar.login=$SONAR_TOKEN
                    """
                }
            }
        }

        stage('Docker Build, Trivy Scan & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DOCKER_USER',
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                      docker login -u $DOCKER_USER -p $DOCKER_PASS
                      docker build -t $DOCKER_IMAGE_FRONTEND:latest ./frontend
                      docker build -t $DOCKER_IMAGE_BACKEND:latest ./backend
                      
                      trivy image --exit-code 1 --severity HIGH,CRITICAL $DOCKER_IMAGE_FRONTEND:latest
                      trivy image --exit-code 1 --severity HIGH,CRITICAL $DOCKER_IMAGE_BACKEND:latest
                      
                      docker push $DOCKER_IMAGE_FRONTEND:latest
                      docker push $DOCKER_IMAGE_BACKEND:latest
                    """
                }
            }
        }

        stage('ArgoCD Deploy') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'argocd-creds',
                                                  usernameVariable: 'ARGO_USER',
                                                  passwordVariable: 'ARGO_PASS')]) {
                    sh """
                      argocd login argocd.akhilsabbisetty.site --username $ARGO_USER --password $ARGO_PASS --insecure
                      argocd app sync $APP_NAME
                      argocd app wait $APP_NAME --health --timeout 300
                    """
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            mail to: "$MAIL_TO",
                 subject: "Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Please check Jenkins build logs at ${env.BUILD_URL}"
        }
        success {
            mail to: "$MAIL_TO",
                 subject: "Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Congratulations! Build & deploy succeeded.\nCheck ArgoCD app status at argocd.akhilsabbisetty.site"
        }
    }
}
