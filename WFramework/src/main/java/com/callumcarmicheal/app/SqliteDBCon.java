package com.callumcarmicheal.app;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.PooledConnection;

import com.callumcarmicheal.app.models.User;

import org.sqlite.javax.SQLiteConnectionPoolDataSource;

public class SqliteDBCon {
    private static SQLiteConnectionPoolDataSource dataSource;
    private static PooledConnection connectionPool;

    /**
     * Setup the database pool
     * @throws SQLException
     */
    public static void InitializeDatabase() throws SQLException {
        dataSource = new SQLiteConnectionPoolDataSource();
        dataSource.setUrl("jdbc:sqlite:application.db");

        // Optional Configuration Settings
        org.sqlite.SQLiteConfig config = new org.sqlite.SQLiteConfig();
        config.enforceForeignKeys(true);
        config.enableLoadExtension(true);
        dataSource.setConfig(config);

        connectionPool = dataSource.getPooledConnection();
    }

    /**
     * Setup the database orm's 
     * @param con
     */
    public static void SetupORM(Connection con) {
        // Add all ORM models here
        User.Initialize(con);
    }

    /**
     * Retrieve a connection from the pool
     */
    public static Connection GetConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    /**
     * Retrieve a connection from the pool
     */
    public static Connection GetConnectionSafe() {
        try {
            return connectionPool.getConnection(); 
        } catch (SQLException e) {
            return null;
        }
    }
}