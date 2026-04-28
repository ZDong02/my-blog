#!/bin/bash

# SSL Setup Script for Let's Encrypt
# Run this after frontend and backend are deployed

set -e

DOMAIN="zdong01.com"
ADMIN_EMAIL="admin@zdong01.com"

echo "Setting up SSL certificate for $DOMAIN..."

certbot --nginx \
    -d $DOMAIN \
    -d www.$DOMAIN \
    --non-interactive \
    --agree-tos \
    -m $ADMIN_EMAIL \
    --redirect

echo "SSL setup complete!"