#!/bin/bash
# ==============================================================================
# Deploys projects.anudeepananth.com to the VPS.
# Builds the server jar and the web bundle on this Mac, copies them over, rebuilds
# the container, checks its health, then installs the shared-nginx site config
# (tested with nginx -t before reload, and removed again if the test fails).
# Server connection details live in scripts/deploy.env, which is never committed.
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPOSITORY_ROOT_DIR="$(cd "${PROJECT_DIR}/.." && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/deploy.env"

CONTAINER_NAME="anudeep-projects"
CONTAINER_PORT=8585
NGINX_CONTAINER_NAME="shared-nginx"
NGINX_VHOST_PATH="/var/lib/docker/volumes/nginx-conf-vol/_data/projects-anudeepananth.conf"

SSH_CONTROL_PATH="/tmp/ssh-anudeep-projects-%r@%h:%p"
SSH_OPTIONS="-o ControlMaster=auto -o ControlPath=${SSH_CONTROL_PATH} -o ControlPersist=600 -o BatchMode=yes -o ConnectTimeout=10"
run_on_vps() {
    ssh -p "${SSH_PORT}" ${SSH_OPTIONS} "${VPS_HOST}" "$@"
}
trap 'ssh -p "${SSH_PORT}" ${SSH_OPTIONS} -O exit "${VPS_HOST}" 2>/dev/null || true' EXIT

if [ ! -f "${PROJECT_DIR}/server/src/main/kotlin/com/anudeepananth/projects/util/ConfidentialObjects.kt" ]; then
    echo "Missing ConfidentialObjects.kt (copy the .example file and fill it in)"
    exit 1
fi

echo "==> Building server jar and web bundle"
(cd "${REPOSITORY_ROOT_DIR}" && ./gradlew :projects:server:buildFatJar :projects:composeApp:wasmJsBrowserDistribution --console=plain -q)

echo "==> Copying files to the server"
run_on_vps "sudo mkdir -p ${VPS_PROJECT_DIR} && sudo chown \$(whoami) ${VPS_PROJECT_DIR}"
(cd "${PROJECT_DIR}" && rsync -az --delete --relative \
    -e "ssh -p ${SSH_PORT} -o ControlPath=${SSH_CONTROL_PATH} -o BatchMode=yes" \
    docker-compose.yml deploy/Dockerfile nginx server/build/libs/server-all.jar composeApp/build/dist/wasmJs/productionExecutable \
    "${VPS_HOST}:${VPS_PROJECT_DIR}/")

echo "==> Rebuilding container"
run_on_vps "cd ${VPS_PROJECT_DIR} && sudo docker compose build && sudo docker compose up -d --force-recreate ${CONTAINER_NAME}"

echo "==> Waiting for the server to become healthy"
for attempt in $(seq 1 20); do
    if run_on_vps "sudo docker exec ${NGINX_CONTAINER_NAME} wget -q -O - http://${CONTAINER_NAME}:${CONTAINER_PORT}/health" 2>/dev/null | grep -q ok; then
        echo "    healthy"
        break
    fi
    if [ "${attempt}" -eq 20 ]; then
        echo "Server did not become healthy. Last logs:"
        run_on_vps "sudo docker logs --tail 40 ${CONTAINER_NAME}"
        exit 1
    fi
    sleep 3
done

echo "==> Installing shared-nginx site config"
run_on_vps "sudo cp ${VPS_PROJECT_DIR}/nginx/projects-anudeepananth.conf ${NGINX_VHOST_PATH}.new && sudo test ! -f ${NGINX_VHOST_PATH} || sudo cp ${NGINX_VHOST_PATH} ${NGINX_VHOST_PATH%.conf}.previous"
run_on_vps "sudo mv ${NGINX_VHOST_PATH}.new ${NGINX_VHOST_PATH} && sudo chmod 644 ${NGINX_VHOST_PATH}"
if run_on_vps "sudo docker exec ${NGINX_CONTAINER_NAME} nginx -t" 2>&1 | grep -q "test is successful"; then
    run_on_vps "sudo docker exec ${NGINX_CONTAINER_NAME} nginx -s reload"
    echo "==> Live at https://projects.anudeepananth.com"
else
    echo "nginx -t failed. Restoring the previous site config so other sites keep working"
    run_on_vps "sudo test -f ${NGINX_VHOST_PATH%.conf}.previous && sudo cp ${NGINX_VHOST_PATH%.conf}.previous ${NGINX_VHOST_PATH} || sudo rm -f ${NGINX_VHOST_PATH}"
    exit 1
fi

run_on_vps "sudo docker image prune -f" > /dev/null || true
