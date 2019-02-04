package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.controllers.IndexController;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.controllers.TopicController;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class Server {
    private static final int __THREAD_COUNT = 4;

    private HttpServer Server;
    private boolean Started = false;

    public Server(int Port) throws Exception {
        Server = HttpServer.create(new InetSocketAddress(Port), 0);
    }

    public void Start() {
        if (Started) return;
        Started=true;

        Server.createContext("/", new IndexController());
        Server.createContext("/topic", new TopicController());
        
        Server.setExecutor(Executors.newFixedThreadPool(__THREAD_COUNT));
        Server.start();
    }

    public static Map<String, String> ParseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }else{
                result.put(entry[0], "");
            }
        }
        return result;
    }

    public static Map<String, String> ParseQuery(HttpExchange Exchange) {
        String query = Exchange.getRequestURI().getQuery();
        return ParseQuery(query);
    }

    public static boolean IsDebugging() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    }
    
    public static void ParseDataQuery(String query, Map<String, Object> parameters)
    throws UnsupportedEncodingException {
        
        if (query != null) {
            String pairs[] = query.split("[&]");
            for (String pair : pairs) {
                String param[] = pair.split("[=]");
                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                }
                
                if (param.length > 1) {
                    value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                }
                
                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);
                        
                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }
}
