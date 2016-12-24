/*
 * Created on Jan 19, 2005
 *
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

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;

/**
 * Requires a projectJarFile
 * 
 * This filter stores canonicalTestRun, canonicalTestOutcomeCollection, and
 * canonicalTestOutcomeMap in the request.
 */
public class CanonicalTestRunFilter extends SubmitServerFilter {

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        Project project = (Project) request.getAttribute(PROJECT);


        Connection conn = null;
        try {
            conn = getConnection();
            ProjectJarfile projectJarfile = ProjectJarfile.lookupByProjectJarfilePK(project.getProjectJarfilePK(), conn);
            if (projectJarfile != null) {
            TestRun canonicalTestRun = TestRun.lookupByTestRunPK(projectJarfile
                    .getTestRunPK(), conn);
            // System.out.println("Canonical test run pk: " + canonicalTestRun.getTestRunPK());
            TestOutcomeCollection canonicalTestOutcomeCollection = TestOutcomeCollection
                    .lookupByTestRunPK(canonicalTestRun.getTestRunPK(), conn);
            Map<String, TestOutcome> canonicalTestOutcomeMap = new HashMap<String, TestOutcome>();
            List canonicalTestList = canonicalTestOutcomeCollection.getAllTestOutcomes();
            for (Iterator i = canonicalTestList.iterator(); i
                    .hasNext();) {
                TestOutcome testOutcome = (TestOutcome) i.next();
                canonicalTestOutcomeMap.put(testOutcome.getTestName(), testOutcome);
            }
            request.setAttribute("canonicalJarfile", projectJarfile);
            request.setAttribute("canonicalTestRun", canonicalTestRun);
            request.setAttribute("canonicalTestOutcomeCollection",
                    canonicalTestOutcomeCollection);
            request.setAttribute("canonicalTestOutcomeMap", canonicalTestOutcomeMap);
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(request, response);
    }

}