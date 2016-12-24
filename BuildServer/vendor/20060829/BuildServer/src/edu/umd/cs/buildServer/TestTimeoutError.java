/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Sep 27, 2004
 */
package edu.umd.cs.buildServer;

import junit.framework.AssertionFailedError;

/**
 * A variation of AssertionFailedError that indicates that
 * a single junit test exceeded its timeout.
 * 
 * @author David Hovemeyer
 */
public class TestTimeoutError extends AssertionFailedError {
	private static final long serialVersionUID = 3258130245704822836L;

	/**
	 * Constructor.
	 * @param msg a message describing the timeout
	 */
	public TestTimeoutError(String msg) {
		super(msg);
	}
}
