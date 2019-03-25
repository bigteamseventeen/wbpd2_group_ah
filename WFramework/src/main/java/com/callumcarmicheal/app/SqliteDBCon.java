package com.callumcarmicheal.app;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.callumcarmicheal.app.models.Account;
import com.callumcarmicheal.app.models.User;

import org.sqlite.SQLiteConnectionPoolDataSource;

class SqliteDBCon {
    private static SQLiteConnectionPoolDataSource dataSource;

    /**
     * Connect to a sample database
     */
    public static Connection Connect() {
        
        
        Connection conn = null;

        try {
            // db parameters
            String url = "jdbc:sqlite:application.db";

            // create a connection to the database
            conn = DriverManager.getConnection(url);
            DatabaseMetaData meta = conn.getMetaData();
            
            System.out.println("Connection to SQLite has been established.");
            System.out.println("Database driver name is " + meta.getDriverName());
            
            return conn;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void InitializeDatabase(Connection c) {
        // TODO: Create database tables
        User.Initialize(c);
    }
}