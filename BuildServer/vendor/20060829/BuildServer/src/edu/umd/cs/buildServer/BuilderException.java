/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Aug 31, 2004
 */
package edu.umd.cs.buildServer;


/**
 * Exception thrown by a Builder if something other than a
 * compiler error goes wrong trying to build a project.
 * Compiler errors are thrown as CompileFailureExceptions.
 * 
 * <p> Basically, a BuilderException is an internal error in
 * the build server.
 * 
 * @author David Hovemeyer
 */
public class BuilderException extends Exception {
	private static final long serialVersionUID = 3691037664686323504L;

	/**
	 * Constructor
	 * @param msg message explaining the reason for failure
	 */
	public BuilderException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor
	 * @param msg message explaining the reason for failure
	 * @param reason a Throwable conveying the reason for failure
	 */
	public BuilderException(String msg, Throwable reason) {
		super(msg, reason);
	}
	
	/**
	 * Constructor
	 * @param e An exception explaining the reason for failure.
	 */
	public BuilderException(Exception e) {
	    super(e);
	}
}
