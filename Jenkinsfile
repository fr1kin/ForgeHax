#!groovy

node {
  try {
    // Checkout the proper revision into the workspace.
    stage('checkout') {
      checkout scm
    }

    // Execute `cibuild` wrapped within a plugin that translates
    // ANSI color codes to something that renders inside the Jenkins
    // console.
    stage('cibuild') {
      withEnv(['JENKINS_BUILDING=yes']) {
        wrap([$class: 'AnsiColorBuildWrapper']) {
          sh './scripts/cibuild'
          archiveArtifacts artifacts: '**/build/libs/ForgeHax*.jar', fingerprint: true
        }
      }
    }
  } catch (err) {
    // Re-raise the exception so that the failure is propagated to
    // Jenkins.
    throw err
  } finally {
    // Pass or fail, ensure that the services and networks
    // created by Docker Compose are torn down.
    sh 'docker-compose -f docker-compose.yml down'
  }
}
