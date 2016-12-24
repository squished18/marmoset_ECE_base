package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;

public class CreateDotSubmitFile extends SubmitServerServlet
{

    /**
     * The doGet method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to get.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        Course course=(Course)request.getAttribute("course");
        Project project=(Project)request.getAttribute("project");
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println("projectNumber="+project.getProjectNumber());
        out.println("courseName="+course.getCourseName());
        out.println("semester="+course.getSemester());
        out.println("submitURL="+
            request.getScheme()+"://"+
            request.getLocalName()+":"+
            request.getLocalPort()+
            request.getContextPath()+
            ECLIPSE_SUBMIT_PATH);
        
        out.flush();
        out.close();
    }

}
