package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.BadPasswordException;
import edu.umd.cs.submitServer.CanNotFindDirectoryIDException;
import edu.umd.cs.submitServer.ClientRequestException;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.SubmitServerUtilities;

/**
 * Authenticates a user with their one-time password.  This filter is used in situations where
 * it is not possible or does not make sense to use the web-interface for authentication
 * and to establish a session, i.e. Eclipse-based submissions and uploading the Eclipse
 * runlogs.
 * <p>
 * The one-time passwords show up in the logs, but they are generated at the server anyway 
 * and so are already in the database.  Note that we <b>never</b> log the 
 * user's DirectoryID password.
 * <p>
 * This Filter performs its own access logging (rather than relying on the AccessLogFilter)
 * because any servlet this filter is applied to (mapped to the /eclipse prefix) will not
 * have a session we can use to track who is making the request.
 * 
 * @author jspacco
 *
 */
public class OneTimePasswordAuthenticationFilter extends SubmitServerFilter {

	public void doFilter(ServletRequest req, ServletResponse resp,FilterChain chain)
	throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;
		Connection conn=null;
		String courseName= null;
        String projectNumber = null;
        String semester = null;
        String cvsAccount = null;
        String oneTimePassword = null;
        String requestURL = SubmitServerUtilities.extractURL(request);
		
        try {
			conn=getConnection();

			//set by filter
			MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);

			courseName= multipartRequest.getStringParameter("courseName");
            projectNumber = multipartRequest.getStringParameter("projectNumber");
            semester = multipartRequest.getStringParameter("semester");
            cvsAccount = multipartRequest.getStringParameter("cvsAccount");
            oneTimePassword = multipartRequest.getStringParameter("oneTimePassword");

            getAuthenticationLog().info("OneTimePasswordAuthentication attempt:\t" +
            		cvsAccount +"\t"+
            		oneTimePassword +"\t"+
            		courseName+ "\t"+
            		semester+"\t"+
            		projectNumber+ "\t" +
            		requestURL);
            
            Project project = Project.lookupByCourseProjectSemester(courseName, projectNumber, semester, conn);
            if (project == null)
                throw new ServletException("Cannot find projectNumber " +projectNumber+ " for course " +courseName+
                        " in semester " +semester);
            Course course = project.getCorrespondingCourse(conn);
           
            StudentRegistration studentRegistration = StudentRegistration.lookupByCvsAccountAndProjectPKAndOneTimePassword(
                    cvsAccount,
                    oneTimePassword,
                    project.getProjectPK(),
                    conn);
            if (studentRegistration == null) {
                studentRegistration = StudentRegistration.lookupByCvsAccountAndCoursePK(
                        cvsAccount,
                        course.getCoursePK(),
                        conn);
                if (studentRegistration != null)     
                    throw new BadPasswordException(HttpServletResponse.SC_UNAUTHORIZED,
                    		"The one-time password stored in .submitUser is incorrect");
                else
                    throw new CanNotFindDirectoryIDException(
                    		HttpServletResponse.SC_UNAUTHORIZED, 
                    		cvsAccount +" is not registered for "+
                    			course.getCourseName()+" in " +course.getSemester());
            }

            Student student = studentRegistration.getCorrespondingStudent(conn);
            
            getAuthenticationLog().info("OneTimePasswordAuthentication success:\t" +
            		cvsAccount +"\t"+
            		oneTimePassword +"\t"+
            		courseName+ "\t"+
            		semester+"\t"+
            		projectNumber+ "\t" +
            		requestURL);
            
            request.setAttribute("course", course);
            request.setAttribute("studentRegistration", studentRegistration);
            request.setAttribute("user", student);
            request.setAttribute(PROJECT, project);
		} catch (ClientRequestException e) {
			getAuthenticationLog().warn("OneTimePasswordAuthentication failed:\t"+
					e.getMessage()+"\t"+
					cvsAccount +"\t"+
            		oneTimePassword +"\t"+
            		courseName+ "\t"+
            		semester+"\t"+
            		projectNumber+ "\t" +
            		requestURL);
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}

}
