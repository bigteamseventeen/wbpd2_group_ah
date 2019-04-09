package com.callumcarmicheal.wframe.testing;

import java.io.IOException;
import java.util.Map;

import com.callumcarmicheal.wframe.GetRequest;
import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.Server;
import com.callumcarmicheal.wframe.web.Session;

public class SimpleDebugApp {
    public static void main(String[] args) throws Exception {
        Server server = new Server(9000, "com.callumcarmicheal.wframe.testing");
        server.start();
    }

    @GetRequest("/")
    public void getIndexPage(HttpRequest request) throws IOException {
        Map<String,String> cookies = request.getRequestCookies();
        Session s = request.session();

        request.Write("<pre>");
        for (Map.Entry<String,String> cookie : cookies.entrySet())
            request.Write(cookie.getKey() + " = " + cookie.getValue() + "\n");

        request.Write("\n");
        request.Send("Session Id = " + request.session().getSessionKey() + "\n" +
            "Session no  + " + (request.session().set("i", request.session().get("i", 0) + 1)));
    }
}