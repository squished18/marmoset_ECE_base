/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Nov 5, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import edu.umd.cs.marmoset.modelClasses.BackgroundRetest;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * FailedBackgroundRetestFilter
 * @author jspacco
 */
public class FailedBackgroundRetestFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,FilterChain chain)
    throws IOException, ServletException
    {
        Connection conn=null;
        try {
            conn=getConnection();
            
            Project project = (Project)req.getAttribute(PROJECT);
            
            List<Submission> failedBackgroundRetestSubmissionList = new LinkedList<Submission>();
            Map<String,BackgroundRetest> backgroundRetestMap = new HashMap<String,BackgroundRetest>();
            Submission.lookupAllWithFailedBackgroundRetestsByProjectPK(
                project.getProjectPK(),
                failedBackgroundRetestSubmissionList,
                backgroundRetestMap,
                conn);
            req.setAttribute("failedBackgroundRetestSubmissionList", failedBackgroundRetestSubmissionList);
            req.setAttribute("backgroundRetestMap", backgroundRetestMap);
            
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(req,resp);
    }

}
