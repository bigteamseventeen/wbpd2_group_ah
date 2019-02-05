package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.controllers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.apache.log4j.Logger;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.Topic;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TopicController extends Controller {
	final static Logger logger = Logger.getLogger(TopicController.class);
	
	@Override
	protected void Get() throws Exception {
		System.out.println("TopicController.get()");
		Map<String,String> query = this.getQuery();
		
		if (!query.containsKey("id")) {
			topicNotFound();
			return;
		}
		
		int id = -1;
		try { id = Integer.parseInt(query.get("id")); }
		catch (NumberFormatException ex) {
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
	
	@Override
	protected void Post() throws Exception {
		System.out.println("TopicController.post()");
		
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
	
	
	private void temp() throws Exception {
		/*System.out.println("TopicController.temp()");

		// parse request
		Map<String, Object> parameters = new HashMap<>();
		InputStreamReader isr = new InputStreamReader(exchange.getRequest(), "utf-8");
		BufferedReader br = new BufferedReader(isr);
		String query = br.readLine();
		System.out.println("Query = " + query);
		Server.ParseDataQuery(query, parameters);
		
		// send response
		String response = "";
		for (String key : parameters.keySet())
			response += key + " = " + parameters.get(key) + "\n";
		exchange.sendResponseHeaders(200, response.length());
		
		System.out.println(response.toString());
		OutputStream os = exchange.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();*/
		
		InputStreamReader isr =  new InputStreamReader(exchange.getRequestBody(),"utf-8");
		BufferedReader br = new BufferedReader(isr);

		// From now on, the right way of moving from bytes to utf-8 characters:
		int b;
		StringBuilder buf = new StringBuilder(512);
		while ((b = br.read()) != -1) {
			buf.append((char) b);
		}
		
		br.close();
		isr.close();
		
		System.out.println(buf.toString());
		Send(buf.toString());
	}
	
	private void topicNotFound() {
		System.out.println("topic not found!");
		SendMessagePage("Topic not found", "Im sorry we failed to find the topic specified.");
	}
}
