package com.bigteamseventeen.wpd2_ah.milestones.console.commands;

import java.sql.Connection;
import java.sql.SQLException;

import com.bigteamseventeen.wpd2_ah.milestones.Main;
import com.bigteamseventeen.wpd2_ah.milestones.SqliteDBCon;
import com.bigteamseventeen.wpd2_ah.milestones.console.IConsoleCommand;
import com.bigteamseventeen.wpd2_ah.milestones.console.TerminalProcessor;
import com.bigteamseventeen.wpd2_ah.milestones.models.User;
import com.callumcarmicheal.wframe.database.exceptions.MissingColumnValueException;
import com.callumcarmicheal.wframe.exception.RequestPathConfliction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.units.qual.radians;

public class UsersAdminCommand implements IConsoleCommand {
    static final Logger logger = LogManager.getLogger();

    @Override
    public String getCommand() {
        return "users/admin";
    }

    @Override
    public String getDescription() {
        return "Add's or removes admin status from a user.";
    }

    @Override
    public String getParameterHint() {
        return "{add (a) / remove (rem, r)} {email}";
    }

    @Override
    public void process(TerminalProcessor processor, String[] args) {
        logger.info("UsersAdminCommand: ");

        if (args.length < 2) {
            logger.trace("UsersAdminCommand: Invalid amount of arguments, 2 arguments required. Expected - "
                    + getParameterHint());
            return;
        }

        String argState = args[0].toLowerCase();
        String email = args[1];

        if (argState != "add" && argState != "a" && argState != "remove" && argState != "rem" && argState != "r") {
            logger.trace("UsersAdminCommand: Invalid argument (" + argState + "). Expected - " + getParameterHint());
            return;
        }

        int isAdmin = (argState == "add" || argState == "a") ? 1 : 0;

        // Try to find the user by email
        User user = null;
        Connection con = null;

        try {
            // Get the user and connection, and update the user status
            con = SqliteDBCon.GetConnection();
            user = User.FindEmail(con, email);

            user.setAdmin(isAdmin);
            user.save();
        } catch (SQLException e) {
            logger.error("UsersAdminCommand: Failed to get connection.", e);
        } catch (MissingColumnValueException e) {
            logger.error("UsersAdminCommand: failed to update user orm object.", e);
        }

        // Close the connection
        try { if (con != null) con.close(); }
        catch (Exception ignored) {}
    }
}