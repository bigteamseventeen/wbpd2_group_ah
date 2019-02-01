package uk.ac.alc.wpd2.callumcarmicheal.messageboard.console;

import uk.ac.alc.wpd2.callumcarmicheal.messageboard.MessageBoard;

public class Main {
    MessageBoard mb;
    MessageBoardMenu menu;
    
    public Main(String t){
        mb = new MessageBoard(t);
        menu = new MessageBoardMenu(mb);
    }
    
    public void start() {
        menu.displayMessageBoardMenu();
    }
    
    public static void main(String[] args) {
        try {
            Main main = new Main("Simple TopicMessage MB 1");
            main.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}