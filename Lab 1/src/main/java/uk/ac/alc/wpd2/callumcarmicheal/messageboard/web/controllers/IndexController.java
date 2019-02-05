package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.controllers;

import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Context;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Controller;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Resource;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Template;

import java.io.File;


public class IndexController extends Controller {
    @Override
    protected void Get() throws Exception {
        System.out.println("IndexController: Handling Request" + "(" + exchange.getRequestURI() + ")");

        String request = exchange.getRequestURI().toString();
        if (!request.equals("/")) {
            if (request.startsWith("/"))
                 LoadResource(request.substring(1));
            else LoadResource(request);
            return;
        }
    
        IndexPage();
    }
    
    private void IndexPage() throws Exception {
        Context ctx = Template.CreateContext();
        String response = Template.Execute("home", ctx);
        Send(response);
    }
    
    private void LoadResource(String resource) throws Exception {
        // Todo caching
        
        // Protect against traversal attacks
        if (Resource.IsUnsafePath(resource)) {
            Send(400, "<h1>Malformed resource request</h1><p>Unable to process request for resource</p>");
            return;
        }
        
        // Attempt to load the file
        File f = Resource.GetFile();
        
        // The file does not exist
        if (f == null || !f.exists() || !f.canRead()) {
            Send(404, "<h1>Resource not found</h1><p>The requested resource could not be found</p>");
            return;
        }
        
        // We can now output the file to the request
        SendFile(400, f);
    }
    
}
