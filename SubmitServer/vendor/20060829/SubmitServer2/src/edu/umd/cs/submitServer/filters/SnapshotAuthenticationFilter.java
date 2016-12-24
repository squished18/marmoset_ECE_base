/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 14, 2005
 *
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 *
 */
public class SnapshotAuthenticationFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		
		try {
		    String serverPassword = servletContext.getInitParameter(SNAPSHOT_PASSWORD);
		    MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
		    String clientPassword = multipartRequest.getStringParameter("password");
		    
		    if (!serverPassword.equals(clientPassword))
		    {
		        throw new ServletException("Invalid Authentication for the SnapshotAuthenticationFilter");
		    }
		} catch (InvalidRequiredParameterException e) {
		    throw new ServletException(e);
		}
        chain.doFilter(request, response);
    }

}
