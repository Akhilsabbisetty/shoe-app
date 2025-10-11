pipeline {
  agent any

  environment {
    MAVEN_SETTINGS = "${env.WORKSPACE}/settings.xml"
    APP_NAME       = "shoe-app"
    DOCKER_IMAGE   = "akhilsabbisetty/shoe-app"
    ARGOCD_SERVER  = "argocd.akhilsabbisetty.site"
    SONAR_URL      = "http://3.6.40.138:9000"
    TRIVY_SEVERITY = "HIGH,CRITICAL"
  }

  stages {

    stage('Checkout') {
      steps {
        cleanWs()
        git branch: 'main',
            url: 'https://github.com/Akhilsabbisetty/shoe-app.git',
            credentialsId: 'github-creds'
      }
    }

    stage('Build & Sonar Scan (Parallel)') {
      parallel {

        stage('Backend Build + Sonar') {
          steps {
            dir('backend') {
              // Build Backend
              sh 'mvn -B -DskipTests clean package'

              // SonarQube Scan
              withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                sh """
                  mvn sonar:sonar \
                    -Dsonar.host.url=$SONAR_URL \
                    -Dsonar.token=$SONAR_TOKEN
                """
              }
            }
          }
        }

        stage('Frontend Build + Sonar') {
          steps {
            dir('frontend') {
              // Install & Build Frontend
              sh 'npm install'
              sh 'npm run build'

              // SonarQube Scan via Docker Scanner CLI
              withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                sh """
                  docker run --rm \
                    -v \$(pwd):/usr/src \
                    -w /usr/src \
                    --user \$(id -u):\$(id -g) \
                    sonarsource/sonar-scanner-cli:latest \
                    sonar-scanner \
                      -Dsonar.projectKey=shoes-frontend \
                      -Dsonar.sources=. \
                      -Dsonar.host.url=$SONAR_URL \
                      -Dsonar.token=$SONAR_TOKEN \
                      -Dsonar.exclusions=node_modules/**,build/**
                """
              }
            }
          }
        }
      }
    }

    stage('Docker Build & Push (Parallel)') {
      parallel {
        stage('Backend Docker Build & Push') {
          steps {
            withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
              sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
              sh 'docker build -f backend/Dockerfile -t ${DOCKER_IMAGE}:backend-${BUILD_NUMBER} .'
              sh 'docker push ${DOCKER_IMAGE}:backend-${BUILD_NUMBER}'
            }
          }
        }

        stage('Frontend Docker Build & Push') {
          steps {
            dir('frontend') {
              withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                sh 'docker build -t ${DOCKER_IMAGE}:frontend-${BUILD_NUMBER} .'
                sh 'docker push ${DOCKER_IMAGE}:frontend-${BUILD_NUMBER}'
              }
            }
          }
        }
      }
    }

    stage('Trivy Scan (Parallel)') {
      parallel {
        stage('Backend Trivy Scan') {
          steps {
            sh """
              trivy image --exit-code 1 --severity ${TRIVY_SEVERITY} ${DOCKER_IMAGE}:backend-${BUILD_NUMBER} \
              || (echo "❌ Trivy found issues in backend image" && exit 1)
            """
          }
        }

        stage('Frontend Trivy Scan') {
          steps {
            sh """
              trivy image --exit-code 1 --severity ${TRIVY_SEVERITY} ${DOCKER_IMAGE}:frontend-${BUILD_NUMBER} \
              || (echo "❌ Trivy found issues in frontend image" && exit 1)
            """
          }
        }
      }
    }

    stage('Update K8s Manifests') {
      steps {
        sh """
          sed -i 's|REPLACE_BACKEND_IMAGE|${DOCKER_IMAGE}:backend-${BUILD_NUMBER}|g' k8s/backend-deployment.yaml || true
          sed -i 's|REPLACE_FRONTEND_IMAGE|${DOCKER_IMAGE}:frontend-${BUILD_NUMBER}|g' k8s/frontend-deployment.yaml || true
        """
      }
    }

    stage('ArgoCD Sync') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'argocd-creds', usernameVariable: 'ARGO_USER', passwordVariable: 'ARGO_PASS')]) {
          sh """
            argocd login $ARGOCD_SERVER --username $ARGO_USER --password $ARGO_PASS --insecure || true
            argocd app sync ${APP_NAME} || true
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
