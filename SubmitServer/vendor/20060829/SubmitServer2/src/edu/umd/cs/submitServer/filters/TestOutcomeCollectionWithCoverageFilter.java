/**
 * 
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.codeCoverage.CoverageStats;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * @author jspacco
 *
 */
public class TestOutcomeCollectionWithCoverageFilter extends SubmitServerFilter {

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
	throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;
		
		TestOutcomeCollection collection = (TestOutcomeCollection)request.getAttribute("testOutcomeCollection");
		Map<String, CoverageStats> testOutcomeToCoverageStatsMap = new HashMap<String, CoverageStats>();
		
		for (TestOutcome outcome : collection) {
			if (!outcome.isCoverageType()) continue;
			if (outcome.isCardinalTestType() || 
					outcome.isStudentTestType())
			{
				testOutcomeToCoverageStatsMap.put(outcome.getKey(), outcome.getCodeCoverageResults().getOverallCoverageStats());
			}
		}
		request.setAttribute("testOutcomeToCoverageStatsMap", testOutcomeToCoverageStatsMap);
		
		chain.doFilter(request, response);
	}

}
