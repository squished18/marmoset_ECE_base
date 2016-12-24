/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Aug 31, 2004
 */
package edu.umd.cs.buildServer;

/**
 * Exception thrown by the Builder if a project fails to compile.
 * 
 * @author David Hovemeyer
 */
public class CompileFailureException extends Exception {
	private static final long serialVersionUID = 3905804180017788472L;

	private String compilerOutput;
	
	/**
	 * Constructor.
	 * @param msg the failure message
	 */
	public CompileFailureException(String msg, String compilerOutput) {
		super(msg);
		this.compilerOutput = compilerOutput;
	}

	/**
	 * Constructor.
	 * @param msg the failure message
	 * @param t the cause
	 */
	public CompileFailureException(String msg, Throwable t, String compilerOutput) {
		super(msg, t);
		this.compilerOutput = compilerOutput;
	}
	
	/**
	 * Get the compiler output.
	 * @return the compiler output
	 */
	public String getCompilerOutput() {
		return compilerOutput;
	}
}
