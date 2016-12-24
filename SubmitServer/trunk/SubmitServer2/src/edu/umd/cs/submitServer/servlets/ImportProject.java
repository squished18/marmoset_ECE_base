package edu.umd.cs.submitServer.servlets;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.UserSession;

public class ImportProject extends SubmitServerServlet
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
        FileItem fileItem =null;
        boolean transactionSuccess=false;
        try {
            conn=getConnection();
            
            // MultipartRequestFilter is required
            MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
            Course course=(Course)request.getAttribute(COURSE);
            UserSession userSession=(UserSession)request.getSession().getAttribute(USER_SESSION);
            StudentRegistration canonicalStudentRegistration=StudentRegistration.lookupByStudentRegistrationPK(
                multipartRequest.getParameter("canonicalStudentRegistrationPK"),
                conn);
            
            fileItem = multipartRequest.getFileItem();
            
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
            Project project=Project.importProject(fileItem.getInputStream(), course, canonicalStudentRegistration, conn);
            
            conn.commit();
            transactionSuccess=true;
            
            String redirectUrl=request.getContextPath() +
                "/view/instructor/projectUtilities.jsp?projectPK=" +
                project.getProjectPK();
            response.sendRedirect(redirectUrl);
            
        } catch (ClassNotFoundException e) {
            throw new ServletException(e);
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
        }
    }
}
