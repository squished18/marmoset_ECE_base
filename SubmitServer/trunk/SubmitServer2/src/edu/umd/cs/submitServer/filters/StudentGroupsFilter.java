/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 21, 2005
 *
 * @author omnafees
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Student;

/**
 * @author jspacco
 *
 * TODO take extensions into account
 */
public class StudentGroupsFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;

	/*
        Student user = (Student) request.getAttribute("user");
        if (!user.isSuperUser()) {
            throw new ServletException("Not superuser");
        }
	*/
        Connection conn=null;
        try {
            conn=getConnection();
            
            List<Student> studentGroups = 
		new ArrayList<Student>(Student.lookupAllRemoteUserStudentGroups(request.getRemoteUser(), conn).values());
            
            Collections.sort(studentGroups);
           
            request.setAttribute("studentGroups", studentGroups);
            
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(request, response); 
    }

   
}
