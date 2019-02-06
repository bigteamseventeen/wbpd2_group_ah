package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mitchellbosecke.pebble.error.LoaderException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sun.misc.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.*;
import java.util.Set;

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
        //System.out.println("Controller.handle()");
        Request();
        
        // Serve for POST requests only
        if (e.getRequestMethod().equalsIgnoreCase("POST")) {
            try { Post(); return; } catch (Exception ex) { ThrowException(ex); }
        }
        
        try { Get(); return; } catch (Exception ex) { ThrowException(ex); }
    }
    
    /**
     * Called on every request
     */
    protected void Request() {}
    
    /**
     * Called on Get requests
     * @throws Exception
     */
    protected void Get() throws Exception {}
    
    /**
     * Called on Post requests
     * @throws Exception
     */
    protected void Post() throws Exception {}
    
    // -----------
    
    // Write data into the buffer
    protected void Write(String str)   { buffer.write(str.getBytes()); }
    protected void Write(byte[] bytes) { buffer.write(bytes); }
    protected void Write(int i)        { buffer.write(i); }
    
    // Redirect to a page
    protected void Redirect(String to) throws IOException { Redirect(to, "Redirecting to " + to); }
    protected void Redirect(String to, String why) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Location", to);
        Send(302, why);
    }

    
    /**
     * Send the http response code with buffer
     * @param Response
     * @throws IOException
     */
    protected void Send(int Response) throws IOException {
        byte[] buf = buffer.toByteArray();
        exchange.sendResponseHeaders(Response, buf.length);
        OutputStream os = exchange.getResponseBody();
        os.write(buf);
        os.close();
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
        ex.printStackTrace();

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
    
    protected Map<String,String> getQuery() {
        return Server.ParseQuery(this.exchange);
    }

    protected String getQueryString() {
        return Server.GetQueryString(this.exchange);
    }

    protected byte[] GetPost() throws IOException {
        Headers requestHeaders = exchange.getRequestHeaders();
        Set<Map.Entry<String, List<String>>> entries = requestHeaders.entrySet();

        int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));

        InputStream is = exchange.getRequestBody();
        byte[] data = new byte[contentLength];
        int length = is.read(data);

        return data;
    }

    protected Map<String,String> GetPostForm() throws IOException {
        return Server.ParseQueryEncoding(new String(GetPost()));
    }

    protected void Clear() {
        this.buffer = ByteStreams.newDataOutput();
    }
    
    protected void SendMessagePage(String Title, String Message) {
        SendMessagePage(Title, Message, 200);
    }
    
    protected void SendMessagePage(String Title, String Message, int HttpResponse){
        Clear();
        
        Context ctx = Template.CreateContext();
        ctx.put("MessageTitle", Title);
        ctx.put("MessageText", Message);
    
        try {
            Send(HttpResponse, Template.Execute("_framework/message", ctx));
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