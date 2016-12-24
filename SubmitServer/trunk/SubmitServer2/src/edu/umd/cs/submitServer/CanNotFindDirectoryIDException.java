/*
 * Created on Jan 30, 2005
 *
 */
package edu.umd.cs.submitServer;

public class CanNotFindDirectoryIDException extends ClientRequestException {
	public CanNotFindDirectoryIDException(int code, String message)
	{
		super(code, message);
	}
}
