package com.bigteamseventeen.wpd2_ah.milestones.controllers;

import java.io.IOException;

import com.bigteamseventeen.wpd2_ah.milestones.models.User;
import com.callumcarmicheal.wframe.HttpRequest;

class Controller {
    /**
     * Attempt to get the current logged in user
     * 
     * @param request
     * @return
     */
    protected User getUser(HttpRequest request) {
        // Get the current user 
        return User.GetSessionUser(request.session());
    }

    /**
     * Attempts to get user or will redirect to login
     * 
     * @param request
     * @return
     * @throws IOException
     */
    protected User getUserOrLogin(HttpRequest request) throws IOException {
        // Get the user from session
        User user = User.GetSessionUser(request.session());

        // If the user is null the we are not logged in
        if (user == null) {
            // Send redirect to login
            request.Redirect("/login", "Redirecting to authentication page");

            // We dont have a user 
            return null;
        }

        // Check if the user is banned
        else if (user.isBanned()) {
            // Logout of the session
            user.logoutSession(request.session());

            // Send redirect to login
            request.Redirect("/login?error=4", "Redirecting to authentication page");

            // We dont have a user 
            return null;
        }

        // We have the user
        return user;
    }
}