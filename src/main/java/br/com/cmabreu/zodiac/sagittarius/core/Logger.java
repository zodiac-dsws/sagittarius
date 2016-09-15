package br.com.cmabreu.zodiac.sagittarius.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.cmabreu.zodiac.sagittarius.misc.PathFinder;

public class Logger {
	private static Logger instance;
	private boolean enabled;
	private boolean toFile = true;
	private PrintWriter out;
	
	public Logger() {
		try {
			String fileName = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format( new Date() ) + ".txt";
			String path = PathFinder.getInstance().getPath() + "/logs/";
			File fil = new File( path );
			fil.mkdirs();
			
			FileWriter fw = new FileWriter( path + fileName, true );
		    BufferedWriter bw = new BufferedWriter(fw);
		    out = new PrintWriter(bw);
		} catch ( Exception e ) {
			toFile = false;
		}
	}
	
	public void enable() {
		enabled = true;
	}
	
	public void canOutputToFile( boolean toFile ) {
		this.toFile = toFile;
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
	
	private synchronized void print( String s ) {
		System.out.println( s );
		
		if (s.toLowerCase().contains("repository") ) return;
		if (s.toLowerCase().contains("service") ) return;
		if (s.toLowerCase().contains("dao") ) return;
		
		if ( toFile ) {
			out.println( s );
			out.flush();
		}
		
	}
	
	public void debug( String className, String message ) {
		if ( !enabled ) { return; }
		String s = "[DEBUG] " + getClassName(className) + " " + message;
		print( s );
	}
	
	public void error( String className, String message ) {
		if ( !enabled ) { return; }
		String s = "[ERROR] " + getClassName(className) + " " + message;
		print( s );
	}
	
	public void warn( String className, String message ) {
		if ( !enabled ) { return; }
		String s = "[WARN] " + getClassName(className) + " " + message;
		print( s );
	}

	

}
