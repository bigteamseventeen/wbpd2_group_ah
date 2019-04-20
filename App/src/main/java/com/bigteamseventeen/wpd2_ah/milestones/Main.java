package com.bigteamseventeen.wpd2_ah.milestones;

import java.sql.Connection;
import java.sql.SQLException;

import com.bigteamseventeen.wpd2_ah.milestones.console.TerminalProcessor;
import com.bigteamseventeen.wpd2_ah.milestones.console.commands.ExitCommand;
import com.bigteamseventeen.wpd2_ah.milestones.console.commands.HelpCommand;
import com.bigteamseventeen.wpd2_ah.milestones.console.commands.ReloadServerCommand;
import com.bigteamseventeen.wpd2_ah.milestones.console.commands.UsersListCommand;
import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.Resource;
import com.callumcarmicheal.wframe.Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    // Package class that will contain all the controllers
    static final String CONTROLLERSPACKAGE = "com.bigteamseventeen.wpd2_ah.milestones.controllers";
    static final int PORT = 8080; // Web Server port

    final static Logger logger = LogManager.getLogger();

    // Instances
    protected static Server server = null;
    protected static TerminalProcessor console = null;

    /**
     * Application startup and bootstrapping
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        logger.info("BigTeamSeventeen WPD2 Group AH: Bootstrapping Application");

        // Setup the console input
        if (setupConsole()) {
            logger.error("BigTeamSeventeen WPDB2 Group AH: Stopping application because SetupDatabase() failed.");
            System.exit(1);
        }

        // Setup the database
        if (setupDatabase()) {
            logger.error("BigTeamSeventeen WPDB2 Group AH: Stopping application because SetupDatabase() failed.");
            System.exit(1);
        }

        // Start the server
        if (setupServer()) {
            logger.error("BigTeamSeventeen WPDB2 Group AH: Stopping application because SetupServer() failed.");
            System.exit(1);
        }

        // Start console listener
        console.start();
    }

    private static boolean setupConsole() {
        // Setup the console handler
        console = new TerminalProcessor()
            .registerCommand(new ExitCommand())
            .registerCommand(new HelpCommand())
            .registerCommand(new UsersListCommand())
            .registerCommand(new ReloadServerCommand());

        // No need to exit
        return false;
    }

    /**
     * Setup the database connection
     * @return
     */
    private static boolean setupDatabase() {
        logger.info("Connecting to database");
        Connection DB;

        try {
            SqliteDBCon.InitializeDatabase();
            
            if ((DB = SqliteDBCon.GetConnection_s()) == null) {
                logger.error("ERROR: Failed to connect to database.");
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            return true;
        }

        logger.info("Initializing Database");
        SqliteDBCon.SetupORM(DB);

        // Release the database resource
        try { DB.close(); }
        catch(Exception ex) {
            logger.error("Failed to release test database connection to pool", ex);            
        };

        return false;
    }

    /**
     * Setup the server connection
     * @return
     */
    private static boolean setupServer() {
        try {
            // We are starting the server
            logger.info("Starting server!");

            // Set our request extensions
            HttpRequest.HttpExtensions = new RequestExtensions();

            // Start the server
            server = new Server(PORT, CONTROLLERSPACKAGE)
                .setResourcesEnabled(true) // enable resources in /bin/public
                    .setResourcesDirectory(Resource.getWorkingDirection() + "/public")
                .start();
            
            // We have started the server
            logger.info("§aServer started on port: §e" + PORT + "§r!");

            return false;
        } catch (Exception e) {
            logger.error("Failed to start server instance", e);
            return true;
        }
    }

    public static Server getServer() {
        return server;
    }
}
