package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.submitServer.ClientRequestException;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.StudentForUpload;

public class RegisterOneStudent extends SubmitServerServlet
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
        boolean transactionSuccess=false;
        try {
            conn=getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            
            RequestParser parser = new RequestParser(request);
            StudentForUpload studentForUpload = new StudentForUpload(parser);
            Course course=(Course)request.getAttribute("course");
            String accountType=request.getParameter("accountType");
            
            String result=StudentForUpload.registerStudent(course,studentForUpload,accountType,conn);
            
            conn.commit();
            transactionSuccess=true;

            String link="<a href=\"" +request.getContextPath()+ "/view/instructor/course.jsp?coursePK=" +course.getCoursePK()+
                "\">Back to the main page for this course</a>";
            
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
            out.println("<HTML>");
            out.println("  <HEAD><TITLE>Register an Instructor</TITLE></HEAD>");
            out.println("  <BODY>");
            out.println(result +"<p>");
            
            // [NAT P003] Inform user of generated password
            if (studentForUpload.hasGeneratedPassword()) {
            	out.println("<pre>");
            	out.println("A password was automatically generated. Please save this information:");
            	out.println(studentForUpload.toPasswordString());
            	out.println("</pre>");
            }
            // [end NAT P003]
            
            out.println("<br>" +link);
            out.println("</body>");
            out.println("</html>");
            out.flush();
            out.close();
        } catch (ClientRequestException e) {
            throw new ServletException(e);
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess,conn);
        }
    }

}
