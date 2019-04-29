package com.bigteamseventeen.wpd2_ah.milestones.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.bigteamseventeen.wpd2_ah.milestones.HashBuilder;
import com.bigteamseventeen.wpd2_ah.milestones.MapBuilder;
import com.bigteamseventeen.wpd2_ah.milestones.Renderer;
import com.bigteamseventeen.wpd2_ah.milestones.SqliteDBCon;
import com.bigteamseventeen.wpd2_ah.milestones.models.Planner;
import com.bigteamseventeen.wpd2_ah.milestones.models.User;
import com.callumcarmicheal.wframe.props.GetRequest;
import com.callumcarmicheal.wframe.HttpRequest;
import com.google.common.collect.ImmutableMap;


public class HomeController extends Controller {
    @GetRequest("/")
    public void home(HttpRequest request) throws IOException {
        // Redirect the user to the respected page
        User user; // If user == null then a redirect has happened
        if ((user = getUserOrLogin(request)) == null) return;

        // Get all of the planners
        Connection con = null;
        Planner[] planners = null; // = Planner.All(con)

        try {
            // Get connection and planners
            con = SqliteDBCon.GetConnection();
            planners = Planner.AllFor(con, user);
        } catch (SQLException ex) {
            request.throwException("Failed to load planners.", ex);
            return;
        } finally {
            // Close the database connection
            try { if (con != null && con.isClosed()) con.close(); } catch(Exception e) {}
        }
        
        // Render the Page
        new Renderer().setUser(user)
            .render(request, "home", 200, HashBuilder.<String,Object>builder()
                .put("planners", planners)
            .build());
    }

    // @GetRequest("/test")
    // public void test(HttpRequest request) throws IOException {
    //     request.throwException( "There was an parsing the request information.", "Failed to load user session information.", new IOException("Index out of range.") );
    // }
}