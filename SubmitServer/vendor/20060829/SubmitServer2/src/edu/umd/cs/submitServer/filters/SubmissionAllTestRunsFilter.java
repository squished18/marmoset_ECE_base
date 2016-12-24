/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 17, 2005
 *
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.BackgroundRetest;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;

/**
 * @author jspacco
 *
 */
public class SubmissionAllTestRunsFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain)
    throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
        
        Connection conn=null;
        try {
            conn=getConnection();
            
            Submission submission = (Submission)request.getAttribute("submission");
            
            Map<String, TestOutcomeCollection> testOutcomeCollectionMap = new HashMap<String, TestOutcomeCollection>();
            List<TestRun> testRunList = (List<TestRun>)request.getAttribute("testRunList");
            for (TestRun testRun : testRunList) {
                TestOutcomeCollection collection = TestOutcomeCollection.lookupByTestRunPK(
                        testRun.getTestRunPK(),
                        conn);
                testOutcomeCollectionMap.put(testRun.getTestRunPK(), collection);
            }
            
            request.setAttribute("testOutcomeCollectionMap", testOutcomeCollectionMap);
            
            Map<String,BackgroundRetest> backgroundRetestMap=
                BackgroundRetest.lookupMapBySubmissionPK(submission.getSubmissionPK(), conn);
            request.setAttribute("backgroundRetestMap", backgroundRetestMap);
        
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(request, response);
    }
}
