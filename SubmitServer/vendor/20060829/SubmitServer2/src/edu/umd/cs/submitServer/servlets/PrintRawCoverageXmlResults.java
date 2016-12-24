package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.submitServer.RequestParser;

public class PrintRawCoverageXmlResults extends SubmitServerServlet {

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
		Connection conn=null;
		try {
			conn=getConnection();
			
			RequestParser parser = new RequestParser(request);
			String testRunPK = parser.getParameter("testRunPK");
			String testName = parser.getParameter("testName");
			String testNumber = parser.getParameter("testNumber");
			
			String results = "Can't find raw code coverage XML results for " +testRunPK+ ", " +
					testNumber+ " => " +testName;
			
			TestOutcomeCollection collection = TestOutcomeCollection.lookupByTestRunPK(testRunPK, conn);
			for (TestOutcome outcome : collection) {
				if (outcome.getTestName().equals(testName)) {
					results = outcome.getCodeCoverageXMLResultsAsString();
				}
			}
			
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			
			out.print(results);
			out.flush();
			out.close();
			
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}
}
