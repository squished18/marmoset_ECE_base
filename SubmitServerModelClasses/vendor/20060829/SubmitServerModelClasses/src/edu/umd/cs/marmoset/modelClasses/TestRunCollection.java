/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 20, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * @deprecated
 * @author jspacco
 */
public class TestRunCollection
{
    private List<TestRun> testRuns = new LinkedList<TestRun>();
    
    public void add(TestRun testRun)
    {
        testRuns.add(testRun);
    }
    
    public void addAll(TestRunCollection other)
    {
    	testRuns.addAll(other.getCollection());
    }
    
    public List<TestRun> getCollection()
    {
        return testRuns;
    }
    
    public static TestRunCollection lookupCurrentByProjectPK(String projectPK, Connection conn)
    throws SQLException
    {
    	String query =
    		" SELECT " +TestRun.ATTRIBUTES+
    		" FROM test_runs, submissions " +
    		" WHERE test_runs.test_run_pk = submissions.current_test_run_pk " +
    		" AND submissions.project_pk = ? ";
    	PreparedStatement stmt=conn.prepareStatement(query);
    	stmt.setString(1, projectPK);
    	
    	return getAllFromPreparedStatement(stmt);
    }
    
    public static TestRunCollection lookupAllByProjectPK(String projectPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +TestRun.ATTRIBUTES+
            " FROM test_runs, projects " +
            " WHERE test_runs.project_jarfile_pk = projects.project_jarfile_pk " +
            " AND projects.project_pk = ? ";
        
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1, projectPK);
        
        return getAllFromPreparedStatement(stmt);
    }
    
    public static TestRunCollection lookupAllBySubmissionPK(String submissionPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +TestRun.ATTRIBUTES+
            " FROM test_runs " +
            " WHERE test_runs.submission_pk = ? " +
            " ORDER BY test_run_pk DESC ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, submissionPK);

        return getAllFromPreparedStatement(stmt);
    }
    
    private static TestRunCollection getAllFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        ResultSet rs = stmt.executeQuery();
        try {
            TestRunCollection collection = new TestRunCollection();
            while (rs.next())
            {
                TestRun testRun = new TestRun();
                testRun.fetchValues(rs, 1);
                collection.add(testRun);
            }
            return collection;
        } finally {
            Queries.closeStatement(stmt);
        }
    }
}
