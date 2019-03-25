package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.advanced.controllers;

import org.jetbrains.annotations.Nullable;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.Topic;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.TopicMessage;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Context;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.HttpRequest;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Template;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.WebBoard;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.advanced.Get;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.advanced.Post;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class ThreadController {
	
	/**
	 * @request GET /topic?id={id}
	 * @throws Exception
	 */
	@Get("/topic")
	public void getTopic(HttpRequest r) throws Exception {
		System.out.println("TopicController: Loading information on topic");
		
		Map<String,String> query = r.getQuery();
		
		// We are rendering a topic
		Topic topic;
		if ((topic = getTopicFromRequest(r,query)) == null)
			return;
		
		Context ctx = Template.CreateContext();
		ctx.put("Topic", topic);
		ctx.put("Messages", topic.getMessageList());
		
		String response = Template.Execute("advanced/thread", ctx);
		r.Send(response);
	}
	
	@Get("/topic/new")
	public void newTopic(HttpRequest r) throws Exception {
		Context ctx = Template.CreateContext();
		String response = Template.Execute("advanced/new-topic", ctx);
		r.Send(response);
		return;
	}
	
	@Post("/topic/new")
	public void createTopic(HttpRequest r) throws IOException {
		// Get the post data
		Map<String,String> post = r.GetPostForm();
		
		// Error checking
		if (!post.containsKey("topic")) {
			r.SendMessagePage("Malformed Request",
					"Failed to process request form", HttpURLConnection.HTTP_BAD_REQUEST);
			return;
		}
		
		// Get the topic text
		String topic = post.get("topic");
		
		// Clamp the text length
		if (topic.length() > 50) {
			r.SendMessagePage("Topic title is too long.",
					"The topic title must be between 5 and 50 characters.", HttpURLConnection.HTTP_BAD_REQUEST);
			return;
		} else if (topic.length() < 5) {
			r.SendMessagePage("Topic title is too short.",
					"The topic title must be between 5 and 50 characters.", HttpURLConnection.HTTP_BAD_REQUEST);
			return;
		}
		
		// Create the new topic
		Topic t = new Topic(topic);
		
		// If we have a description
		if (post.containsKey("desc")) {
			String desc = post.get("desc");
			
			// If we are over 100
			if (desc.length() > 100) {
				r.SendMessagePage("Topic description is too long.",
						"The topic description has to be less then 100 characters", HttpURLConnection.HTTP_BAD_REQUEST);
				return;
			}
			
			// Set the description
			t.setDescription(desc);
		}
		
		// Add the topic and redirect to the render page
		int i = WebBoard.MB.addTopic(t);
		r.Redirect("/topic?id=" + i);
	}
	
	/**
	 * @request POST /topic?id={id}
	 * @throws Exception
	 */
	@Post("/topic")
	public void postComment(HttpRequest r) throws Exception {
		System.out.println("NewComment: Received post request");
		Map<String,String> query = r.getQuery();
		
		// Get the request topic
		Topic topic;
		if ((topic = getTopicFromRequest(r,query)) == null)
			return;
		
		// Get the post data
		Map<String,String> post = r.GetPostForm();
		
		// Error checking
		if (!post.containsKey("author") || !post.containsKey("comment")) {
			r.SendMessagePage("Malformed Request",
					"Failed to process request form", HttpURLConnection.HTTP_BAD_REQUEST);
			return;
		}
		
		String author = post.get("author"), comment = post.get("comment");
		
		if (author.equalsIgnoreCase("system")) {
			r.SendMessagePage("Invalid Author Name",
					"The author name SYSTEM is reserved for official usage only!", HttpURLConnection.HTTP_OK);
			return;
		}
		
		// We now update the message board topic with the new message
		topic.addNewMessage(new TopicMessage(post.get("author"), post.get("comment")));
		
		int id = -1;
		try { id = Integer.parseInt(query.get("id")); }
		catch (NumberFormatException ex) {
			topicNotFound(r);
			return;
		}
		
		r.Redirect("/topic?id=" + id);
	}
	
	@Nullable
	private Topic getTopicFromRequest(HttpRequest r, Map<String,String> query) {
		if (query == null)
			query = r.getQuery();
		
		if (!query.containsKey("id")) {
			topicNotFound(r);
			return null;
		}
		
		int id = -1;
		try { id = Integer.parseInt(query.get("id")); }
		catch (NumberFormatException ex) {
			topicNotFound(r);
			return null;
		}
		
		if (id > WebBoard.MB.getNumberOfTopics() -1) {
			topicNotFound(r);
			return null;
		}
		
		return WebBoard.MB.getTopic(id);
	}
	
	private void topicNotFound(HttpRequest r) {
		System.out.println("topic not found!");
		r.SendMessagePage("Topic not found", "Im sorry we failed to find the topic specified.",
				HttpURLConnection.HTTP_NOT_FOUND);
	}
}
