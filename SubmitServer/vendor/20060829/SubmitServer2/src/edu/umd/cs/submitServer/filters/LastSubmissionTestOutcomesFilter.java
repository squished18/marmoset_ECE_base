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
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * @author jspacco
 *
 * Expects a studentRegistrationSet to be set as a request attribute.
 * TODO take extensions into account
 */
public class LastSubmissionTestOutcomesFilter extends SubmitServerFilter
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
            
            
            Set registrationSet = (Set)request.getAttribute(STUDENT_REGISTRATION_SET);
            
            Map lastSubmission =  (Map) request.getAttribute("lastSubmission");

            Map<String, TestOutcomeCollection> lastOutcomeCollection = new HashMap<String, TestOutcomeCollection>();
            
            for(Iterator i = lastSubmission.values().iterator(); i.hasNext(); ) {
            		Submission submission = (Submission) i.next();
            		if (submission.getCurrentTestRunPK() == null) continue;
            		TestOutcomeCollection outcome = TestOutcomeCollection.lookupByTestRunPK(submission.getCurrentTestRunPK(), conn);
            		if (outcome == null) continue;
            		lastOutcomeCollection.put(submission.getStudentRegistrationPK(), outcome);
            }
            Set<String> studentsWhoHaveOutcomes = lastOutcomeCollection.keySet();
            for(Iterator i = registrationSet.iterator(); i.hasNext(); ) {
            	StudentRegistration studentRegistration = (StudentRegistration) i.next();
            	if (!studentsWhoHaveOutcomes.contains(studentRegistration.getStudentRegistrationPK()))
            		i.remove();
            }
            request.setAttribute("lastOutcomeCollection", lastOutcomeCollection);
           

        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(request, response); 
    }
}
