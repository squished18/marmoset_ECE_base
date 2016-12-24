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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
public class PrintGrades extends SubmitServerServlet
{

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
            
            // get the project and all the student registrations
            Map<String, Submission> bestSubmissionMap = (Map<String, Submission>)request.getAttribute("bestSubmissionMap");
            
            Project project = (Project)request.getAttribute("project");
            Set<StudentRegistration> registrationSet = (Set<StudentRegistration>)request.getAttribute("studentRegistrationSet");
            
            response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "attachment; filename=project-" + project.getProjectNumber()+ "-grades.csv");
            response.setHeader("Cache-Control","private");
            response.setHeader("Pragma","IE is broken");
            PrintWriter out = response.getWriter();
            
            // get the outcome from the canonical run; we'll use this to retrieve the names of the test cases
            TestOutcomeCollection canonicalCollection = TestOutcomeCollection.lookupCanonicalOutcomesByProjectPK(
                    project.getProjectPK(),
                    conn);

            // format and print the header
            String header = "cvsAccount,total,status";
            for (Iterator ii=canonicalCollection.iterator(); ii.hasNext();)
            {
                TestOutcome outcome = (TestOutcome)ii.next();
                if (outcome.getTestType().equals(TestOutcome.BUILD_TEST)) continue;
                header += "," +outcome.getTestType() +"_"+ outcome.getTestName();
            }
            out.println(header);
            
            for (StudentRegistration registration : registrationSet) {
            	Submission submission = bestSubmissionMap.get(registration.getStudentRegistrationPK());
                
                if (submission != null)
                {
                    String result = registration.getCvsAccount() +","+ 
                    	submission.getAdjustedScore() +","+
                    	submission.getStatus();
                    
                    TestOutcomeCollection outcomeCollection = TestOutcomeCollection.lookupByTestRunPK(
                            submission.getCurrentTestRunPK(),
                            conn);
                
                    for (TestOutcome outcome : outcomeCollection) {
                        // Skip anything that is not a cardinal test type (public,release,secret)
                    	if (!outcome.isCardinalTestType()) continue;
                        if (outcome.getOutcome().equals(TestOutcome.PASSED))
                        {
                            result += "," +outcome.getPointValue();
                        }
                        else
                        {
                            result += ",0";
                        }
                    }
                    out.println(result);
                }
            }
            
            out.flush();
            out.close();
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }
}
