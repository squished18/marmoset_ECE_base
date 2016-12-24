/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 13, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import metadata.identification.FormatDescription;
import metadata.identification.FormatIdentification;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.CopyUtils;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 *
 */
public class UploadSubmission extends SubmitServerServlet {

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
	throws IOException, ServletException
    {
		// TODO should be passed in
		Timestamp submissionTimestamp = new Timestamp(System
				.currentTimeMillis());

		// these are set by filters or previous servlets
		Project project = (Project) request.getAttribute(PROJECT);
		StudentRegistration studentRegistration = (StudentRegistration) request
				.getAttribute(STUDENT_REGISTRATION);
		MultipartRequest multipartRequest = (MultipartRequest) request
				.getAttribute(MULTIPART_REQUEST);

		Connection conn = null;
		FileItem fileItem=null;
		boolean transactionSuccess = false;
		try {
			conn = getConnection();

			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            
            fileItem = multipartRequest.getFileItem();
            // get size in bytes
            long sizeInBytes = fileItem.getSize();
            if (sizeInBytes == 0) {
                throw new ServletException("Trying upload file of size 0");
            }

            // copy the fileItem into a byte array
            InputStream is = fileItem.getInputStream();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(
                    (int) sizeInBytes);
            CopyUtils.copy(is,bytes);
            
            byte[] bytesForUpload=bytes.toByteArray();
            // Make sure the bytes we have look like a zipfile
            FormatDescription desc=FormatIdentification.identify(bytesForUpload);
            if (desc==null || !desc.getMimeType().equals("application/zip")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,"You MUST submit files that are either zipped or jarred");
                return;
            }

            Submission submission=Submission.submit(
                bytesForUpload,
                studentRegistration,
                project,
                multipartRequest.getParameter("cvstagTimestamp"),
                multipartRequest.getStringParameter("submitClientTool"),
                multipartRequest.getParameter("submitClientVersion"),
                submissionTimestamp,
                conn);
/*            
			// find StudentSubmitStatus record
			StudentSubmitStatus studentSubmitStatus = StudentSubmitStatus
					.lookupByStudentRegistrationPKAndProjectPK(
							studentRegistration.getStudentRegistrationPK(),
							project.getProjectPK(), conn);

			// if the submitStatus record doesn't exist, then create it
			if (studentSubmitStatus == null) {
				studentSubmitStatus = StudentSubmitStatus.createAndInsert(
				        project.getProjectPK(),
				        studentRegistration.getStudentRegistrationPK(),
				        conn);
			}

			if (project.getCanonicalStudentRegistrationPK().equals(
					studentRegistration.getStudentRegistrationPK()))
				ProjectJarfile.resetAllFailedTestSetups(project.getProjectPK(),
						conn);
            int submissionNumber = studentSubmitStatus.getNumberSubmissions() + 1;
			// figure out how many submissions have already been made
			studentSubmitStatus.setNumberSubmissions(submissionNumber);
			studentSubmitStatus.update(conn);

			// prepare new submission record
			Submission submission = new Submission();
			submission.setStudentRegistrationPK(studentRegistration
					.getStudentRegistrationPK());
			submission.setProjectPK(project.getProjectPK());
			//submission.setNumTestOutcomes(0);
			submission.setSubmissionNumber(Integer.toString(submissionNumber));
			submission.setSubmissionTimestamp(submissionTimestamp);
			// OK if this is null
			submission.setCvsTagTimestamp(multipartRequest
					.getParameter("cvstagTimestamp"));
			submission.setBuildStatus(project.getInitialBuildStatus());
			// figure out the type and version of the submit client
			String submitClientTool = multipartRequest
					.getStringParameter("submitClientTool");
			String submitClientVersion = multipartRequest
					.getParameter("submitClientVersion");
			if (submitClientVersion != null)
				submitClientTool += "-" + submitClientVersion;
			submission.setSubmitClient(submitClientTool);

			

			// set the byte array as the archive
			submission.setArchiveForUpload(bytesForUpload);
            

			submission.insert(conn);
*/
            conn.commit();
            request.setAttribute("submission", submission);

            transactionSuccess = true;
            boolean webBasedUpload = ((Boolean) request.getAttribute("webBasedUpload")).booleanValue();


            if (!webBasedUpload) {

                response.setContentType("text/plain");
                PrintWriter out = response.getWriter();
                out.println("Successful submission #" + submission.getSubmissionNumber()
                        + " received for project " + project.getProjectNumber());

                out.flush();
                out.close();
                return;
            }
            boolean instructorUpload = ((Boolean) request
                    .getAttribute("instructorViewOfStudent")).booleanValue();
            //boolean isCanonicalSubmission="true".equals(request.getParameter("isCanonicalSubmission"));
            // set the successful submission as a request attribute
            String redirectUrl;
            
            if (project.getCanonicalStudentRegistrationPK().equals(
                studentRegistration.getStudentRegistrationPK()))
            {
                redirectUrl = request.getContextPath() + 
                "/view/instructor/projectUtilities.jsp?projectPK=" +
                project.getProjectPK();
            } else if (instructorUpload) {
                redirectUrl = request.getContextPath()
                        + "/view/instructor/project.jsp?projectPK="
                        + project.getProjectPK();
            } else {
                redirectUrl = request.getContextPath() + "/view/project.jsp?projectPK="
                        + project.getProjectPK();
            }
        
            response.sendRedirect(redirectUrl);

		} catch (InvalidRequiredParameterException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
			if (fileItem != null) fileItem.delete();
		}
	}
}