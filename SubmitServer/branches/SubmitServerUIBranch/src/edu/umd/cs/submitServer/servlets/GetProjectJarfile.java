/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 16, 2005
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

import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.submitServer.IOUtilities;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 *
 */
public class GetProjectJarfile extends SubmitServerServlet
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
        Connection conn=null;
        try {
            conn=getConnection();
            
            MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
            
			String projectJarfilePK = multipartRequest.getStringParameter("projectJarfilePK");
			ProjectJarfile projectJarfile = ProjectJarfile.lookupByProjectJarfilePK(projectJarfilePK, conn);
			if (projectJarfile == null)
			{
			    throw new ServletException("Cannot find projectJarfile with PK " +projectJarfilePK);
			}
			
			// get the archive in bytes
			byte[] bytes = projectJarfile.downloadArchive(conn);
			IOUtilities.sendBytesToClient(bytes, response, "application/x-zip");
        } catch (InvalidRequiredParameterException e) {
            throw new ServletException(e);
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }
}
