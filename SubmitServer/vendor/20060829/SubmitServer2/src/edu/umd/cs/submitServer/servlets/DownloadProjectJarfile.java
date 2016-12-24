/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 24, 2005
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
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.submitServer.IOUtilities;

/**
 * @author jspacco
 *
 */
public class DownloadProjectJarfile extends SubmitServerServlet
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

        Connection conn=null;
        try {
            conn = getConnection();
            
            
            ProjectJarfile projectJarfile = (ProjectJarfile)request.getAttribute("projectJarfile");
            Project project= (Project)request.getAttribute("project");
            byte[] bytes = projectJarfile.downloadArchive(conn);
            response.setHeader("Content-Disposition", "attachment; filename=setup-"+project.getProjectNumber()+"-"+projectJarfile.getProjectJarfilePK()+".zip");
            response.setHeader("Cache-Control","private");
            response.setHeader("Pragma","IE is broken");
            
            IOUtilities.sendBytesToClient(bytes, response, "application/zip");
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }

}
