package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web;

import org.apache.log4j.BasicConfigurator;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.Topic;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.TopicMessage;

public class Main {

    public static void main(String[] args) {
        try {
            BasicConfigurator.configure();

            System.err.println("Starting server!");

            ServiceWarmup();

            Server server = new Server(8080);
            server.Start();
            System.err.println("Server started!");
        } catch (Exception e) {
            System.err.println("Failed to start server!");
            e.printStackTrace();
        }
    }

    private static void ServiceWarmup() {
        Topic t = new Topic("Default Topic");
        t.addNewMessage(new TopicMessage("SYSTEM", "New topic created!"));
        WebBoard.MB.addTopic(t);
    }
}
