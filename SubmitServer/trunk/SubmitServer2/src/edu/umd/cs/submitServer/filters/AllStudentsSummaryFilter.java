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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.BestSubmissionPolicy;
import edu.umd.cs.submitServer.DefaultBestSubmissionPolicy;
import edu.umd.cs.submitServer.ReleaseTestAwareBestSubmissionPolicy;

/**
 * @author jspacco
 *
 * Expects a studentRegistrationSet to be set as a request attribute.
 */
public class AllStudentsSummaryFilter extends SubmitServerFilter
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
            
            Project project = (Project)request.getAttribute(PROJECT);
            
            Set<StudentRegistration> registrationSet = (Set<StudentRegistration>)request.getAttribute(STUDENT_REGISTRATION_SET);
            
            // null should return the DefaultBestSubmissionPolicy.
            BestSubmissionPolicy bestSubmissionPolicy=getBestSubmissionPolicy(project.getBestSubmissionPolicy());
            
            Map<String, Submission> lastSubmission = bestSubmissionPolicy.lookupLastSubmissionMap(project, conn);
            Map<String, Submission> lastOnTime = bestSubmissionPolicy.lookupLastOntimeSubmissionMap(project, conn);
            Map<String, Submission> lastLate = bestSubmissionPolicy.lookupLastLateSubmissionMap(project, conn);
            Map<String, Submission> lastVeryLate = bestSubmissionPolicy.lookupLastVeryLateSubmissionMap(project, conn);

            request.setAttribute("lastSubmission", lastSubmission);
            request.setAttribute("lastOnTime", lastOnTime);
            request.setAttribute(LAST_LATE, lastLate);
            request.setAttribute("lastVeryLate", lastVeryLate);
            
            Map<String,Submission> bestSubmissionMap = bestSubmissionPolicy.getBestSubmissionMap(
                    registrationSet,
                    lastOnTime,
                    lastLate);
            request.setAttribute("bestSubmissionMap", bestSubmissionMap);

            String sortKey = request.getParameter("sortKey");
            if ("time".equals(sortKey)) {
                TreeSet<StudentRegistration> sortedByTime  = new TreeSet<StudentRegistration>(StudentRegistration.getSubmissionViaTimestampComparator(lastSubmission));
                sortedByTime.addAll(registrationSet);
                request.setAttribute("studentRegistrationSet", sortedByTime);
            }
            if ("score".equals(sortKey)) {
                TreeSet<StudentRegistration> sortedByScore  = new TreeSet<StudentRegistration>(StudentRegistration.getSubmissionViaMappedValuesComparator(lastSubmission));
                sortedByScore.addAll(registrationSet);
                request.setAttribute("studentRegistrationSet", sortedByScore);
            }
           
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(request, response); 
    }
}
