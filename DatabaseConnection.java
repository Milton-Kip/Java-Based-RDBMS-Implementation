package com.company.db;

/**
 *
 * @author Kipyegon M
 */
// File: DatabaseConnection.java

import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/company_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private static Connection connection = null;
    
    // Connection Pooling
    private static final int MAX_POOL_SIZE = 10;
    private static final Connection[] connectionPool = new Connection[MAX_POOL_SIZE];
    private static boolean[] connectionInUse = new boolean[MAX_POOL_SIZE];
    
    static {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            initializeConnectionPool();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }
    
    private static void initializeConnectionPool() {
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            try {
                connectionPool[i] = createNewConnection();
                connectionInUse[i] = false;
            } catch (SQLException e) {
                System.err.println("Failed to create connection for pool index " + i);
                e.printStackTrace();
            }
        }
    }
    
    private static Connection createNewConnection() throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", DB_USER);
        connectionProps.put("password", DB_PASSWORD);
        connectionProps.put("useSSL", "false");
        connectionProps.put("serverTimezone", "UTC");
        connectionProps.put("characterEncoding", "UTF-8");
        connectionProps.put("useUnicode", "true");
        
        return DriverManager.getConnection(DB_URL, connectionProps);
    }
    
    public static Connection getConnection() throws SQLException {
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            if (!connectionInUse[i] && connectionPool[i] != null && !connectionPool[i].isClosed()) {
                connectionInUse[i] = true;
                return connectionPool[i];
            }
        }
        
        // All connections are in use, create a new one (temporary)
        System.out.println("Connection pool exhausted, creating temporary connection");
        return createNewConnection();
    }
    
    public static void releaseConnection(Connection conn) {
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            if (connectionPool[i] == conn) {
                connectionInUse[i] = false;
                return;
            }
        }
        
        // If it's not from the pool, close it
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void closeAllConnections() {
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            if (connectionPool[i] != null) {
                try {
                    if (!connectionPool[i].isClosed()) {
                        connectionPool[i].close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection successful!");
                
                // Test with a simple query
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT 1");
                    if (rs.next()) {
                        System.out.println("Database is responding correctly.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}