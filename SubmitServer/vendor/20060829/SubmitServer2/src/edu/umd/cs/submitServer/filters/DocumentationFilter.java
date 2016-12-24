/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 28, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.submitServer.UserSession;

/**
 * Ensures that the user has Instructor capability for at least one course.
 * @author jspacco
 */
public class DocumentationFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
        HttpSession session = request.getSession();
        UserSession userSession = (UserSession)session.getAttribute(USER_SESSION);
        
        Connection conn=null;
        try {
            conn=getConnection();
            //CourseCollection collection = CourseCollection.lookupAllWithReadOnlyCapability(userSession.getStudentPK(), conn);
            List<Course> courseList=Course.lookupallWithReadOnlyCapability(userSession.getStudentPK(), conn);
            releaseConnection(conn);

            if (courseList.size() == 0)
            {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not authorized to view this page");
                return;
            }
            
            chain.doFilter(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }

}
