package com.bigteamseventeen.wpd2_ah.milestones.controllers;

import java.io.IOException;
import java.util.Map;

import com.callumcarmicheal.wframe.GetRequest;
import com.callumcarmicheal.wframe.HttpRequest;
import com.sun.net.httpserver.Headers;

public class AccountController {
    
    @GetRequest("/")
    public void IndexPage(HttpRequest request) throws IOException {
        int x = request.session().get("inc", 0) + 1;
        request.session().set("inc", x);

        Map<String,String> cookies = request.getRequestCookies();

        for(Map.Entry<String,String> m : cookies.entrySet()) 
            request.Write(m.getKey() + " = " + m.getValue() + "\n");

        request.Send("Hello World :) - " + x);
    }
}