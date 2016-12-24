package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;

/**
 * UpdatePostDeadlineOutcomeVisibility
 * @author jspacco
 */
public class UpdatePostDeadlineOutcomeVisibility extends SubmitServerServlet {

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException 
	{
		Connection conn = null;
        try {
            conn = getConnection();

            String newPostDeadlineOutcomeVisibility = (String)request.getParameter("newPostDeadlineOutcomeVisibility");
            getSubmitServerServletLog().debug("newPostDeadlineOUtcomeVisibility = " +newPostDeadlineOutcomeVisibility);
            if (!newPostDeadlineOutcomeVisibility.equals(Project.POST_DEADLINE_OUTCOME_VISIBILITY_EVERYTHING) &&
            		!newPostDeadlineOutcomeVisibility.equals(Project.POST_DEADLINE_OUTCOME_VISIBILITY_NOTHING)) {
            	throw new ServletException("Only valid settings for the post-deadline " +
            			"outcome visibility of a project are '" +
            			Project.POST_DEADLINE_OUTCOME_VISIBILITY_EVERYTHING+"' and '" 
            			+Project.POST_DEADLINE_OUTCOME_VISIBILITY_NOTHING +"'");
            }
            // TODO check the project deadline
            
            Project project = (Project)request.getAttribute("project");
            String currentVisibility = project.getPostDeadlineOutcomeVisibility();
            if (!newPostDeadlineOutcomeVisibility.equals(currentVisibility)) {
            	project.setPostDeadlineOutcomeVisibility(newPostDeadlineOutcomeVisibility);
            	project.update(conn);
            }
            String target = request.getContextPath()+"/view/instructor/projectUtilities.jsp?projectPK=" +project.getProjectPK();
            response.sendRedirect(target);
        } catch (SQLException e) {
        	throw new ServletException(e);
        } finally {
        	releaseConnection(conn);
        }
		
	}

}
