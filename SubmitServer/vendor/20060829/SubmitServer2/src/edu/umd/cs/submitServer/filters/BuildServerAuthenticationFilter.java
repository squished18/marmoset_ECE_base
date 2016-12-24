/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 15, 2005
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
 */
public class BuildServerAuthenticationFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain)
    throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
        
        MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
        if (multipartRequest == null)
        {
            throw new ServletException("MultipartRequest should not be null; did the MultipartRequestFilter get applied first?");
        }
        // get the password I was sent
        // we're allowing a null value through but then checking for null later
        String providedPassword = multipartRequest.getParameter("password");
        
        // get the actual password from the web.xml file
        String realPassword = servletContext.getInitParameter("buildserver.password");
        
        if (providedPassword == null || !providedPassword.equals(realPassword)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(req, resp);
    }
}
