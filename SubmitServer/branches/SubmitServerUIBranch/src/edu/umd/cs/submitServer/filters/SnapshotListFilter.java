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
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Snapshot;

/**
 * @author jspacco
 *
 */
public class SnapshotListFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        
        Connection conn=null;
        try {
            String projectPK = request.getParameter("projectPK");
            String studentPK = request.getParameter("studentPK");
            if (projectPK == null)
                throw new ServletException("projectPK == null");
            
            conn=getConnection();
            List snapshotList = Snapshot.lookupAllByProjectPKAndStudentPK(projectPK, studentPK, conn);
            Map testRunMap = Snapshot.getCurrentTestRunMapByProjectPKAndStudentPK(
                    projectPK,
                    studentPK,
                    conn);
            request.setAttribute("snapshotList", snapshotList);
            request.setAttribute("testRunMap", testRunMap);
            
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        
        chain.doFilter(request, response);
    }

}
