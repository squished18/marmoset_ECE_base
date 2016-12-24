/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Apr 13, 2005
 *
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter gets a connection and binds it to "connection" as a request attribute.
 * Useful for jsps that require a connection to the database for statis method
 * calls (e.g. sourceCode.jsp, courses.jsp)
 * 
 * @author jspacco
 */
public class ConnectionFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
        Connection conn=null;
        try {
            conn=getConnection();
            request.setAttribute("connection", conn);
            
            chain.doFilter(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }

    }

}
