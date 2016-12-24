package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Submission;

public class ChangeSubmissionBuildStatus extends SubmitServerServlet {

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
		try {
			conn=getConnection();
			Submission submission = (Submission)request.getAttribute("submission");
			//submission.setBuildStatus(Submission.RETEST);
            String buildStatus=request.getParameter("buildStatus");
            submission.setBuildStatus(buildStatus);
			submission.update(conn);
			
			String url = request.getContextPath()+"/view/instructor/submission.jsp?submissionPK=" +submission.getSubmissionPK();
			response.sendRedirect(url);
			return;
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}
}
