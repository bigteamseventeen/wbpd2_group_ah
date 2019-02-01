package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mitchellbosecke.pebble.error.LoaderException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

@SuppressWarnings("UnstableApiUsage")
public abstract class Controller implements HttpHandler {

    private ByteArrayDataOutput buffer;
    protected HttpExchange exchange;

    protected void HandleRequest(HttpExchange e) {
        buffer = ByteStreams.newDataOutput();
        this.exchange = e;
    }

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
                    Send("<br><p>Exception message: " + ex.getMessage() + "</p>");
                } else {
                    Send("<h1>There was an error</h1><p>Im sorry there was a error loading resources.</p>");
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
                ctx.put("message", ex.getMessage());

                String response = Template.Execute("_framework/exception", ctx);
                Send(response);
            } catch (Exception e) { System.out.println("Failed to send exception to client."); }
        }
    }
}