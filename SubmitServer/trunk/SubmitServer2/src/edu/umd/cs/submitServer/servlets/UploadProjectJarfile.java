/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 17, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import metadata.identification.FormatDescription;
import metadata.identification.FormatIdentification;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.CopyUtils;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 *
 */
public class UploadProjectJarfile extends SubmitServerServlet
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
    	// TODO sanity checks on the format of the test setup
        MultipartRequest multipartRequest= (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
        Connection conn=null;
        FileItem fileItem=null;
        boolean transactionSuccess=false;
        try {
            conn=getConnection();
            
            fileItem = multipartRequest.getFileItem();
            if (fileItem == null)
                throw new ServletException("fileItem is null; this is not good");
            
            Project project=(Project)request.getAttribute(PROJECT);
            // could be null
            String comment = multipartRequest.getParameter("comment");
            
//          get size in bytes
            long sizeInBytes = fileItem.getSize();
			if (sizeInBytes == 0)
			{
				throw new ServletException("Trying upload file of size 0");
			}

			// copy the fileItem into a byte array
			InputStream is = fileItem.getInputStream();
			ByteArrayOutputStream bytes = new ByteArrayOutputStream((int)sizeInBytes);
			CopyUtils.copy(is,bytes);
            
            byte[] byteArray=bytes.toByteArray();
            
            FormatDescription desc=FormatIdentification.identify(byteArray);
            if (desc==null || !desc.getMimeType().equals("application/zip")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,"You MUST submit test-setups that are either zipped or jarred");
                return;
            }
            // start transaction here
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			ProjectJarfile.submit(byteArray, project, comment, conn);
            conn.commit();
            transactionSuccess=true;
            
            String redirectUrl = request.getContextPath() + "/view/instructor/projectUtilities.jsp?projectPK=" + project.getProjectPK();
            response.sendRedirect(redirectUrl);
           
			
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
            releaseConnection(conn);
            if (fileItem != null) fileItem.delete();
        }
    }

}
