package uk.ac.alc.wpd2.callumcarmicheal.messageboard.web;

import java.io.File;

public class Resource {
	private static final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	
	public static File GetResource(String fileName) {
		return new File(classLoader.getResource(fileName).getFile());
	}
	
	public static File GetFile() {
		return null;
	}
	
	public static boolean IsUnsafePath(String path) {
		// To do, add some additional security checks
		return path.contains("../");
	}
	
	// Todo: File caching
}
