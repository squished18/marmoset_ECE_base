/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 24, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.umd.cs.submitServer.UserSession;

/**
 * Catches the ServletExceptions and prints them to the response as text.
 * 
 * @author jspacco
 */
public class MonitorSlowTransactionsFilter extends SubmitServerFilter
{
    int lowerBound = 3000;
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        String bound = filterConfig.getInitParameter("logTransactionsLongerThanThisManyMilliseconds");
        if (bound != null) lowerBound = Integer.parseInt(bound);

    }
    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,FilterChain chain)
    throws IOException, ServletException
	{
    	HttpServletRequest request = (HttpServletRequest)req;
    	
    	long start = System.currentTimeMillis();
    	try {
    		chain.doFilter(req, resp);
    	} finally {
    		long end = System.currentTimeMillis();
    		long duration = end-start;
    		if (duration > lowerBound) {
    	        String url = request.getRequestURI();
    	        if (request.getQueryString() != null) 
    	            url += "?" + request.getQueryString();
    	        HttpSession session = request.getSession(false);
    	        UserSession userSession = session == null ? null : (UserSession) session.getAttribute(USER_SESSION);
    	        if (userSession != null)
    	            url += " for student " + userSession.getStudentPK();
    	        String msg = duration + " ms required to service " +url;
    	        
    	        String referer = request.getHeader("referer");
    	        if (referer != null) msg += " "+ referer;
    	        
    	        getSubmitServerFilterLog().info(msg);
    		}
    	}
	}
}
