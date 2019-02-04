package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.controllers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.Topic;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TopicController extends Controller {
	@Override
	public void handle(HttpExchange e) {
		HandleRequest(e);
		
		// Serve for POST requests only
		if (e.getRequestMethod().equalsIgnoreCase("POST")) {
			try { temp(); return; } catch (Exception ex) { ThrowException(ex); }
		}
		
		try { get(); return; } catch (Exception ex) { ThrowException(ex); }
	}
	
	private void get() throws Exception {
		System.out.println("TopicController: Handling Request");
		Map<String,String> query = this.getQuery();
		
		if (!query.containsKey("id")) {
			topicNotFound();
			return;
		}
		
		int id = -1;
		try {
			id = Integer.parseInt(query.get("id"));
		} catch (NumberFormatException ex) {
			topicNotFound();
			return;
		}
		
		if (id > WebBoard.MB.getNumberOfTopics() -1) {
			topicNotFound();
			return;
		}
		
		Topic topic = WebBoard.MB.getTopic(id);
		Context ctx = Template.CreateContext();
		ctx.put("Topic", topic);
		ctx.put("TopicId", id);
		ctx.put("Messages", topic.getMessageList());
		
		String response = Template.Execute("thread", ctx);
		Send(response);
	}
	
	private void temp() throws Exception {
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
		InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
		BufferedReader br = new BufferedReader(isr);
		String query = br.readLine();
		Server.ParseDataQuery(query, parameters);
		
		// send response
		String response = "";
		for (String key : parameters.keySet())
			response += key + " = " + parameters.get(key) + "\n";
		exchange.sendResponseHeaders(200, response.length());
		
		System.out.println(response.toString());
		OutputStream os = exchange.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();
	}
	
	private void post() throws Exception {
		System.out.println("This is a post request");
		Headers requestHeaders = exchange.getRequestHeaders();
		Set<Map.Entry<String, List<String>>> entries = requestHeaders.entrySet();
		
		int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));
		System.out.println(""+requestHeaders.getFirst("Content-length"));
		
		InputStream is = exchange.getRequestBody();
		
		byte[] data = new byte[contentLength];
		int length = is.read(data);
		Map<String,String> query = Server.ParseQuery(new String(data));

		System.out.println(query);
		SendMessagePage("Hello World",  new String(data) + "   \n   " + query.toString());
	}
	
	private void topicNotFound() {
		System.out.println("topic not found!");
		SendMessagePage("Topic not found", "Im sorry we failed to find the topic specified.");
	}
}
