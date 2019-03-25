package com.callumcarmicheal.wframe;

import java.io.File;

public class Resource {
	private static final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	private static final String workingDirectory = System.getProperty("user.dir");
	
	public static File GetResource(String fileName) {
		return new File(classLoader.getResource(fileName).getFile());
	}
	
	public static File GetFile(String absolutePath) {
		// Open a file and if it does not exist return null
		File f = new File(absolutePath);
		if (!f.exists())
			return null;
		
		// Return the file
		return f;
	}
	
	public static File GetPublicFile(String path) {
		// Refuse to read any unsafe paths
		if (IsUnsafePath(path))
			return null;
		
		// Return the file
		return GetFile(workingDirectory + "/public/" + path);
	}
	
	public static boolean IsUnsafePath(String path) {
		// To do, add some additional security checks
		return path.contains("../");
	}
	
	// Todo: File caching
}
