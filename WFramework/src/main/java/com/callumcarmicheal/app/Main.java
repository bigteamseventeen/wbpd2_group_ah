package com.callumcarmicheal.app;

import java.sql.Connection;

import com.callumcarmicheal.app.models.User;
import com.callumcarmicheal.wframe.Server;
import com.callumcarmicheal.wframe.database.querybuilder.CType;
import com.callumcarmicheal.wframe.database.querybuilder.QueryResults;

import org.apache.log4j.BasicConfigurator;
import org.sqlite.core.DB;

public class Main {
    private static final String CONTROLLERSPACKAGE = "com.callumcarmicheal.app.controllers";
    private static final int PORT = 8080;
    public static Connection DB; // TODO: Add global disposing handler to release and save database

    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();

            System.out.println("Connecting to database");
            DB = SqliteDBCon.Connect();
            
            if (DB == null) {
                System.err.println("ERROR: Failed to connect to database.");
                return;
            } else {
                System.out.println("Initializing Database");
                SqliteDBCon.InitializeDatabase(DB);
            }

            // (new User())
            //     .setUsername("username")
            //     .setPassword("password")
            //     .save();
            
            QueryResults<User> query = 
                User.where(DB, "username", "=", "Callum")
                    .andWhere ("password", "=", "password")
                    .Execute();
            
            System.exit(0);

            System.out.println("Starting server!");
            
            ServiceWarmup();
            
            Server server = new Server(PORT, CONTROLLERSPACKAGE);
            server.Start();

            System.out.println("Server started on port: " + PORT + "!");
        } catch (Exception e) {
            throw e;
            // System.err.println("Failed to start server!");
            // e.printStackTrace();
        }
    }

    private static void ServiceWarmup() {
        // 
    }
}
