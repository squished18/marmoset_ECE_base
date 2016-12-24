/*
 * Created on Jan 19, 2005
 *
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * Requires a projectPK.
 * 
 * Stores a list of the test-setups into the request.
 * Also stores a list of the canonical submissions into the request.
 */
public class AllProjectTestSetups extends SubmitServerFilter {

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		Project project = (Project) request.getAttribute(PROJECT);

		Connection conn = null;
		try {
			conn = getConnection();
			Collection allTestSetups = ProjectJarfile.lookupAllByProjectPK(
					project.getProjectPK(),
                    conn);
			request.setAttribute("allTestSetups", allTestSetups);
            
            Collection<Submission> canonicalSubmissions=Submission.lookupAllByStudentRegistrationPKAndProjectPK(
                project.getCanonicalStudentRegistrationPK(),
                project.getProjectPK(),
                conn);
            request.setAttribute("canonicalSubmissions", canonicalSubmissions);

		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}

}