/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Apr 21, 2005
 *
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.submitServer.RequestParser;

/**
 * @author jspacco
 *
 */
public class QueryTestOutcomesFilter extends SubmitServerFilter
{
    public static final String PUBLIC_OR_RELEASE_TESTS = "tests";

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
            conn=getConnection();
            
            RequestParser parser = new RequestParser(request);
            String testType = parser.getParameter("testType");
            String testName = parser.getParameter("testName");
            String optionalQuality = parser.getParameter("optionalQuality");
            String offset = parser.getParameter("offset");
            if (offset == null) {
                offset = "0";
                request.setAttribute("offset", offset);
            }
            String numRecords = parser.getParameter("numRecords");
            if (numRecords == null) {
                numRecords = "100";
                request.setAttribute("numRecords", numRecords);
            }
            
            if (testType != null)
            {
                List<TestOutcome> outcomeList = null;
                
                if (testType.equals(PUBLIC_OR_RELEASE_TESTS))
                    outcomeList = lookupPublicAndReleaseTests(optionalQuality, 
                            offset,
                            numRecords,
                            conn);
                else if (testType.equals(TestOutcome.FINDBUGS_TEST) ||
                        testType.equals(TestOutcome.PMD_TEST))
                {
                    if (optionalQuality == null || optionalQuality.equals(""))
                        outcomeList = lookupWarningsByTypeAndName(testType, testName, offset, numRecords, conn);
                    else
                        outcomeList = lookupWarningsByTypeNameAndPriority(
                            testType,
                            testName,
                            optionalQuality,
                            offset,
                            numRecords,
                            conn);
                }

                request.setAttribute("testType", testType);
                request.setAttribute("testName", testName);
                request.setAttribute("optionalQuality", optionalQuality);
                request.setAttribute("outcomeList", outcomeList);
                request.setAttribute("offset", offset);
                request.setAttribute("numRecord", numRecords);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(request, response);
    }

    private static List<TestOutcome> lookupPublicAndReleaseTests(
            String exceptionClassName,
            String offset,
            String numRecords,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +TestOutcome.ATTRIBUTES+
            " FROM " +TestOutcome.TABLE_NAME+
            " WHERE (test_type = 'public' OR test_type = 'release')" +
            " AND exception_class_name LIKE ? " +
            " AND outcome = 'failed' " +
            " LIMIT ? , ? ";
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(query);
            setStringOrWildcard(stmt, 1, exceptionClassName);
            stmt.setString(2, offset);
            stmt.setString(3, offset);

            //System.out.println(stmt);

            return getListFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    private static List<TestOutcome> lookupWarningsByTypeAndName(
            String testType,
            String testName,
            String offset,
            String numRecords,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +TestOutcome.ATTRIBUTES+
            " FROM " +TestOutcome.TABLE_NAME+
            " WHERE test_type = ? " +
            " AND test_name LIKE ? "+
            " LIMIT ?, ? ";
        
        System.out.println(query);
        
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, testType);
            stmt.setString(2, offset);
            stmt.setString(3, numRecords);

            setStringOrWildcard(stmt, 2, testName);

            return getListFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    /**
     * Looks up a list of warnings with the given type, name and priority.  If testName
     * is null, then a wildcard will be inserted.  testType and priority <b>cannot</b> be null.
     * @param testType the type of test (currently 'findbugs' or 'pmd')
     * @param testName the name of the test, or null for a wildcard
     * @param priority the priority of the warnings
     * @param offset the offset into the returned list of warnings
     * @param numRecords the number of warning records to return
     * @param conn the connection to the database
     * @return a list of the first numRecords warnings starting at offset with the given
     * testType, testName and priority.
     * @throws SQLException
     */
    private static List<TestOutcome> lookupWarningsByTypeNameAndPriority(
            String testType,
            String testName,
            String priority,
            String offset,
            String numRecords,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +TestOutcome.ATTRIBUTES+
            " FROM " +TestOutcome.TABLE_NAME+
            " WHERE test_type = ? " +
            " AND test_name LIKE ? " +
            " AND exception_class_name = ? " +
            " LIMIT ?, ? ";
        
        System.out.println(query);
        
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, testType);
            stmt.setString(2, offset);
            stmt.setString(3, numRecords);

            setStringOrWildcard(stmt, 2, testName);
            stmt.setString(3, priority);
            
            System.out.println(stmt);

            return getListFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    private static void setStringOrWildcard(PreparedStatement stmt, int index, String value)
    throws SQLException
    {
        if (value != null && !value.equals(""))
            stmt.setString(index, value);
        else
            stmt.setString(index, "%");
    }

    private static List<TestOutcome> getListFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        ResultSet rs = stmt.executeQuery();
        
        List<TestOutcome> result = new LinkedList<TestOutcome>();
        while (rs.next())
        {
            TestOutcome outcome = new TestOutcome();
            outcome.fetchValues(rs, 1);
            result.add(outcome);
        }
        return result;
    }
}
