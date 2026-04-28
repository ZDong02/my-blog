# =====================================================
# Build Instructions for Blog Project
# =====================================================

## Local Build (Windows/Mac/Linux)

### 1. Build Backend
```bash
cd blog-backend
mvn clean package -DskipTests
# Output: target/blog-backend-1.0.0.jar
```

### 2. Build Frontend
```bash
cd social-factory-blog
npm install
npm run build
# Output: dist/
```

## Upload to Server

### Backend JAR
- Local: `blog-backend/target/blog-backend-1.0.0.jar`
- Remote: `/opt/blog/blog-backend-1.0.0.jar`

### Frontend Dist
- Local: `social-factory-blog/dist/`
- Remote: `/var/www/blog/dist/`

### Database Schema
- Local: `blog-backend/src/main/resources/db/init-tables.sql`
- Remote: Run on MySQL server

## Server Directory Structure
```
/opt/blog/
├── blog-backend-1.0.0.jar
└── logs/

/var/www/blog/
├── dist/           (frontend static files)
└── uploads/        (user uploaded files)

/etc/nginx/sites-available/blog
/etc/systemd/system/blog-backend.service
```

## Environment Variables for Backend
```bash
DB_URL=jdbc:mysql://localhost:3306/blog_db
DB_USERNAME=blog_user
DB_PASSWORD=B1ogP@ss2026
JWT_SECRET=your-secret-key-here
APP_LOG_LEVEL=INFO
```

## Quick Deploy Commands
```bash
# 1. SSH to server
ssh root@your-server-ip

# 2. Create directories
mkdir -p /opt/blog /var/www/blog/uploads

# 3. Upload files (from your local machine)
scp blog-backend-1.0.0.jar root@your-server-ip:/opt/blog/
scp -r dist/* root@your-server-ip:/var/www/blog/dist/

# 4. Import database
mysql -u blog_user -p blog_db < init-tables.sql

# 5. Start backend
systemctl start blog-backend
systemctl status blog-backend

# 6. Check logs
journalctl -u blog-backend -f
```