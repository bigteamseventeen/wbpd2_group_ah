package com.bigteamseventeen.wpd2_ah.milestones.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.bigteamseventeen.wpd2_ah.milestones.Renderer;
import com.bigteamseventeen.wpd2_ah.milestones.SqliteDBCon;
import com.bigteamseventeen.wpd2_ah.milestones.models.User;
import com.callumcarmicheal.wframe.props.GetRequest;
import com.callumcarmicheal.wframe.props.PostRequest;
import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.database.exceptions.MissingColumnValueException;
import com.google.common.collect.ImmutableMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdminController extends Controller {
    final static Logger logger = LogManager.getLogger();
    
    @GetRequest("/admin/users")
    public void listUsers(HttpRequest request) throws IOException {
        // Redirect the user to the respected page
        User user; // If user == null then a redirect has happened
        if ((user = getUserOrLogin(request)) == null) return;
        
        // Datbase Connection
        Connection con = null;

        // Check if the user is an admin
        if (!user.isAdmin()) {
            request.SendMessagePage("Unauthorized", "This page if for authorized persons only!");
            return;
        }

        ImmutableMap.Builder<String, Object> viewBag = ImmutableMap.<String, Object>builder();
        Map<String,String> query = request.getQuery();

        // Check if we have a error and pass it to the view
        if (query.containsKey("error")) {
            try {
                int i = Integer.parseInt(query.get("error").trim());
                viewBag.put("error", i);
            } catch (NumberFormatException nfe) { }
        }

        // Get the connection and users
        // Connection con = null;
        User[] users = null;
        
        // Get the users and close the connection
        try                     { con = SqliteDBCon.GetConnection(); users = User.All(con); } 
        catch(SQLException ex)  { request.throwException("Failed to recieve all users from database", ex); } 
        finally                 {  try { if (con != null && !con.isClosed()) con.close(); } catch (Exception ex) {} }

        // Render the page
        viewBag.put("users", users);
        new Renderer().setUser(user)
            .render(request, "admin/usersList", 200, viewBag.build());
    }

    @PostRequest("/admin/users/admin")
    public void modifyAdminStatus(HttpRequest request) throws IOException {
        // Redirect the user to the respected page
        User currentUser; // If user == null then a redirect has happened
        if ((currentUser = getUserOrLogin(request)) == null) 
            return;
        
        // Check if the user is an admin
        if (!currentUser.isAdmin()) {
            request.SendMessagePage("Unauthorized", "This page if for authorized persons only!");
            return;
        }

        // 
        Map<String,String> post = request.GetPostForm();

        // Error checking
        String[] requiredKeys = new String[] { "user_id", "state" };
        for (String input : requiredKeys) {
            if (!post.containsKey(input)) {
                // Display error message
                request.Redirect("/admin/users?error=1");
                return;
            }
        }

        // Variables
        Connection con = null;
        User user = null;
        int userId = -1;
        int adminState = -1;

        try {
            // Try to parse the request information 
            userId = Integer.parseInt(post.get("user_id"));
            adminState = Integer.parseInt(post.get("state"));

            // Get the connection and user
            con = SqliteDBCon.GetConnection();
            user = User.Get(con, userId);

            // If the user does not exist
            if (user == null) {
                try { if (con != null && !con.isClosed()) con.close(); } catch (Exception ex) {}
                
                request.Redirect("/admin/users?error=2");
                return;
            }

            // If the banned state is valid
            if (adminState == 1 || adminState == 0) {
                user.setAdmin(adminState);
                user.save();
            } else {
                try { if (con != null && !con.isClosed()) con.close(); } catch (Exception ex) {}
                
                request.Redirect("/admin/users?error=1");
                return;
            }
        } catch (NumberFormatException nfe) { 
            request.Redirect("/admin/users?error=1");
            return;
        } catch (SQLException ex) {
            logger.error("Failed to get connection or user from database", ex);

            request.Redirect("/admin/users?error=1");
            return;
        } catch (MissingColumnValueException ignored) {
        } finally { try { if (con != null && !con.isClosed()) con.close(); } catch (Exception ex) {} }        

        request.Redirect("/admin/users");
    }

    @PostRequest("/admin/users/ban")
    public void modifyBanStatus(HttpRequest request) throws IOException {
        // Redirect the user to the respected page
        User currentUser; // If user == null then a redirect has happened
        if ((currentUser = getUserOrLogin(request)) == null) 
            return;
        
        // Check if the user is an admin
        if (!currentUser.isAdmin()) {
            request.SendMessagePage("Unauthorized", "This page if for authorized persons only!");
            return;
        }

        // 
        Map<String,String> post = request.GetPostForm();

        // Error checking
        String[] requiredKeys = new String[] { "user_id", "state" };
        for (String input : requiredKeys) {
            if (!post.containsKey(input)) {
                // Display error message
                request.Redirect("/admin/users?error=1");
                return;
            }
        }

        // Variables
        Connection con = null;
        User user = null;
        int userId = -1;
        int bannedState = -1;

        try {
            // Try to parse the request information 
            userId = Integer.parseInt(post.get("user_id"));
            bannedState = Integer.parseInt(post.get("state"));

            // Get the connection and user
            con = SqliteDBCon.GetConnection();
            user = User.Get(con, userId);

            // If the user does not exist
            if (user == null) {
                try { if (con != null && !con.isClosed()) con.close(); } catch (Exception ex) {}
                
                request.Redirect("/admin/users?error=2");
                return;
            }

            // If the banned state is valid
            if (bannedState == 1 || bannedState == 0) {
                user.setBanned(bannedState);
                user.save();
            } else {
                try { if (con != null && !con.isClosed()) con.close(); } catch (Exception ex) {}
                
                request.Redirect("/admin/users?error=1");
                return;
            }
        } catch (NumberFormatException nfe) { 
            request.Redirect("/admin/users?error=1");
            return;
        } catch (SQLException ex) {
            logger.error("Failed to get connection or user from database", ex);

            request.Redirect("/admin/users?error=1");
            return;
        } catch (MissingColumnValueException ignored) {
        } finally { try { if (con != null && !con.isClosed()) con.close(); } catch (Exception ex) {} }        

        request.Redirect("/admin/users");
    }
}