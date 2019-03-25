package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mitchellbosecke.pebble.error.LoaderException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sun.misc.IOUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.*;
import java.util.Set;


public abstract class Controller extends HttpRequest implements HttpHandler  {
    
    @Override
    public void handle(HttpExchange e) {
        Prepare(e);
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
}