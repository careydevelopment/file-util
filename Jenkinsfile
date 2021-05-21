node {
	def app
	def image = 'careydevelopment/file-util'
	def branch = scm.branches[0].name.substring(2)
	
	try {
		stage('Clone repository') {
	    	git branch: branch,
	        	credentialsId: 'GitHub Credentials',
	        	url: 'https://github.com/careydevelopment/file-util.git'
	    } 
	
		stage('Build JAR') {
	    	docker.image('maven:3.6.3-jdk-11').inside('-v /root/.m2:/root/.m2') {
	        	sh 'mvn -B -Dmaven.test.skip=true clean package'
	    	}
	    }	     
	} catch (e) {
		echo 'Error occurred during build process!'
		echo e.toString()
		currentBuild.result = 'FAILURE'
	} finally {
	}
}