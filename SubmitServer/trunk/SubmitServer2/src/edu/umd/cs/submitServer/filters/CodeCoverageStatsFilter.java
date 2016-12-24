package edu.umd.cs.submitServer.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestProperties;

/**
 * Retrieves overall code coverage stats from the testOutcomeCollection and stores
 * the results as request attributes.  Only works for Java code.
 * @author jspacco
 *
 */
public class CodeCoverageStatsFilter extends SubmitServerFilter {

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
	throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;
		//Project project = (Project)request.getAttribute("project");
        TestProperties testProperties=(TestProperties)request.getAttribute(TEST_PROPERTIES);
		if (testProperties.isJava()) {
			TestOutcomeCollection collection = (TestOutcomeCollection)request.getAttribute("testOutcomeCollection");
			
			if (collection!=null) {
				boolean hasCodeCoverageResults=false;
				CodeCoverageResults publicCoverageResults = collection.getOverallCoverageResultsForPublicTests();
				CodeCoverageResults releaseCoverageResults = collection.getOverallCoverageResultsForReleaseTests();
				CodeCoverageResults studentCoverageResults = collection.getOverallCoverageResultsForStudentTests();
				CodeCoverageResults cardinalCoverageResults = collection.getOverallCoverageResultsForCardinalTests();
				CodeCoverageResults releaseUniqueResults = new CodeCoverageResults();
                
				if (studentCoverageResults.size() > 0) {
					request.setAttribute("studentCoverageStats", studentCoverageResults.getOverallCoverageStats());
					hasCodeCoverageResults=true;
				}
				if (releaseCoverageResults.size() > 0) {
					request.setAttribute("releaseCoverageStats", releaseCoverageResults.getOverallCoverageStats());
					hasCodeCoverageResults=true;
				}
				if (cardinalCoverageResults.size() > 0) {
					request.setAttribute("cardinalCoverageStats", cardinalCoverageResults.getOverallCoverageStats());
					hasCodeCoverageResults=true;
				}
				if (publicCoverageResults.size() > 0) {
					request.setAttribute("publicCoverageStats", publicCoverageResults.getOverallCoverageStats());
					hasCodeCoverageResults=true;
				}
                				
				CodeCoverageResults publicAndStudentCoverageResults = new CodeCoverageResults(publicCoverageResults);
				publicAndStudentCoverageResults.union(studentCoverageResults);
				if (publicAndStudentCoverageResults.size() > 0) {
					request.setAttribute("publicAndStudentCoverageStats", publicAndStudentCoverageResults.getOverallCoverageStats());
					hasCodeCoverageResults=true;
				}
                if (releaseCoverageResults.size() > 0) {
                    request.setAttribute("releaseCoverageStats", releaseCoverageResults.getOverallCoverageStats());
                    hasCodeCoverageResults=true;
                    releaseUniqueResults=new CodeCoverageResults(releaseCoverageResults);
                    releaseUniqueResults.excluding(publicAndStudentCoverageResults);
                    request.setAttribute("releaseUniqueStats", releaseUniqueResults.getOverallCoverageStats());
                }
				
				CodeCoverageResults intersectionCoverageResults = new CodeCoverageResults(studentCoverageResults);
				intersectionCoverageResults.union(publicAndStudentCoverageResults);
				intersectionCoverageResults.intersect(cardinalCoverageResults);
				if (intersectionCoverageResults.size() > 0) {
					request.setAttribute("intersectionCoverageStats", intersectionCoverageResults.getOverallCoverageStats());
					hasCodeCoverageResults=true;
				}
				request.setAttribute("hasCodeCoverageResults", hasCodeCoverageResults);
			}
		}
		chain.doFilter(request, response);
	}
}
