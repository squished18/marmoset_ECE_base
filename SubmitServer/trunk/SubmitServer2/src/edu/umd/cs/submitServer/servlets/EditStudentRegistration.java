package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
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

/**
 * EditStudentRegistration
 * <br>
 * TODO Handle users who are not bound to particular courses (i.e. super-users, research users)
 * @author jspacco
 */
public class EditStudentRegistration extends SubmitServerServlet
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
			
			String studentRegistrationPK = parser.getStringParameter("studentRegistrationPK");
			StudentRegistration studentRegistration = StudentRegistration.lookupByStudentRegistrationPK(studentRegistrationPK, conn);
			if (studentRegistration==null)
				throw new SQLException("Potential database corruption: cannot find studentRegistration with PK " +studentRegistrationPK);
			Student student = studentRegistration.getCorrespondingStudent(conn);
			
			student.setCampusUID(studentForUpload.campusUID);
			student.setEmployeeNum(studentForUpload.employeeNum);
			student.setFirstname(studentForUpload.firstname);
			student.setLastname(studentForUpload.lastname);
			
			// [NAT P005] update the password if it changes in the registration
			if (studentForUpload.password != null) 
				student.setPassword(studentForUpload.password);
			// [end NAT P005]
			
			student.update(conn);
			
			studentRegistration.setFirstname(studentForUpload.firstname);
			studentRegistration.setLastname(studentForUpload.lastname);
			studentRegistration.setCvsAccount(studentForUpload.cvsAccount);
			studentRegistration.update(conn);
			conn.commit();
			transactionSuccess=true;
			
			String redirectURL = request.getContextPath() + "/view/instructor/editStudentRegistration.jsp?studentRegistrationPK=" +studentRegistrationPK+
				"&editStudentRegistrationMessage=Successful!";
			response.sendRedirect(redirectURL);
		} catch (ClientRequestException e) {
			response.sendError(e.getErrorCode(), e.getMessage());
			return;
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
		}
	}

}
