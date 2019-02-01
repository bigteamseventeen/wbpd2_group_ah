package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web;

import uk.ac.alc.wpd2.callumcarmicheal.messageboard.MessageBoard;

public class WebBoard {
    public static final MessageBoard MB = CreateBoard();

    private static MessageBoard CreateBoard() {
        return new MessageBoard("Callum's Message MB");
    }

}
