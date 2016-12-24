/*
 * Created on Jan 19, 2005
 *
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.marmoset.modelClasses.TestRun;

/**
 * Requires a testRun as an attribute
 * 
 * Adds a projectJarfile
 * 
 * This filter stores studentRegistration, student, course, project,
 * studentSubmitStatus and submissionList attributes in the request.
 */
public class ProjectJarfileFilter extends SubmitServerFilter {

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        TestRun testRun = (TestRun) request.getAttribute("testRun");
        
        String projectJarFilePK;
        if (testRun != null) projectJarFilePK = testRun.getProjectJarfilePK();
        else {
            Project project = (Project) request.getAttribute(PROJECT);
            projectJarFilePK = project.getProjectJarfilePK();
        }
        Connection conn = null;
        try {
            conn = getConnection();

            ProjectJarfile projectJarfile = ProjectJarfile.lookupByProjectJarfilePK(
                    projectJarFilePK, conn);

            if (projectJarfile != null)
                request.setAttribute("projectJarfile", projectJarfile);

        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }

        chain.doFilter(request, response);
    }

}
