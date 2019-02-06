package uk.ac.alc.wpd2.callumcarmicheal.messageboard;

import java.util.ArrayList;
import java.util.List;

public class Topic {
	private String title;
	private String description = "";
	private List<TopicMessage> messageList;
	
	
	public Topic(String title) {
		this.title = title;
		this.messageList = new ArrayList<>();
	}
	
	public void displayThread() {
		System.out.println("[THREAD START] " + this.title + "\n\n");
		
		// Loop the messages and print em
		for (int x = 0; x < messageList.size(); x++) {
			TopicMessage msg = messageList.get(x);
			System.out.println(
				String.format("%d: %s -------- %s\n\t%s\n", x, msg.getDate(), msg.getAuthor(), msg.getMessage()));
		}
		
		System.out.println("\n[THREAD END]");
	}
	
	public void addNewMessage(TopicMessage msg) {
		messageList.add(msg);
	}
	
// -- Getters
	
	public String getTitle() {
		return title;
	}
	
	public TopicMessage getMessageIDX(int x) {
		return messageList.get(x);
	}
	
	public void setMessageIDX(int x, TopicMessage m) {
		messageList.set(x, m);
	}
	
	public boolean hasMessage(TopicMessage m) {
		return messageList.contains(m);
	}

	public boolean hasMessages() {
		return !messageList.isEmpty();
	}
	
	public void removeMessage(int x) {
		messageList.remove(x);
	}
	
	public void removeMessage(TopicMessage m) {
		messageList.remove(m);
	}
	
	public int getSize() {
		return this.messageList.size();
	}
	
	public List<TopicMessage> getMessageList() {
		return this.messageList;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%d messages)", this.title, this.messageList.size());
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}