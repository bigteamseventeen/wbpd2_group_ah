package com.bigteamseventeen.wpd2_ah.milestones.controllers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bigteamseventeen.wpd2_ah.milestones.HashBuilder;
import com.bigteamseventeen.wpd2_ah.milestones.MapBuilder;
import com.bigteamseventeen.wpd2_ah.milestones.Renderer;
import com.bigteamseventeen.wpd2_ah.milestones.SqliteDBCon;
import com.bigteamseventeen.wpd2_ah.milestones.misc.SearchResult;
import com.bigteamseventeen.wpd2_ah.milestones.models.Planner;
import com.bigteamseventeen.wpd2_ah.milestones.models.User;
import com.callumcarmicheal.wframe.props.GetRequest;
import com.callumcarmicheal.wframe.props.PostRequest;
import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.database.querybuilder.QueryResults;
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
                .put("title", "Please select a planner")
                .put("planners", planners)
            .build());
    }

    @GetRequest("/shared")
    public void mySharedPlanners(HttpRequest request) throws IOException {
        // Redirect the user to the respected page
        User user; // If user == null then a redirect has happened
        if ((user = getUserOrLogin(request)) == null) return;

        // Get all of the planners
        Connection con = null;
        Planner[] planners = null; // = Planner.All(con)

        try {
            // Get connection and planners
            con = SqliteDBCon.GetConnection();
            planners = Planner.AllSharedFor(con, user);
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
                .put("title", "Select one of your shared planners")
                .put("planners", planners)
            .build());
    }

    @PostRequest("/search")
    public void searchPublicPlanners(HttpRequest request) throws IOException {
        // Redirect the user to the respected page
        User user; // If user == null then a redirect has happened
        if ((user = getUserOrLogin(request)) == null) return;

        // Get the post form data
        System.out.println("Search controller->Post!");
        Map<String,String> post = request.GetPostForm();

        // Error checking
        if (!post.containsKey("query")) {
            request.SendMessagePage("Malformed Request",
                    "Failed to process request form", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }
        
        // Get the search query from the form
        String query = post.get("query");
        
        // Length checking on the query
        if (query.length() > 100) {
            request.SendMessagePage("Query is too long.",
                    "The query must be between 2 and 100 characters.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        } else if (query.length() < 2) {
            request.SendMessagePage("Query is too short.",
                    "The query must be between 2 and 100 characters.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        } else if (query.contains("%")) {
            request.SendMessagePage("Query is invalid.",
                    "The query cannot contain the following characters: %.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        // Get all of the planners
        Connection con = null;
        Planner[] planners = null; // = Planner.All(con)
        List<SearchResult> sr = new ArrayList<>();
        try {
            // Get connection and planners
            con = SqliteDBCon.GetConnection();
            
            QueryResults<Planner> planner = Planner
                .where(con, "lower(title)", "LIKE", "%" + query + "%")
                .orWhere("lower(description)", "LIKE", "%" + query + "%")
                .execute();

            String lq = query.toLowerCase();

            if (planner.Successful) {
                int index = 0;
                for( Planner p : planner.Rows ) {
                    if (!p.getPublicStatus()) continue;

                    // Check if the title or description contains criteria
                    boolean inName = p.getTitle().toLowerCase().contains(lq),
                            inDesc = p.getDescription().toLowerCase().contains(lq);
                    
                    // Store the SearchResult and compile the HTML
                    if (inName || inDesc)
                        sr.add(new SearchResult(inName,inDesc,p,index,query));

                    index++;
                }
            } else {
                request.SendMessagePage("Could not find any results",
                    "The query came back empty!", HttpURLConnection.HTTP_BAD_REQUEST);
                return;
            }

        } catch (SQLException ex) {
            request.throwException("Failed to load planners.", ex);
            return;
        } finally {
            // Close the database connection
            try { if (con != null && con.isClosed()) con.close(); } catch(Exception e) {}
        }
        
        // Render the Page
        new Renderer().setUser(user)
            .render(request, "search", 200, HashBuilder.<String,Object>builder()
                .put("title", "Select one of your shared planners")
                .put("SearchCriteria", query)
                .put("SearchResults", sr)
            .build());
    }

    // @GetRequest("/test")
    // public void test(HttpRequest request) throws IOException {
    //     request.throwException( "There was an parsing the request information.", "Failed to load user session information.", new IOException("Index out of range.") );
    // }
}