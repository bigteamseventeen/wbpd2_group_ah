package com.callumcarmicheal.app.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import com.callumcarmicheal.app.Main;
import com.callumcarmicheal.app.models.User;
import com.callumcarmicheal.wframe.Get;
import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.Post;
import com.callumcarmicheal.wframe.database.querybuilder.CType;
import com.callumcarmicheal.wframe.database.querybuilder.QueryResults;

public class TestController {
    @Get("/test")
    public void Index(HttpRequest r) throws IOException {
        r.SendMessageText("Hello", "from the other side");
    }

    @Post("/test")
    public void Test(HttpRequest r) throws IOException {
        r.SendMessageText("Hello World", "How are you doing.");
    }

    @Post("/users/add")
    public void AddUser(HttpRequest r) throws IOException {
        Map<String, String> postForm = r.GetPostForm();

        String username = postForm.get("name");
        String password = postForm.get("password");

        try {
            (new User(Main.DB)).setUsername(username).setPassword(password).save();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}