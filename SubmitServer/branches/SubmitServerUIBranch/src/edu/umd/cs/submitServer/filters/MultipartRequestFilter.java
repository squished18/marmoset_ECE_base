/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 13, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.submitServer.MultipartRequest;


/**
 * @author jspacco
 *
 * XXX This only handles form uploads with <b>ONE</b> File.  If there are more
 * we will ignore them.
 */
public class MultipartRequestFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain)
    throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		
		int maxSize = Integer.parseInt(servletContext.getInitParameter("fileupload.maxsize"));
		MultipartRequest multipartRequest = MultipartRequest.parseRequest(request, maxSize);

		request.setAttribute(MULTIPART_REQUEST, multipartRequest);
		
		chain.doFilter(request, response);
    }
}
