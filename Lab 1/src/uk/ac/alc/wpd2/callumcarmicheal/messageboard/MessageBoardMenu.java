package uk.ac.alc.wpd2.callumcarmicheal.messageboard;

public class MessageBoardMenu {
	private MessageBoard currentBoard;
	
	public MessageBoardMenu(MessageBoard mb) {
		currentBoard = mb;
	}
	
	public void displayMessageBoardMenu() {
		int option = 0;
		
		
		do {
			Console.Clear();
			currentBoard.display();
			
			System.out.println("\nMain Menu:");
			System.out.println("----------");
			System.out.println("1. Add new topic");
			System.out.println("2. Select a topic to view or post to");
			System.out.println("3. Quit");
			System.out.println("----------");
			option = Console.Integer("Enter your choice> ");
			
			switch (option) {
				case 1:
					System.out.print("\n\nEnter the title of your new topic\n> ");
					String t = Console.String();
					
					System.out.println("Adding new topic '" + t + "'");
					currentBoard.addTopic(new Topic(t));
					Console.WaitMessage();
					break;
				
				case 2:
					
					System.out.print("\n\nEnter the number of the topic you would like to go to\n");
					int choice = Console.Integer("> ");
					
					if (choice >= 0 && choice < currentBoard.getNumberOfTopics()) {
						Topic topic = currentBoard.getTopic(choice);
						System.out.println("Topic " + choice + " (" + topic.getTitle() + ") selected");
						new TopicMenu(topic).displayTopicMenu();
					} else {
						System.out.println("Invalid index.");
					}
					
					
					Console.WaitMessage();
					break;
					
				case 3:
					System.out.println("Goodbye.");
					System.exit(0);
				default:
					System.out.println("Invalid option. Please enter one of the options above.");
					displayMessageBoardMenu();
					
					Console.WaitMessage();
			}
		} while (option != 3);
	}
}
