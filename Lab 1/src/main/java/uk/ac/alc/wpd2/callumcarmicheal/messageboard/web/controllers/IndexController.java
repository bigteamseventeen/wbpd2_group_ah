package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.controllers;

import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Context;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Controller;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Resource;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Template;

import java.io.File;


public class IndexController extends Controller {
    protected void Get() throws Exception {
        System.out.println("IndexController: Handling Request" + "(" + Exchange.getRequestURI() + ")");

        // Check if we are requesting a resource (something that is not just index /)
        String request = Exchange.getRequestURI().toString();
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
        // Protect against traversal attacks
        if (Resource.IsUnsafePath(resource)) {
            SendMessagePage("Resource not found", "The requested resource could not be found or the request was malformed", 404);
            return;
        }
        
        // Attempt to load the file
        File f = Resource.GetPublicFile(resource);
        
        // The file does not exist
        if (f == null || !f.exists() || !f.canRead()) {
            if (f == null) {
                SendMessagePage(
                        "Resource not found",
                        "The requested resource could not be found. (f == null)", 404);
                return;
            }
            
            if (!f.exists()) {
                SendMessagePage(
                        "Resource not found",
                        "The requested resource could not be found. (!f.exists()) - Path: " + f.getAbsolutePath(), 404);
                return;
            }
            
            SendMessagePage(
                    "Resource not found",
                    "The requested resource could not be found. (f == null, !f.exists(), !f.canRead())", 404);
            return;
        }
        
        // We can now output the file to the request
        SendFile(200, f);
    }
    
}
