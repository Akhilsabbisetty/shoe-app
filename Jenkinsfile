pipeline {
  agent any

  // 🔁 Automatically trigger pipeline when a commit is pushed to GitHub
  triggers {
    githubPush()
  }

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
        echo "🔄 Checking out latest code from GitHub..."
        git branch: 'main',
            url: 'https://github.com/Akhilsabbisetty/shoe-app.git',
            credentialsId: 'github-creds'
        sh 'ls -R'
      }
    }

    stage('Build Backend') {
      steps {
        dir('backend') {
          echo "⚙️ Building backend using Maven..."
          sh 'mvn -B -DskipTests clean package'
        }
      }
    }

    stage('SonarQube Scan - Backend') {
      steps {
        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
          dir('backend') {
            echo "🔍 Running SonarQube analysis on backend..."
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
          echo "🔍 Running SonarQube analysis on frontend..."
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
        echo "🐳 Building and pushing backend Docker image..."
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
        echo "🐳 Building and pushing frontend Docker image..."
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
        echo "🧪 Scanning backend image with Trivy..."
        sh """
          trivy image --exit-code 0 --severity ${TRIVY_SEVERITY} ${DOCKER_IMAGE}:backend-${BUILD_NUMBER} \
          || echo "⚠️ Trivy found issues in backend image (continuing...)"
        """
      }
    }

    stage('Trivy Scan - Frontend Image') {
      steps {
        echo "🧪 Scanning frontend image with Trivy..."
        sh """
          trivy image --exit-code 0 --severity ${TRIVY_SEVERITY} ${DOCKER_IMAGE}:frontend-${BUILD_NUMBER} \
          || echo "⚠️ Trivy found issues in frontend image (continuing...)"
        """
      }
    }

    stage('Update K8s Manifests') {
      steps {
        echo "📝 Updating Kubernetes manifests with new image tags..."
        sh """
          sed -i 's|REPLACE_BACKEND_IMAGE|${DOCKER_IMAGE}:backend-${BUILD_NUMBER}|g' k8s/backend-deployment.yaml
          sed -i 's|REPLACE_FRONTEND_IMAGE|${DOCKER_IMAGE}:frontend-${BUILD_NUMBER}|g' k8s/frontend-deployment.yaml

          echo "🚀 Applying updated manifests to Kubernetes..."
          kubectl apply -f k8s/backend-deployment.yaml -n shoes --validate=false
          kubectl apply -f k8s/frontend-deployment.yaml -n shoes --validate=false
          kubectl apply -f k8s/backend-service.yaml -n shoes --validate=false
          kubectl apply -f k8s/frontend-service.yaml -n shoes --validate=false
          kubectl apply -f k8s/ingress.yaml -n shoes --validate=false
          kubectl apply -f k8s/postgres-service.yaml -n shoes --validate=false
          kubectl apply -f k8s/postgres-statefulset.yaml -n shoes --validate=false
        """
      }
    }

    stage('ArgoCD Sync') {
      steps {
        echo "🔄 Triggering ArgoCD application sync..."
        withCredentials([usernamePassword(credentialsId: 'argocd-creds', usernameVariable: 'ARGO_USER', passwordVariable: 'ARGO_PASS')]) {
          sh """
            argocd login $ARGOCD_SERVER --username $ARGO_USER --password $ARGO_PASS --insecure || true
            argocd app sync ${APP_NAME} --async || true
            argocd app wait ${APP_NAME} --health || true
          """
        }
      }
    }
  }

  post {
    success {
      echo "✅ Deployment successful! Git commit changes have been deployed to Kubernetes."
    }
    failure {
      echo "❌ Pipeline failed. Please check Jenkins logs for errors."
    }
    always {
      cleanWs()
    }
  }
}
