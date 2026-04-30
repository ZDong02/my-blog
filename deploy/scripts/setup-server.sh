#!/bin/bash

# =====================================================
# Blog Deployment Script for Alibaba Cloud Ubuntu 22.04
# 2 Core 2GB RAM Server
# =====================================================

set -e

# Configuration
DOMAIN="zdong01.com"
DB_NAME="blog_db"
DB_USER="blog_user"
DB_PASS="B1ogP@ss2026"
ADMIN_EMAIL="admin@zdong01.com"

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

# Install MySQL 8.0
install_mysql() {
    log_info "Installing MySQL 8.0..."
    apt install -y mysql-server

    log_info "Configuring MySQL..."
    systemctl enable mysql
    systemctl start mysql

    log_info "Creating database and user..."
    mysql <<EOF
CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;
EOF

    log_info "Importing database schema..."
    if [ -f /opt/blog/blog-backend/src/main/resources/db/init-tables.sql ]; then
        mysql -u ${DB_USER} -p${DB_PASS} ${DB_NAME} < /opt/blog/blog-backend/src/main/resources/db/init-tables.sql
    fi

    log_info "MySQL installation complete!"
}

# Install Java 17
install_java() {
    log_info "Installing OpenJDK 17..."
    apt install -y openjdk-17-jdk

    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    echo "JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64" >> /etc/environment
}

# Install Nginx
install_nginx() {
    log_info "Installing Nginx..."
    apt install -y nginx certbot python3-certbot-nginx

    systemctl enable nginx
    systemctl start nginx

    log_info "Nginx installation complete!"
}

# Configure Nginx
configure_nginx() {
    log_info "Configuring Nginx..."

    cat > /etc/nginx/sites-available/blog <<EOF
server {
    listen 80;
    server_name 8.136.159.160;

    root /var/www/blog/dist;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml application/json;

    # Static files caching
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # API proxy
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_read_timeout 90;
        proxy_connect_timeout 90;
    }

    # Upload files
    location /uploads/ {
        proxy_pass http://127.0.0.1:8080/api/uploads/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }

    # SPA fallback
    location / {
        try_files \$uri \$uri/ /index.html;
    }
}
EOF

    ln -sf /etc/nginx/sites-available/blog /etc/nginx/sites-enabled/blog

    rm -f /etc/nginx/sites-enabled/default

    nginx -t && systemctl reload nginx
    log_info "Nginx configuration complete!"
}

# Setup SSL with Let's Encrypt
setup_ssl() {
    log_info "Setting up SSL certificate..."
    # Note: Let's Encrypt requires domain. For IP-only, skip this step.
    log_warn "SSL skipped - using IP address instead of domain"
}

# Create application directory
create_app_dir() {
    log_info "Creating application directory..."
    mkdir -p /opt/blog
    mkdir -p /var/www/blog
    mkdir -p /var/www/blog/uploads

    chown -R www-data:www-data /var/www/blog
}

# Deploy backend
deploy_backend() {
    log_info "Deploying backend application..."

    if [ ! -f /opt/blog/blog-backend-1.0.0.jar ]; then
        log_error "Backend JAR not found! Please upload blog-backend-1.0.0.jar to /opt/blog/"
        exit 1
    fi

    cp /opt/blog/blog-backend-1.0.0.jar /opt/blog/blog-backend.jar

    cat > /etc/systemd/system/blog-backend.service <<EOF
[Unit]
Description=Blog Backend Application
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/blog
ExecStart=/usr/bin/java -Xmx768m -Xms256m -jar /opt/blog/blog-backend.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

    systemctl daemon-reload
    systemctl enable blog-backend
    systemctl start blog-backend

    log_info "Backend deployment complete!"
}

# Main installation flow
main() {
    log_info "========================================="
    log_info "  Blog Deployment Script"
    log_info "  Domain: ${DOMAIN}"
    log_info "========================================="

    check_root
    update_system
    install_mysql
    install_java
    install_nginx
    create_app_dir
    configure_nginx

    log_info ""
    log_info "========================================="
    log_info "  Basic installation complete!"
    log_info "========================================="
    log_info ""
    log_info "Next steps:"
    log_info "1. Upload frontend dist/ to /var/www/blog/dist"
    log_info "2. Upload backend jar to /opt/blog/blog-backend-1.0.0.jar"
    log_info "3. Run: sudo bash setup-ssl.sh (for HTTPS)"
    log_info "4. Run: sudo systemctl start blog-backend"
    log_info ""
}

main "$@"