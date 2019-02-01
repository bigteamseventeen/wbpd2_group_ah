package uk.ac.alc.wpd2.callumcarmicheal.messageboard.console;

import uk.ac.alc.wpd2.callumcarmicheal.messageboard.Topic;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.TopicMessage;

public class TopicMenu {
	private Topic topic;
	
	/**
	 * A menu to select features of the topic
	 * @param topic     The topic email
	 */
	public TopicMenu(Topic topic) {
		this.topic = topic;
	}
	
	
	public void displayTopicMenu() {
		topic.displayThread();
		
		int option = 0;
		TopicMessage msg;
		
		do {
			Console.Clear();
			System.out.println(String.format("Topic: %s\n------------", topic.getTitle()));
			System.out.println("1. Print Messages");
			System.out.println("2. Add new message");
			System.out.println("3. Delete message");
			System.out.println("4. Go Back");
			System.out.println("----------");
			
			option = Console.Integer("Enter your choice> ");
			
			switch (option) {
				case 1:
					Console.Clear();
					topic.displayThread();
					
					Console.WaitMessage();
					break;
				
				case 2:
					System.out.println("\nAdding new message...");
					String author = Console.String("Author> ");
					String message = Console.String("Message> ");
					msg = new TopicMessage(author, message);
					topic.addNewMessage(msg);
					
					System.out.println("Added message: \n");
					System.out.println(String.format("%d: %s -------- %s\n\t%s\n", topic.getSize(), msg.getDate(),
							msg.getAuthor(), msg.getMessage()));
					Console.WaitMessage();
					break;
				
				case 3:
					System.out.println("\nDeleting a message...");
					int idx = Console.Integer("Enter message id#> ");
					
					if (topic.getSize()-1 < idx) {
						System.out.println("IndexController is out of bounds.");
						Console.WaitMessage();
						break;
					}
					
					msg = topic.getMessageIDX(idx);
					
					System.out.println("Removed message: \n");
					System.out.println(String.format("%d: %s -------- %s\n\t%s\n", idx, msg.getDate(),
							msg.getAuthor(), msg.getMessage()));
					
					
					topic.removeMessage(idx);
					
					Console.WaitMessage();
					
					break;
					
				case 4: return;
					
				default:
					System.out.println("Invalid option. Please enter one of the options above.");
					break;
			}
		} while (true);
	}
}
