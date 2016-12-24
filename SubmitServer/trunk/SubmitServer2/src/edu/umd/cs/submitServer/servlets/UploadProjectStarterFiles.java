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
import edu.umd.cs.submitServer.MultipartRequest;

public class UploadProjectStarterFiles extends SubmitServerServlet
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
        MultipartRequest multipartRequest=(MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
        Project project=(Project)request.getAttribute(PROJECT);
        
        Connection conn=null;
        FileItem fileItem=null;
        try {
            conn=getConnection();
            
            fileItem=multipartRequest.getFileItem();
            
            long sizeInBytes = fileItem.getSize();
            if (sizeInBytes == 0) {
                throw new ServletException("Trying upload file of size 0");
            }

            // copy the fileItem into a byte array
            InputStream is = fileItem.getInputStream();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream((int) sizeInBytes);
            CopyUtils.copy(is,bytes);
            
            byte[] bytesForUpload=bytes.toByteArray();

            // set the byte array as the archive
            project.setArchiveForUpload(bytesForUpload);
            FormatDescription desc=FormatIdentification.identify(bytesForUpload);
            if (desc==null || !desc.getMimeType().equals("application/zip")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,"You MUST submit starter files that are either zipped or jarred");
                return;
            }
            
            if (project.getArchivePK()==null) {
                // If there is no archive, then upload and create a new one
                project.uploadCachedArchive(conn);
                project.update(conn);
            } else {
                // Otherwise, update the archive we already have
                project.updateCachedArchive(bytesForUpload,conn);
            }
            
            String redirectUrl = request.getContextPath() + "/view/instructor/projectUtilities.jsp?projectPK=" + project.getProjectPK();
            response.sendRedirect(redirectUrl);
            
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }
}
