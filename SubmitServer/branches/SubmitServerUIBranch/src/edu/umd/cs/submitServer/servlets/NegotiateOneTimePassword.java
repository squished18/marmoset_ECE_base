/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 14, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.submitServer.ClientRequestException;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.SubmitServerUtilities;
import edu.umd.cs.submitServer.filters.AccessLogFilter;

/**
 * @author jspacco
 *
 */
public class NegotiateOneTimePassword extends SubmitServerServlet
{
	private static Logger accessLog;
	private Logger getAccessLog()
	{
		if (accessLog==null) {
			accessLog=Logger.getLogger(AccessLogFilter.class);
		}
		return accessLog;
	}
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
        boolean transactionSuccess=false;
        Connection conn=null;
        String courseName= null;
        String projectNumber = null;
        String semester = null;
        String campusUID = null;
        String uidPassword = null;
        try {
        	RequestParser parser = new RequestParser(request);
        	courseName= parser.getStringParameter("courseName");
            projectNumber = parser.getStringParameter("projectNumber");
            semester = parser.getStringParameter("semester");
            campusUID = parser.getStringParameter("campusUID");
            uidPassword = parser.getStringParameter("uidPassword");
            
            getAccessLog().info("NegotiateOneTimePassword attempt:\t" +campusUID+"\t"+
            		semester+"\t"+courseName+"\t"+projectNumber);
        	
        	conn = getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
      
            // fetch course and project records
            // This cannot be done in a filter because this is the only place it happens
            Project project = Project.lookupByCourseProjectSemester(courseName, projectNumber, semester, conn);
            if (project == null) throw new ServletException("Could not find record for project " + projectNumber
                    + " in " + courseName + ", " + semester);
            // authenticate the student and find their Student record
			
            Student student = getIAuthenticationService().authenticateLDAP(campusUID, uidPassword, conn, skipLDAP);
            //Student student = PerformLogin.authenticateLDAP(campusUID, uidPassword, conn,  "true".equals(skipLDAP));
            
            // I need to do my own logging here-- AccessLogFilter cannot be applied
            // to this servlet!
            getAccessLog().info("studentPK " +student.getStudentPK()+
    	            " requesting " +SubmitServerUtilities.extractURL(request));
            
            StudentRegistration studentRegistration = StudentRegistration.lookupByStudentPKAndCoursePK(
                    student.getStudentPK(),
                    project.getCoursePK(),
                    conn);

            if (studentRegistration == null)
            {
                throw new ServletException(student.getFirstname() +" "+ student.getLastname() + " is not registered for this course.  " +
                		" If you changed your DirectoryID, please notify your instructor so that we can get the system upated");
            }
            
            // XXX transaction should really start here...
            
            StudentSubmitStatus submitStatus = StudentSubmitStatus.lookupByStudentRegistrationPKAndProjectPK(
                    studentRegistration.getStudentRegistrationPK(),
                    project.getProjectPK(),
                    conn);
            
            if (submitStatus == null)
            {
                submitStatus = StudentSubmitStatus.createAndInsert(
                        project.getProjectPK(),
                        studentRegistration.getStudentRegistrationPK(),
                        conn);
            }
            
            String cvsAccount = studentRegistration.getCvsAccount();
            String oneTimePassword = submitStatus.getOneTimePassword();
            
            conn.commit();
            transactionSuccess=true;

            // write out the cvsAccount/oneTimePassword pair
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            out.println("cvsAccount=" +cvsAccount);
            out.println("oneTimePassword=" +oneTimePassword);
            out.flush();
            out.close();
            
            getAccessLog().info("studentPK " +student.getStudentPK() + " successful "+SubmitServerUtilities.extractURL(request));
            getAuthenticationLog().info("NegotiateOneTimePassword success:\t" +
        			campusUID+"\t"+
            		semester+"\t"+
            		courseName+"\t"+
            		projectNumber);

        } catch (ClientRequestException e) {
        	getAuthenticationLog().warn("NegotiateOneTimePassword failed:\t" +
        			e.getMessage()+"\t"+
        			campusUID+"\t"+
            		semester+"\t"+
            		courseName+"\t"+
            		projectNumber);
        	throw new ServletException(e);
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NamingException e) {
		    throw new ServletException(e);
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
        }
    }
    
    boolean skipLDAP;
}
