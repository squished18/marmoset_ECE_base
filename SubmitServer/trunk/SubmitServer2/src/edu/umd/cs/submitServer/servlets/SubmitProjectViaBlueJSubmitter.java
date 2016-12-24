package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.ClientRequestException;
import edu.umd.cs.submitServer.IAuthenticationService;
import edu.umd.cs.submitServer.MultipartRequest;

public class SubmitProjectViaBlueJSubmitter extends SubmitServerServlet
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
        MultipartRequest multipartRequest=(MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
        String campusUID=multipartRequest.getParameter("campusUID");
        String password=multipartRequest.getParameter("password");
        String courseName=multipartRequest.getParameter("courseName");
        String semester=multipartRequest.getParameter("semester");
        String projectNumber=multipartRequest.getParameter("projectNumber");
        Connection conn=null;
        try {
            conn=getConnection();
            
            // Authenticate student against the database/LDAP system
            Student student=getIAuthenticationService().authenticateLDAP(campusUID, password, conn, false);
            if (student==null)
                throw new ServletException("Password doesn't match username " +campusUID);
            
            // Lookup the project
            Project project = Project.lookupByCourseProjectSemester(courseName, projectNumber, semester, conn);
            if (project == null) throw new ServletException("Could not find record for project " + projectNumber
                    + " in " + courseName + ", " + semester);
            // Get corresponding course.
            Course course=project.getCorrespondingCourse(conn);

            // Get studentRegistration
            StudentRegistration studentRegistration = StudentRegistration.lookupByStudentPKAndCoursePK(
                student.getStudentPK(),
                project.getCoursePK(),
                conn);
            if (studentRegistration == null)
                throw new ServletException(student.getFirstname() +" "+ student.getLastname() + " is not registered for this course.  " +
                " If you changed your DirectoryID, please notify your instructor so that we can get the system upated");
            
            request.setAttribute("course", course);
            request.setAttribute("studentRegistration", studentRegistration);
            request.setAttribute("user", student);
            request.setAttribute("project", project);
            request.setAttribute("webBasedUpload", Boolean.FALSE);
            // forward to the UploadSubmission servlet for the heavy lifting
            String uploadSubmission = "/action/UploadSubmission";
            RequestDispatcher dispatcher = request.getRequestDispatcher(uploadSubmission);
            dispatcher.forward(request, response);
        } catch (NamingException e) {
            throw new ServletException(e);
        } catch (ClientRequestException e) {
            throw new ServletException(e);
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }
}
