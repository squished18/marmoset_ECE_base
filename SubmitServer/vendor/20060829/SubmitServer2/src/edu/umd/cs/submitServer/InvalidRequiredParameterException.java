/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Sep 4, 2004
 *
 */
package edu.umd.cs.submitServer;

import javax.servlet.http.HttpServletResponse;

/**
 * @author jspacco
 *
 */
public class InvalidRequiredParameterException extends ClientRequestException {
	public InvalidRequiredParameterException(String msg) {
		super(HttpServletResponse.SC_BAD_REQUEST, msg);
	}
}