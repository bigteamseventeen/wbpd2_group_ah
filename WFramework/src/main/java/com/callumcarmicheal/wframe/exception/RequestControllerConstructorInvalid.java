package com.callumcarmicheal.wframe.exception;

import java.lang.reflect.Method;

public class RequestControllerConstructorInvalid extends Exception {
    private static final long serialVersionUID = 13235123458L;

    public Class controllerClass;
    public Exception baseException;

    /**
     * Create a Invalid Constructor exception
     * 
     * @param controllerClass
     * @param baseException
     */
    public RequestControllerConstructorInvalid(Class controllerClass, Exception baseException) {
        super("Failed to initialize constructor for class. " + baseException.getMessage());

        this.controllerClass = controllerClass;
        this.baseException = baseException;
    }
}