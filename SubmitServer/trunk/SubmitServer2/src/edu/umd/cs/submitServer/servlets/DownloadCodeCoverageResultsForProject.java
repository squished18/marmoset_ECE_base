package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

public class DownloadCodeCoverageResultsForProject extends SubmitServerServlet {

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
			
			Project project = (Project)request.getAttribute("project");
			Set<StudentRegistration> registrationSet = (Set<StudentRegistration>)request.getAttribute("studentRegistrationSet");
			Map<String,Submission> bestSubmissionMap = (Map<String,Submission>)request.getAttribute("bestSubmissionMap");
			
			response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "attachment; filename=coverage-results-" +
            		project.getProjectNumber()+"-"+
            		project.getProjectJarfilePK()+".csv");
            response.setHeader("Cache-Control","private");
            response.setHeader("Pragma","IE is broken");
            PrintWriter out = response.getWriter();
            
            String extraTabs = "\t\t\t";
            out.println("class_acct\tstatus\t" +
            		"studentTests\t" +
            		"student" +extraTabs+
            		"public" +extraTabs+
            		"public_and_student" +extraTabs+
            		"public_release_secret" +extraTabs+
            		"public_release_intersect_public_student" +extraTabs+
            		"totals" +extraTabs);
            String headers = "stmts\t" +
    			"cond\t" +
    			"methods";
            String totalHeaders = "stmts\t" +
            	"cond\t" +
            	"methods";
            out.println("class_acct\tstatus\t" +"studentTests\t" +headers +"\t" +
            		headers +"\t"+
            		headers +"\t"+
            		headers +"\t"+
            		headers +"\t"+
            		totalHeaders);
            
            for (StudentRegistration registration : registrationSet) {
            	Submission submission = bestSubmissionMap.get(registration.getStudentRegistrationPK());
            	
            	if (submission==null) {
            		continue;
            	}
            	out.print(registration.getCvsAccount() +"\t"+ submission.getStatus()+"\t"); 
				
				TestOutcomeCollection collection = TestOutcomeCollection.lookupByTestRunPK(submission.getCurrentTestRunPK(), conn);
				out.print(collection.getNumStudentWrittenTests() +"\t");
				// TODO refactor the next chunk of lines so that it uses the method available
				// in TestOutcomeCollection and then a static method for display
				CodeCoverageResults publicCoverageResults = collection.getOverallCoverageResultsForPublicTests();
				//CodeCoverageResults releaseCoverageResults = collection.getOverallCoverageResultsForReleaseTests();
				CodeCoverageResults studentCoverageResults = collection.getOverallCoverageResultsForStudentTests();
				CodeCoverageResults cardinalCoverageResults = collection.getOverallCoverageResultsForCardinalTests();
								
				CodeCoverageResults publicAndStudentCoverageResults = new CodeCoverageResults(publicCoverageResults);
				publicAndStudentCoverageResults.union(studentCoverageResults);
				
				CodeCoverageResults intersectionCoverageResults = new CodeCoverageResults(studentCoverageResults);
				intersectionCoverageResults.union(publicAndStudentCoverageResults);
				intersectionCoverageResults.intersect(cardinalCoverageResults);
				
				// Print just the values
				out.print(studentCoverageResults.getOverallCoverageStats().getCSVValues()+"\t");
				out.print(publicCoverageResults.getOverallCoverageStats().getCSVValues()+"\t");
				out.print(publicAndStudentCoverageResults.getOverallCoverageStats().getCSVValues()+"\t");
				out.print(cardinalCoverageResults.getOverallCoverageStats().getCSVValues()+"\t");
				out.print(intersectionCoverageResults.getOverallCoverageStats().getCSVValues()+"\t");
				// Now print the totals once
				out.print(cardinalCoverageResults.getOverallCoverageStats().getCSVTotals() +"\n");
				
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		
	}

}
