/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 8, 2005
 *
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;

/**
 * @author jspacco
 *
 */
public class GrantExtension extends SubmitServerServlet
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
        boolean transactionSuccess=false;
        try {
            conn=getConnection();
            //MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
            RequestParser parser = new RequestParser(request);
            
            String studentRegistrationPK = parser.getStringParameter("studentRegistrationPK");
            String projectPK = parser.getStringParameter("projectPK");
            int extension = parser.getIntParameter("extension");
            
            // low amount of transaction isolation required here since this record is read/modified infrequently
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            
            StudentSubmitStatus studentSubmitStatus = StudentSubmitStatus.lookupByStudentRegistrationPKAndProjectPK(
                    studentRegistrationPK,
                    projectPK,
                    conn);
            
            if (studentSubmitStatus == null)
            {
                // if the student hasn't submitted anything, create a submitStatus record
                // this is where the extension information is stored 
                studentSubmitStatus = StudentSubmitStatus.createAndInsert(
                        projectPK,
                        studentRegistrationPK,
                        conn);
            }
            
            studentSubmitStatus.setExtension(extension);
            
            studentSubmitStatus.update(conn);
            conn.commit();
            transactionSuccess=true;
            
            // redirect to project page
            String url = request.getContextPath();
            url += "/view/instructor/project.jsp?projectPK=" +projectPK;
            response.sendRedirect(url);
            return;
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } catch (InvalidRequiredParameterException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
        }
    }

}
