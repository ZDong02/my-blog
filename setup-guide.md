# Quick Setup Guide

## 🚀 One-Command Setup (If Available)

If you have all prerequisites installed, follow these steps:

### Step 1: Database Setup
```bash
# Create and initialize database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p blog_db < blog-backend/database/schema.sql
```

### Step 2: Backend Setup
```bash
cd blog-backend
# Update database credentials in src/main/resources/application.yml if needed
mvn clean install
mvn spring-boot:run
```

### Step 3: Frontend Setup
```bash
cd social-factory-blog
npm install
npm run dev
```

### Step 4: Access Your Blog
- Frontend: http://localhost:4321
- Backend API: http://localhost:8080/api

## 📋 Prerequisites Checklist

- [ ] Java 17 installed (`java -version`)
- [ ] Maven installed (`mvn -version`)
- [ ] Node.js 18+ installed (`node --version`)
- [ ] MySQL 5.7.44+ installed and running
- [ ] Database credentials configured

## 🔍 Testing Your Setup

### Test Backend API
```bash
# Test if backend is running
curl http://localhost:8080/api/posts

# Expected: Empty array or posts list
```

### Test Frontend
1. Open http://localhost:4321 in browser
2. Should see the blog homepage
3. Try accessing /login and /register pages

## 👤 Creating Your First Account

1. Go to http://localhost:4321/register
2. Fill in registration form
3. **First user automatically becomes ADMIN**
4. Login at http://localhost:4321/login
5. Access dashboard at http://localhost:4321/dashboard

## 🛠️ Common Setup Issues

### MySQL Issues
```bash
# If MySQL connection fails, check:
# 1. Is MySQL running?
sudo service mysql status

# 2. Can you connect manually?
mysql -u root -p

# 3. Update credentials in application.yml if needed
```

### Java/Maven Issues
```bash
# Check Java version
java -version
# Should show version 17.x.x

# Check Maven
mvn -version
```

### Node.js Issues
```bash
# Check Node.js version
node --version
# Should be 18.x.x or higher

# If npm install fails, try:
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

## 🔧 Configuration Checklist

### Backend Configuration
- [ ] Database URL in `application.yml`
- [ ] Database username/password
- [ ] JWT secret (optional for development)
- [ ] Server port (default: 8080)

### Frontend Configuration
- [ ] API base URL in `api.js` (default: localhost:8080)
- [ ] CORS settings (already configured)

## 🚀 Production Deployment Checklist

### Backend
- [ ] Use environment variables for secrets
- [ ] Configure HTTPS
- [ ] Set up database backup
- [ ] Configure logging
- [ ] Use process manager (PM2/systemd)

### Frontend
- [ ] Build for production: `npm run build`
- [ ] Deploy to CDN/web server
- [ ] Configure domain and HTTPS
- [ ] Set up CI/CD pipeline

## 📞 Getting Help

If you encounter issues:

1. **Check the logs**: Backend logs show detailed error messages
2. **Verify database**: Ensure MySQL is running and accessible
3. **Check ports**: Ensure 8080 (backend) and 4321 (frontend) are available
4. **Review configuration**: Double-check database credentials

## 🎯 Next Steps After Setup

1. **Create Admin Content**: Login as admin and create your first blog post
2. **Test Interactions**: Try liking, bookmarking, and commenting
3. **Explore Dashboard**: Check your activity statistics
4. **Add Content**: Start building your blog content
5. **Customize**: Modify frontend design to match your brand

## 🔄 Development Workflow

### Making Changes
1. Edit backend code → Restart backend server
2. Edit frontend code → Hot reload (automatic)
3. Test changes using the frontend

### Adding Features
1. Create new entity/mapper/service in backend
2. Add REST controller endpoints
3. Update frontend API client
4. Create/modify frontend components

## 📊 Monitoring Your Blog

- **Backend logs**: Show API requests and errors
- **Database**: Monitor user growth and content
- **Frontend**: Browser console for client-side issues

---

**You're all set! Your complete blog system is ready to use.** 🎉

For detailed documentation, see `README.md` in the project root.