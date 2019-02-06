package uk.ac.alc.wpd2.callumcarmicheal.messageboard.console;

/**
 * Collection of useful functions
 */
public class Console {
	/**
	 * Read a string from the console
	 * @return input
	 */
	public static String String() {
		return System.console().readLine();
	}
	
	/**
	 * Read a string from the console
	 * @param text Prompt text
	 * @return input
	 */
	public static String String(String text) {
		System.out.print(text);
		return System.console().readLine();
	}
	
	/**
	 * Read a integer from the console
	 * @param text Prompt text
	 * @return
	 */
	public static int Integer(String text) {
		while(true) {
			System.out.print(text);
			
			int value = -1;
			try {
				value = Integer();
			} catch (NumberFormatException ex) {continue;}
		
			return value;
		}
	}
	
	/**
	 * Read a integer from the console
	 * @return
	 */
	public static int Integer() {
		return ParseInteger();
	}
	
	/**
	 * Parse a integer from the console
	 * @return The parsed integer
	 * @throws NumberFormatException
	 */
	public static int ParseInteger() throws NumberFormatException {
		String input = System.console().readLine().trim();
		return Integer.parseInt(input);
	}
	
	/**
	 * Wait for user input
	 */
	public static void Wait() {
		System.console().readLine();
	}
	
	/**
	 * Prompt and Wait for user input (any key)
	 * @param x Prompt Text
	 */
	public static void Wait(String x) {
		System.out.println(x);
		Wait();
	}
	
	/**
	 * Prompt a wait message and then wait for user input
	 */
	public static void WaitMessage() {
		Wait("\n  [  Press Enter to Continue  ]");
	}
	
	/** Cached operating system name */
	private static String OS = null;
	
	/**
	 * Get the operating system's name
	 * @return
	 */
	private static String getOsName() {
		if(OS == null) { OS = System.getProperty("os.name"); }
		return OS;
	}
	
	/**
	 * Attempt to clear the console
	 */
	public static void Clear() {
		// If we start with windows, we want to create a child process with the
		// command cls, we do this because java interactive processes has a chance
		// of not finding the CLS command as it its not cls.exe this is just cleaner.
		if (getOsName().startsWith("Windows")) {
			try {
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			} catch(Exception ex) {}
		}
		
		// Clear the line for Macs and Linux
		else {
			System.out.print("\033[H\033[2J");
			System.out.flush();
		}
	}
}
