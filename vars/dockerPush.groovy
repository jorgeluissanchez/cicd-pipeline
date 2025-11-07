def call(String imageName, String imageTag, String registryCredentials) {
    withCredentials([usernamePassword(credentialsId: registryCredentials, 
                                      usernameVariable: 'DOCKER_USER', 
                                      passwordVariable: 'DOCKER_PASS')]) {
        sh """
            echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
            docker push ${imageName}:${imageTag}
            docker logout
        """
    }
}

