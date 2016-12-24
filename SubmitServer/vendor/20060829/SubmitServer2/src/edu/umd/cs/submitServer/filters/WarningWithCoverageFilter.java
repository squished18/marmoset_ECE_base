/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Jul 5, 2005
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

import edu.umd.cs.marmoset.modelClasses.FileNameLineNumberPair;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;

/**
 * @author jspacco
 *
 */
public class WarningWithCoverageFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain)
    throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
        
        Connection conn=null;
        try {
            conn=getConnection();
            
            RequestParser parser = new RequestParser(request);
            String longTestResult = parser.getStringParameter("longTestResult");
            String warningName = parser.getStringParameter("warningName");
            String priority = parser.getStringParameter("priority");
            
            String shortTestResult = parser.getStringParameter("shortTestResult");
            FileNameLineNumberPair pair = TestOutcome.getFileNameLineNumberPair(shortTestResult);
            String fileName = pair.getFileName();
            int lineNumber = pair.getLineNumber();
            
            String testRunPK = parser.getStringParameter("testRunPK");
            
            // Create a FindBugs warning to set as a request attribute.
            // Safer to set this as parameters of the request rather than
            // giving the necessary information to look it up from the database
            // for situations where we re-run only the FindBugs results and don't
            // bother to put them into the database.
            TestOutcome warning = new TestOutcome();
            warning.setTestType(TestOutcome.FINDBUGS_TEST);
            warning.setOutcome(TestOutcome.WARNING);
            warning.setTestRunPK(testRunPK);
            warning.setExceptionClassName(priority);
            warning.setTestName(warningName);
            warning.setLongTestResult(longTestResult);
            warning.setShortTestResult(shortTestResult);
            request.setAttribute("warning", warning);
            
            TestOutcomeCollection testOutcomeCollection = TestOutcomeCollection.lookupByTestRunPK(
                    testRunPK,
                    conn);
            
            getSubmitServerFilterLog().debug(warning);
            getSubmitServerFilterLog().debug("File: " +fileName +" at "+ lineNumber);
            
            testOutcomeCollection = testOutcomeCollection.getTestOutcomesCoveringFileAtLine(fileName, lineNumber);
            request.setAttribute("testOutcomeCollection", testOutcomeCollection);
            getSubmitServerFilterLog().debug("testOutcomeCollection contains " +testOutcomeCollection.size()+ " elements");
        } catch (InvalidRequiredParameterException e) {
            throw new ServletException(e);
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(request, response);
    }
}
