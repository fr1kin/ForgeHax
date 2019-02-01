#!groovy

node {
	try {
        setGitHubPullRequestStatus context: 'Jenkins', message: 'The Jenkins build is in progress.', state: 'PENDING'
	// Checkout the proper revision into the workspace.
	    stage('checkout') {
	        checkout scm 
	    }
		
        // Execute `update` wrapped within a plugin that translates
        // ANSI color codes to something that renders inside the Jenkins
        // console.
        stage('update') {
            wrap([$class: 'AnsiColorBuildWrapper']) {
                sh './scripts/update'
            }
        }
    
        // Execute `cibuild` wrapped within a plugin that translates
        // ANSI color codes to something that renders inside the Jenkins
        // console.
        stage('cibuild') {
            wrap([$class: 'AnsiColorBuildWrapper']) {
                sh './scripts/cibuild'
                archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true 
                setGitHubPullRequestStatus context: 'Jenkins', message: 'The pull request was successfully built.', state: 'SUCCESS'
            }
        }		
	} catch (err) {
            setGitHubPullRequestStatus context: 'Jenkins', message: 'The pull request was failed to build.', state: 'FAILURE'
	    // Re-raise the exception so that the failure is propagated to
	    // Jenkins.
	    throw err
	} finally {
	    // Pass or fail, ensure that the services and networks
	    // created by Docker Compose are torn down.
	    sh 'docker-compose -f docker-compose.ci.yml down -v'
	}
}
