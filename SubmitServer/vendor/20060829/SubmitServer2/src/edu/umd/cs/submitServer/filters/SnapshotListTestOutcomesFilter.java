/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 17, 2005
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

import edu.umd.cs.marmoset.modelClasses.Snapshot;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * @author jspacco
 *
 */
public class SnapshotListTestOutcomesFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;

        Connection conn=null;
        try {
            conn=getConnection();
            
            
            List snapshotList = (List)request.getAttribute("snapshotList");


            Map<String, TestOutcomeCollection> snapshotOutcomeCollection = new HashMap<String, TestOutcomeCollection>();
            
            for(Iterator i = snapshotList.iterator(); i.hasNext(); ) {
            		Snapshot snapshot = (Snapshot) i.next();
            		if (snapshot.getCurrentTestRunPK() == null) continue;
            		TestOutcomeCollection outcome = TestOutcomeCollection.lookupByTestRunPK(snapshot.getCurrentTestRunPK(), conn);
            		if (outcome == null) continue;
            		snapshotOutcomeCollection.put(snapshot.getSubmissionPK(), outcome);
            }

            request.setAttribute("snapshotOutcomeCollection", snapshotOutcomeCollection);
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }

        chain.doFilter(request, response);
    }

}
