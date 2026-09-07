#!/bin/bash

# ==============================================================================
# DEPLOYMENT SCRIPT: anudeepananth.com Ktor app to Hostinger VPS
# ==============================================================================
# Same rsync + docker compose loop as PeekoPals. Rsyncs the working tree,
# including gitignored ConfidentialObjects.kt, so the image can compile.
# ==============================================================================
set -euo pipefail

VPS_HOST="reaptive@72.60.219.67"
VPS_PROJECT_DIR="/root/anudeep-ananth"
CONTAINER_NAME="anudeep-ananth"
PEEKOPALS_GRADLE_ZIP="/Users/anudeepmac/IntelliJ/Peekopals/PeekoPals/docker-build-cache/gradle-9.5.1-bin.zip"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

SSH_CONTROL_PATH="/tmp/ssh-anudeep-deploy-%r@%h:%p"
SSH_OPTIONS="-o ControlMaster=auto -o ControlPath=${SSH_CONTROL_PATH} -o ControlPersist=600 -o BatchMode=yes -o ConnectTimeout=10"

cleanup_ssh() {
    if [ -S "${SSH_CONTROL_PATH}" ]; then
        echo -e "\n${YELLOW}Closing SSH connection...${NC}"
        ssh -p 1379 ${SSH_OPTIONS} -O exit ${VPS_HOST} 2>/dev/null || true
    fi
}
trap cleanup_ssh EXIT

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
BUILD_TAG=$(date '+%Y%m%d-%H%M%S')

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deploying anudeepananth.com to VPS${NC}"
echo -e "${GREEN}========================================${NC}"

if [ ! -f "${PROJECT_DIR}/src/main/kotlin/util/ConfidentialObjects.kt" ]; then
    echo -e "${RED}ConfidentialObjects.kt is missing — the image cannot compile without it.${NC}"
    exit 1
fi

if [ ! -f "${PROJECT_DIR}/docker-build-cache/gradle-9.5.1-bin.zip" ] && [ -f "${PEEKOPALS_GRADLE_ZIP}" ]; then
    echo -e "${YELLOW}Seeding docker-build-cache from PeekoPals Gradle zip${NC}"
    mkdir -p "${PROJECT_DIR}/docker-build-cache"
    cp "${PEEKOPALS_GRADLE_ZIP}" "${PROJECT_DIR}/docker-build-cache/gradle-9.5.1-bin.zip"
fi

echo -e "\n${YELLOW}Establishing SSH connection...${NC}"
ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "echo 'SSH connection established'" > /dev/null
echo -e "${GREEN}SSH connection ready${NC}"

echo -e "\n${YELLOW}Step 1: Creating project directory on VPS...${NC}"
ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "sudo mkdir -p ${VPS_PROJECT_DIR} && sudo chown reaptive:reaptive ${VPS_PROJECT_DIR}"
echo -e "${GREEN}Project directory ready${NC}"

echo -e "\n${YELLOW}Step 2: Transferring source code to VPS...${NC}"
RSYNC_SSH_CMD="ssh -p 1379 -o ControlMaster=auto -o ControlPath=${SSH_CONTROL_PATH} -o ControlPersist=600 -o BatchMode=yes"

rsync -avz -e "$RSYNC_SSH_CMD" --delete \
    --exclude 'build/' \
    --exclude '.gradle/' \
    --exclude '.idea/' \
    --exclude '.git/' \
    --exclude '.kotlin/' \
    --exclude '.superpowers/' \
    --exclude '*.log' \
    --exclude '.DS_Store' \
    --exclude 'logs/' \
    --exclude 'temporary-output.md' \
    --exclude 'Anudeep Resume.pdf' \
    --exclude 'intro-video/' \
    "${PROJECT_DIR}/" ${VPS_HOST}:${VPS_PROJECT_DIR}/

echo -e "${GREEN}Source code transferred${NC}"

echo -e "\n${YELLOW}Step 3: Building Docker image on VPS...${NC}"
ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "cd ${VPS_PROJECT_DIR} && sudo docker compose build"
ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "sudo docker tag anudeep-ananth:latest anudeep-ananth:${BUILD_TAG}"
echo -e "${GREEN}Docker image built${NC}"

echo -e "\n${YELLOW}Step 4: Deploying application...${NC}"
ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "cd ${VPS_PROJECT_DIR} && sudo docker compose up -d --force-recreate anudeep-ananth"
echo -e "${GREEN}Application deployed${NC}"

echo -e "\n${YELLOW}Step 5: Verifying container...${NC}"
sleep 8
if ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "sudo docker ps | grep ${CONTAINER_NAME}"; then
    echo -e "${GREEN}Container is running${NC}"
    ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "sudo docker logs --tail 30 ${CONTAINER_NAME}"
else
    echo -e "${RED}Container failed to start${NC}"
    ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "sudo docker logs ${CONTAINER_NAME}"
    exit 1
fi

echo -e "\n${YELLOW}Step 6: Installing nginx vhost...${NC}"
"${SCRIPT_DIR}/setup-nginx-anudeepananth.sh"

echo -e "\n${YELLOW}Step 7: Cleaning up old Docker images...${NC}"
ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "sudo docker images anudeep-ananth --format '{{.Repository}}:{{.Tag}} {{.CreatedAt}}' \
    | grep -v '<none>' \
    | grep -v 'latest' \
    | sort -k2 -r \
    | tail -n +3 \
    | awk '{print \$1}' \
    | xargs -r sudo docker rmi" || true
ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "sudo docker image prune -f" || true
ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "sudo docker builder prune -af" || true
echo -e "${GREEN}Cleanup completed${NC}"

DEPLOY_TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
DEPLOY_USER=$(whoami)
GIT_COMMIT=$(git -C "${PROJECT_DIR}" rev-parse --short HEAD 2>/dev/null || echo "unknown")
ssh -p 1379 ${SSH_OPTIONS} ${VPS_HOST} "echo '${DEPLOY_TIMESTAMP} | User: ${DEPLOY_USER} | Commit: ${GIT_COMMIT} | Container: ${CONTAINER_NAME} | Status: SUCCESS' | sudo tee -a /var/log/anudeep-ananth-deployments.log > /dev/null"

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment completed${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "  HTTPS: https://www.anudeepananth.com"
echo -e "  Health: https://www.anudeepananth.com/health"
echo -e "\nRe-run: ./scripts/deploy-vps-build.sh"
echo -e "Logs:    ssh -p 1379 ${VPS_HOST} 'sudo docker logs -f ${CONTAINER_NAME}'"
