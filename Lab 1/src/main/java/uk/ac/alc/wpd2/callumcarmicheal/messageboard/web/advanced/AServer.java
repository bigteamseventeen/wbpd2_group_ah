package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.advanced;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.library.Tuple;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.library.Tuple3;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.HttpRequest;
import uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Resource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

@SuppressWarnings("UnstableApiUsage")
public class AServer implements HttpHandler {
	private static final int __THREAD_COUNT = 4;
	
	private HttpServer Server;
	private boolean Started = false;
	
	private HashMap<String, ControllerMethodPair> Router;
	
	private enum RequestType { GET, POST }
	private class ControllerMethodPair {
		public Object GetInstance = null;
		public Method Get = null;
		public Object PostInstance = null;
		public Method Post = null;
	}
	
	public AServer(int Port) throws Exception {
		Router = new HashMap<>();
		
		SetupRouter();
		
		Server = HttpServer.create(new InetSocketAddress(Port), 0);
	}
	
	@SuppressWarnings("Duplicates")
	private void SetupRouter() {
		System.out.println("AdvancedServer: Indexing Controllers and Methods.");
		
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage("uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.advanced.controllers"))
				.setScanners(
						new SubTypesScanner(false),
						new TypeAnnotationsScanner(),
						new MethodAnnotationsScanner()
						));
		
		Set<Method> getMethods = reflections.getMethodsAnnotatedWith(Get.class);
		Set<Method> postMethods = reflections.getMethodsAnnotatedWith(Post.class);
		HashMap<String, Tuple<RequestType, Method>> paths = new HashMap<>();
		HashMap<Class, ArrayList<Tuple3<String, Method, RequestType>>> classes = new HashMap<>();
		HashMap<Class, Object> instances = new HashMap<>();

		boolean throwError = false;
		
		// Loop Router
		for (Method m : getMethods) {
			Class c = m.getDeclaringClass();
			Get g = m.getAnnotation(Get.class);
			String path = g.value();
			
			if (!Modifier.isPublic(m.getModifiers())) {
				System.err.println("AdvancedServer: ERROR Method needs to be public!");
				System.err.println("    Route: GET " + path);
				System.err.println("    at " + Package(m.toGenericString()));
				System.exit(1);
			}
			
			if (paths.containsKey(path)) {
				Tuple<RequestType,Method> rt = paths.get(path);
				
				if (rt.x == RequestType.GET) {
					System.err.println("AdvancedServer: WARNING Duplicate value's resolution");
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
			Post p = m.getAnnotation(Post.class);
			String path = p.value();
			
			if (!Modifier.isPublic(m.getModifiers())) {
				System.err.println("AdvancedServer: ERROR Method needs to be public!");
				System.err.println("    Route: POST " + path);
				System.err.println("    at " + m.toGenericString());
				System.exit(1);
			}
			
			if (paths.containsKey(path)) {
				Tuple<RequestType,Method> rt = paths.get(path);
				
				if (rt.x == RequestType.POST) {
					System.err.println("AdvancedServer: WARNING Duplicate value's resolution");
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
								System.err.println("AdvancedServer: WARNING Duplicate method resolution");
								System.err.println("    Methods are conflicting for value: " + t.x);
								System.err.println("    Method 1: " + Package(cmp.Get.toGenericString()));
								System.err.println("    Method 2: " + Package(t.y.toGenericString()));
								System.exit(1);
							} break;
						case POST:
							if (cmp.Post != null) {
								System.err.println("AdvancedServer: WARNING Duplicate method resolution");
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
	
	public void Start() {
		if (Started) return;
		Started = true;
		
		Server.createContext("/", this);
		Server.setExecutor(Executors.newFixedThreadPool(__THREAD_COUNT));
		Server.start();
	}
	
	@Override
	public void handle(HttpExchange e) {
		HttpRequest r = new HttpRequest(e);
		boolean isPost = e.getRequestMethod().equalsIgnoreCase("POST");
		boolean requestStartsWithSlash = false;
		String path = r.getRequestURI(false);
		String request = r.getRequestURI(true);
		
		System.out.println(
			String.format("AdvancedServer: %s %s", isPost ? "GET " : "POST", path));
		
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
		// Get the resource without the leading /
		String resource = request.startsWith("/") ? request.substring(1) : request;
		
		// Protect against traversal attacks
		if (Resource.IsUnsafePath(resource)) {
			display404(r);
			return;
		}
		
		// Attempt to load the file
		File f = Resource.GetPublicFile(resource);
		
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
}

