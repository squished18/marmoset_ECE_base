package edu.umd.cs.submitServer.servlets;

import java.sql.Connection;
import java.sql.SQLException;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Student;

public class ChangePassword extends SubmitServerServlet
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
        Student student=(Student)request.getAttribute(STUDENT);
        String currentPassword=request.getParameter("currentPassword");
        String newPassword=request.getParameter("newPassword");
        String confirmPassword=request.getParameter("confirmNewPassword");
        String message;
        String title;
        String link="<a href=\"" +request.getContextPath()+ "/view/changePassword.jsp?studentPK=" +student.getStudentPK()+"\"> Try again! </a>";
        if (currentPassword==null || !currentPassword.equals(student.getPassword())) {
            message="The current password does not match the password for this account!<br> " +
                    "Your password has <b>NOT</b> been changed";
            title="Password update not successful";
        } else if (newPassword==null || !newPassword.equals(confirmPassword)) {
            message="The two new passwords entered don't match!<br> " +
                    "Your password has <b>NOT</b> been changed";
            title="Password update not successful";
        } else {
            try {
                Connection conn = getConnection();
                student.setPassword(newPassword);
                student.update(conn);
                message="Your password has been successfully updated!";
                title="Password update successful!";
                link="<a href=\"" +request.getContextPath()+ "/view/index.jsp\"> Back to main page </a>";
            } catch(SQLException e) {
                throw new ServletException(e);
            }
        }
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        out.println("<HTML>");
        out.println("  <HEAD><TITLE>" +title+ "</TITLE></HEAD>");
        out.println("  <BODY>");
        out.println(message);
        out.println("<p>");
        out.println(link);
        out.println("");
        out.println("  </BODY>");
        out.println("</HTML>");
        out.flush();
        out.close();
    }

}
