/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 5, 2005
 *
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;

/**
 * @author jspacco
 *
 */
public class UpdateProject extends SubmitServerServlet
{

    /**
     * The doPost method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to post.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        Connection conn = null;
        try {
            conn = getConnection();
            RequestParser parser = new RequestParser(request);
            Project project = parser.getProject();
            project.update(conn);
            
            // TODO If the project has been changed from "Upload-only" to "Upload and Test"
            // then we need to change all the submissions that were submitting with
            // the wrong information.
            String previousInitialBuildStatus=parser.getParameter("previousInitialBuildStatus");
            if (!previousInitialBuildStatus.equals(project.getInitialBuildStatus())) {
                // Find any submissions marked either 'new' or 'accepted' and change them
                // Will require a transaction.
            }
            String redirectUrl = request.getContextPath() + "/view/instructor/projectUtilities.jsp?projectPK=" + project.getProjectPK();
            response.sendRedirect(redirectUrl);
        } catch (InvalidRequiredParameterException e) {
            throw new ServletException(e);
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }

}
