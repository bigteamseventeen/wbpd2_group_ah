package uk.ac.alc.wpd2.callumcarmicheal.messageboard;

import java.util.ArrayList;
import java.util.List;

public class MessageBoard {
	private final String mBTitle;
	private List<Topic> topics;
	
	public MessageBoard(String name) {
		mBTitle = name;
		topics = new ArrayList<Topic>();
	}

	public String getTitle() {
		return this.mBTitle;
	}

	public int addTopic(Topic t) {
		int index = topics.size();
		topics.add(t);
		return index;
	}
	
	public Topic getTopic(int n) {
		return topics.get(n);
	}
	
	public int getNumberOfTopics() {
		return topics.size();
	}
	
	public void display() {
		System.out.println(mBTitle);
		System.out.println("-----------");
		
		// Print our topics
		for (int i = 0; i < topics.size(); i++)
			System.out.println(String.format("%d: %s", i, topics.get(i).toString()));
		
		System.out.println();
	}


	public List<Topic> getLatestTopics(int n) {
		// Get the last(n) items in the list
		return topics.subList(Math.max(topics.size() - 3, 0), topics.size());
	}

	public List<Topic> getTopics() {
		return this.topics;
	}
}