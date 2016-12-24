/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 24, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.NamingException;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Catches the ServletExceptions and prints them to the response as text.
 * 
 * @author jspacco
 */
public class ServletExceptionFilter extends SubmitServerFilter
{
	private static Logger servletExceptionLog;
	private Logger getServletExceptionLog() {
		if (servletExceptionLog==null) {
			servletExceptionLog=Logger.getLogger("edu.umd.cs.submitServer.logging.servletExceptionLog");
		}
		return servletExceptionLog;
	}
    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
    throws IOException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
        	getServletExceptionLog().error(e.getMessage(), e);
        	
        	// Get the most specific non-null message.
        	Throwable cause=e.getCause();
        	String message=e.getMessage();
        	if (cause!=null) {
        		if (cause.getMessage()!=null)
        			message=e.getMessage();
        	}
        	if (message==null)
        		message=e.getClass().getName();
        	
        	// TODO can check for other exception sub-types and http response codes
        	if (cause instanceof NamingException) {
        			message += "\nThe campus LDAP authentication system is not responding.  " +
        			"This happens intermittently and usually only lasts for a minute or two.  " +
        			"Please try again in a couple of minutes";
        	}

            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            PrintWriter out = response.getWriter();
            
            out.println("\nRequest failed: " + message);
            //printStacks(3, e, out);
            
            out.flush();
            out.close();
         }
    }
    
    private static void printStacks(int numStacks, Throwable e, PrintWriter out)
    {
        if (e.getMessage() != null) out.println(e.getClass().getName());
        StackTraceElement[] trace = e.getStackTrace();
        for (int ii=0; ii < numStacks && ii < trace.length; ii++)
            out.println("   " + trace[ii]);
    }
}
