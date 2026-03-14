# Blog Backend - Spring Boot 3

A full-featured blog backend built with Spring Boot 3, MyBatis-Plus, and JWT authentication.

## Features

- **User Authentication**: JWT-based authentication with role-based access control
- **User Management**: Registration, login, profile management
- **Blog Management**: Create, edit, publish, and manage blog posts (Admin only)
- **Comments System**: Add comments and replies to blog posts
- **Interactions**: Like and bookmark posts
- **User Dashboard**: View activity statistics and recent interactions
- **Categories**: Organize posts by categories

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MySQL 5.7.44+
- **ORM**: MyBatis-Plus 3.5.5
- **Security**: Spring Security + JWT
- **Build Tool**: Maven

## Database Setup

1. Create the database:
```sql
CREATE DATABASE blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Run the schema script:
```bash
mysql -u root -p blog_db < database/schema.sql
```

## Configuration

Update `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password
```

## Running the Application

1. Build the project:
```bash
mvn clean install
```

2. Run the application:
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/refresh` - Refresh JWT token

### Posts
- `GET /api/posts` - Get published posts (paginated)
- `GET /api/posts/{id}` - Get a specific post
- `POST /api/posts` - Create a new post (Admin only)
- `PUT /api/posts/{id}` - Update a post (Admin only)
- `PUT /api/posts/{id}/publish` - Publish a post (Admin only)
- `DELETE /api/posts/{id}` - Delete a post (Admin only)

### Comments
- `GET /api/comments/post/{postId}` - Get comments for a post
- `POST /api/comments/post/{postId}` - Add a comment
- `DELETE /api/comments/{commentId}` - Delete a comment

### Interactions
- `POST /api/interactions/like/{postId}` - Like a post
- `DELETE /api/interactions/like/{postId}` - Unlike a post
- `POST /api/interactions/bookmark/{postId}` - Bookmark a post
- `DELETE /api/interactions/bookmark/{postId}` - Remove bookmark

### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile

### Dashboard
- `GET /api/dashboard/stats` - Get user statistics
- `GET /api/dashboard/recent-activity` - Get recent user activity

### Categories
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get a specific category

## User Roles

- **USER**: Can view posts, comment, like, bookmark, and manage their profile
- **ADMIN**: All USER permissions + can create, edit, publish, and delete posts

## First User Setup

The first registered user automatically becomes an ADMIN user. Subsequent registrations will be regular USER accounts.

## Security

- JWT tokens expire after 24 hours
- Passwords are hashed using BCrypt
- CORS is enabled for frontend integration
- Role-based access control for admin endpoints

## Frontend Integration

The API is designed to work with the Astro frontend. All endpoints return JSON responses and support CORS for cross-origin requests.

## Testing

Run tests with:
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.