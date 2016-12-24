/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 12, 2005
 *
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * @author jspacco
 *
 */
public class TestResultHistory extends SubmitServerServlet
{

    static class MutableInt {
        int value = 0;
    }
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
            PrintWriter out = response.getWriter();
            
            Project project = (Project)request.getAttribute("project");
            Timestamp mostRecent = project.getLate();
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (now.compareTo(mostRecent) < 0) {
                // haven't reached late deadline yet
                mostRecent = now;
            }
            int numDays = 7;
            HashMap<String, TestOutcomeCollection> testRuns = new HashMap<String, TestOutcomeCollection>();

            Map<String, StudentRegistration> studentRegistrationMap = (Map<String, StudentRegistration>)request.getAttribute("studentRegistrationMap");
            
            // TODO turn this into a map
            TestOutcomeCollection canonicalTestOutcomeCollection 
                = (TestOutcomeCollection)request.getAttribute("canonicalTestOutcomeCollection");

            Timestamp when[] = new Timestamp[numDays];
            Map asOf[]  = new Map[numDays];
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            out.println("<table><tr><th>as of " + timeFormat.format(mostRecent));
            for(int d = 0; d < numDays; d++) {
                when[d] = new Timestamp(mostRecent.getTime() - d * 24L*3600*1000);
                out.println("<th class=\"number\">");
                out.println(dateFormat.format(when[d]));
                
                Map lastSubmission = Submission.lookupLastSubmissionBeforeTimestampMap(project, when[d], conn);
                Map<String, MutableInt> histogram = new LinkedHashMap<String, MutableInt>();
                asOf[d] = histogram;
                for(Iterator i = lastSubmission.values().iterator(); i.hasNext(); ) {
                    Submission submission = (Submission) i.next();
                    if (submission.getCurrentTestRunPK() == null) continue;
                    StudentRegistration studentRegistration = studentRegistrationMap.get(
                            submission.getStudentRegistrationPK());
                    if (studentRegistration == null || studentRegistration.getInstructorLevel() > StudentRegistration.STUDENT_CAPABILITY_LEVEL)
                    	continue;
                    TestOutcomeCollection outcome;
                    if (testRuns.containsKey(submission.getCurrentTestRunPK()))
                            outcome = testRuns.get(submission.getCurrentTestRunPK());
                    else {
                        outcome = TestOutcomeCollection.lookupByTestRunPK(submission.getCurrentTestRunPK(), conn);
                        testRuns.put(submission.getCurrentTestRunPK(), outcome);
                    }
                    for(Iterator j = outcome.getAllTestOutcomes().iterator(); j.hasNext(); ) {
                        TestOutcome test = (TestOutcome) j.next();
                        if (test.getOutcome().equals(TestOutcome.PASSED)) {
                            MutableInt count = histogram.get(test.getTestName());
                            if (count == null) {
                                count = new MutableInt();
                                histogram.put(test.getTestName(), count);
                            }
                            count.value++;
                        }
                    }
                    
                }
                
            }
            out.println("</tr>");
            int count = 0;
            for(Iterator i = canonicalTestOutcomeCollection.getAllTestOutcomes().iterator(); i.hasNext(); ) {
                TestOutcome outcome = (TestOutcome) i.next();
                String testName = outcome.getTestName();
                out.println("<tr class=\"r" + count%2 + "\"><td class=\"description\">" + outcome.getShortTestName());
                count++;
                for(int d = 0; d < numDays; d++) {
                    MutableInt num = (MutableInt) asOf[d].get(testName);
                    if (num == null) out.println("<td>&nbsp;");
                    else out.println("<td class=\"number\">"+num.value);
                }
                out.println("</tr>");
            }

            out.println("</table>");
            
            out.flush();

        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }
}
