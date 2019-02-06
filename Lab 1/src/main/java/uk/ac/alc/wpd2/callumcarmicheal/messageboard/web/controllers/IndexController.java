package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.controllers;

import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Context;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Controller;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Resource;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Template;

import java.io.File;


public class IndexController extends Controller {
    protected void Get() throws Exception {
        System.out.println("IndexController: Handling Request" + "(" + exchange.getRequestURI() + ")");

        // Check if we are requesting a resource (something that is not just index /)
        String request = exchange.getRequestURI().toString();
        if (!request.equals("/")) {
            if (request.startsWith("/"))
                 LoadResource(request.substring(1));
            else LoadResource(request);
            return;
        }
    
        // Render the index page
        IndexPage();
    }
    
    /**
     * Index page
     * @request GET /
     * @throws Exception
     */
    private void IndexPage() throws Exception {
        // Render the page
        Send(Template.Execute("home", Template.CreateContext()));
    }
    
    
    private void LoadResource(String resource) throws Exception {
        // Todo caching and sending of resources
        
        // Protect against traversal attacks
        if (Resource.IsUnsafePath(resource)) {
            SendMessagePage("Resource not found", "The requested resource could not be found.", 404);
            return;
        }
        
        // Attempt to load the file
        File f = Resource.GetFile();
        
        // The file does not exist
        if (f == null || !f.exists() || !f.canRead()) {
            SendMessagePage(
                    "Resource not found",
                    "The requested resource could not be found.", 404);
            return;
        }
        
        // This function is not finished yet.
        SendMessagePage("Not implemented", "Not implemented");
        
        // We can now output the file to the request
        //SendFile(400, f);
    }
    
}
