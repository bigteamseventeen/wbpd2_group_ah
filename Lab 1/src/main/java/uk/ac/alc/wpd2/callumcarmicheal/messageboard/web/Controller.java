package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mitchellbosecke.pebble.error.LoaderException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sun.misc.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.*;

@SuppressWarnings("UnstableApiUsage")
public abstract class   Controller implements HttpHandler {

    private ByteArrayDataOutput buffer;
    protected HttpExchange exchange;

    protected void HandleRequest(HttpExchange e) {
        buffer = ByteStreams.newDataOutput();
        this.exchange = e;
    }
    
    // -----------
    
    @Override
    public void handle(HttpExchange e) {
        HandleRequest(e);
        System.out.println("Controller.handle()");
        Request();
        
        // Serve for POST requests only
        if (e.getRequestMethod().equalsIgnoreCase("POST")) {
            try { Post(); return; } catch (Exception ex) { ThrowException(ex); }
        }
        
        try { Get(); return; } catch (Exception ex) { ThrowException(ex); }
    }
    
    protected void Request() { System.out.println("Controller.Request"); }
    
    protected void Get() throws Exception { System.out.println("Controller.Get"); }
    
    protected void Post() throws Exception { System.out.println("Controller.Post"); }
    
    // -----------
    
    
    protected void Write(String str) {
        buffer.write(str.getBytes());
    }
    
    protected void Write(byte[] bytes) {
        buffer.write(bytes);
    }
    
    protected void Write(int i) {
        buffer.write(i);
    }

    protected void Send(int Response) throws IOException {
        byte[] buf = buffer.toByteArray();
        exchange.sendResponseHeaders(Response, buf.length);
        OutputStream os = exchange.getResponseBody();
        os.write(buf);
        os.close();

        System.out.println("Send response to client!");
    }

    protected void Send(String data) throws IOException {
        Write(data);
        Send(200);
    }

    protected void Send(int Response, String data) throws IOException {
        Write(data);
        Send(Response);
    }

    protected void Send() throws IOException {
        Send(200);
    }

    protected void ThrowException(Exception ex) {
        if (ex instanceof LoaderException) {
            // Clear the output buffer.
            buffer = ByteStreams.newDataOutput();

            // Attempt to tell the browser something went wrong.
            try {
                if (Server.IsDebugging()) {
                    Write("<h1>There was an error</h1><p>Im sorry there was a error loading resources.</p>");
                    Write("<br><p>Exception message : <b>" + ex.getMessage() + "</b></p>");
                    Write("<br><pre style=\"background:#ccc\">");
                    
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    Write(sw.toString());
                    
                    Write("</pre>");
                    Send(500);
                } else {
                    Send(500,
                            "<h1>There was an error</h1><p>Im sorry there was a error loading resources.</p>");
                }
            }
            catch (IOException e2) { /**/ }
        }

        else {
            System.out.println(ex.getMessage());

            try {
                Context ctx = Template.CreateContext();
                ctx.put("ex", ex);
                ctx.put("debug", Server.IsDebugging());

                if (Server.IsDebugging()) {
                    ctx.put("message", ex.getMessage());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    ctx.put("stack", sw.toString());
                }

                String response = Template.Execute("_framework/exception", ctx);
                Send(500, response);
            } catch (Exception e) { System.out.println("Failed to send exception to client."); }
        }
    }
    
    protected Map<String, String> getQuery() {
        return Server.ParseQuery(this.exchange);
    }
    
    protected void Clear() {
        this.buffer = ByteStreams.newDataOutput();
    }
    
    protected void SendMessagePage(String Title, String Message) {
        SendMessagePage(Title, Message, 200);
    }
    
    protected void SendMessagePage(String Title, String Message, int Error){
        Clear();
        
        Context ctx = Template.CreateContext();
        ctx.put("MessageTitle", Title);
        ctx.put("MessageText", Message);
    
        try {
            Send(Error, Template.Execute("_framework/message", ctx));
        } catch (Exception e) {
            ThrowException(e);
        }
    }
    
    protected boolean SendFileSafe(int code, File f) {
        try {
            SendFile(code, f);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    protected void SendFile(int code, File f) throws IOException {
        // Clear the output buffer as we dont want to use it
        Clear();
    
        byte [] fileBytes = Files.readAllBytes(f.toPath());
        
//
//        exchange.sendResponseHeaders(code, fileBytes.length);
//
//        OutputStream os = exchange.getResponseBody();
//        os.write(buf);
//        os.close();
    }
}