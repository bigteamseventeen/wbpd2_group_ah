package com.callumcarmicheal.wframe.props;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// create a custom Annotation
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionsRequest {
	String value();
	String requestType() default "OPTIONS";
}