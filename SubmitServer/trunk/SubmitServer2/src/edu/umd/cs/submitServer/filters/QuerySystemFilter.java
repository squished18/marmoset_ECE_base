/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Dec 6, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Snapshot;

/**
 * QuerySystemFilter
 * @author jspacco
 */
public class QuerySystemFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
    throws IOException, ServletException
    {
        HttpServletRequest request=(HttpServletRequest)req;
        Connection conn=null;
        
        try {
            conn=getConnection();
            
            // Should make sure we have the projects
            Course course=(Course)request.getAttribute("course");
            List<Project> projectList=Project.lookupAllByCoursePK(course.getCoursePK(), conn);
            request.setAttribute("projectList", projectList);
            
            Project project=(Project)request.getAttribute(PROJECT);
            
            String numLinesChanged=request.getParameter("numLinesChanged");
            getSubmitServerFilterLog().warn("numLinesChanged: " +numLinesChanged);
            if (numLinesChanged!=null) {
                List<Snapshot> snapshotList=Snapshot.lookupAllByProjectPKAndNumLinesChanged(
                    project.getProjectPK(),
                    numLinesChanged,
                    Snapshot.EQ,
                    conn);
                getSubmitServerFilterLog().warn("size of snapshotList: " +snapshotList.size());
                request.setAttribute("snapshotList", snapshotList);
            }
            
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(request,resp);
    }
}
