package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.advanced;

import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.QualifierArgument;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// create a custom Annotation
@Retention(RetentionPolicy.RUNTIME)
public @interface Post {
	String value();
}
