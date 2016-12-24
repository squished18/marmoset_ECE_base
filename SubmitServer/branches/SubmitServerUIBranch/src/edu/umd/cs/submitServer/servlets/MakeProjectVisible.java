/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 25, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;

/**
 * @author jspacco
 * 
 */
public class MakeProjectVisible extends SubmitServerServlet {

    /**
     * The doPost method of the servlet. <br>
     * 
     * This method is called when a form has its tag value method equals to
     * post.
     * 
     * @param request
     *            the request send by the client to the server
     * @param response
     *            the response send by the server to the client
     * @throws ServletException
     *             if an error occurred
     * @throws IOException
     *             if an error occurred
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

		Project project = (Project) request.getAttribute("project");
		  project.setVisibleToStudents(true);
         Connection conn = null;
    
        try {
        	  conn = getConnection();
        	   project.update(conn);
       
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        String redirectUrl = request.getContextPath()
                + "/view/instructor/projectUtilities.jsp?projectPK=" + project.getProjectPK();
        response.sendRedirect(redirectUrl);
    }

}
