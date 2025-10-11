pipeline {
  agent any

  environment {
    MAVEN_SETTINGS = "${env.WORKSPACE}/settings.xml"
    APP_NAME       = "shoe-app"
    DOCKER_IMAGE   = "akhilsabbisetty/shoe-app"
    ARGOCD_SERVER  = "a85d1510c705a43a19eabc55ff45e3f0-216968049.ap-south-1.elb.amazonaws.com"
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
        sh 'ls -R'
      }
    }

    stage('Build Backend') {
      steps {
        dir('backend') {
          sh 'mvn -B -DskipTests clean package'
        }
      }
    }

    stage('SonarQube Scan - Backend') {
      steps {
        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
          dir('backend') {
            sh """
              mvn sonar:sonar \
                -Dsonar.host.url=$SONAR_URL \
                -Dsonar.token=$SONAR_TOKEN
            """
          }
        }
      }
    }

    stage('SonarQube Scan - Frontend') {
      steps {
        dir('frontend') {
          sh 'npm install'
          sh 'npm run build'
          withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
            sh """
              docker run --rm \
                -v \$(pwd):/usr/src \
                -w /usr/src \
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

    stage('Build Backend Image') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
          dir('backend') {
            sh 'docker build -t ${DOCKER_IMAGE}:backend-${BUILD_NUMBER} .'
            sh 'docker push ${DOCKER_IMAGE}:backend-${BUILD_NUMBER}'
          }
        }
      }
    }

    stage('Build Frontend Image') {
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

    stage('Trivy Scan - Backend Image') {
      steps {
        sh """
          trivy image --exit-code 0 --severity ${TRIVY_SEVERITY} ${DOCKER_IMAGE}:backend-${BUILD_NUMBER} \
          || echo "⚠️ Trivy found issues in backend image (continuing...)"
        """
      }
    }

    stage('Trivy Scan - Frontend Image') {
      steps {
        sh """
          trivy image --exit-code 0 --severity ${TRIVY_SEVERITY} ${DOCKER_IMAGE}:frontend-${BUILD_NUMBER} \
          || echo "⚠️ Trivy found issues in frontend image (continuing...)"
        """
      }
    }

    stage('Update K8s Manifests') {
      steps {
        sh """
          # Replace images in manifests
          sed -i 's|REPLACE_BACKEND_IMAGE|${DOCKER_IMAGE}:backend-${BUILD_NUMBER}|g' k8s/backend-deployment.yaml
          sed -i 's|REPLACE_FRONTEND_IMAGE|${DOCKER_IMAGE}:frontend-${BUILD_NUMBER}|g' k8s/frontend-deployment.yaml

          # Apply manifests to EKS cluster
          kubectl apply -f k8s/backend-deployment.yaml -n shoes --validate=false
          kubectl apply -f k8s/frontend-deployment.yaml -n shoes --validate=false
          kubectl apply -f k8s/backend-service.yaml -n shoes --validate=false
          kubectl apply -f k8s/frontend-service.yaml -n shoes --validate=false
          kubectl apply -f k8s/ingress.yaml -n shoes --validate=false
          kubectl apply -f k8s/postgres-pvc.yaml -n shoes --validate=false
          kubectl apply -f k8s/postgres-service.yaml -n shoes --validate=false
          kubectl apply -f k8s/postgres-statefulset.yaml -n shoes --validate=false
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
