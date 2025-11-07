def call(String dockerfile = 'Dockerfile') {
    sh """
        echo "[Hadolint] Checking ${dockerfile}"
        
        # Run Hadolint via Docker (no installation needed)
        docker run --rm -i \
            hadolint/hadolint:latest \
            hadolint - < ${dockerfile} || {
            echo "[Hadolint] Warning: Issues found in Dockerfile"
            echo "[Hadolint] Continuing pipeline..."
        }
        
        echo "[Hadolint] Check completed"
    """
}

