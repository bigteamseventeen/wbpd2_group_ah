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

    public void Get() throws IOException {
        // Redirect back to the homepage

        System.out.println("Redirecting client");
        Redirect("/", "Redirecting back to homepage");
    }

    @Override
    public void Post() throws Exception {
        System.out.println("Search controller->Post!");
        Map<String,String> post = GetPostForm();

        // Error checking
        if (!post.containsKey("query")) {
            SendMessagePage("Malformed Request",
                    "Failed to process request form", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        String query = post.get("query");

        if (query.length() > 50) {
            SendMessagePage("Query is too long.",
                    "The query must be between 2 and 50 characters.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        if (query.length() < 2) {
            SendMessagePage("Query is too short.",
                    "The query must be between 2 and 50 characters.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        // Now we search for the topics
        String lq = query.toLowerCase();
        List<SearchResult> sr = new ArrayList<>();
        int index = 0;
        for (Topic t : WebBoard.MB.getTopics()) {
            boolean inName = t.getTitle().toLowerCase().contains(lq),
                    inDesc = t.getDescription().toLowerCase().contains(lq);

            if (inName || inDesc)
                sr.add(new SearchResult(inName,inDesc,t,index,query));

            index++;
        }

        Context ctx = Template.CreateContext();
        ctx.put("SearchResults", sr);
        ctx.put("SearchCriteria", query);

        String response = Template.Execute("search", ctx);
        Send(response);
    }

}
