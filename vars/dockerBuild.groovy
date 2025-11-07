def call(String imageName, String imageTag, String branch) {
    def dockerfile = 'Dockerfile'
    def port = branch == 'main' ? '3000' : '3001'
    def logoFile = branch == 'main' ? 'logo-main.svg' : 'logo-dev.svg'
    
    sh """
        if [ -f src/${logoFile} ]; then
            cp src/${logoFile} src/logo.svg
        fi
        docker build -t ${imageName}:${imageTag} --build-arg PORT=${port} .
    """
}

