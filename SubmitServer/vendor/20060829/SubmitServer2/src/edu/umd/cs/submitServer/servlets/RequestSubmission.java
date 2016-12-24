/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 15, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.CopyUtils;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.HttpHeaders;
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.shared.BuildServerMonitor;

/**
 * @author jspacco
 *
 */
public class RequestSubmission extends SubmitServerServlet
{

    private static final int MAX_BUILD_DURATION_MINUTES = 15;
    private boolean isResearchServer;
    
    private static Object lock = new Object();
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
        // Create model objects to represent a submission and
        // auxiliary info
        Submission submission = new Submission();
        //Course course = new Course();
        //StudentRegistration studentRegistration = new StudentRegistration();
        ProjectJarfile projectJarfile = new ProjectJarfile();
        try {
            // XXX Using static synchronization in the Java code rather than relying on 
            // transactions within the database.  Yes, this is a hack, but it works in
            // practice.  And I just don't understand database locks.
            synchronized(lock) {
            	conn = getConnection();
                //          Set up the connection to make the entire
                // request/response a database transaction.
                // This is serializable because we want to avoid sending
                // the same project to two build servers.
                // If two build servers request a project at the same
                // time, one will block until the other has
                // completed the transaction.
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                
                MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
                
                // what courses does this buildServer support?
                String courses = multipartRequest.getParameter("courses");
                if (courses == null)
                {
                    throw new ServletException("buildServer must sent a list of the courses it supports");
                }
                String[] allowedCourses = null;
                ArrayList<String> courseArrayList = new ArrayList<String>();
                String[] allowedCourseNames = courses.split(",");
                getSubmitServerServletLog().trace("RequestSubmission: courses = \"" + courses + "\"");
                if (allowedCourseNames.length == 0)
                {
                    getSubmitServerServletLog().trace("RequestSubmission: allowedCourseNames.length");
                    throw new ServletException("buildServer must sent a list of the courses it supports");
                }
                // what semester?
                // I'm assuming that a single buildServer will support only courses from the same semester
                String semester = multipartRequest.getParameter("semester");
                if (semester == null) {
                    // submitServer has a "default" semester
                    semester = getServletContext().getInitParameter("semester");
                }
                semester = semester.trim();
                getSubmitServerServletLog().trace("RequestSubmission: semester = \"" + semester + "\"");
                if (semester == null)
                    throw new ServletException("semester is null in WEB-INF/web.xml");
                
                for (int ii=0; ii < allowedCourseNames.length; ii++)
                {
                    getSubmitServerServletLog().trace("RequestSubmission: \"" + allowedCourseNames[ii] + "\"");
                    Course course = Course.lookupCourseByCourseNameSemester(
                            allowedCourseNames[ii].trim(),
                            semester,
                            conn);
                    if (course == null) {
                        throw new ServletException("Could not find info for \"" + allowedCourseNames[ii].trim() + "\", \"" + semester + "\"");
                    }
                    
                    getSubmitServerServletLog().trace("RequestSubmission: " + allowedCourseNames[ii]
                                                                              + ", pk = " + course.getCoursePK());                                                 		
                    courseArrayList.add(course.getCoursePK());
                    
                }
                allowedCourses = new String[courseArrayList.size()];
                allowedCourses = (String[])courseArrayList.toArray(allowedCourses);
                
                // Log that we've heard from this buildServer
                String hostname = multipartRequest.getOptionalStringParameter("hostname");
                BuildServerMonitor buildServerMonitor = getBuildServerMonitor();
                buildServerMonitor.logRequestSubmission(hostname, semester, courses);
                
                boolean foundSubmissionForBackgroundRetesting=false;
                // Priority of submissions for testing:
                // 1) new project_jarfiles with no test_runs
                // 2) explicit retests
                // 3) most recent submission without a current test run for each student, oldest first
                // 4) submissions without a current test run, oldest first

                // 5) submissions whose current test run is not the for 
                //		the current project_jarfile and were marked for release testing,
                //      newest first
                // 6) most recent submission whose current test run is not for the current
                //      project_jarfile, newest first
                // 7) all other submissions whose current test run is not the for 
                //      the current project_jarfile, newest first
                // 8) pending but timed out
                // 9) explicit background retest
                // 10) ambient (random) background retests
                //
                
                // TODO Refactor to use labeled break statements!
                
                // 1) first look for a new project jarfile that hasn't been tested
                boolean foundNewProjectJarfile = Queries.lookupNewProjectJarfile(
                        conn,
                        submission,
                        projectJarfile,
                        allowedCourses,
                        MAX_BUILD_DURATION_MINUTES);
                
                if (!foundNewProjectJarfile)
                {
                    // 2) look for explicit retests
                    boolean foundReTestSubmission = Queries.lookupReTestSubmission(
                    conn,
                    submission,
                    projectJarfile,
                    allowedCourses);
                    if (!foundReTestSubmission)
                    {
                        // 3)
                        // If this is a research server, use the version without a dependent subquery
                        // That slows down these queries by an order of magnitude or more.
                        // We'll still get a new submission, just not necessarily the newest 
                        // per student first.
                        boolean foundMostRecentNewSubmission = isResearchServer ?
                        Queries.lookupNewSubmission(conn, submission, projectJarfile, allowedCourses) :
                            Queries.lookupMostRecentNewSubmission(conn,submission,projectJarfile,allowedCourses);
                        
                        if (!foundMostRecentNewSubmission)
                        {
                            // 4) now look for new submissions
                            boolean foundNewSubmission = Queries.lookupNewSubmission(
                            conn,
                            submission,
                            projectJarfile,
                            allowedCourses);
                            if (!foundNewSubmission)
                            {
                                
                                // 5) now look for pending submissions
                                boolean foundPendingSubmission = Queries.lookupPendingSubmission(
                                conn,
                                submission,
                                projectJarfile,
                                allowedCourses,
                                MAX_BUILD_DURATION_MINUTES);
                                if (!foundPendingSubmission)
                                {
                                    // *) now look for submissions lacking a testRun for the current test-setup
                                    // that were also release tested
                                    boolean foundOutdatedReleaseTestedSubmission =
                                        Queries.lookupOutdatedReleaseTestedSubmission(
                                        conn,
                                        submission,
                                        projectJarfile,
                                        allowedCourses,
                                        MAX_BUILD_DURATION_MINUTES);
                                    if (!foundOutdatedReleaseTestedSubmission)
                                    {
                                        // *) now look for most recent outdated submission
                                        // Don't do this if this is a research server because
                                        // the dependent subqueries are too slow with a 
                                        // large number of submissions
                                        boolean foundMostRecentOutdatedSubmission =
                                            isResearchServer ? false :
                                                Queries.lookupMostRecentOutdatedSubmission(
                                                conn,
                                                submission,
                                                projectJarfile,
                                                allowedCourses,
                                                MAX_BUILD_DURATION_MINUTES);
                                        
                                        if (!foundMostRecentOutdatedSubmission)
                                        {
                                            // *) look for submissions lacking a testRun for the current test-setup
                                            boolean foundOutdatedProjectJarfileSubmission =
                                                (isResearchServer)? 
                                                Queries.lookupOutdatedProjectJarfileSubmissionForResearchServer(
                                                conn,
                                                submission,
                                                projectJarfile,
                                                allowedCourses,
                                                MAX_BUILD_DURATION_MINUTES) :
                                                    Queries.lookupOutdatedProjectJarfileSubmission(
                                                    conn,
                                                    submission,
                                                    projectJarfile,
                                                    allowedCourses,
                                                    MAX_BUILD_DURATION_MINUTES);
                                                    
                                            if (!foundOutdatedProjectJarfileSubmission)
                                            {
                                                // *) look for explicit background retest
                                                foundSubmissionForBackgroundRetesting = 
                                                    Queries.lookupExplicitBackgroundRetest(
                                                    conn,
                                                    submission,
                                                    projectJarfile,
                                                    allowedCourses,
                                                    isBackgroundRetestingEnabled());
                                                
                                                if (!foundSubmissionForBackgroundRetesting) {
                                                    foundSubmissionForBackgroundRetesting =
                                                        //*) look for ambient background retest
                                                        Queries.lookupSubmissionForBackgroundRetest(
                                                        conn,
                                                        submission,
                                                        projectJarfile,
                                                        allowedCourses,
                                                        isBackgroundRetestingEnabled());
                                                    if (!foundSubmissionForBackgroundRetesting)
                                                    {
                                                        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                                                        NO_SUBMISSIONS_AVAILABLE_MESSASGE);
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                getSubmitServerServletLog().trace("RequestSubmission: submission pk = " +
                        submission.getSubmissionPK()
                        + ", projectPK =  " 
                        + submission.getProjectPK());
                // XXX at this point, either submission and projectJarfile are non-null
                // or we've already sent an error
                
                // Tell client not to cache this object.
                // See http://www.jguru.com/faq/view.jsp?EID=377
                response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
                response.setHeader("Pragma","no-cache"); //HTTP 1.0
                response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
                
                // Send submission PK in an HTTP header.
                response.setHeader(HttpHeaders.HTTP_SUBMISSION_PK_HEADER, submission.getSubmissionPK());
                
                // Send project PK in an HTTP header.
                response.setHeader(HttpHeaders.HTTP_PROJECT_JARFILE_PK_HEADER, projectJarfile.getProjectJarfilePK());
                
                // TODO move this code up to where these values are set.
                // if we found a new project_jarfile, let the buildserver know
                if (foundNewProjectJarfile)
                {
                    response.setHeader(HttpHeaders.HTTP_NEW_PROJECT_JARFILE, "yes");
                    // set the status to 'pending' and set the current time for datePosted
                    // date_posted is a horrible name, this is the field I'm using for timeouts
                    projectJarfile.setJarfileStatus(ProjectJarfile.PENDING);
                    projectJarfile.setDatePosted(new Timestamp(System.currentTimeMillis()));
                    projectJarfile.update(conn);
                }
                else
                    response.setHeader(HttpHeaders.HTTP_NEW_PROJECT_JARFILE, "no");
                
                if (foundSubmissionForBackgroundRetesting)
                    response.setHeader(HttpHeaders.HTTP_BACKGROUND_RETEST, "yes");
                else
                    response.setHeader(HttpHeaders.HTTP_BACKGROUND_RETEST, "no");
                
                // If this is *NOT* a background re-test then
                // update build_status to pending, and build_request_timestamp to current time.
                if (!foundSubmissionForBackgroundRetesting) {
                	submission.setBuildStatus(Submission.PENDING);
                	submission.setBuildRequestTimestamp(new Timestamp(System.currentTimeMillis()));
                	submission.update(conn);
                }
                
                // commit the transaction early to prevent deadlocks
                // if communication with the buildserver fails after this
                // then we'll wait for things to time out
                conn.commit();
            }
            transactionSuccess = true;
            
            
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
        }
        
        try {
            conn = getConnection();
//          prepare byte array input stream of the submission
            byte[] bytes = submission.downloadArchive(conn);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            
            // Inform client of file length 
            response.setContentLength(bytes.length);
            
            // Inform client of content type
            response.setContentType("application/x-zip");
            
            // TODO: maybe we should send the md5sum?
            
            OutputStream out = response.getOutputStream();
            CopyUtils.copy(bais,out);
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }

    static final String NO_SUBMISSIONS_AVAILABLE_MESSASGE = "No submissions available";

    private  boolean performBackgroundRetesting=false;
	/* (non-Javadoc)
	 * @see edu.umd.cs.submitServer.servlets.SubmitServerServlet#init()
	 */
	@Override
	public void init() throws ServletException
	{
		// TODO Auto-generated method stub
		super.init();
		String performBackgroundRetestingStr = getServletContext().getInitParameter("perform.background.retesting");
		if ("true".equals(performBackgroundRetestingStr))
			performBackgroundRetesting=true;
        isResearchServer="true".equals(getServletContext().getInitParameter("research.server"));
	}
	private  boolean isBackgroundRetestingEnabled() {
    	return performBackgroundRetesting;
    }
}
