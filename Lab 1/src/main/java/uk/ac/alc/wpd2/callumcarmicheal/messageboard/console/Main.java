package uk.ac.alc.wpd2.callumcarmicheal.messageboard.console;

import uk.ac.alc.wpd2.callumcarmicheal.messageboard.MessageBoard;

import java.net.URLConnection;

public class Main {
    MessageBoard mb;
    MessageBoardMenu menu;
    
    public Main(String t){
        // Create the instances, MessageBoard and then the Menu
        mb = new MessageBoard(t); // Create mb with the Title of t.
        menu = new MessageBoardMenu(mb);
    }
    
    public void start() {
        // Display the message board menu
        menu.displayMessageBoardMenu();
    }
    
    public static void main(String[] args) {
        try {
            // We want to instantiate the Main class
            Main main = new Main("Simple TopicMessage MB 1");
            main.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}