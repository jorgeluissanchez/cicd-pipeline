def call(String imageName, String imageTag) {
    def exitCode = sh(
        script: "trivy image --exit-code 0 --severity HIGH,MEDIUM,LOW --no-progress ${imageName}:${imageTag}",
        returnStatus: true
    )
    
    if (exitCode != 0) {
        echo "Trivy scan found vulnerabilities"
    }
    
    sh "trivy image --exit-code 0 --severity HIGH,MEDIUM,LOW --no-progress ${imageName}:${imageTag}"
}

