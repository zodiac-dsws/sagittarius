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
	
	public void debug( String className, String message ) {
		if ( !enabled ) { return; }
		System.out.println( "[DEBUG] " + className + " " + message);
	}
	
	public void error( String className, String message ) {
		if ( !enabled ) { return; }
		System.out.println( "[ERROR] " + className + " " + message);
	}
	
	public void warn( String className, String message ) {
		if ( !enabled ) { return; }
		System.out.println( "[WARN] " + className + " " + message);
	}
	

}
