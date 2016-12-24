/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 2, 2005
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
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.submitServer.ClientRequestException;
import edu.umd.cs.submitServer.RequestParser;

/**
 * @author jspacco
 *
 */
public class ChangeTestSetupStatus extends SubmitServerServlet
{

    /**
     * The doGet method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to get.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        // TODO make this a post method and make the link to it a form button
        Connection conn=null;
        boolean transactionSuccess=false;
        try {
            conn=getConnection();
            ProjectJarfile projectJarfile = (ProjectJarfile)request.getAttribute("projectJarfile");
            Project project = (Project)request.getAttribute("project");
            RequestParser requestParser = new RequestParser(request);
            String jarfileStatus = requestParser.getStringParameter("jarfileStatus");
            
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            if (jarfileStatus.equals(ProjectJarfile.INACTIVE))
            {
                // Can only mark something INACTIVE that was previously ACTIVE
                if (projectJarfile.getJarfileStatus().equals(ProjectJarfile.ACTIVE))
                {
                    projectJarfile.setJarfileStatus(ProjectJarfile.INACTIVE);
                    projectJarfile.update(conn);
                    project.setProjectJarfilePK("0");
                    project.update(conn);
                }
            } else if (jarfileStatus.equals(ProjectJarfile.BROKEN) &&  
                (projectJarfile.getJarfileStatus().equals(ProjectJarfile.FAILED) ||
                    projectJarfile.getJarfileStatus().equals(ProjectJarfile.TESTED) ||
                    projectJarfile.getJarfileStatus().equals(ProjectJarfile.INACTIVE) ||
                    projectJarfile.getJarfileStatus().equals(ProjectJarfile.PENDING) ||
                    projectJarfile.getJarfileStatus().equals(ProjectJarfile.NEW)))
            {
                // Can mark broken only if current state is:
                // FAILED, TESTED, INACTIVE, or PENDING
                // If you mark something BROKEN that was pending, it might re-appear
                // if the buildserver was stalled.
                projectJarfile.setJarfileStatus(jarfileStatus);
                projectJarfile.update(conn);
            }

            conn.commit();
            transactionSuccess=true;
            String url = request.getContextPath() + "/view/instructor/projectUtilities.jsp?projectPK="+projectJarfile.getProjectPK();
            response.sendRedirect(url);
        } catch (ClientRequestException e) {
            response.sendError(e.getErrorCode(), e.getMessage());
            return;
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
        }
    }
}
