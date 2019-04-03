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
import org.slf4j.Logger;

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
		Router = new HashMap<>();
		this.controllersPackage = ControllersPackage;
		this.sessionList = new SessionList();
			
		Logger _TEMP = Reflections.log;
		Reflections.log = null; 
		SetupRouter();
		Reflections.log = _TEMP;
		
		Server = HttpServer.create(new InetSocketAddress(Port), 0);
	}
	
	private void SetupRouter() {
		System.out.println("WFrameworkServer: Indexing Controllers and Methods.");

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
				System.err.println("WFrameworkServer: ERROR Method needs to be public!");
				System.err.println("    Route: GET " + path);
				System.err.println("    at " + Package(m.toGenericString()));
				System.exit(1);
			}
			
			if (paths.containsKey(path)) {
				Tuple<RequestType,Method> rt = paths.get(path);
				
				if (rt.x == RequestType.GET) {
					System.err.println("WFrameworkServer: WARNING Duplicate value's resolution");
					System.err.println("    Request Type: GET");
					System.err.println("    Methods are conflicting for value: " + path);
					System.err.println("    Method 1: " + Package(m.toGenericString()));
					System.err.println("    Method 2: " + Package(rt.y.toGenericString()));
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
				System.err.println("WFrameworkServer: ERROR Method needs to be public!");
				System.err.println("    Route: POST " + path);
				System.err.println("    at " + m.toGenericString());
				System.exit(1);
			}
			
			if (paths.containsKey(path)) {
				Tuple<RequestType,Method> rt = paths.get(path);
				
				if (rt.x == RequestType.POST) {
					System.err.println("WFrameworkServer: WARNING Duplicate value's resolution");
					System.err.println("    Request Type: POST");
					System.err.println("    Methods are conflicting for value: " + path);
					System.err.println("    Method 1: " + Package(m.toGenericString()));
					System.err.println("    Method 2: " + Package(rt.y.toGenericString()));
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
					System.err.println("Failed to find constructor or create instance for controller class.");
					System.err.println("    " + k.getCanonicalName());
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
								System.err.println("WFrameworkServer: WARNING Duplicate method resolution");
								System.err.println("    Methods are conflicting for value: " + t.x);
								System.err.println("    Method 1: " + Package(cmp.Get.toGenericString()));
								System.err.println("    Method 2: " + Package(t.y.toGenericString()));
								System.exit(1);
							} break;
						case POST:
							if (cmp.Post != null) {
								System.err.println("WFrameworkServer: WARNING Duplicate method resolution");
								System.err.println("    Methods are conflicting for value: " + t.x);
								System.err.println("    Method 1: " + Package(cmp.Post.toGenericString()));
						 		System.err.println("    Method 2: " + Package(t.y.toGenericString()));
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
						
						System.out.print("Registered route, GET  (" + t.x + ")");
						System.out.println(" @ " + Package(t.y.getDeclaringClass().getTypeName()) + "." + t.y.getName());
						break;
					case POST:
						cmp.PostInstance = inst;
						cmp.Post = t.y;
						
						System.out.print("Registered route, POST (" + t.x + ")");
						System.out.println(" @ " + Package(t.y.getDeclaringClass().getTypeName()) + "." + t.y.getName());
						break;
				}
				
				if (!existingInst)
					Router.put(t.x, cmp);
			}
		}
	}
	
	String Package(String s){
		return s.replaceAll("\\B\\w+(\\.[a-z])","$1");
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
	
	@Override
	public void handle(HttpExchange e) {
		
		HttpRequest r = new HttpRequest(e, sessionList);
		boolean isPost = e.getRequestMethod().equalsIgnoreCase("POST");
		boolean requestStartsWithSlash = false;
		String path = r.getRequestURI(false);
		String request = r.getRequestURI(true);

		// FIXME: Display remote ip address and store it in the HttpRequest
		System.out.println(
			String.format("WFrameworkServer: %s %s %s", e.getRemoteAddress().toString(), isPost ? "POST " : "GET", path));
		
		try {
			// Check if we have the request in our router
			if (Router.containsKey(request) ||
					(requestStartsWithSlash = (request.startsWith("/") && Router.containsKey(request.substring(1))))) {
				
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
			try { r.ThrowException(ex); }
			catch (Exception ignored) { }
		}
	}
	
	private void handleFileResource(HttpRequest r, String request) throws IOException {
		if (!resourcesEnabled){
			display404(r);
			return;
		}

		// Get the resource without the leading /
		String resource = request.startsWith("/") ? request.substring(1) : request;
		
		// Protect against traversal attacks
		if (Resource.IsUnsafePath(resource)) {
			display404(r);
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
			display404(r);
			return;
		}
		
		// We can now output the file to the request
		r.SendFile(200, f);
	}
	
	private void display404(HttpRequest r) {
		r.SendMessagePage("Resource not found",
			"The requested resource could not be found or the request was malformed",
			404);
	}
	
	public static Map<String,String> ParseQuery(String query) {
		return ParseQuery(query,true);
	}
	
	public static Map<String,String> ParseQueryEncoding(String query) throws UnsupportedEncodingException {
		return ParseQueryEncoding(query, "UTF-8");
	}
	
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
	
	public static Map<String, String> ParseQuery(String query, boolean decode) {
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
	
	public static String GetQueryString(HttpExchange Exchange) {
		return Exchange.getRequestURI().getQuery();
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

