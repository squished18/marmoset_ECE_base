/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Sep 4, 2004
 */
package edu.umd.cs.buildServer;

/**
 * Exception throw to indicate that the build server is
 * missing a required configuration property.
 * @author David Hovemeyer
 */
public class MissingConfigurationPropertyException extends Exception {
	private static final long serialVersionUID = 3256726195092666675L;

	/**
	 * Constructor.
	 * @param msg the message describing the exception
	 */
	public MissingConfigurationPropertyException(String msg) {
		super(msg);
	}
}
