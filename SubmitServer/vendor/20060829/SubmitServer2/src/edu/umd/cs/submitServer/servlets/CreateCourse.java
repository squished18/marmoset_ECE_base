/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 11, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.UserSession;

/**
 * @author jspacco
 *
 */
public class CreateCourse extends SubmitServerServlet {

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
        HttpSession session = request.getSession();
        UserSession userSession = (UserSession)session.getAttribute(USER_SESSION);
        
        Connection conn=null;
        try {
            conn = getConnection();
            RequestParser parser = new RequestParser(request);
            Course course = parser.getCourse();
            
            // insert the course
            course.insert(conn);
            
            // Bootstrap purposes:
            // Register for this course the administrator that created this course
            // with full "instructor" privileges 
            String studentPK = userSession.getStudentPK();
            Student student = Student.lookupByStudentPK(studentPK, conn);
            
            StudentRegistration studentRegistration = new StudentRegistration();

            studentRegistration.setStudentPK(student.getStudentPK());
            studentRegistration.setFirstname(student.getFirstname());
            studentRegistration.setLastname(student.getLastname());
            studentRegistration.setCoursePK(course.getCoursePK());
            studentRegistration.setCvsAccount(student.getCampusUID());
            studentRegistration.setInstructorCapability(StudentRegistration.MODIFY_CAPABILITY);
            
            studentRegistration.insert(conn);
            
            userSession.setInstructorActionCapability(course.getCoursePK());
            userSession.setInstructorCapability(course.getCoursePK());
            
            // TODO redirect to the login page with the URL of the new course encoded as the target
            // or reset the user's session
            // otherwise this redirect doesn't work because the user's session doesn't reflect
            // instructor access to the new course
            String redirectUrl = request.getContextPath() + "/view/instructor/course.jsp?coursePK=" + course.getCoursePK();
            response.sendRedirect(redirectUrl);
            
        } catch (InvalidRequiredParameterException e) {
            throw new ServletException(e);
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        }
    }

}
