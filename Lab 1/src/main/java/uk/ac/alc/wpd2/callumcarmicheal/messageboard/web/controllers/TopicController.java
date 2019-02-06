package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.controllers;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.Topic;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.TopicMessage;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.*;

import java.net.HttpURLConnection;
import java.util.Map;

public class TopicController extends Controller {
	final static Logger logger = Logger.getLogger(TopicController.class);
    
    /**
     * @request GET /topic?id={id}
     * @throws Exception
     */
	protected void Get() throws Exception {
        System.out.println("TopicController: Loading information on topic");

        Map<String,String> query = this.getQuery();

        // If we are creating a new topic
        if (query.containsKey("new")) {
            renderNewTopic();
            return;
        }

        // We are rendering a topic
        Topic topic;
        if ((topic = getTopicFromRequest(query)) == null)
            return;

        renderTopicPage(topic);
	}
    
    /**
     * @request POST /topic?id={id}
     * @throws Exception
     */
	protected void Post() throws Exception {
	    System.out.println("TopicController: Received post request");
        Map<String,String> query = this.getQuery();

        // If we are processing the new topic form
        if (query.containsKey("new")) {
            processNewTopicRequest();
            return;
        }

        // We are adding a comment
        processNewCommentRequest();
    }
    
    /**
     *
     * @throws Exception
     */
    private void processNewTopicRequest() throws Exception {
        // Get the post data
        Map<String,String> post = GetPostForm();

        // Error checking
        if (!post.containsKey("topic")) {
            SendMessagePage("Malformed Request",
                    "Failed to process request form", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        // Get the topic text
        String topic = post.get("topic");
        
        // Clamp the text length
        if (topic.length() > 50) {
            SendMessagePage("Topic title is too long.",
                    "The topic title must be between 5 and 50 characters.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        } else if (topic.length() < 5) {
            SendMessagePage("Topic title is too short.",
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
                SendMessagePage("Topic description is too long.",
                        "The topic description has to be less then 100 characters", HttpURLConnection.HTTP_BAD_REQUEST);
                return;
            }
            
            // Set the description
            t.setDescription(desc);
        }

        // Add the topic and redirect to the render page
        int i = WebBoard.MB.addTopic(t);
        Redirect("/topic?id=" + i);
    }
    
    /**
     *
     * @throws Exception
     */
    private void processNewCommentRequest() throws Exception {
        Map<String,String> query = this.getQuery();

        // Get the request topic
        Topic topic;
        if ((topic = getTopicFromRequest(query)) == null)
            return;

        // Get the post data
        Map<String,String> post = GetPostForm();

        // Error checking
        if (!post.containsKey("author") || !post.containsKey("comment")) {
            SendMessagePage("Malformed Request",
                    "Failed to process request form", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        String author = post.get("author"), comment = post.get("comment");

        if (author.equalsIgnoreCase("system")) {
            SendMessagePage("Invalid Author Name",
                    "The author name SYSTEM is reserved for official usage only!", HttpURLConnection.HTTP_OK);
            return;
        }

        // We now update the message board topic with the new message
        topic.addNewMessage(new TopicMessage(post.get("author"), post.get("comment")));

        int id = -1;
        try { id = Integer.parseInt(query.get("id")); }
        catch (NumberFormatException ex) {
            topicNotFound();
            return;
        }

        Redirect("/topic?id=" + id);
    }

    private void renderNewTopic() throws Exception {
        Context ctx = Template.CreateContext();
        String response = Template.Execute("new-topic", ctx);
        Send(response);
    }

    private void renderTopicPage(Topic topic) throws Exception {
        Context ctx = Template.CreateContext();
        ctx.put("Topic", topic);
        ctx.put("Messages", topic.getMessageList());

        String response = Template.Execute("thread", ctx);
        Send(response);
    }

    private Topic getTopicFromRequest() {return getTopicFromRequest(null);}
    @Nullable
    private Topic getTopicFromRequest(Map<String,String> query) {
	    if (query == null)
            query = this.getQuery();

        if (!query.containsKey("id")) {
            topicNotFound();
            return null;
        }

        int id = -1;
        try { id = Integer.parseInt(query.get("id")); }
        catch (NumberFormatException ex) {
            topicNotFound();
            return null;
        }

        if (id > WebBoard.MB.getNumberOfTopics() -1) {
            topicNotFound();
            return null;
        }

        return WebBoard.MB.getTopic(id);
    }
	
	private void topicNotFound() {
		System.out.println("topic not found!");
		SendMessagePage("Topic not found", "Im sorry we failed to find the topic specified.",
                HttpURLConnection.HTTP_NOT_FOUND);
	}
}
