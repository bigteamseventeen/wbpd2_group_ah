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

	public void addTopic(Topic t) {
		topics.add(t);
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
		
		for (int i = 0; i < topics.size(); i++)
			System.out.println(String.format("%d: %s", i, topics.get(i).toString()));
		
		System.out.println();
	}


	public List<Topic> getLatestTopics(int n) {
		return topics.subList(Math.max(topics.size() - 3, 0), topics.size());
	}

	public List<Topic> getTopics() {
		return this.topics;
	}
}