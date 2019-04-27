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
import com.callumcarmicheal.wframe.props.*;

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

public class RequestReflection {
    final static Logger logger = LogManager.getLogger();

    private Server server;
    private Reflections reflections;

    //
    HashMap<Method, Object> classInstances;
    HashMap<String, HashMap<String, Method>> routeList;

    /**
     * Setup a reflection engine
     * @param server
     */
    public RequestReflection(Server server) {
        this.server = server;
    }

    /**
     * Generate a compressed abbreviated package name from a long package.
     * 
     * @param Package The package to abbreviate
     * @return An abbreviated package
     */
    protected static String Package(String Package) {
        return Package.replaceAll("\\B\\w+(\\.[a-z])", "$1");
    }

    /**
     * Begin searching for annotation's
     * 
     * @throws RequestPathConfliction
     */
    public void scanForRequests() throws RequestPathConfliction {
        // Recreate the lists
        classInstances = new HashMap<>();
        routeList = new HashMap<>();
        
        // Setup the reflection engine
        logger.info("WFrameworkServer: Indexing Controllers and Methods.");
        reflections = new Reflections(
                new ConfigurationBuilder()
                    .setUrls(ClasspathHelper
                    .forPackage(server.controllersPackage))
                    .setScanners(
                        new SubTypesScanner(false), 
                        new TypeAnnotationsScanner(), 
                        new MethodAnnotationsScanner()));
        
        // Parse the basic request annotations
        this.parseRequestAnnotations(new Class[] {
            DeleteRequest.class,    GetRequest.class,       HeadRequest.class, 
            OptionsRequest.class,   PatchRequest.class,     PostRequest.class, 
            PutRequest.class,       WebRequest.class
        });
    }

     /**
     * Parse a basic annotation to get the path
     * 
     * @param annotationClass
     * @throws RequestPathConfliction
     */
    private void parseRequestAnnotations(Class<? extends Annotation> annotationClasses[])
            throws RequestPathConfliction {
        // Loop the annotation classes
        for(Class<? extends Annotation> ann : annotationClasses)
            this.parseRequestAnnotation(ann);
    }

    /**
     * Parse a basic annotation to get the path
     * 
     * @param annotationClass
     * @throws RequestPathConfliction
     */
    private void parseRequestAnnotation(Class<? extends Annotation> annotationClass)
            throws RequestPathConfliction {
        Set<Method> methodsList = reflections.getMethodsAnnotatedWith(annotationClass);
        for (Method method : methodsList) {
            // Get the annotation
            Annotation annotation = method.getAnnotation(annotationClass);
            String requestPath = null;
            String requestType = null;

            // Try to get the value method and get the path value
            try {
                Method valueMethod = annotation.getClass().getMethod("value");

                // Try to call the value() method to get the path
                try {
                    requestPath = cleanRequestPath((String) valueMethod.invoke(annotation));
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    logger.error("Skipping web path method because of attribute exception. (Path invoke)\nMethod: "
                            + Package(method.toGenericString()), e);
                    continue;
                }
            } catch (NoSuchMethodException | SecurityException e) {
                logger.error("Skipping web path method because of attribute exception. (Value method)\nMethod: "
                        + Package(method.toGenericString()), e);
                continue;
            }

            // Try to get the value method and get the request type
            try {
                Method valueMethod = annotation.getClass().getMethod("requestType");

                // Try to call the value() method to get the request type
                try {
                    requestType = ((String) valueMethod.invoke(annotation)).toUpperCase();
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    logger.error(
                            "Skipping web path method because of attribute exception. (Request Type invoke)\nMethod: "
                                + Package(method.toGenericString()),
                            e);
                    continue;
                }
            } catch (NoSuchMethodException | SecurityException e) {
                logger.error("Skipping web path method because of attribute exception. (Request Type method)\nMethod: "
                        + Package(method.toGenericString()), e);
                continue;
            }

            // We now have the path
            logger.info("Discovered route " + requestType + " /" + requestPath + " @ " + Package(method.toGenericString()));
            HashMap<String, Method> requestMethods;

            // Set or get the request Methods routing
            if (!routeList.containsKey(requestPath)) {
                routeList.put(requestPath, requestMethods = new HashMap<>());
            }
            

            // Check if the request already exists for the request
            requestMethods = routeList.get(requestPath);

            if (requestMethods.containsKey(requestType))
                throw new RequestPathConfliction(requestType, requestPath,
                        routeList.get(requestPath).get(requestType), method);
            

            // Add the method to the request methods.
            requestMethods.put(requestType, method);
        }
    }

    /**
     * Remove leading slashes from the path
     * @param request
     * @return
     */
    private String cleanRequestPath(String request) {
        int i = 0;
        request = request.substring(0, (i = request.indexOf("?")) == -1 ? request.length() : i);
        return request.startsWith("/") ? request.replaceAll("^/+", "") : request;
    }

    /**
     * Check if we have the request
     * 
     * @param requestType The request type
     * @param requestPath The requested web path
     * @return
     */
    public boolean hasRequest(String requestType, String requestPath) {
        requestType = requestType.toUpperCase();
        requestPath = cleanRequestPath(requestPath);

        // Check if we dont have the path
        if (!routeList.containsKey(requestPath))
            return false;

        // Check if we have the request type
        HashMap<String, Method> rm = null;
        if (((rm = routeList.get(requestPath)) != null) && rm.containsKey(requestType))
            return true;

        // We have the request!
        return false;
    }

    /**
     * Execute a request
     * 
     * @param request       The web request
     * @param requestType   The request type
     * @param requestPath   The request path
     * @return              If the request was successful
     * @throws RequestControllerConstructorInvalid
     */
    public boolean executeRequest(HttpRequest request, String requestType, String requestPath)
            throws RequestControllerConstructorInvalid {
        //
        requestType = requestType.toUpperCase();
        requestPath = cleanRequestPath(requestPath);

        // Check if we dont have the request
        if (!this.hasRequest(requestType, requestPath))
            return false;

        // Get the method
        Method webRequestMethod = routeList.get(requestPath).get(requestType);
        Object instance = null;

        // Create an instance
        if ((instance = classInstances.get(webRequestMethod)) == null) {
            try {
                Constructor<?> ctor = webRequestMethod.getDeclaringClass().getDeclaredConstructor();
                instance = (Object) ctor.newInstance(new Object[] {});
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                    | InvocationTargetException e) {
                throw new RequestControllerConstructorInvalid(webRequestMethod.getClass(), e);
            }

            classInstances.put(webRequestMethod, instance);
        }

        // Try to invoke the method
        try { 
            webRequestMethod.invoke(instance, request); 
        } 
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("Failed to invoke web request for " + requestType + " /" + requestPath + " @ " + Package(webRequestMethod.toGenericString()), e);

            if (request != null) {
                request.throwException("A exception was thrown in the server.", 
                    "Failed to invoke web request for " + requestType + " /" + requestPath + 
                        " @ " + Package(webRequestMethod.toGenericString()), e);
            }

            return false;
        }

        // We have successfully invoked the method
        return true;
    }
}