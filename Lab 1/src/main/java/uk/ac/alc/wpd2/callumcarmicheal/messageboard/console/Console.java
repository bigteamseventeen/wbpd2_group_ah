package uk.ac.alc.wpd2.callumcarmicheal.messageboard.console;

public class Console {
	public static String String() {
		return System.console().readLine();
	}
	
	public static String String(String text) {
		System.out.print(text);
		return System.console().readLine();
	}
	
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
	
	public static int Integer() {
		return ParseInteger();
	}
	
	public static int ParseInteger() throws NumberFormatException {
		String input = System.console().readLine().trim();
		return Integer.parseInt(input);
	}
	
	public static void Wait() {
		System.console().readLine();
	}
	
	public static void Wait(String x) {
		System.out.println(x);
		Wait();
	}
	
	public static void WaitMessage() {
		Wait("\n  [  Press Enter to Continue  ]");
	}
	
	private static String OS = null;
	private static String getOsName() {
		if(OS == null) { OS = System.getProperty("os.name"); }
		return OS;
	}
	
	public static void Clear() {
		if (getOsName().startsWith("Windows")) {
			try {
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			} catch(Exception ex) {}
		}
		
		else {
			System.out.print("\033[H\033[2J");
			System.out.flush();
		}
	}
}
