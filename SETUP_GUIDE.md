# CI/CD Pipeline Setup Guide

## Overview
This guide will help you set up a Multibranch Pipeline (CICD) and a Manual Pipeline (CD_deploy_manual) in Jenkins for deploying a React application with different configurations for `main` and `dev` branches.

## Prerequisites
- Jenkins installed with the following plugins:
  - Docker Pipeline
  - Docker plugin
  - Git plugin
  - Groovy
  - NodeJS plugin
  - Pipeline plugin
- Docker installed on Jenkins server
- Git installed
- GitHub account

## Step 1: Repository Setup

### 1.1 Create Your Own GitHub Repository
1. Go to GitHub and create a new repository (e.g., `my-cicd-pipeline`)
2. Keep it public or private as per your preference

### 1.2 Push the Code
```bash
cd /mnt/c/Users/Administrador/Documents/Proyectos/cicd-pipeline

# Initialize git if not already done
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit with Jenkinsfiles"

# Add your remote repository
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git

# Push to main branch
git branch -M main
git push -u origin main
```

### 1.3 Create Dev Branch
```bash
# Create and checkout dev branch
git checkout -b dev

# Replace logo for dev branch (RED color)
cp logo-dev.svg src/logo.svg

# Commit the change
git add src/logo.svg
git commit -m "Update logo for dev environment"

# Push dev branch
git push -u origin dev

# Switch back to main
git checkout main

# Replace logo for main branch (GREEN color)
cp logo-main.svg src/logo.svg

# Commit the change
git add src/logo.svg
git commit -m "Update logo for main environment"

# Push main branch
git push origin main
```

## Step 2: Jenkins Configuration

### 2.1 Configure Global Tools
1. Go to Jenkins → Manage Jenkins → Global Tool Configuration
2. Scroll to **NodeJS installations**
3. Click **Add NodeJS**
   - Name: `Node 7.8.0`
   - Version: Select `7.8.0` or install automatically
4. Save

### 2.2 Configure Docker (if needed)
Ensure Jenkins user has permissions to run Docker:
```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

## Step 3: Create Multibranch Pipeline (CICD)

### 3.1 Create the Pipeline
1. Go to Jenkins Dashboard
2. Click **New Item**
3. Enter name: `CICD`
4. Select **Multibranch Pipeline**
5. Click **OK**

### 3.2 Configure Branch Sources
1. Under **Branch Sources**, click **Add source** → **Git**
2. **Project Repository**: Enter your GitHub repository URL
   - Example: `https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git`
3. If private repo, add credentials:
   - Click **Add** → **Jenkins**
   - Kind: **Username with password**
   - Username: Your GitHub username
   - Password: Your GitHub Personal Access Token
   - ID: `github-credentials`
   - Description: `GitHub Credentials`

### 3.3 Configure Build Configuration
1. Under **Build Configuration**:
   - Mode: **by Jenkinsfile**
   - Script Path: `Jenkinsfile`

### 3.4 Configure Scan Repository Triggers
1. Under **Scan Multibranch Pipeline Triggers**:
   - Check **Periodically if not otherwise run**
   - Interval: `1 minute`
   
   **Note**: If you have a public IP, you can set up webhooks instead, which is a better approach.

### 3.5 Save Configuration
Click **Save**

## Step 4: Create Manual Pipeline (CD_deploy_manual)

### 4.1 Create the Pipeline
1. Go to Jenkins Dashboard
2. Click **New Item**
3. Enter name: `CD_deploy_manual`
4. Select **Pipeline**
5. Click **OK**

### 4.2 Configure Pipeline
1. Under **General**:
   - Check **This project is parameterized**
   - This will be automatically set by the Jenkinsfile_manual

2. Under **Pipeline**:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: Your GitHub repository URL
   - Credentials: Select the credentials you created earlier
   - Branch Specifier: `*/main` (or `*/*` to work with any branch)
   - Script Path: `Jenkinsfile_manual`

3. Click **Save**

## Step 5: Test the Pipelines

### 5.1 Test Multibranch Pipeline (CICD)
1. Go to the **CICD** pipeline
2. Click **Scan Multibranch Pipeline Now**
3. Jenkins will discover both `main` and `dev` branches
4. Both branches will start building automatically
5. Check the build logs for each branch

**Expected Results**:
- **main** branch: Creates image `nodemain:v1.0`, deploys on port 3000, GREEN logo
- **dev** branch: Creates image `nodedev:v1.0`, deploys on port 3001, RED logo

### 5.2 Test Manual Pipeline (CD_deploy_manual)
1. Go to the **CD_deploy_manual** pipeline
2. Click **Build with Parameters**
3. Select **ENVIRONMENT**: `main` or `dev`
4. Click **Build**
5. Check the build logs

**Note**: The manual pipeline requires the images to exist (built by CICD pipeline first)

## Step 6: Verify Deployment

### 6.1 Check Docker Containers
```bash
# List running containers
docker ps

# You should see:
# nodemain container on port 3000
# nodedev container on port 3001
```

### 6.2 Check Docker Images
```bash
# List images
docker images

# You should see:
# nodemain:v1.0
# nodedev:v1.0
```

### 6.3 Access Applications
- **Main Environment**: http://localhost:3000 (GREEN logo)
- **Dev Environment**: http://localhost:3001 (RED logo)

## Pipeline Stages Explained

### Multibranch Pipeline (Jenkinsfile)
1. **Declarative: Checkout SCM**: Checks out code from Git
2. **Declarative: Tool Install**: Verifies Node.js installation
3. **Build**: Runs `npm install`
4. **Test**: Runs `npm test`
5. **Docker build**: Builds Docker image with branch-specific name
6. **Deploy**: Deploys container with branch-specific port

### Manual Pipeline (Jenkinsfile_manual)
1. **Validate**: Checks if the Docker image exists
2. **Deploy**: Deploys the selected environment
3. **Verify**: Verifies the container is running

## Key Features

### Port Configuration
- **main** branch: Port 3000
- **dev** branch: Port 3001

### Image Names
- **main** branch: `nodemain:v1.0`
- **dev** branch: `nodedev:v1.0`

### Logo Colors
- **main** branch: GREEN (#4CAF50)
- **dev** branch: RED (#FF6B6B)

### Environment-Specific Deployment
The pipeline uses conditional logic:
```groovy
PORT = "${env.BRANCH_NAME == 'main' ? '3000' : '3001'}"
IMAGE_NAME = "${env.BRANCH_NAME == 'main' ? 'nodemain' : 'nodedev'}"
```

## Troubleshooting

### Issue: Tests Failing
**Solution**: Add `CI=true` to npm test command to prevent interactive mode
```groovy
sh 'CI=true npm test'
```

### Issue: Docker Permission Denied
**Solution**: Add Jenkins user to docker group
```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### Issue: Container Port Already in Use
**Solution**: Stop the conflicting container
```bash
docker ps
docker stop <container_id>
```

### Issue: Git Credentials Not Working
**Solution**: Use GitHub Personal Access Token
1. Go to GitHub → Settings → Developer settings → Personal access tokens
2. Generate new token with `repo` scope
3. Use this token as password in Jenkins credentials

## Screenshots Required for Assignment

For your assignment submission, capture:
1. Jenkins UI showing both pipelines (CICD and CD_deploy_manual)
2. Jenkinsfile content (already created as `Jenkinsfile`)
3. Jenkinsfile_manual content (already created as `Jenkinsfile_manual`)
4. Browser showing deployed application from main branch (port 3000, GREEN logo)
5. Browser showing deployed application from dev branch (port 3001, RED logo)

## Important Notes

1. **Node Version**: Ensure Node 7.8.0 is configured in Global Tool Configuration
2. **Multibranch Scan**: Set to 1 minute or use webhooks for better performance
3. **Container Cleanup**: The pipeline automatically removes old containers before deploying new ones
4. **Environment Isolation**: Each environment (main/dev) has its own container and image

## Success Criteria

✅ Two pipelines created in Jenkins (CICD, CD_deploy_manual)
✅ Both branches (main, dev) discovered by multibranch pipeline
✅ Different logos displayed for each branch
✅ Different ports working (3000 for main, 3001 for dev)
✅ Automatic build on code changes (via scan or webhook)
✅ Manual deployment working with parameter selection
✅ Docker images created with different names (nodemain:v1.0, nodedev:v1.0)

