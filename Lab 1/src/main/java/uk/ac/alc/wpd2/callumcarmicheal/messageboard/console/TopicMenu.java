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
		// Store the topic
		this.topic = topic;
	}
	
	
	/**
	 * Display the topic menu
	 */
	public void displayTopicMenu() {
		// New message object
		TopicMessage msg;
		
		do {
			// Render the menu
			Console.Clear();
			System.out.println(String.format("Topic: %s\n------------", topic.getTitle()));
			System.out.println("1. Print Messages");
			System.out.println("2. Add new message");
			System.out.println("3. Delete message");
			System.out.println("4. Go Back");
			System.out.println("----------");

			// Switch the option
			int option = Console.Integer("Enter your choice> ");
			switch (option) {
				// Print messages
				case 1:
					Console.Clear();
					topic.displayThread();
					
					Console.WaitMessage();
					break;
				
				// Add new message
				case 2:
					
					System.out.println("\nAdding new message...");
					
					// Read input from the console
					String author = Console.String("Author> ");
					String message = Console.String("Message> ");
					
					// Create the new topic message and append it
					msg = new TopicMessage(author, message);
					topic.addNewMessage(msg);
					
					// Display the message as added
					System.out.println("Added message: \n");
					System.out.println(String.format("%d: %s -------- %s\n\t%s\n", topic.getSize(), msg.getDate(),
							msg.getAuthor(), msg.getMessage()));
					Console.WaitMessage();
					break;
				
				// Delete a message
				case 3:
					// Get the index form the console
					System.out.println("\nDeleting a message...");
					int idx = Console.Integer("Enter message id#> ");
					
					// Make sure the index is in range
					if (topic.getSize()-1 < idx) {
						System.out.println("IndexController is out of bounds.");
						Console.WaitMessage();
						break;
					}
					
					// Get message by index
					msg = topic.getMessageIDX(idx);
					
					// Print the message as removed
					System.out.println("Removed message: \n");
					System.out.println(String.format("%d: %s -------- %s\n\t%s\n", idx, msg.getDate(),
							msg.getAuthor(), msg.getMessage()));
					
					// Remove the message
					topic.removeMessage(idx);
					Console.WaitMessage();
					break;
					
				// Invalid option
				case 4: return;
				default:
					System.out.println("Invalid option. Please enter one of the options above.");
					break;
			}
		} while (true);
	}
}
