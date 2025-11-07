def call(String dockerfile = 'Dockerfile') {
    sh """
        if ! command -v hadolint &> /dev/null; then
            wget -O /usr/local/bin/hadolint https://github.com/hadolint/hadolint/releases/download/v2.12.0/hadolint-Linux-x86_64 || true
            chmod +x /usr/local/bin/hadolint || true
        fi
        hadolint ${dockerfile} || echo "Hadolint warnings found"
    """
}

