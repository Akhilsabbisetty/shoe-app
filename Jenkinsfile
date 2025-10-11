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
          sed -i 's|REPLACE_BACKEND_IMAGE|${DOCKER_IMAGE}:backend-${BUILD_NUMBER}|g' k8s/backend-deployment.yaml
          sed -i 's|REPLACE_FRONTEND_IMAGE|${DOCKER_IMAGE}:frontend-${BUILD_NUMBER}|g' k8s/frontend-deployment.yaml

          docker run --rm \
            -v /var/lib/jenkins/.kube:/root/.kube \
            -v /var/lib/jenkins/workspace/shoe-app-pipeline:/workdir \
            -w /workdir \
            bitnami/kubectl:latest \
            --server=https://10.0.101.179:443 apply -f k8s/backend-deployment.yaml -n shoes --kubeconfig=/root/.kube/config

           docker run --rm \
             -v /var/lib/jenkins/.kube:/root/.kube \
             -v /var/lib/jenkins/workspace/shoe-app-pipeline:/workdir \
             -w /workdir \
             bitnami/kubectl:latest \
             --server=https://10.0.101.179:443 apply -f k8s/frontend-deployment.yaml -n shoes --kubeconfig=/root/.kube/config
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
