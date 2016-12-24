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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * @author jspacco
 *
 */
public class PrintTestDetailsForDatabase extends SubmitServerServlet
{

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
            

            
            Project project = (Project)request.getAttribute("project");
            
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            
            // get the outcome from the canonical run; we'll use this to retrieve the names of the test cases
            TestOutcomeCollection canonicalCollection = TestOutcomeCollection.lookupCanonicalOutcomesByProjectPK(
                    project.getProjectPK(),
                    conn);

            // format and print the header
            for (TestOutcome outcome : canonicalCollection) {
            	//Skip anything that is not a cardinal test type (public,release,secret)
            	if (!outcome.isCardinalTestType()) continue;
                out.println(outcome.getTestName() + "," + outcome.getPointValue());
            }
                        
            out.flush();
            out.close();
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }
}
