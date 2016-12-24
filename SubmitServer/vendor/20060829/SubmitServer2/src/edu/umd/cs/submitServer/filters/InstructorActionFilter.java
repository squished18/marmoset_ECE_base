/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 11, 2005
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

/**
 * @author jspacco
 *
 */
public class InstructorActionFilter extends SubmitServerFilter {

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain)
    throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;

		if (!((Boolean)request.getAttribute("instructorActionCapability")).booleanValue())
		{
		    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You don't have InstructionAction permission for this action");
		    return;
		}
        chain.doFilter(req, resp);
    }

}
