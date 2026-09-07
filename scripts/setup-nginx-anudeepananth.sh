#!/bin/bash

# ==============================================================================
# NGINX SETUP: add anudeepananth.com vhost to shared-nginx (idempotent)
# ==============================================================================
# Live vhosts live in the docker volume nginx-conf-vol, NOT
# /root/unicrew-server/nginx/nginx.conf. This copies a sibling file into
# /etc/nginx/conf.d/ so the giant shared config is not edited.
# ==============================================================================
set -euo pipefail

VPS_HOST="reaptive@72.60.219.67"
SSH_PORT=1379
CONTAINER_NAME="shared-nginx"
HOST_VOLUME_CONF_PATH="/var/lib/docker/volumes/nginx-conf-vol/_data/anudeepananth.conf"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOCAL_CONF="${SCRIPT_DIR}/../nginx/anudeepananth.conf"

if [ ! -f "${LOCAL_CONF}" ]; then
    echo -e "${RED}Missing ${LOCAL_CONF}${NC}"
    exit 1
fi

SSH_CONTROL_PATH="/tmp/ssh-anudeep-nginx-%r@%h:%p"
SSH_OPTIONS="-o ControlMaster=auto -o ControlPath=${SSH_CONTROL_PATH} -o ControlPersist=600 -o BatchMode=yes -o ConnectTimeout=10"

cleanup_ssh() {
    if [ -S "${SSH_CONTROL_PATH}" ]; then
        ssh -p ${SSH_PORT} ${SSH_OPTIONS} -O exit ${VPS_HOST} 2>/dev/null || true
    fi
}
trap cleanup_ssh EXIT

echo -e "${GREEN}Setting up nginx for anudeepananth.com${NC}"

ssh -p ${SSH_PORT} ${SSH_OPTIONS} ${VPS_HOST} "echo 'SSH connection established'" > /dev/null

echo -e "\n${YELLOW}Copying vhost onto the nginx-conf-vol host path${NC}"
ssh -p ${SSH_PORT} ${SSH_OPTIONS} ${VPS_HOST} "sudo test -f ${HOST_VOLUME_CONF_PATH} && sudo cp ${HOST_VOLUME_CONF_PATH} ${HOST_VOLUME_CONF_PATH}.bak.\$(date +%Y%m%d_%H%M%S) || true"
scp -P ${SSH_PORT} -o ControlPath=${SSH_CONTROL_PATH} -o BatchMode=yes "${LOCAL_CONF}" ${VPS_HOST}:/tmp/anudeepananth.conf
ssh -p ${SSH_PORT} ${SSH_OPTIONS} ${VPS_HOST} "sudo cp /tmp/anudeepananth.conf ${HOST_VOLUME_CONF_PATH} && rm -f /tmp/anudeepananth.conf && sudo chmod 644 ${HOST_VOLUME_CONF_PATH}"

echo -e "\n${YELLOW}Testing nginx config${NC}"
if ssh -p ${SSH_PORT} ${SSH_OPTIONS} ${VPS_HOST} "sudo docker exec ${CONTAINER_NAME} nginx -t"; then
    echo -e "${GREEN}nginx -t passed${NC}"
else
    echo -e "${RED}nginx -t failed — removing new vhost${NC}"
    ssh -p ${SSH_PORT} ${SSH_OPTIONS} ${VPS_HOST} "sudo rm -f ${HOST_VOLUME_CONF_PATH}"
    exit 1
fi

echo -e "\n${YELLOW}Reloading nginx${NC}"
ssh -p ${SSH_PORT} ${SSH_OPTIONS} ${VPS_HOST} "sudo docker exec ${CONTAINER_NAME} nginx -s reload"
echo -e "${GREEN}nginx reloaded for www.anudeepananth.com${NC}"
