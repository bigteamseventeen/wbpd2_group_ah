package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.advanced;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// create a custom Annotation
@Retention(RetentionPolicy.RUNTIME)
public @interface Get {
	String value();
}
	
	