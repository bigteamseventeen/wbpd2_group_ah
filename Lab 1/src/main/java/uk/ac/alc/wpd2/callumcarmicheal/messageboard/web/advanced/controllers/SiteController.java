package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.advanced.controllers;


import uk.ac.alc.wpd2.callumcarmicheal.messageboard.Topic;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Context;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.HttpRequest;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Template;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.WebBoard;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.advanced.Get;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.advanced.Post;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.models.SearchResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class SiteController {
	@Get("/")
	public void indexPage(HttpRequest r) throws Exception {
		// Render the page
		r.Send(Template.Execute("advanced/home", Template.CreateContext()));
	}
	
	@Get("/search")
	public void redirectHome_Search(HttpRequest r) throws IOException {
		// Redirect back to the homepage
		r.Redirect("/", "Redirecting back to homepage");
	}
	
	@Post("/search")
	public void searchThreads(HttpRequest r) throws Exception {
		System.out.println("Search Request");
		Map<String,String> post = r.GetPostForm();
		
		// Error checking
		if (!post.containsKey("query")) {
			r.SendMessagePage("Malformed Request",
					"Failed to process request form", HttpURLConnection.HTTP_BAD_REQUEST);
			return;
		}
		
		// Get the search query from the form
		String query = post.get("query");
		
		// Length checking on the query
		if (query.length() > 50) {
			r.SendMessagePage("Query is too long.",
					"The query must be between 2 and 50 characters.", HttpURLConnection.HTTP_BAD_REQUEST);
			return;
		} else if (query.length() < 2) {
			r.SendMessagePage("Query is too short.",
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
		String response = Template.Execute("advanced/search", ctx);
		r.Send(response);
	}
}
