/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 9, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.BackgroundData;
import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.BadPasswordException;
import edu.umd.cs.submitServer.CanNotFindDirectoryIDException;
import edu.umd.cs.submitServer.ClientRequestException;
import edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService;
import edu.umd.cs.submitServer.UserSession;

/**
 * @author jspacco
 *
 */
public class PerformLogin extends SubmitServerServlet {

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
        boolean superUserLogin = false;
        String superUserStudentPK=null;
		HttpSession session = request.getSession(false);
		if (session != null) {
            UserSession userSession = (UserSession) session.getAttribute(USER_SESSION);
            if (userSession!= null) {
                superUserLogin = userSession.isSuperUser();
                superUserStudentPK = userSession.getStudentPK();
            }
            session.invalidate();
        }
        session = request.getSession(true);
		String campusUID = request.getParameter("campusUID");
        if (campusUID == null || campusUID.trim().equals("")) {
            request.setAttribute("missingIDOrPasswordException", Boolean.TRUE);
            request.getRequestDispatcher("/index.jsp").forward(request,
                    response);
            return;
        }
		String uidPassword = request.getParameter("uidPassword");
		if (!superUserLogin && (uidPassword == null  || uidPassword.trim().equals(""))) {
			request.setAttribute("missingIDOrPasswordException", Boolean.TRUE);
			request.getRequestDispatcher("/index.jsp").forward(request,
					response);
			return;
		}
		Connection conn = null;
		try {
			conn = getConnection();

			String skipAuthentication = this.getServletContext().getInitParameter(SKIP_LDAP);

			// [NAT P001]
			// Lookup campusUID
			Student student=Student.lookupByCampusUID(campusUID,conn);
	        if (student==null)
	            throw new ClientRequestException("Cannot find user " +campusUID);

			// If it has a password, authenticate generically, otherwise authenticate 
			// via default service
			if (student.getPassword() != null) {
				student = new GenericStudentPasswordAuthenticationService().authenticateLDAP(
		                campusUID,
		                uidPassword,
		                conn,
		                superUserLogin || "true".equals(skipAuthentication));
				
			} else {
			
				// Note: this is a read-only query.
				// So, we do not start a transaction here.
	
				student = getIAuthenticationService().authenticateLDAP(
	                campusUID,
	                uidPassword,
	                conn,
	                superUserLogin || "true".equals(skipAuthentication));
			}
			// [end NAT P001]
            
            if (superUserLogin) {
                getAuthenticationLog().info("studentPK " +superUserStudentPK+ " just authenticated as " +student.getStudentPK());
            }

			// Sets required information in the user's session.
			setUserSession(session, student, conn);

			// check to see if user tried to view a page before logging in
			String target = request.getParameter("target");

			if (target != null && !target.equals("")) {
				response.sendRedirect(target);
				return;
			}

			// otherwise redirect to the main view page
			response.sendRedirect(request.getContextPath() + "/view/index.jsp");
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} catch (NamingException e) {
			// TODO Catch this exception and send the students some other message
			throw new ServletException(e);
		} catch (CanNotFindDirectoryIDException e) {
			getAuthenticationLog().error(e.getMessage(), e);
			request.setAttribute("canNotFindDirectoryID", Boolean.TRUE);
			request.setAttribute("campusUID", campusUID);
			request.getRequestDispatcher("/index.jsp").forward(request,
					response);
		} catch (BadPasswordException e) {
			getAuthenticationLog().error(e.getMessage(), e);
			request.setAttribute("badPassword", Boolean.TRUE);
			request.setAttribute("campusUID", campusUID);
			request.getRequestDispatcher("/index.jsp").forward(request,
					response);
		} catch (ClientRequestException e) {
			getAuthenticationLog().error(e.getMessage(), e);
			request.setAttribute("otherError", Boolean.TRUE);
			request.getRequestDispatcher("/index.jsp").forward(request,
					response);
		} finally {
			releaseConnection(conn);
		}
	}

	/**
     * @param session
     * @param conn
     * @param student
     * @throws SQLException
     */
    static void setUserSession(HttpSession session, 
            Student student,
            Connection conn)
    throws SQLException
    {
        // look up list of student registrations for this studentPK
        List<StudentRegistration> collection = StudentRegistration.lookupAllByStudentPK(student.getStudentPK(), conn);

        UserSession userSession = new UserSession();

        // set studentPK and superUser
        userSession.setStudentPK(student.getStudentPK());
        userSession.setSuperUser(student.isSuperUser());
        // has this user returned a conset form?
        // we don't care if they've consented or not, just that they've returned a form
        userSession.setGivenConsent(student.getGivenConsent());
        
        // set flag for backgroundData in the session
        BackgroundData backgroundData = BackgroundData.lookupByStudentPK(student.getStudentPK(), conn);
        if (backgroundData != null)
            userSession.setBackgroundDataComplete(backgroundData.isComplete());

        for (StudentRegistration registration : collection) {
        	Course course = Course.lookupByStudentRegistrationPK(registration
        			.getStudentRegistrationPK(), conn);

        	// in my current database implementation, I give the modify capability
        	// to mean that someone has both modify and read-only capability
        	// it might be a better idea to specifically give someone both abilities
        	// but it doesn't make any sense to have modify capability without read-only
        	// for this software.
        	if (StudentRegistration.MODIFY_CAPABILITY.equals(registration.getInstructorCapability())) {
        		// modify capability implies read-only capability as well
        		// i.e. hasInstructorActionCapability() -> hasInstructorCapability()
        		userSession.setInstructorCapability(course.getCoursePK());
        		userSession.setInstructorActionCapability(course
        				.getCoursePK());
        	}
        	if (StudentRegistration.READ_ONLY_CAPABILITY.equals(registration.getInstructorCapability())) {
        		// read-only capability does not necessarily imply any other privileges
        		userSession.setInstructorCapability(course.getCoursePK());
        	}
        }
        // set background data
        session.setAttribute(USER_SESSION, userSession);
    }
}