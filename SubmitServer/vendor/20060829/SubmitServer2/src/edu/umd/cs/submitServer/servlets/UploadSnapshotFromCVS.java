/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 14, 2005
 *
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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.CopyUtils;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Snapshot;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 *
 */
public class UploadSnapshotFromCVS extends SubmitServerServlet
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
        FileItem fileItem=null;
        boolean transactionSuccess=false;
        try {
            MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
            
            String semester = multipartRequest.getStringParameter("semester");
            String cvsAccount = multipartRequest.getStringParameter("cvsAccount");
            long commitCvstag = multipartRequest.getLongParameter("commitCvstag");
            String courseName = multipartRequest.getStringParameter("courseName");
            String projectNumber = multipartRequest.getStringParameter("projectNumber");
            // will be null for unsubmitted snapshots
            String cvsTagTimestamp = multipartRequest.getOptionalStringParameter("cvsTagTimestamp");
            // could be useful to convert from GMT to localtime
            Timestamp commitTimestamp = new Timestamp(commitCvstag);
            
            conn=getConnection();
            
            String submitClientTool = multipartRequest
            .getStringParameter("submitClientTool");
            String submitClientVersion = multipartRequest
            .getParameter("submitClientVersion");
            if (submitClientVersion != null)
                submitClientTool += "-" + submitClientVersion;

            fileItem = multipartRequest.getFileItem();

            // get size in bytes
            long sizeInBytes = fileItem.getSize();
            if (sizeInBytes == 0) {
                throw new ServletException("Trying upload file of size 0");
            }
            
            // copy the fileItem into a byte array input stream from which we can extract a byte array
            InputStream is = fileItem.getInputStream();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(
                    (int) sizeInBytes);
            CopyUtils.copy(is,bytes);
            
            // now upload the snapshot; this method returns null if the snapshot has already been uploaded
            Snapshot snapshot = Snapshot.submitOneProject(
                    cvsAccount,
                    projectNumber,
                    semester,
                    commitCvstag,
                    courseName,
                    cvsTagTimestamp,
                    submitClientTool,
                    bytes.toByteArray(),
                    conn);
            
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            
            if (snapshot != null) {
                out.println("Successful commit #" + snapshot.getCommitNumber()
                        + " received for project " + projectNumber);
            } else {
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                out.println("Already submitted, commitCvstag: " +commitCvstag+
                        ", cvs account: "
                        +cvsAccount+
                        ", projectNumber: " +projectNumber);
            }
            

            out.println("semester: " +semester);
            out.println("courseName: " +courseName);
            out.println("commitCvstag: " +commitCvstag);
            out.println("commitTimestamp: " +commitTimestamp);
            out.println("cvsTagTimestamp: " +cvsTagTimestamp);
            out.println("projectNumber: " +projectNumber);
            out.println("cvsAccount: " +cvsAccount);
            
            out.flush();
            out.close();

        } catch (InvalidRequiredParameterException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }
            
    
    
    
    public void doPost2(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        Connection conn=null;
        FileItem fileItem=null;
        boolean transactionSuccess=false;
        try {
            MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute("multipartRequest");
            
            String semester = multipartRequest.getStringParameter("semester");
            String cvsAccount = multipartRequest.getStringParameter("cvsAccount");
            long commitCvstag = multipartRequest.getLongParameter("commitCvstag");
            String courseName = multipartRequest.getStringParameter("courseName");
            String projectNumber = multipartRequest.getStringParameter("projectNumber");
            // will be null for unsubmitted snapshots
            String cvsTagTimestamp = multipartRequest.getOptionalStringParameter("cvsTagTimestamp");
            // could be useful to convert from GMT to localtime
            Timestamp commitTimestamp = new Timestamp(commitCvstag);
            
            // start a transaction
            conn=getConnection();
            conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            
            // look up project
			Project project = Project.lookupByCourseProjectSemester(
                    courseName,
                    projectNumber,
                    semester,
                    conn);
            
            // look up studentRegistration
			StudentRegistration studentRegistration = StudentRegistration.lookupByCvsAccountAndCoursePK(
                    cvsAccount,
                    project.getCoursePK(),
                    conn);

			// find StudentSubmitStatus record
			StudentSubmitStatus studentSubmitStatus = 
			    StudentSubmitStatus.lookupByStudentRegistrationPKAndProjectPK(
			            studentRegistration.getStudentRegistrationPK(),
			            project.getProjectPK(),
			            conn);

			// if StudentSubmitStatus doesn't exist, then create it
			if (studentSubmitStatus == null) {
			    studentSubmitStatus = StudentSubmitStatus.createAndInsert(
			            project.getProjectPK(),
			            studentRegistration.getStudentRegistrationPK(),
			            conn);
			}

            int commitNumber = studentSubmitStatus.getNumberCommits() + 1;
			// fetch how many commits have already been made
			studentSubmitStatus.setNumberCommits(commitNumber);
			int submitNumber = -1;
			// If snapshot was also submission, increment the numberSubmissions
			if (cvsTagTimestamp != null)
			{
			    submitNumber = studentSubmitStatus.getNumberSubmissions() + 1;
			    studentSubmitStatus.setNumberSubmissions(submitNumber);
			}
			studentSubmitStatus.update(conn);

			// prepare new snapshot record
			Snapshot snapshot = new Snapshot();
			snapshot.setStudentRegistrationPK(studentRegistration.getStudentRegistrationPK());
			
			snapshot.setProjectPK(project.getProjectPK());

			// number of intermediate commits
			snapshot.setCommitNumber(new Integer(commitNumber));
			// set the commitTimestamp and submissionTimestamp based on the uploaded long value
			snapshot.setCommitTimestamp(commitTimestamp);
			// set the commitCvstag-- this is the ONLY way we can match submissions
			// in the DB with information in the CVS repository
			snapshot.setCommitCvstag(Long.toString(commitCvstag));

			// cvs submission tag; this will be null for unsubmitted snapshots
			if (cvsTagTimestamp != null)
			{
			    snapshot.setCvsTagTimestamp(cvsTagTimestamp);
			    snapshot.setSubmissionNumber(new Integer(submitNumber).toString());
			    snapshot.setSubmissionTimestamp(commitTimestamp);
			}
			
			snapshot.setBuildStatus(project.getInitialBuildStatus());
			// figure out the type and version of the submit client
			String submitClientTool = multipartRequest
					.getStringParameter("submitClientTool");
			String submitClientVersion = multipartRequest
					.getParameter("submitClientVersion");
			if (submitClientVersion != null)
				submitClientTool += "-" + submitClientVersion;
			snapshot.setSubmitClient(submitClientTool);

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

			// set the byte array as the archive
			snapshot.setArchiveForUpload(bytes.toByteArray());

			snapshot.insert(conn);
            conn.commit();
            transactionSuccess = true;

            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            
            out.println("Successful commit #" + commitNumber
                    + " received for project " + project.getProjectNumber());

            out.println("semester: " +semester);
            out.println("courseName: " +courseName);
            out.println("commitCvstag: " +commitCvstag);
            out.println("commitTimestamp: " +commitTimestamp);
            out.println("cvsTagTimestamp: " +cvsTagTimestamp);
            out.println("projectNumber: " +projectNumber);
            out.println("cvsAccount: " +cvsAccount);
            
            out.flush();
            out.close();
        } catch (InvalidRequiredParameterException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess,conn);
            if (fileItem != null) fileItem.delete();
        }
    }

}
