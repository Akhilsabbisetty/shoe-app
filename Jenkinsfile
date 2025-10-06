pipeline {
    agent any

    environment {
        MAVEN_SETTINGS = "${env.WORKSPACE}/settings.xml"
        APP_NAME       = "shoe-app"
        DOCKER_IMAGE   = "akhilsabbisetty/shoe-app"
        K8S_NAMESPACE  = "shoes"
        SONAR_URL      = "http://13.201.95.250:9000"
        ARGOCD_SERVER  = "argocd.akhilsabbisetty.site"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Akhilsabbisetty/shoe-app.git',
                    credentialsId: 'github-creds'
            }
        }

        stage('Maven Build & Deploy to JFrog') {
            steps {
                writeFile file: 'settings.xml', text: '''
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>jfrog</id>
    </server>
  </servers>
</settings>
'''
                withCredentials([usernamePassword(credentialsId: 'jFrog-Cred',
                                                  usernameVariable: 'JFROG_USER',
                                                  passwordVariable: 'JFROG_PASS')]) {
                    sh """
                      mvn clean deploy -s $MAVEN_SETTINGS \
                        -DaltDeploymentRepository=jfrog::default::https://artifactory.akhilsabbisetty.site/artifactory/maven-release \
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
                        -Dsonar.host.url=$SONAR_URL \
                        -Dsonar.login=$SONAR_TOKEN
                    """
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('frontend') {
                    sh '''
                      # Ensure Node is available
                      if ! command -v node &> /dev/null; then
                        curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
                        apt-get install -y nodejs
                      fi

                      npm ci
                      npm run build
                    '''
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

                      # Frontend image
                      docker build -t $DOCKER_IMAGE:frontend-${BUILD_NUMBER} ./frontend

                      # Backend image
                      docker build -t $DOCKER_IMAGE:backend-${BUILD_NUMBER} .

                      # Trivy scan
                      trivy image --exit-code 1 --severity HIGH,CRITICAL $DOCKER_IMAGE:frontend-${BUILD_NUMBER}
                      trivy image --exit-code 1 --severity HIGH,CRITICAL $DOCKER_IMAGE:backend-${BUILD_NUMBER}

                      # Push to registry
                      docker push $DOCKER_IMAGE:frontend-${BUILD_NUMBER}
                      docker push $DOCKER_IMAGE:backend-${BUILD_NUMBER}
                    """
                }
            }
        }

        stage('Argo CD Deploy') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'argocd-creds',
                                                  usernameVariable: 'ARGO_USER',
                                                  passwordVariable: 'ARGO_PASS')]) {
                    sh """
                      argocd login $ARGOCD_SERVER --username $ARGO_USER --password $ARGO_PASS --insecure
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
    }
}
