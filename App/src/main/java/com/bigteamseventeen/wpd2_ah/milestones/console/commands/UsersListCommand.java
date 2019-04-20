package com.bigteamseventeen.wpd2_ah.milestones.console.commands;

import java.sql.Connection;
import java.sql.SQLException;

import com.bigteamseventeen.wpd2_ah.milestones.SqliteDBCon;
import com.bigteamseventeen.wpd2_ah.milestones.console.IConsoleCommand;
import com.bigteamseventeen.wpd2_ah.milestones.console.TerminalProcessor;
import com.bigteamseventeen.wpd2_ah.milestones.models.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UsersListCommand implements IConsoleCommand {
    static final Logger logger = LogManager.getLogger();

    @Override public String getCommand() {
        return "users/list";
    }

    @Override public String getDescription() {
        return "Lists all users in the database";
    }

    @Override public String getParameterHint() {
        return "<> | {email (e) | username (u)} {query}";
    }

    @Override public void process(TerminalProcessor processor, String[] args) {
        if (args.length == 0) {
            // We have no query
            listAllUsers();
            return;
        }

        // We have a query
        if (args.length != 2) {
            logger.warn("List Users Command: Invalid parameters passed - " + args);
            logger.warn("Expecting - {type} {query}: eg (users/list email admin@example.com)");
            return;
        }

        displayUser(args[0], args[1]);
    }

    /**
     * List all users in the console
     * @return
     */
    private void listAllUsers() {
        logger.trace("ListUsers: Setting up connection and querying database...");

        // Connection and Users
        Connection con = null;
        User[] users = null;

        // String
        StringBuffer sb = new StringBuffer();
        String format = "\t%s\n";

        try {
            con = SqliteDBCon.GetConnection();
            users = User.All(con);
        } catch(SQLException ex) {
            logger.error("ListUsers: Failed to get connection or execute query.", ex);
        } finally {
            // Close / release the connection
            if (con != null) try {con.close();} catch(Exception ex) {}
        }

        // Now we want to list all users
        for (User u : users) 
            sb.append(String.format(format, u));
        
        // Print the users
        logger.trace("ListUsers: Displaying " + users.length + " users in database:\n" + sb.substring(0, sb.length()-1));
    }

    private void displayUser(String type, String value) {
        // Todo: User specific code
    }
}