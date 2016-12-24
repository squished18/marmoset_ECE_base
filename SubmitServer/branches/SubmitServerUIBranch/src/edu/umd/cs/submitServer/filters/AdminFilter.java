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
import javax.servlet.http.HttpSession;

import edu.umd.cs.submitServer.UserSession;

/**
 * @author jspacco
 *
 */
public class AdminFilter extends SubmitServerFilter
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
        HttpSession session = request.getSession();
        
        UserSession userSession = (UserSession)session.getAttribute(USER_SESSION);

        if (!userSession.isSuperUser())
        {
            // System.out.println("not super user, sending error");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You need admin privileges to view " +request.getRequestURI());
            return;
        }
        // System.out.println("superuser OK according to adminfilter");
        chain.doFilter(req, resp);
    }
}
