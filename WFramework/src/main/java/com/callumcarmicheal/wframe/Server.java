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
import com.callumcarmicheal.wframe.web.SessionList;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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


	private static final int __THREAD_COUNT = 4;
	private String controllersPackage = null;
	private HttpServer Server;
	private boolean Started = false;

	private HashMap<String, ControllerMethodPair> Router;
	private SessionList sessionList;

	private boolean resourcesEnabled = false;
	private String resourcesDirectory = "";

	// TODO: Support full rest functionality - POST, PUT, PATCH, GET, DELETE.
	private enum RequestType { GET, POST }
	private class ControllerMethodPair {
		public Object GetInstance = null;
		public Method Get = null;
		public Object PostInstance = null;
		public Method Post = null;
	}

	public Server  setResourcesEnabled(boolean v) { resourcesEnabled = v; return this; }
	public boolean getResourcesEnabled() { return resourcesEnabled; }
	public Server  setResourcesDirectory(String v) { resourcesDirectory = v; return this; }
	public String  getResourcesDirectory() { return resourcesDirectory; }

	/**
	 * Create a new server
	 * 
	 * @param Port The port that the server will bind to
	 * @param ControllersPackage The package structure that will be searched for the controller's and get, post methods.
	 */
	public Server(int Port, String ControllersPackage) throws Exception {
		// Set our controllers package
		this.controllersPackage = ControllersPackage;

		// Create our session cache
		this.sessionList = new SessionList();
			
		// Temporarly disable the logger
		ReloadRouter();
		
		// Setup our http server
		Server = HttpServer.create(new InetSocketAddress(Port), 0);
	}
	
	/**
	 * Rescans for any changed method's and reindexes the available web requests
	 */
	public void ReloadRouter() {
		// Reset the router hashmap
		Router = new HashMap<>();

		logger.info("WFrameworkServer: Indexing Controllers and Methods.");

		Reflections reflections = new Reflections(new ConfigurationBuilder()
			.setUrls(ClasspathHelper.forPackage(controllersPackage))
			.setScanners(
				new SubTypesScanner(false),
				new TypeAnnotationsScanner(),
				new MethodAnnotationsScanner()
			));
		
		Set<Method> getMethods = reflections.getMethodsAnnotatedWith(GetRequest.class);
		Set<Method> postMethods = reflections.getMethodsAnnotatedWith(PostRequest.class);
		HashMap<String, Tuple<RequestType, Method>> paths
				= new HashMap<>();
		HashMap<Class, ArrayList<Tuple3<String, Method, RequestType>>> classes = new HashMap<>();
		HashMap<Class, Object> instances = new HashMap<>();

		// Loop Router
		for (Method m : getMethods) {
			Class c = m.getDeclaringClass();
			GetRequest g = m.getAnnotation(GetRequest.class);
			String path = g.value();
			
			if (!Modifier.isPublic(m.getModifiers())) {
				logger.error("WFrameworkServer: ERROR Method needs to be public!");
				logger.error("    Route: GET " + path);
				logger.error("    at " + Package(m.toGenericString()));
				System.exit(1);
			}
			
			if (paths.containsKey(path)) {
				Tuple<RequestType,Method> rt = paths.get(path);
				
				if (rt.x == RequestType.GET) {
					
					logger.error("WFrameworkServer: WARNING Duplicate value's resolution");
					logger.error("    Request Type: GET");
					logger.error("    Methods are conflicting for value: " + path);
					logger.error("    Method 1: " + Package(m.toGenericString()));
					logger.error("    Method 2: " + Package(rt.y.toGenericString()));
					System.exit(1);
				}
			} else {
				paths.put(path, new Tuple<>(RequestType.GET, m));
			}
			
			if (!classes.containsKey(c))
				classes.put(c, new ArrayList< Tuple3<String, Method, RequestType> >());
			
			classes.get(c).add(new Tuple3<>(path, m, RequestType.GET));
		}
		
		for (Method m : postMethods) {
			Class c = m.getDeclaringClass();
			PostRequest p = m.getAnnotation(PostRequest.class);
			String path = p.value();
			
			if (!Modifier.isPublic(m.getModifiers())) {
				logger.error("WFrameworkServer: ERROR Method needs to be public!");
				logger.error("    Route: POST " + path);
				logger.error("    at " + m.toGenericString());
				System.exit(1);
			}
			
			if (paths.containsKey(path)) {
				Tuple<RequestType,Method> rt = paths.get(path);
				
				if (rt.x == RequestType.POST) {
					logger.error("WFrameworkServer: WARNING Duplicate value's resolution");
					logger.error("    Request Type: POST");
					logger.error("    Methods are conflicting for value: " + path);
					logger.error("    Method 1: " + Package(m.toGenericString()));
					logger.error("    Method 2: " + Package(rt.y.toGenericString()));
					System.exit(1);
				}
			} else {
				paths.put(path, new Tuple<>(RequestType.POST, m));
			}
			
			if (!classes.containsKey(c)) {
				classes.put(c, new ArrayList< Tuple3<String, Method, RequestType> >());
			}
			
			classes.get(c).add(new Tuple3<>(path, m, RequestType.POST));
		}
	
		// We are now generate value controller list.
		for (Class k : classes.keySet()) {
			ArrayList<Tuple3<String,Method,RequestType>> v = classes.get(k);
			
			Object inst = null;
			
			if (!instances.containsKey(k)) {
				try {
					Constructor<?> ctor = k.getDeclaredConstructor();
					inst = (Object) ctor.newInstance(new Object[] {});
					instances.put(k, inst);
				} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
					logger.error("Failed to find constructor or create instance for controller class.");
					logger.error("    " + k.getCanonicalName());
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			if (inst == null)
				inst = instances.get(k);
			
			for (Tuple3<String,Method,RequestType> t : v) {
				ControllerMethodPair cmp;
				boolean existingInst = Router.containsKey(t.x);
				
				if (existingInst) {
					cmp = Router.get(t.x);
					switch (t.z) {
						case GET:
							if (cmp.Get != null) {
								logger.error("WFrameworkServer: WARNING Duplicate method resolution");
								logger.error("    Methods are conflicting for value: " + t.x);
								logger.error("    Method 1: " + Package(cmp.Get.toGenericString()));
								logger.error("    Method 2: " + Package(t.y.toGenericString()));
								System.exit(1);
							} break;
						case POST:
							if (cmp.Post != null) {
								logger.error("WFrameworkServer: WARNING Duplicate method resolution");
								logger.error("    Methods are conflicting for value: " + t.x);
								logger.error("    Method 1: " + Package(cmp.Post.toGenericString()));
								logger.error("    Method 2: " + Package(t.y.toGenericString()));
								System.exit(1);
							} break;
					}
				} else {
					cmp = new ControllerMethodPair();
				}
				
				switch (t.z) {
					case GET:
						cmp.GetInstance = inst;
						cmp.Get = t.y;
						
						logger.info("Registered route, GET  (" + t.x + ") @ " + Package(t.y.getDeclaringClass().getTypeName()) + "." + t.y.getName());
						break;
					case POST:
						cmp.PostInstance = inst;
						cmp.Post = t.y;
						
						logger.info("Registered route, POST (" + t.x + ") @ " + Package(t.y.getDeclaringClass().getTypeName()) + "." + t.y.getName());
						break;
				}
				
				if (!existingInst)
					Router.put(t.x, cmp);
			}
		}
	}
	
	/**
	 * Generate a compressed abbreviated package name from a long package.
	 * @param Package The package to abbreviate
	 * @return An abbreviated package
	 */
	String Package(String Package){
		return Package.replaceAll("\\B\\w+(\\.[a-z])","$1");
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
	 * @return
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
		boolean isPost = false;
		boolean requestStartsWithSlash = false;
		String path = null;
		String request = null;

		try {
			r = new HttpRequest(e, sessionList);
			
			isPost = e.getRequestMethod().equalsIgnoreCase("POST");
			path = r.getRequestURI(false);
			request = r.getRequestURI(true);
		} catch (Exception ex) {
			logger.error("WFrameworkServer: Failed to initialize request response", ex);
			throw ex;
		}
		

		// Log the request
		logger.info(
			String.format("WFrameworkServer: %s %-6s %s", e.getRemoteAddress().toString(), e.getRequestMethod(), path));
		
		try {
			// Check if we have the request in our router
			if (Router.containsKey(request) ||
					(requestStartsWithSlash = (request.startsWith("/") && Router.containsKey(request.substring(1))))) {
				// Get the controller pair
				ControllerMethodPair cmp;
				
				if (requestStartsWithSlash)
					 cmp = Router.get(request.substring(1));
				else cmp = Router.get(request);
				
				// Check if we are invoking the Get or Post request
				if (isPost) {
					if (cmp.Post == null || cmp.PostInstance == null) {
						// The post request does not exist or cannot be processed
						display404(r);
					} else {
						cmp.Post.invoke(cmp.PostInstance, r);
					}
				} else {
					if (cmp.Get == null || cmp.GetInstance == null) {
						// The post request does not exist or cannot be processed
						display404(r);
					} else {
						cmp.Get.invoke(cmp.GetInstance, r);
					}
				}
				
				return;
			}
			
			// We did not have the request and pass the information to our resource loader
			handleFileResource(r, request);
		} catch (Exception ex) {
			// Attempt to send the message
			try { r.throwException(ex); }
			catch (Exception ignored) { }
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
			display404(request);
			return;
		}

		// Get the resource without the leading /
		String resource = requestedResource.startsWith("/") ? requestedResource.substring(1) : requestedResource;
		
		// Protect against traversal attacks
		if (Resource.IsUnsafePath(resource)) {
			display404(request);
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
			display404(request);
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

