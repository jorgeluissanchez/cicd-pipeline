def call(String imageName, String imageTag, String branch) {
    def port = branch == 'main' ? '3000' : '3001'
    def containerName = branch == 'main' ? 'app-main' : 'app-dev'
    
    sh """
        if docker ps -a | grep -q ${containerName}; then
            docker stop ${containerName} || true
            docker rm ${containerName} || true
        fi
        docker run -d --name ${containerName} -p ${port}:3000 ${imageName}:${imageTag}
    """
}

