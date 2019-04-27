package com.callumcarmicheal.wframe.props;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GetRequest {
	String value();
	String requestType() default "GET";
}