/*
 * Created on Jan 30, 2005
 *
 */
package edu.umd.cs.submitServer;

public class BadPasswordException extends ClientRequestException {
	public BadPasswordException(int code, String message)
	{
		super(code, message);
	}
}
