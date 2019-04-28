package com.callumcarmicheal.wframe;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.callumcarmicheal.wframe.library.Tuple;
import com.callumcarmicheal.wframe.library.Tuple3;
import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.Resource;
import com.callumcarmicheal.wframe.exception.RequestControllerConstructorInvalid;
import com.callumcarmicheal.wframe.exception.RequestPathConfliction;
import com.callumcarmicheal.wframe.web.SessionList;
import com.callumcarmicheal.wframe.props.GetRequest;
import com.callumcarmicheal.wframe.props.PostRequest;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.Executors;

@SuppressWarnings("rawtypes")
public class Server implements HttpHandler {
	final static Logger logger = LogManager.getLogger();

	protected static final int __THREAD_COUNT = 4;
	protected String controllersPackage = null;
	protected HttpServer Server;
	protected boolean Started = false;
	protected RequestReflection reflectionEngine = null;

	private SessionList sessionList;
	private boolean resourcesEnabled = false;
	private String resourcesDirectory = "";

	public Server setResourcesEnabled(boolean v) {
		resourcesEnabled = v;
		return this;
	}

	public boolean getResourcesEnabled() {
		return resourcesEnabled;
	}

	public Server setResourcesDirectory(String v) {
		resourcesDirectory = v;
		return this;
	}

	public String getResourcesDirectory() {
		return resourcesDirectory;
	}

	/**
	 * Create a new server
	 * 
	 * @param Port               The port that the server will bind to
	 * @param ControllersPackage The package structure that will be searched for the
	 *                           controller's and get, post methods.
	 */
	public Server(int Port, String ControllersPackage) throws RequestPathConfliction, IOException {
		// Set our controllers package
		this.controllersPackage = ControllersPackage;

		// Create our session cache
		this.sessionList = new SessionList();

		// Temporarly disable the logger
		reflectionEngine = new RequestReflection(this);
		scanForRequests();

		// Setup our http server
		Server = HttpServer.create(new InetSocketAddress(Port), 0);
	}

	/**
	 * Scan for all requests
	 * @throws RequestPathConfliction
	 */
	public void scanForRequests() throws RequestPathConfliction {
		reflectionEngine.scanForRequests();
	}

	/**
	 * Generate a compressed abbreviated package name from a long package.
	 * @param Package The package to abbreviate
	 * @return An abbreviated package
	 */
	String Package(String Package){
		return RequestReflection.Package(Package);
	}
	
	/**
	 * Start the server
	 * @return
	 */
	public Server start() {
		if (Started) return this;
		Started = true;
		
		Server.createContext("/", this);
		Server.setExecutor(Executors.newFixedThreadPool(__THREAD_COUNT));
		Server.start();

		return this;
	}

	/**
	 * Stop the server
	 */
	public void stop() {
		// Check if we have not started
		if (!Started) return;

		// Stop the server
		if (Server != null) 
			Server.stop(0);
	}
	
	@Override
	public void handle(HttpExchange e) {
		HttpRequest r = null;
		String path = null;
		String request = null;

		try {
			r = new HttpRequest(e, sessionList);
			path = r.getRequestURI(false);
			request = r.getRequestURI(true);
		} catch (Exception ex) {
			logger.error("WFrameworkServer: Failed to initialize request response", ex);
			throw ex;
		}
		

		// Log the request
		String requestType = e.getRequestMethod().toUpperCase();
		logger.info(
			String.format("WFrameworkServer: %s %-6s %s", e.getRemoteAddress().toString(), requestType, path));
		
		try {
			if (this.reflectionEngine.hasRequest(requestType, path)) {
				this.reflectionEngine.executeRequest(r, requestType, path);
				return;
			}
			
			// We did not have the request and pass the information to our resource loader
			handleFileResource(r, request);
		} catch (IOException ex) {
			int x = 0;

			// Attempt to send the message
			try { r.throwException(ex); }
			catch (Exception ignored) { }
		} catch (RequestControllerConstructorInvalid ex) {
			int x = 0;

			// Attempt to send the message
			try { 
				r.throwException("Failed to initiate server request. Please try again later.", 
					"A constructor has failed to be initialized on the controller for: " + requestType + " " + path, ex.baseException); 
			} catch (Exception ignored) { }
		}
	}
	
	/**
	 * Handle the resource request
	 * @param request 				Http request
	 * @param requestedResource		Requested file resource
	 * @throws IOException
	 */
	private void handleFileResource(HttpRequest request, String requestedResource) throws IOException {
		if (!resourcesEnabled){
			display404(request, requestedResource);
			return;
		}

		// Get the resource without the leading /
		String resource = requestedResource.startsWith("/") ? requestedResource.substring(1) : requestedResource;
		
		// Protect against traversal attacks
		if (Resource.IsUnsafePath(resource)) {
			display404(request, requestedResource);
			return;
		}
		
		// Attempt to load the file
		File f = Resource.GetFile(this.resourcesDirectory + "/" + resource);
		
		// The file does not exist
		if (f == null || !f.exists() || !f.canRead()) {
			// File is null: Debug
//			if (f == null) { /*This is used for debugging purposes only*/ }
			
			// File does not exist: Debug
//			if (!f.exists()) { /*This is used for debugging purposes only*/ }
			
			// Display a generic message
			display404(request, requestedResource);
			return;
		}
		
		// We can now output the file to the request
		request.SendFile(200, f);
	}
	
	/**
	 * Display the 404 error page
	 * @param r
	 */
	private void display404(HttpRequest r) {
		logger.info("WFrameworkServer: Could not find requested resource.");
		r.SendMessagePage("Resource not found",
			"The requested resource could not be found or the request was malformed",
			404);
	}

	/**
	 * Display the 404 error page
	 * @param r
	 * @param requestedResource
	 */
	private void display404(HttpRequest r, String requestedResource) {
		logger.info("WFrameworkServer: Could not find requested resource. Requested Resource: " + requestedResource);
		r.SendMessagePage("Resource not found",
			"The requested resource could not be found or the request was malformed",
			404);
	}
	
	
	/**
	 * Parse a query string ?x=1
	 * @param query	
	 * @return
	 */
	public static Map<String,String> ParseQuery(String query) {
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
	
	/**
	 * Parse a query string ?x=1 with a specific encoding
	 * @param query	
	 * @return
	 */
	public static Map<String,String> ParseQueryEncoding(String query) throws UnsupportedEncodingException {
		return ParseQueryEncoding(query, "UTF-8");
	}
	
	/**
	 * Parse a query string ?x=1 with a specific encoding
	 * @param query	
	 * @param encoding	The wanted encoding to be used, for example UTF-8
	 * @return
	 */
	public static Map<String,String> ParseQueryEncoding(String query, String encoding) throws UnsupportedEncodingException {
		Map<String, String> result = new HashMap<>();
		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				result.put(entry[0], URLDecoder.decode(entry[1], encoding));
			} else {
				result.put(entry[0], "");
			}
		}
		
		return result;
	}
	
	public static String GetQueryString(HttpExchange Exchange) {
		// Check if the query does not exist, if so then return an empty string instead of null
		String query;
		return (query = Exchange.getRequestURI().getQuery()) == null ? "" : query;
	}
	
	public static Map<String, String> ParseQuery(HttpExchange Exchange) {
		String query = GetQueryString(Exchange);
		return ParseQuery(query);
	}
	
	/**
	 * Is the JVM currently being debugged
	 * @return
	 */
	public static boolean IsDebugging() {
		return java.lang.management.ManagementFactory.getRuntimeMXBean().
			getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
	}
	
	/**
	 * Parse the body content request
	 * @param query
	 * @param parameters
	 * @throws UnsupportedEncodingException
	 */
	public static void ParseDataQuery(String query, Map<String, Object> parameters)
			throws UnsupportedEncodingException {
		
		if (query != null) {
			String pairs[] = query.split("[&]");
			for (String pair : pairs) {
				String param[] = pair.split("[=]");
				String key = null;
				String value = null;
				if (param.length > 0) 
					key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
				
				if (param.length > 1) 
					value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
				
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

