package com.callumcarmicheal.wframe.props;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface WebRequest {
    public String path();
    public String requestType() default "GET";
}