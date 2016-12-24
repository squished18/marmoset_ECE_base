/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 14, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.ReleaseInformation;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.UserSession;

/**
 * @author jspacco
 *
 */
public class RequestReleaseTest extends SubmitServerServlet
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
        // TODO make fetching the userSession a method in SubmitServerServlet/SubmitServerFilter
        HttpSession session = request.getSession();
        UserSession userSession = (UserSession)session.getAttribute(USER_SESSION);

        boolean transactionSuccess=false;
        Connection conn=null;
        try {
            conn = getConnection();
            
            // fetch submissionPK parameter from request
            RequestParser parser = new RequestParser(request);
            String submissionPK = parser.getStringParameter("submissionPK");

            // XXX CANNOT use SubmissionFilter because I need a transaction
            // start new transaction
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            
            // fetch submission
            Submission submission = Submission.lookupByStudentPKAndSubmissionPK(
                    userSession.getStudentPK(),
                    submissionPK,
                    conn);
            
            if (submission == null)
            {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "There is no submission with submissionPK " +submissionPK);
                return;
            }
            
            // fetch project
            Project project = Project.getByProjectPK(submission.getProjectPK(), conn);
            // fetch previous release-tested submissions
            List submissionList = Submission.lookupAllForReleaseTesting(
                    userSession.getStudentPK(),
                    project.getProjectPK(),
                    conn);
            
            ReleaseInformation releaseInformation = new ReleaseInformation(project, submissionList);
            if (!releaseInformation.isReleaseRequestOK())
            {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have enough release tokens remaining!  Tokens remaining: " +releaseInformation.getTokensRemaining());
                return;
            }
            
            Timestamp now = new Timestamp(System.currentTimeMillis());
            submission.setReleaseRequest(now);

            submission.update(conn);

            conn.commit();
            transactionSuccess=true;
            
            // redirect to /view/oneSubmission.jsp?submissionPK=submissionPK
            String target = request.getContextPath() + "/view/submission.jsp?submissionPK=" +submissionPK;
            
            response.sendRedirect(target);
        } catch (InvalidRequiredParameterException e) {
            throw new ServletException(e);
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
            releaseConnection(conn);
        }
    }

}
