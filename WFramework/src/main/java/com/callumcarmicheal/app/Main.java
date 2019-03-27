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

    public static void main(String[] args) throws Exception {
        System.out.println("Starting WFramework");

        try {
            BasicConfigurator.configure();

            System.out.println("Connecting to database");
            SqliteDBCon.InitializeDatabase();
            Connection DB = SqliteDBCon.GetConnection();

            if (DB == null) {
                System.err.println("ERROR: Failed to connect to database.");
                return;
            } else {
                System.out.println("Initializing Database");
                SqliteDBCon.SetupORM(DB);
            }

        //    (new User(DB))
        //        .setUsername("CallumCarmicheal")
        //        .setPassword("password")
        //        .setEmail("callum@gmail.com")
        //        .setAdmin(1)
        //        .setBanned(0)
        //        .save();
//
//            System.exit(0);
//
           QueryResults<User> query =
               User.where(DB, "username", "=", "CallumC")
                   .Execute();
//
//            System.out.println("\n\n\n");
//            System.out.println("Length: " + query.Length);
//
//            for (int i = 0; i < query.Length; i++)
//                System.out.println("Row " + i + ": " + query.Rows[i]);
//
//            System.exit(0);

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
