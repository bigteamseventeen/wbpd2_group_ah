package com.bigteamseventeen.wpd2_ah.milestones.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.bigteamseventeen.wpd2_ah.milestones.Renderer;
import com.bigteamseventeen.wpd2_ah.milestones.SqliteDBCon;
import com.bigteamseventeen.wpd2_ah.milestones.models.Project;
import com.bigteamseventeen.wpd2_ah.milestones.models.User;
import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.database.exceptions.MissingColumnValueException;
import com.callumcarmicheal.wframe.props.GetRequest;
import com.callumcarmicheal.wframe.props.PostRequest;
import com.google.common.collect.ImmutableMap;

public class PlannerController extends Controller {
    @GetRequest("/planner/new")
    public void newProjectForm(HttpRequest request) throws IOException {
        // Redirect the user to the respected page
        User user; // If user == null then a redirect has happened
        if ((user = getUserOrLogin(request)) == null) return;
        
        int errorCode = -1;

        // Check if we had any errors        
        Map<String, String> query = request.getQuery();
        if (query.containsKey("error")) {
            try {
                errorCode = Integer.parseInt(query.get("error").trim());
            } catch (NumberFormatException nfe) { }
        }

        // Render the page
        new Renderer().setUser(user)
            .render(request, "planner/new", 200, ImmutableMap.<String,Object>builder()
                .put("error", errorCode)
            .build());
    }

    @PostRequest("/planner/new")
    public void createNewProject(HttpRequest request) throws IOException {
        // Redirect the user to the respected page
        User user; // If user == null then a redirect has happened
        if ((user = getUserOrLogin(request)) == null) return;
        
        // Get the form
        Map<String,String> post = request.GetPostForm();

        // Error checking
        String[] requiredKeys = new String[] { "title", "description" };
        for (String input : requiredKeys) {
            if (!post.containsKey(input)) {
                
                // Display error message
                request.Redirect("/planner/new");
                return;
            }
        }

        // Create a database connection
        Connection con = null;
        try {
            // Connect to the database
            con = SqliteDBCon.GetConnection();

            // Create a new project
            new Project(con)
                .setAuthor(user.getId())
                .setTitle(post.get("title"))
                .setDescription(post.get("description"))
                .save();
            
            // Redirect home
            request.Redirect("/home");
        } catch (SQLException | MissingColumnValueException ex) {
            // Throw the error
            request.throwException(ex);
            return;
        } finally {
            // Close the database connection
            try { if (con != null && con.isClosed()) con.close(); } catch(Exception e) {}
        }
    }
}