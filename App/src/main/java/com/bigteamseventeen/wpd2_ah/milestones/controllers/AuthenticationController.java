package com.bigteamseventeen.wpd2_ah.milestones.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.bigteamseventeen.wpd2_ah.milestones.Renderer;
import com.bigteamseventeen.wpd2_ah.milestones.SqliteDBCon;
import com.bigteamseventeen.wpd2_ah.milestones.models.User;
import com.callumcarmicheal.wframe.GetRequest;
import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.PostRequest;
import com.callumcarmicheal.wframe.database.querybuilder.SDWhereQuery.QueryValueType;
import com.google.common.collect.ImmutableMap;

public class AuthenticationController extends Controller {
    @GetRequest("/logout")
    public void logout(HttpRequest request) throws IOException {
        // Attempt to get the user
        User user; // if null then we are not logged in, redirect to "/"
        if ((user = getUser(request)) == null) { request.Redirect("/"); return; }

        // We are logged in so remove the session then redirect to "/"
        user.logoutSession(request.session());
        request.Redirect("/");
    }

    @GetRequest("/login")
    public void login(HttpRequest request) throws IOException {
        // Check if we are logged in, if so then redirect to index to be redirect back
        // to home
        if (User.IsSessionAuthenticated(request.session())) {
            request.Redirect("/");
            return;
        }

        // Viewbag
        ImmutableMap.Builder<String, Object> viewBag = ImmutableMap.<String, Object>builder();

        // Check if we had any errors        
        Map<String, String> query = request.getQuery();
        
        if (query.containsKey("error")) {
            try {
                int i = Integer.parseInt(query.get("error").trim());
                viewBag.put("error", i);
            } catch (NumberFormatException nfe) { }
        }

        if (query.containsKey("status")) {
            try {
                int i = Integer.parseInt(query.get("status").trim());
                viewBag.put("status", i);
            } catch (NumberFormatException nfe) { }
        }

        // Render the page
        new Renderer().render(request, "login", 200, viewBag.build());
    }

    @PostRequest("/login")
    public void processLogin(HttpRequest request) throws IOException {
        // Check if we are logged in, if so then redirect to index to be redirect back to home
        if (User.IsSessionAuthenticated(request.session())) {
            request.Redirect("/"); 
            return;
        }

        // Get the post data
        Map<String, String> post = request.GetPostForm();

        // Error checking
        String[] requiredKeys = new String[] { "email", "password" };
        for (String input : requiredKeys) {
            if (!post.containsKey(input)) {
                // Display error message
                request.Redirect("/login?error=1");
                return;
            }
        }
        
        // Get the values
        String email = post.get("email");
        String password = post.get("password");

        // Check if the user with the email exists
        Connection con = null;
        User user = null;

        // Try to get the user
        try {
            con = SqliteDBCon.GetConnection();
            user = User.FindEmail(con, email);
        } catch (SQLException ex) {
            request.ThrowException(ex);
            return;
        } finally {
            // Close the connection
            try { if (con != null && con.isClosed()) con.close(); } catch(Exception e) {}
        }

        // Check if we could find the email
        if (user == null) {
            // Could not find email
            request.Redirect("/login?error=2");
            return;
        }

        // Check if the password is valid
        if (!user.checkPassword(password)) {
            // Its not valid
            request.Redirect("/login?error=3");
            return;    
        }

        // The user has successfully authenticated, now we need to store this in the session
        user.authenticateSession(request.session());

        // Redirect to home
        request.Redirect("/");
    }

    @GetRequest("/register")
    public void pageRegister(HttpRequest request) throws IOException {
        // Check if we are logged in, if so then redirect to index to be redirect back to home
        if (User.IsSessionAuthenticated(request.session())) {
            request.Redirect("/"); 
            return;
        }

        // Check if we had any errors
        Map<String,String> query = request.getQuery();
        ImmutableMap.Builder<String,Object> viewBag = ImmutableMap.<String, Object>builder();

        if (query.containsKey("error")) {
            try {
                int i = Integer.parseInt(query.get("error").trim());
                viewBag.put("error", i);
            } catch (NumberFormatException nfe) { }
        }

        // Render the page
        new Renderer().render(request, "register", 200, viewBag.build());
    }

    @PostRequest("/register")
    public void processRegistration(HttpRequest request) throws IOException {
        // Check if we are logged in, if so then redirect to index to be redirect back
        // to home
        if (User.IsSessionAuthenticated(request.session())) {
            request.Redirect("/");
            return;
        }

        // Error checking
        Map<String, String> post = request.GetPostForm();
        String[] requiredKeys = new String[] { "username", "password", "email" };

        for (String input : requiredKeys) {
            if (!post.containsKey(input)) {
                // Display error message
                request.Redirect("/register?error=1");
                return;
            }
        }

        // Get a database connection
        Connection con = null;
        try {
            con = SqliteDBCon.GetConnection();
            // Check if the username exists
            if (User.where(con, "username", "=", post.get("username"), QueryValueType.Bound)
                    .setLimit(1)
                    .execute()
                        .Successful) {
                request.Redirect("/register?error=2");
                return;
            }

            // Check if the email exists
            if (User.where(con, "email", "=", post.get("email"), QueryValueType.Bound)
                    .setLimit(1)
                    .execute()
                        .Successful) {
                request.Redirect("/register?error=3");
                return;
            }

            // Neither the username or email exists, time to create the account
            new User(con)
                .setUsername(post.get("username"))
                .setPasswordEncrypted(post.get("password"))
                .setEmail(post.get("email"))
                .setAdmin(0)
                .setBanned(0)
                .save_s();
        } catch(SQLException ex) {
            request.ThrowException(ex);
            return;
        } finally {
            // Close the connection
            try { if (con != null && con.isClosed()) con.close(); } catch(Exception e) {}
        }
        
        // Redirect to login
        request.Redirect("/login?status=1");
    }
}