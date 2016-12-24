/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 21, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.submitServer.SubmitServerConstants;

/**
 * @author jspacco
 *
 * TODO take extensions into account
 */
public class SubmissionListTestOutcomesFilter extends SubmitServerFilter
{
 
    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;

        Connection conn=null;
        try {
            conn=getConnection();
            
            
            List submissionList = (List)request.getAttribute(SUBMISSION_LIST);


            Map<String, TestOutcomeCollection> testOutcomeMap = new HashMap<String, TestOutcomeCollection>();
            
            for(Iterator i = submissionList.iterator(); i.hasNext(); ) {
            		Submission submission = (Submission) i.next();
            		if (submission.getCurrentTestRunPK() == null) continue;
            		TestOutcomeCollection outcome = TestOutcomeCollection.lookupByTestRunPK(submission.getCurrentTestRunPK(), conn);
            		if (outcome == null) continue;
            		testOutcomeMap.put(submission.getSubmissionPK(), outcome);
            }

            request.setAttribute(TEST_OUTCOMES_MAP, testOutcomeMap);
           

        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(request, response); 
    }
}
