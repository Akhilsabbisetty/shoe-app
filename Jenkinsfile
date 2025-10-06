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
          // write settings.xml with injected creds
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
        } // withCredentials
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
          // Optional: add wait/retry if Sonar is slow to start.
          sh """
            mvn sonar:sonar \
              -Dsonar.host.url=$SONAR_URL \
              -Dsonar.login=$SONAR_TOKEN
          """
        }
      }
    }

    stage('Frontend Build (must exist)') {
      steps {
        script {
          // debug listing so you can see the workspace layout in logs
          echo "Workspace root: ${env.WORKSPACE}"
          sh "ls -la ${env.WORKSPACE} || true"
          sh "ls -la ${env.WORKSPACE}/frontend || true"

          // fail fast if package.json missing (you requested: do not skip)
          sh """
            if [ ! -f "${env.WORKSPACE}/frontend/package.json" ]; then
              echo "ERROR: frontend/package.json not found at ${env.WORKSPACE}/frontend"
              exit 1
            fi
          """

          // Use Dockerized node (ensures consistent environment and avoids relying on agent node/npm)
          // Requires docker to be available on Jenkins agent (you have docker already in earlier runs)
          sh """
            docker run --rm -v "${env.WORKSPACE}/frontend":/workspace -w /workspace node:18 bash -lc "npm ci && npm run build"
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
            # safer docker login
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

            # FRONTEND image: expects frontend/Dockerfile present
            docker build -f frontend/Dockerfile -t ${DOCKER_IMAGE}:frontend-${BUILD_NUMBER} ./frontend

            # BACKEND image: expects Dockerfile.backend at repo root
            docker build -f Dockerfile.backend -t ${DOCKER_IMAGE}:backend-${BUILD_NUMBER} .

            # STATIC SCAN WITH TRIVY (fail build on HIGH/CRITICAL)
            trivy image --exit-code 1 --severity HIGH,CRITICAL ${DOCKER_IMAGE}:frontend-${BUILD_NUMBER}
            trivy image --exit-code 1 --severity HIGH,CRITICAL ${DOCKER_IMAGE}:backend-${BUILD_NUMBER}

            # push images
            docker push ${DOCKER_IMAGE}:frontend-${BUILD_NUMBER}
            docker push ${DOCKER_IMAGE}:backend-${BUILD_NUMBER}
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
    always {
      cleanWs()
    }
  }
}
