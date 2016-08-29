package br.com.cmabreu.zodiac.sagittarius.core;

public class Logger {
	private static Logger instance;
	private boolean enabled;
	
	public void enable() {
		enabled = true;
	}
	
	public void disable() {
		enabled = false;
	}
	
	public static Logger getInstance() {
		if ( instance == null ) {
			instance = new Logger();
		}
		return instance;
	}
	
	private String getClassName( String className ) {
		String[] temp = className.split("\\.");
		int pos = temp.length -1 ;
		return temp[ pos ] ;
	}
	
	public void debug( String className, String message ) {
		if ( !enabled ) { return; }
		System.out.println( "[DEBUG] " + getClassName(className) + " " + message);
	}
	
	public void error( String className, String message ) {
		if ( !enabled ) { return; }
		System.out.println( "[ERROR] " + getClassName(className) + " " + message);
	}
	
	public void warn( String className, String message ) {
		if ( !enabled ) { return; }
		System.out.println( "[WARN] " + getClassName(className) + " " + message);
	}

	

}
