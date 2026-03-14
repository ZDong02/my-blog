package com.example.blog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnectionTest {
    public static void main(String[] args) {
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        // 使用默认值
        if (url == null) url = "jdbc:mysql://localhost:3306/blog_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC";
        if (username == null) username = "root";
        if (password == null) password = "123456";

        System.out.println("=== MySQL 连接测试 ===");
        System.out.println("URL: " + url);
        System.out.println("用户名：" + username);
        System.out.println("密码：" + "*".repeat(password.length()));
        System.out.println();

        try {
            // 加载驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[OK] MySQL 驱动加载成功");

            // 尝试连接
            System.out.println("[INFO] 正在连接数据库...");
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                if (conn != null && !conn.isClosed()) {
                    System.out.println("[OK] 数据库连接成功！");
                    System.out.println("[INFO] 数据库产品：" + conn.getMetaData().getDatabaseProductName());
                    System.out.println("[INFO] 数据库版本：" + conn.getMetaData().getDatabaseProductVersion());
                    System.out.println("[INFO] 驱动版本：" + conn.getMetaData().getDriverName() + " " +
                                       conn.getMetaData().getDriverVersion());
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] MySQL 驱动未找到：" + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ERROR] 数据库连接失败！");
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
        }
    }
}
