/*
 * Created on Aug 16, 2004
 *
 */
package edu.umd.cs.marmoset.modelClasses;

import java.util.Date;

/**
 * @author jspacco
 *
 */
public class Debug {

	/**
	 * private empty default constructor prevents instantiation
	 */
	private Debug() {
	}

	// public static final boolean DEBUG = Boolean.getBoolean("edu.umd.cs.eclipse.submitserver.debug");
	public static final boolean DEBUG = true;
	
	/**
	 * Print a debugging message.
	 * Does nothing if DEBUG flag is false.
	 * 
	 * @param message the message
	 */
	public static void print(Object message) {
		if (DEBUG)
			printLogMessage("DEBUG: " + message.toString());
	}
	
	/**
	 * Print a warning message that will (probably) show up 
	 * in tomcat/logs/catalina.out
	 * 
	 * Prepends "SS WARNING:" so that I know SubmitServer is generating the message.
	 * 
	 * @param message
	 */
	public static void warn(String message)
	{
		printLogMessage("SS WARNING: " + message);
	}
	
	/**
	 * Print an error message that will (probably) show up 
	 * in tomcat/logs/catalina.out
	 * 
	 * Prepends "SS ERROR:" so that I know SubmitServer is generating the message.
	 * 
	 * @param message
	 */
	public static void error(String message)
	{
		printLogMessage("SS ERROR: " + message);
	}
	
	/**
	 * Print a debugging message for an exception.
	 */
	public static void print(String message, Throwable e) {
		if (DEBUG) {
			printLogMessage("DEBUG: " + message);
			if (e != null)
				e.printStackTrace(System.out);
		}
	}
	
	private static void printLogMessage(String msg) {
		Date date = new Date();
		System.out.println(date.toString() + ": " + msg);
	}
	
}
