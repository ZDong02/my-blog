#!/bin/bash

# =====================================================
# Docker Deployment Script for Blog Project
# =====================================================

set -e

# Configuration
DOMAIN="zdong01.com"
SERVER_IP="8.136.159.160"
DB_NAME="blog_db"
DB_USER="blog_user"
DB_PASS="B1ogP@ss2026"
MYSQL_ROOT_PASS="123456"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "Please run as root: sudo $0"
        exit 1
    fi
}

# Update system
update_system() {
    log_info "Updating system packages..."
    apt update && apt upgrade -y
}

# Install Docker
install_docker() {
    log_info "Installing Docker..."
    apt install -y ca-certificates curl gnupg lsb-release

    mkdir -p /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

    apt update
    apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

    systemctl enable docker
    systemctl start docker

    log_info "Docker installation complete!"
}

# Create app directory
create_app_dir() {
    log_info "Creating application directory..."
    mkdir -p /opt/blog
    chown -R $USER:$USER /opt/blog
}

# Main installation flow
main() {
    log_info "========================================="
    log_info "  Docker Deployment Script"
    log_info "  Server: ${SERVER_IP}"
    log_info "========================================="

    check_root
    update_system
    install_docker
    create_app_dir

    log_info ""
    log_info "========================================="
    log_info "  Docker installation complete!"
    log_info "========================================="
    log_info ""
    log_info "Next steps:"
    log_info "1. Upload project files to /opt/blog/"
    log_info "2. Run: cd /opt/blog && docker-compose up -d"
    log_info "3. Check logs: docker-compose logs -f"
    log_info ""
}

main "$@"
