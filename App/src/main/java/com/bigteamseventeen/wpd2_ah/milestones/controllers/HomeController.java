package com.bigteamseventeen.wpd2_ah.milestones.controllers;

import java.io.IOException;

import com.bigteamseventeen.wpd2_ah.milestones.Renderer;
import com.bigteamseventeen.wpd2_ah.milestones.models.User;
import com.callumcarmicheal.wframe.props.GetRequest;
import com.callumcarmicheal.wframe.HttpRequest;
import com.google.common.collect.ImmutableMap;

public class HomeController extends Controller {
    @GetRequest("/")
    public void home(HttpRequest request) throws IOException {
        // Redirect the user to the respected page
        User user; // If user == null then a redirect has happened
        if ((user = getUserOrLogin(request)) == null) return;

        // We are logged in
        // request.Send(501, "Not implemented, sorry " + user.getUsername());

        // Render the test
        new Renderer().setUser(user)
            .render(request, "home", 200, ImmutableMap.<String,Object>builder()
            .build());
    }

    @GetRequest("/test")
    public void test(HttpRequest request) throws IOException {
        request.throwException( "There was an parsing the request information.", "Failed to load user session information.", new IOException("Index out of range.") );
    }
}