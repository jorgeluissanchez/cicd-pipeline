def call(String branch) {
    def containerName = branch == 'main' ? 'app-main' : 'app-dev'
    
    sh """
        if docker ps -a | grep -q ${containerName}; then
            docker stop ${containerName} || true
            docker rm ${containerName} || true
        fi
    """
}

