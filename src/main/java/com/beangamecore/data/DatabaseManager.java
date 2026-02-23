package com.beangamecore.data;

import com.beangamecore.Main;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private static final Logger logger = Main.logger();
    private static HikariDataSource dataSource;

    // Initialize the connection pool when the class is loaded
    static {
        initializeDataSource();
    }

    // Configure and initialize the HikariCP connection pool
    private static void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        String url = "jdbc:mysql://server-135-125-139-185.da.direct:3306/dyxudanh_beangame_items" +
             "?useUnicode=true" +
             "&characterEncoding=utf8" +
             "&useSSL=false" +  // or true with proper cert
             "&allowPublicKeyRetrieval=true";
        config.setJdbcUrl(url);
        config.setUsername("dyxudanh_beangame_items");
        config.setPassword("pZ98LJB5RQkT4jS7cZmv");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10); // Adjust pool size based on expected load
        config.setMinimumIdle(2); // Minimum idle connections in the pool
        config.setIdleTimeout(30000); // Timeout for idle connections
        config.setMaxLifetime(1800000); // Maximum lifetime of a connection
        config.setConnectionTimeout(10000); // Timeout for acquiring a connection

        dataSource = new HikariDataSource(config);
        logger.info("Database connection pool initialized.");
    }

    // Get a connection from the pool
    public static Connection getConnection() throws SQLException {
        Connection conn = dataSource.getConnection();
        if (conn == null) {
            logger.severe("Failed to obtain a connection from the pool.");
            throw new SQLException("Failed to obtain a connection from the pool.");
        }
        return conn;
    }

    // Close a connection and return it to the pool
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }

    // Shutdown the connection pool when the application stops
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("Database connection pool shut down.");
        }
    }
}
