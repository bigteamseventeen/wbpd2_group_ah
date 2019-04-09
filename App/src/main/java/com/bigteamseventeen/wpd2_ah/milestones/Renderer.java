package com.bigteamseventeen.wpd2_ah.milestones;

import java.io.IOException;
import java.util.Map;

import com.bigteamseventeen.wpd2_ah.milestones.models.User;
import com.callumcarmicheal.wframe.HttpRequest;

import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

public class Renderer {
    private User currentUser = null;
    public Renderer setUser(User v) { this.currentUser = v; return this; } 

    /**
     * Generate a page model
     * 
     * @return
     */
    private JtwigModel GeneratePageModel() {
        return JtwigModel.newModel()
            .with("_user", currentUser)
        ;
    }

    /**
     * Render a view
     * 
     * @param request      The http request
     * @param template     The template file
     * @param responseCode The http response code
     * @param arguments    The arguments
     * @throws IOException
     */
    public void render(HttpRequest request, String template, int responseCode, Map<String, Object> arguments)
            throws IOException {
        // Get the template 
        JtwigTemplate twigTemplate = JtwigTemplate.classpathTemplate("templates/" + template + ".html");

        // Create a new model
        JtwigModel model = GeneratePageModel();

        // Add the arguments
        for (Map.Entry<String,Object> arg: arguments.entrySet())
            model.with(arg.getKey(), arg.getValue());

        // Render
        request.Send(responseCode, twigTemplate.render(model));
    }

    /**
     * Render a view (with 200 SUCCESS error code)
     * 
     * @param request   The http request
     * @param template  The template file
     * @param arguments The view arguments
     * @throws IOException
     */
    public void render(HttpRequest request, String template, Map<String, Object> arguments) throws IOException {
        render(request, template, 200, arguments);
    }
}