package com.callumcarmicheal.wframe.exception;

import java.lang.reflect.Method;

public class RequestControllerConstructorInvalid extends Exception {
    private static final long serialVersionUID = 13235123457L;

    public Class controllerClass;
    public Exception baseException;

    /**
     * Create a Request Path Confliction
     * 
     * @param requestType        The request type
     * @param requestPath        The request path
     * @param conflictingMethod1 The first method that is conflicting
     * @param conflictingMethod2 The second method that is conflicting
     */
    public RequestControllerConstructorInvalid(Class controllerClass, Exception baseException) {
        super("Failed to initialize constructor for class. " + baseException.getMessage());

        this.controllerClass = controllerClass;
        this.baseException = baseException;
    }
}