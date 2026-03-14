# Complete Blog System

A full-featured blog system built with **Spring Boot 3** (backend) and **Astro** (frontend), featuring user authentication, role-based access, blog management, comments, likes, bookmarks, and a comprehensive user dashboard.

## 🏗️ System Architecture

### Backend (Spring Boot 3)
- **Framework**: Spring Boot 3.2.0 with Java 17
- **Database**: MySQL 5.7.44+ with MyBatis-Plus ORM
- **Security**: JWT-based authentication with role-based access control
- **API**: RESTful APIs with comprehensive error handling

### Frontend (Astro + Tailwind CSS)
- **Framework**: Astro with Tailwind CSS
- **Authentication**: JWT token management with automatic refresh
- **Features**: Responsive design, dark mode support, real-time interactions

## 🚀 Features

### User Management
- ✅ User registration and login
- ✅ Profile management (nickname, avatar, bio)
- ✅ Role-based access (ADMIN/USER)
- ✅ First user automatically becomes ADMIN

### Blog Management (Admin Only)
- ✅ Create, edit, and delete blog posts
- ✅ Draft and publish workflow
- ✅ Category management
- ✅ View analytics (views, likes, comments)

### User Interactions
- ✅ Like/unlike posts
- ✅ Bookmark posts for later
- ✅ Comment system with replies
- ✅ Share functionality

### User Dashboard
- ✅ Personal activity statistics
- ✅ Recent activity timeline
- ✅ Quick access to liked/bookmarked posts
- ✅ Comment history

## 📁 Project Structure

```
D:\my-blog\
├── blog-backend/                 # Spring Boot 3 backend
│   ├── src/main/java/com/example/blog/
│   │   ├── BlogApplication.java
│   │   ├── config/                 # Security & configuration
│   │   ├── controller/            # REST API controllers
│   │   ├── service/               # Business logic
│   │   ├── mapper/                # MyBatis-Plus mappers
│   │   ├── entity/                # Database entities
│   │   ├── dto/                   # Request/Response objects
│   │   ├── security/              # JWT security components
│   │   └── exception/             # Custom exceptions
│   ├── src/main/resources/
│   │   ├── application.yml        # Configuration
│   │   └── mapper/                # XML mapper files
│   ├── database/schema.sql       # Database schema
│   ├── pom.xml                   # Maven dependencies
│   └── README.md
│
└── social-factory-blog/         # Astro frontend
    ├── src/
    │   ├── components/
    │   │   ├── AuthProvider.astro   # Authentication context
│   │   │   └── ThemeToggle.astro
    │   ├── lib/
    │   │   └── api.js             # Backend API client
    │   ├── pages/
    │   │   ├── login.astro         # Login page
    │   │   ├── register.astro      # Registration page
    │   │   ├── dashboard.astro     # User dashboard
    │   │   └── blog/[slug].astro   # Enhanced blog posts
    │   └── layouts/
    ├── public/
    ├── astro.config.mjs
    └── package.json
```

## 🛠️ Setup Instructions

### Prerequisites
- **Java 17** and **Maven** for backend
- **Node.js 18+** and **npm** for frontend
- **MySQL 5.7.44+** database

### Backend Setup

1. **Database Setup**
```bash
# Create database
mysql -u root -p -e "CREATE DATABASE blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Import schema
mysql -u root -p blog_db < blog-backend/database/schema.sql
```

2. **Configure Database**
Edit `blog-backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password
```

3. **Build and Run Backend**
```bash
cd blog-backend
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`

### Frontend Setup

1. **Install Dependencies**
```bash
cd social-factory-blog
npm install
```

2. **Run Development Server**
```bash
npm run dev
```

The frontend will be available at `http://localhost:4321`

## 🔗 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token

### Posts
- `GET /api/posts` - Get published posts
- `GET /api/posts/{id}` - Get specific post
- `POST /api/posts` - Create post (Admin)
- `PUT /api/posts/{id}` - Update post (Admin)
- `DELETE /api/posts/{id}` - Delete post (Admin)

### Comments
- `GET /api/comments/post/{postId}` - Get post comments
- `POST /api/comments/post/{postId}` - Add comment
- `DELETE /api/comments/{id}` - Delete comment

### Interactions
- `POST /api/interactions/like/{postId}` - Like post
- `DELETE /api/interactions/like/{postId}` - Unlike post
- `POST /api/interactions/bookmark/{postId}` - Bookmark post
- `DELETE /api/interactions/bookmark/{postId}` - Remove bookmark

### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update profile

### Dashboard
- `GET /api/dashboard/stats` - Get user statistics
- `GET /api/dashboard/recent-activity` - Get recent activity

## 👤 User Roles

### USER Role
- View published posts
- Comment on posts
- Like and bookmark posts
- Manage personal profile
- View personal dashboard

### ADMIN Role (All USER permissions +)
- Create, edit, delete posts
- Publish/unpublish posts
- Manage categories
- View all posts (including drafts)

## 🔐 Security Features

- **JWT Authentication**: Token-based auth with 24-hour expiry
- **Password Hashing**: BCrypt encryption
- **Role-based Access**: Admin-only endpoints protected
- **CORS Support**: Cross-origin requests enabled
- **Input Validation**: Comprehensive request validation
- **Error Handling**: Graceful error responses

## 🎨 Frontend Features

- **Responsive Design**: Mobile-first approach
- **Dark Mode**: Automatic theme switching
- **Real-time Updates**: Live interaction feedback
- **Loading States**: Smooth user experience
- **Error Handling**: User-friendly error messages
- **Authentication Flow**: Seamless login/register experience

## 📊 Database Schema

- **users**: User accounts with role management
- **posts**: Blog posts with status workflow
- **categories**: Post categorization
- **comments**: Hierarchical comment system
- **likes**: User-post like relationships
- **bookmarks**: User-post bookmark relationships

## 🚀 Development Workflow

1. **Start Backend**
   ```bash
   cd blog-backend
   mvn spring-boot:run
   ```

2. **Start Frontend**
   ```bash
   cd social-factory-blog
   npm run dev
   ```

3. **Access Application**
   - Frontend: http://localhost:4321
   - API: http://localhost:8080/api

4. **Create First Admin User**
   - Register at http://localhost:4321/register
   - First user automatically becomes ADMIN

## 🧪 Testing

### Backend Testing
```bash
cd blog-backend
mvn test
```

### API Testing
Use Postman or curl to test endpoints:
```bash
# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"testuser","password":"password123"}'
```

## 📝 Content Management

### Adding Blog Posts
1. Login as ADMIN user
2. Use `/api/posts` endpoint to create posts
3. Posts start as DRAFT status
4. Use `/api/posts/{id}/publish` to publish

### Managing Categories
Categories are pre-loaded in the database schema. Add more via:
```sql
INSERT INTO categories (name, description) VALUES ('New Category', 'Description');
```

## 🔧 Configuration

### Backend Configuration
- `application.yml`: Database, JWT, logging settings
- `SecurityConfig.java`: Security rules and CORS
- `MyBatisPlusConfig.java`: Database configuration

### Frontend Configuration
- `api.js`: API client with authentication
- `AuthProvider.astro`: Authentication state management
- `astro.config.mjs`: Astro configuration

## 🚨 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify MySQL is running
   - Check database credentials in `application.yml`
   - Ensure database and tables are created

2. **CORS Issues**
   - Backend CORS is configured for all origins
   - Check frontend API base URL

3. **JWT Token Issues**
   - Tokens expire after 24 hours
   - Use refresh token endpoint
   - Clear localStorage if needed

4. **First Admin User Not Working**
   - Ensure database is empty before first registration
   - Check `is_first_user` field in users table

## 📈 Performance Optimizations

- **Database Indexing**: Optimized queries with proper indexes
- **Pagination**: Large datasets handled efficiently
- **Connection Pooling**: HikariCP for database connections
- **Caching**: MyBatis-Plus query optimization
- **Lazy Loading**: Comments loaded on demand

## 🔄 Deployment

### Backend Deployment
1. Build JAR: `mvn clean package`
2. Deploy to server with Java 17
3. Configure environment variables for production
4. Use process manager (PM2, systemd)

### Frontend Deployment
1. Build: `npm run build`
2. Deploy static files to CDN/web server
3. Configure API endpoint URLs

## 📚 Additional Resources

- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **MyBatis-Plus Documentation**: https://baomidou.com/
- **Astro Documentation**: https://docs.astro.build/
- **JWT Documentation**: https://jwt.io/

## 📄 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

---

**Built with ❤️ using Spring Boot 3 and Astro**