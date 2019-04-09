package com.bigteamseventeen.wpd2_ah.milestones.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.bigteamseventeen.wpd2_ah.milestones.Renderer;
import com.bigteamseventeen.wpd2_ah.milestones.SqliteDBCon;
import com.bigteamseventeen.wpd2_ah.milestones.models.User;
import com.callumcarmicheal.wframe.GetRequest;
import com.callumcarmicheal.wframe.HttpRequest;
import com.google.common.collect.ImmutableMap;

public class AdminController extends Controller {
    
    @GetRequest("/admin/users")
    public void listUsers(HttpRequest request) throws IOException {
        // Redirect the user to the respected page
        User user; // If user == null then a redirect has happened
        if ((user = getUserOrLogin(request)) == null) return;

        // Check if the user is an admin
        if (!user.isAdmin()) {
            request.SendMessagePage("Unauthorized", "This page if for authorized persons only!");
            return;
        }

        // Get the connection and users
        Connection con = null;
        User[] users = null;
        
        // Get the users and close the connection
        try                     { con = SqliteDBCon.GetConnection(); users = User.All(con); } 
        catch(SQLException ex)  { request.ThrowException("Failed to recieve all users from database", ex); } 
        finally                 {  try { if (con != null && !con.isClosed()) con.close(); } catch (Exception ex) {} }

        // Render the page
        new Renderer().setUser(user)
            .render(request, "admin_users_list", 200, ImmutableMap.<String,Object>builder()
                .put("users", users)
                .build());
    }
}