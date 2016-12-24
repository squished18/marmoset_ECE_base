package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.ClientRequestException;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.StudentForUpload;

public class RegisterInstructor extends SubmitServerServlet
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
            String coursePK=parser.getParameter("coursePK");
            
            String result="";
            
            // Add/update instructor account
            Student student=new Student();
            student.setCampusUID(studentForUpload.campusUID);
            student.setEmployeeNum(studentForUpload.employeeNum);
            student.setFirstname(studentForUpload.firstname);
            student.setLastname(studentForUpload.lastname);
            student.setPassword(studentForUpload.password);
            student.insertOrUpdate(conn);
            
            StudentRegistration studentRegistration=
                StudentRegistration.lookupByCvsAccountAndCoursePK(studentForUpload.cvsAccount,coursePK,conn);
            if (studentRegistration==null) {
                studentRegistration=new StudentRegistration();
                studentRegistration.setFirstname(studentForUpload.firstname);
                studentRegistration.setLastname(studentForUpload.lastname);
                studentRegistration.setCvsAccount(studentForUpload.cvsAccount);
                studentRegistration.setCoursePK(coursePK);
                studentRegistration.setStudentPK(student.getStudentPK());
                studentRegistration.setInstructorCapability(StudentRegistration.MODIFY_CAPABILITY);
                studentRegistration.insert(conn);
                result+="<br>Inserted new student registration for " +studentForUpload.cvsAccount+
                    " for coursePK " +coursePK;
            }
            
            // Add canonical account
            Student canonicalStudent=new Student();
            canonicalStudent.setCampusUID(studentForUpload.campusUID+"-canonical");
            canonicalStudent.setEmployeeNum(studentForUpload.employeeNum);
            canonicalStudent.setFirstname(studentForUpload.firstname);
            canonicalStudent.setLastname(studentForUpload.lastname);
            canonicalStudent.setPassword(studentForUpload.password);
            canonicalStudent.insertOrUpdate(conn);
            
            StudentRegistration canonicalStudentRegistration=
                StudentRegistration.lookupByCvsAccountAndCoursePK(studentForUpload.cvsAccount+"-canonical",coursePK,conn);
            if (canonicalStudentRegistration==null) {
                canonicalStudentRegistration=new StudentRegistration();
                canonicalStudentRegistration.setFirstname(studentForUpload.firstname);
                canonicalStudentRegistration.setLastname(studentForUpload.lastname);
                canonicalStudentRegistration.setCvsAccount(studentForUpload.cvsAccount+"-canonical");
                canonicalStudentRegistration.setCoursePK(coursePK);
                canonicalStudentRegistration.setStudentPK(canonicalStudent.getStudentPK());
                canonicalStudentRegistration.setInstructorCapability(StudentRegistration.CANONICAL_CAPABILITY);
                canonicalStudentRegistration.insert(conn);
                result+="<br>Inserted new canonical student registration for " +studentForUpload.cvsAccount+"-canonical"+
                " for coursePK " +coursePK;
            }
                
            // Add student test account
            Student testStudent=new Student();
            testStudent.setCampusUID(studentForUpload.campusUID+"-student");
            testStudent.setEmployeeNum(studentForUpload.employeeNum);
            testStudent.setFirstname(studentForUpload.firstname);
            testStudent.setLastname(studentForUpload.lastname);
            testStudent.setPassword(studentForUpload.password);
            testStudent.insertOrUpdate(conn);
            
            StudentRegistration testStudentRegistration=
                StudentRegistration.lookupByCvsAccountAndCoursePK(studentForUpload.cvsAccount+"-student",coursePK,conn);
            if (testStudentRegistration==null) {
                testStudentRegistration=new StudentRegistration();
                testStudentRegistration.setFirstname(studentForUpload.firstname);
                testStudentRegistration.setLastname(studentForUpload.lastname);
                testStudentRegistration.setCvsAccount(studentForUpload.cvsAccount+"-student");
                testStudentRegistration.setCoursePK(coursePK);
                testStudentRegistration.setStudentPK(testStudent.getStudentPK());
                testStudentRegistration.insert(conn);
                result+="<br>Inserted new student test registration for " +studentForUpload.cvsAccount+"-student"+
                " for coursePK " +coursePK;
            }
            
            conn.commit();
            transactionSuccess=true;
            
            String link="<a href=\"" +request.getContextPath()+ "/view/instructor/course.jsp?coursePK=" +coursePK+ "\">Back to the main page for this course</a>";
            
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
            out.println("<HTML>");
            out.println("  <HEAD><TITLE>Register an Instructor</TITLE></HEAD>");
            out.println("  <BODY>");
            out.println(result + "<p>");

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
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
        }
    }
}
