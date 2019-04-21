package com.callumcarmicheal.wframe.exception;

import java.lang.reflect.Method;

public class RequestPathConfliction extends Exception {
    private static final long serialVersionUID = 13235123455L;
    
    public String requestType;
    public String requestPath;

    public Method conflictingMethod1;
    public Method conflictingMethod2;

    /**
     * Create a Request Path Confliction
     * @param requestType The request type
     * @param requestPath The request path
     * @param conflictingMethod1 The first method that is conflicting
     * @param conflictingMethod2 The second method that is conflicting
     */
    public RequestPathConfliction(String requestType, String requestPath, Method conflictingMethod1, Method conflictingMethod2) {
        super("Conflicting web request for " + requestType + " " + requestPath + ".");

        this.requestPath = requestPath;
        this.requestType = requestType;
        this.conflictingMethod1 = conflictingMethod1;
        this.conflictingMethod2 = conflictingMethod2;
    }
}