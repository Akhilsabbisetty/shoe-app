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
                withCredentials([usernamePassword(credentialsId: 'jfrog-creds',
                                                  usernameVariable: 'JFROG_USER',
                                                  passwordVariable: 'JFROG_PASS')]) {
                    // Create settings.xml with credentials injected
                    writeFile file: 'settings.xml', text: """
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>jfrog</id>
      <username>${env.JFROG_USER}</username>
      <password>${env.JFROG_PASS}</password>
    </server>
  </servers>
</settings>
"""
                    sh """
                      mvn clean deploy -s $MAVEN_SETTINGS \
                        -DaltDeploymentRepository=jfrog::default::https://artifactory.akhilsabbisetty.site/artifactory/maven-release
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
            when {
                expression { return fileExists('frontend/package.json') }
            }
            steps {
                dir('frontend') {
                    script {
                        // Check if node is available on agent
                        def hasNode = sh(script: 'command -v node >/dev/null 2>&1 && echo "yes" || echo "no"', returnStdout: true).trim()
                        if (hasNode == 'yes') {
                            echo "Node found on agent — running npm ci && npm run build"
                            sh '''
                                npm ci
                                npm run build
                            '''
                        } else {
                            // Node not present — try Docker fallback
                            def hasDocker = sh(script: 'command -v docker >/dev/null 2>&1 && echo "yes" || echo "no"', returnStdout: true).trim()
                            if (hasDocker == 'yes') {
                                echo "Node not found on agent — using node:18 Docker image to run frontend build"
                                // Use absolute path to workspace so docker volume mounts correctly
                                def frontendPath = "${env.WORKSPACE}/frontend"
                                sh """
                                    docker run --rm -v ${frontendPath}:/workspace -w /workspace node:18 bash -lc "npm ci && npm run build"
                                """
                            } else {
                                error "Node (npm) not found on agent and Docker is not available. Install Node or enable Docker on the Jenkins agent."
                            }
                        }
                    }
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

                      # Frontend image (build only if frontend exists)
                      if [ -f frontend/package.json ]; then
                        docker build -t $DOCKER_IMAGE:frontend-${BUILD_NUMBER} ./frontend
                        trivy image --exit-code 1 --severity HIGH,CRITICAL $DOCKER_IMAGE:frontend-${BUILD_NUMBER} || true
                        docker push $DOCKER_IMAGE:frontend-${BUILD_NUMBER}
                      else
                        echo "No frontend/package.json - skipping frontend image build"
                      fi

                      # Backend image (build from repo root)
                      docker build -t $DOCKER_IMAGE:backend-${BUILD_NUMBER} .
                      trivy image --exit-code 1 --severity HIGH,CRITICAL $DOCKER_IMAGE:backend-${BUILD_NUMBER} || true
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

    } // stages

    post {
        always { cleanWs() }
    }
}
