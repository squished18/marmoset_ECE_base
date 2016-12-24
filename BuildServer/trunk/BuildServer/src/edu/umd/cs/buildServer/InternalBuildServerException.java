package edu.umd.cs.buildServer;

public class InternalBuildServerException extends Exception {
	private static final long serialVersionUID = 3691037664686323504L;
	
	/**
	 * Constructor
	 * @param msg message explaining the reason for failure
	 */
	public InternalBuildServerException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor
	 * @param msg message explaining the reason for failure
	 * @param reason a Throwable conveying the reason for failure
	 */
	public InternalBuildServerException(String msg, Throwable reason) {
		super(msg, reason);
	}
	
	/**
	 * Constructor
	 * @param e An exception explaining the reason for failure.
	 */
	public InternalBuildServerException(Exception e) {
	    super(e);
	}
}
