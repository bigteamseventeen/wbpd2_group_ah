package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.controllers;

import uk.ac.alc.wpd2.callumcarmicheal.messageboard.Topic;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Context;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Controller;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Template;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.WebBoard;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.models.SearchResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchController extends Controller {
    
    /**
     * We don't have a get page for the search
     *
     * @request GET /search
     * @throws IOException
     */
    public void Get() throws IOException {
        // Redirect back to the homepage
        System.out.println("Redirecting client back to homepage.");
        Redirect("/", "Redirecting back to homepage");
    }
    
    /**
     *
     * @throws Exception
     */
    public void Post() throws Exception {
        // Get the post form data
        System.out.println("Search controller->Post!");
        Map<String,String> post = GetPostForm();

        // Error checking
        if (!post.containsKey("query")) {
            SendMessagePage("Malformed Request",
                    "Failed to process request form", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }
        
        // Get the search query from the form
        String query = post.get("query");
        
        // Length checking on the query
        if (query.length() > 50) {
            SendMessagePage("Query is too long.",
                    "The query must be between 2 and 50 characters.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        } else if (query.length() < 2) {
            SendMessagePage("Query is too short.",
                    "The query must be between 2 and 50 characters.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        // Now we search for the topics
        String lq = query.toLowerCase();
        List<SearchResult> sr = new ArrayList<>();
        int index = 0;
        for (Topic t : WebBoard.MB.getTopics()) {
            // Check if the title or description contains criteria
            boolean inName = t.getTitle().toLowerCase().contains(lq),
                    inDesc = t.getDescription().toLowerCase().contains(lq);
            
            // Store the SearchResult and compile the HTML
            if (inName || inDesc)
                sr.add(new SearchResult(inName,inDesc,t,index,query));
            
            index++;
        }

        // Create a view context
        Context ctx = Template.CreateContext();
        ctx.put("SearchResults", sr);
        ctx.put("SearchCriteria", query);
        
        // Execute the template and send the response
        String response = Template.Execute("search", ctx);
        Send(response);
    }
}
