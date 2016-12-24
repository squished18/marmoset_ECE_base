/*
 * Created on Aug 23, 2004
 *
 */
package edu.umd.cs.submitServer;

import javax.servlet.http.HttpServletResponse;

/**
 * @author jspacco
 *
 */
public class ClientRequestException extends Exception {

	protected int errorCode;
	
	public int getErrorCode() { return errorCode; }
	
	public ClientRequestException(int code, String message)
	{
		super(message);
		errorCode = code;
	}
	
	/**
	 * 
	 */
	public ClientRequestException() {
		super();
	}

	/**
	 * @param message
	 */
	public ClientRequestException(String message) {
		this(HttpServletResponse.SC_BAD_REQUEST, message);
	}

	/**
	 * @param cause
	 */
	public ClientRequestException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ClientRequestException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
