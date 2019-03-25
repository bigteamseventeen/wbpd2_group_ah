package com.bigteamseventeen.g1.milestones;

import java.sql.Connection;

import com.bigteamseventeen.g1.milestones.models.User;
import com.callumcarmicheal.wframe.Server;
import com.callumcarmicheal.wframe.database.querybuilder.CType;
import com.callumcarmicheal.wframe.database.querybuilder.QueryResults;

import org.apache.log4j.BasicConfigurator;
import org.sqlite.core.DB;

public class Main {
    private static final String CONTROLLERSPACKAGE = "com.bigteamseventeen.g1.milestones.controllers";
    private static final int PORT = 8080;
    public static Connection DB; // TODO: Add global disposing handler to release and save database

    public static void main(String[] args) throws Exception {
        System.out.println("Starting BigTeamSeventeen WPDB2 Group 1: Milestones");

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
