def call(String imageName, String imageTag) {
    sh """
        echo "[Trivy] Scanning image: ${imageName}:${imageTag}"
        
        # Run Trivy via Docker (no installation needed)
        docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            aquasec/trivy:latest image \
            --severity HIGH,CRITICAL \
            --exit-code 0 \
            --no-progress \
            ${imageName}:${imageTag} || {
            echo "[Trivy] Warning: Vulnerabilities found or scan failed"
            echo "[Trivy] Continuing pipeline..."
        }
        
        echo "[Trivy] Scan completed"
    """
}

