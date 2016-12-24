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
public class PrintGradesForDatabase extends SubmitServerServlet {

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
            Map bestSubmissionMap = (Map)request.getAttribute("bestSubmissionMap");
            
            Project project = (Project)request.getAttribute("project");
            Set<StudentRegistration> registrationSet = (Set<StudentRegistration>)request.getAttribute("studentRegistrationSet");
            
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            
            // get the outcome from the canonical run; we'll use this to retrieve the names of the test cases
            TestOutcomeCollection canonicalCollection = TestOutcomeCollection.lookupCanonicalOutcomesByProjectPK(
                    project.getProjectPK(),
                    conn);
           
            for (StudentRegistration registration : registrationSet) {
            	if (registration.getInstructorLevel() > StudentRegistration.STUDENT_CAPABILITY_LEVEL)
                	continue;
                Submission submission = (Submission)bestSubmissionMap.get(registration.getStudentRegistrationPK());
                
                if (submission != null)
                {
                    if (submission.getStatus().equals(Submission.LATE)) {
                        if (project.getKindOfLatePenalty().equals(Project.CONSTANT))
                        	out.println(registration.getCvsAccount()+",*,-"+project.getLateConstant() + ",Late");
                        else 
                            out.println(registration.getCvsAccount()+",*,*"+project.getLateMultiplier() + ",Late");
                    }
                    else
                        if (submission.getStatus().equals(Submission.VERY_LATE)) 
                             out.println(registration.getCvsAccount()+",*,*0.0,Very Late");
                        
                      
                    
                    TestOutcomeCollection outcomeCollection = TestOutcomeCollection.lookupByTestRunPK(
                            submission.getCurrentTestRunPK(),
                            conn);
                    
                    for (TestOutcome canonicalOutcome : canonicalCollection) {
                        // Skip anything that isn't a cardinal test type (public,release,secret)
                    	if (!canonicalOutcome.isCardinalTestType()) continue;
                        
                    	TestOutcome outcome = outcomeCollection.getTest(canonicalOutcome.getTestName());
                        StringBuffer result = new StringBuffer(registration.getCvsAccount());
                        result.append(",");
                        result.append(canonicalOutcome.getTestName());
                        if (outcome == null)
                        {
                            result.append(",0,Could not run");
                        }
                        else if (outcome.getOutcome().equals(TestOutcome.PASSED))
                        {
                            result.append(",");
                            result.append(canonicalOutcome.getPointValue());
                        }
                        else
                        {
                            result.append(",0,");
                            result.append(outcome.getOutcome());
                        }
                        out.println(result);
                    }
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
    }}
