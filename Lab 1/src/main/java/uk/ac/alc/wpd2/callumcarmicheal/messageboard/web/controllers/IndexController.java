package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.controllers;

import com.sun.net.httpserver.HttpExchange;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Context;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Controller;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Template;


public class IndexController extends Controller {
    @Override
    public void handle(HttpExchange e) {
        HandleRequest(e);
    
        try { get(); } catch (Exception ex) { ThrowException(ex); }
    }

    private void get() throws Exception {
        System.out.println("IndexController: Handling Request");

        Context ctx = Template.CreateContext();
        String response = Template.Execute("home", ctx);

        Send(response);
    }
}
