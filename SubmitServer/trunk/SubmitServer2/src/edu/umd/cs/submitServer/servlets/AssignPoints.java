/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 25, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;

/**
 * @author jspacco
 * 
 */
public class AssignPoints extends SubmitServerServlet {

    /**
     * The doPost method of the servlet. <br>
     * 
     * This method is called when a form has its tag value method equals to
     * post.
     * 
     * @param request
     *            the request send by the client to the server
     * @param response
     *            the response send by the server to the client
     * @throws ServletException
     *             if an error occurred
     * @throws IOException
     *             if an error occurred
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        TestOutcomeCollection testOutcomeCollection = (TestOutcomeCollection) request
                .getAttribute("testOutcomeCollection");
        ProjectJarfile projectJarfile = (ProjectJarfile) request
                .getAttribute("projectJarfile");
        Project project = (Project) request.getAttribute("project");
        TestRun testRun = (TestRun) request.getAttribute("testRun");
        String comment = request.getParameter("comment");
         
        // Create a map of testNames to corresponding testOutcomes
        Map<String, TestOutcome> testOutcomes = new HashMap<String, TestOutcome>();
        for (TestOutcome testOutcome : testOutcomeCollection.getAllTestOutcomes()) {
            testOutcomes.put(testOutcome.getTestName(), testOutcome);
        }

        Connection conn = null;
        boolean transactionSuccess = false;
     
        try {
            conn = getConnection();
            
            ProjectJarfile currentTestingSetup = ProjectJarfile.lookupByProjectJarfilePK(project.getProjectJarfilePK(), conn);
 
            // Update point values for cardinal test types (PUBLIC, RELEASE, SECRET) in the canonical submission
            for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
                String pName = (String) e.nextElement();
                if (testOutcomes.containsKey(pName)) {
                    TestOutcome testOutcome = testOutcomes.get(pName);
                    String pValue = request.getParameter(pName);
                    testOutcome.setPointValue(Integer.parseInt(pValue));
                }
            }
            
            // Now perform the batch-update
            testOutcomeCollection.batchUpdatePointValues(conn);
            
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

            if (currentTestingSetup == null)
                projectJarfile.setVersion(1);
            else {
                
                if (projectJarfile.getProjectJarfilePK().equals( currentTestingSetup.getProjectJarfilePK())) {
                	getSubmitServerServletLog().debug("Huh???");
                    getSubmitServerServletLog().debug("Assigning points to project " + project.getProjectNumber() + ", testing setup PK " + projectJarfile.getProjectJarfilePK());
                    getSubmitServerServletLog().debug("Test run " + testRun.getTestRunPK());
                    getSubmitServerServletLog().debug("old active setup is testing setup PK " + currentTestingSetup.getProjectJarfilePK());
                }
         
                projectJarfile.setVersion(currentTestingSetup.getVersion()+1);
                currentTestingSetup.setJarfileStatus(ProjectJarfile.INACTIVE);
                currentTestingSetup.update(conn);
            }
            projectJarfile.setComment(comment);
            projectJarfile.setJarfileStatus(ProjectJarfile.ACTIVE);
            project.setProjectJarfilePK(projectJarfile.getProjectJarfilePK());
            projectJarfile.update(conn);
            project.update(conn);
            conn.commit(); transactionSuccess = true;

        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
        }
        String redirectUrl = request.getContextPath()
                + "/view/instructor/projectUtilities.jsp?projectPK=" + project.getProjectPK();
        response.sendRedirect(redirectUrl);
    }

}
