/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 10, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jspacco
 *
 */
public class TestRun implements ITestSummary, Cloneable
{
    private String testRunPK;
    private String projectJarfilePK;
    private String submissionPK;
    private Timestamp testTimestamp;
    private String testMachine;
    private int valuePassedOverall;
    private boolean compileSuccessful;
    private int valuePublicTestsPassed;
    private int valueReleaseTestsPassed;
    private int valueSecretTestsPassed;
    private int numFindBugsWarnings;
    private String md5sumClassfiles;
    private String md5sumSourcefiles;

     static final String[] ATTRIBUTE_LIST = {
            "test_run_pk", "project_jarfile_pk",
            "submission_pk", "test_timestamp",
            "test_machine", 
            "num_passed_overall",
            "num_build_tests_passed",
            "num_public_tests_passed",
            "num_release_tests_passed",
            "num_secret_tests_passed",
            "num_findbugs_warnings",
            "md5sum_classfiles",
            "md5sum_sourcefiles"
    };
    
    public static final String ATTRIBUTES = Queries.getAttributeList("test_runs", ATTRIBUTE_LIST);
    public static final String TABLE_NAME = "test_runs";
    
    /**
     * Populate a TestRun object from the database starting at index startingFrom
     * 
     * @param rs the ResultSet
     * @param startingFrom the index into the ResultSet to start from
     * @throws SQLException
     */
    public int fetchValues(ResultSet rs, int startingFrom)
    throws SQLException
    {
        setTestRunPK(rs.getString(startingFrom++));
        setProjectJarfilePK(rs.getString((startingFrom++)));
        setSubmissionPK(rs.getString(startingFrom++));
        setTestTimestamp(rs.getTimestamp(startingFrom++));
        setTestMachine(rs.getString(startingFrom++));
        setValuePassedOverall(rs.getInt(startingFrom++));
        setCompileSuccessful(rs.getInt(startingFrom++)> 0);
        setValuePublicTestsPassed(rs.getInt(startingFrom++));
        setValueReleaseTestsPassed(rs.getInt(startingFrom++));
        setValueSecretTestsPassed(rs.getInt(startingFrom++));
        setNumFindBugsWarnings(rs.getInt(startingFrom++));
        setMd5sumClassfiles(rs.getString(startingFrom++));
        setMd5sumSourcefiles(rs.getString(startingFrom++));
        return startingFrom;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.marmoset.modelClasses.ITestSummary#getValuePassedOverall()
     */
    public int getValuePassedOverall()
    {
        return valuePassedOverall;
    }
    /**
     * Sets the total score for this test run, which is the sum of the
     * public, release and secret tests, or -1 if the associated submission
     * did not compile.
     * <p>
     * <b>NOTE</b> If any category of tests could not run, we represent this as -1.
     * However, this method does not subtract the -1 when computing the valuePassedOverall.  
     * @param valuePassedOverall
     */
    public void setValuePassedOverall(int valuePassedOverall)
    {
        this.valuePassedOverall = valuePassedOverall;
    }
    /* (non-Javadoc)
     * @see edu.umd.cs.marmoset.modelClasses.ITestSummary#isCompileSuccessful()
     */
    public boolean isCompileSuccessful()
    {
        return compileSuccessful;
    }
    /**
     * Sets whether this test run compiled successfully.
     * @param success will be set to true if it compiled successfully; false otherwise
     */
    public void setCompileSuccessful(boolean success)
    {
        this.compileSuccessful = success;
    }
    /* (non-Javadoc)
     * @see edu.umd.cs.marmoset.modelClasses.ITestSummary#getValuePublicTestsPassed()
     */
    public int getValuePublicTestsPassed()
    {
        return valuePublicTestsPassed;
    }
    /**
     * Sets the total score for the public tests passed.
     * @param valuePublicTestsPassed
     */
    public void setValuePublicTestsPassed(int valuePublicTestsPassed)
    {
        this.valuePublicTestsPassed = valuePublicTestsPassed;
    }
    /* (non-Javadoc)
     * @see edu.umd.cs.marmoset.modelClasses.ITestSummary#getValueReleaseTestsPassed()
     */
    public int getValueReleaseTestsPassed()
    {
        return valueReleaseTestsPassed;
    }
    /**
     * Sets the total score for the release tests passed.
     * @param valueReleaseTestsPassed
     */
    public void setValueReleaseTestsPassed(int valueReleaseTestsPassed)
    {
        this.valueReleaseTestsPassed = valueReleaseTestsPassed;
    }
    /* (non-Javadoc)
     * @see edu.umd.cs.marmoset.modelClasses.ITestSummary#getValueSecretTestsPassed()
     */
    public int getValueSecretTestsPassed()
    {
        return valueSecretTestsPassed;
    }
    /**
     * Sets the total score for the secret tests passed.
     * @param valueSecretTestsPassed
     */
    public void setValueSecretTestsPassed(int valueSecretTestsPassed)
    {
        this.valueSecretTestsPassed = valueSecretTestsPassed;
    }
    /**
     * @return Returns the numFindBugsWarnings.
     */
    public int getNumFindBugsWarnings()
    {
        return numFindBugsWarnings;
    }
    /**
     * @param numFindBugsWarnings The numFindBugsWarnings to set.
     */
    public void setNumFindBugsWarnings(int numFindBugsWarnings)
    {
        this.numFindBugsWarnings = numFindBugsWarnings;
    }
    /**
     * @return Returns the projectJarfilePK.
     */
    public String getProjectJarfilePK() {
        return projectJarfilePK;
    }
    /**
     * @param projectJarfilePK The projectJarfilePK to set.
     */
    public void setProjectJarfilePK(String projectJarfilePK) {
        this.projectJarfilePK = projectJarfilePK;
    }
    /**
     * @return Returns the submissionPK.
     */
    public String getSubmissionPK() {
        return submissionPK;
    }
    /**
     * @param submissionPK The submissionPK to set.
     */
    public void setSubmissionPK(String submissionPK) {
        this.submissionPK = submissionPK;
    }
    /**
     * @return Returns the testRunPK.
     */
    public String getTestRunPK() {
        return testRunPK;
    }
    /**
     * @param testRunPK The testRunPK to set.
     */
    public void setTestRunPK(String testRunPK) {
        this.testRunPK = testRunPK;
    }
    /**
     * @return Returns the testTimestamp.
     */
    public Timestamp getTestTimestamp() {
        return testTimestamp;
    }
    /**
     * @param testTimestamp The testTimestamp to set.
     */
    public void setTestTimestamp(Timestamp testTimestamp) {
        this.testTimestamp = testTimestamp;
    }
    /**
     * @return Returns the testMachine.
     */
    public String getTestMachine() {
        return testMachine;
    }
    /**
     * @param testMachine The testMachine to set.
     */
    public void setTestMachine(String testMachine) {
        this.testMachine = testMachine;
    }
    /**
     * @return Returns the md5sumClassfiles.
     */
    public String getMd5sumClassfiles()
    {
        return md5sumClassfiles;
    }
    /**
     * @param md5sumClassfiles The md5sumClassfiles to set.
     */
    public void setMd5sumClassfiles(String md5sumClassfiles)
    {
        this.md5sumClassfiles = md5sumClassfiles;
    }
    /**
     * @return Returns the md5sumSourcefiles.
     */
    public String getMd5sumSourcefiles()
    {
        return md5sumSourcefiles;
    }
    /**
     * @param md5sumSourcefiles The md5sumSourcefiles to set.
     */
    public void setMd5sumSourcefiles(String md5sumSourcefiles)
    {
        this.md5sumSourcefiles = md5sumSourcefiles;
    }
    /**
     * Gets a testRun with the given testRunPK
     * @param testRunPK the PK
     * @param conn the connection to the database
     * @return the testRun; null if no test run with that PK exists
     * @throws SQLException
     */
    public static TestRun lookupByTestRunPK(String testRunPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+
            " FROM test_runs " +
            " WHERE test_run_pk = ? ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, testRunPK);
        
        return getFromPreparedStatement(stmt);
    }
    
    
    
    private static TestRun getFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        try {
            ResultSet rs = stmt.executeQuery();
            if (rs.first())
            {
                TestRun testRun = new TestRun();
                testRun.fetchValues(rs, 1);
                return testRun;
            }
            return null;
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    private int putValues(PreparedStatement stmt, int index)
    throws SQLException
    {
        //stmt.setString(index++, getTestRunPK());
        stmt.setString(index++, getProjectJarfilePK());
        stmt.setString(index++, getSubmissionPK());
        stmt.setTimestamp(index++, getTestTimestamp());
        stmt.setString(index++, getTestMachine());
        stmt.setInt(index++, getValuePassedOverall());
        stmt.setInt(index++, isCompileSuccessful() ? 1 : 0);
        stmt.setInt(index++, getValuePublicTestsPassed());
        stmt.setInt(index++, getValueReleaseTestsPassed());
        stmt.setInt(index++, getValueSecretTestsPassed());
        stmt.setInt(index++, getNumFindBugsWarnings());
        stmt.setString(index++, getMd5sumClassfiles());
        stmt.setString(index++, getMd5sumSourcefiles());
        return index;        
    }
    
    /**
     * @param conn
     */
    public void insert(Connection conn)
    throws SQLException
    {
        String insert = Queries.makeInsertStatement(ATTRIBUTE_LIST.length, ATTRIBUTES, "test_runs");
        
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(insert);
            
            int index=putValues(stmt, 1);
            
            int rows = stmt.executeUpdate();
            
            // set PK to the value of the last autoincrement for this connection
            // this will be the PK used for the inset we just performed
            setTestRunPK(Queries.lastInsertId(conn));
            
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg0) {
        TestRun that = (TestRun) arg0;
        return getValuePassedOverall() - that.getValuePassedOverall();
    }

    /**
     * @param conn
     */
    public void update(Connection conn)
    throws SQLException
    {
	    if (getTestRunPK() == null)
	        throw new IllegalStateException("You cannot try to update a TestRun with a null testRunPK");
	    
	    String update = Queries.makeUpdateStatementWithWhereClause(ATTRIBUTE_LIST, TABLE_NAME, " WHERE test_run_pk = ? ");

	    PreparedStatement stmt = conn.prepareStatement(update);
	    
	    int index = putValues(stmt, 1);
	    stmt.setString(index, getTestRunPK());
	    
	    stmt.executeUpdate();
	    try {
	        stmt.close();
	    } catch (SQLException ignore) {
	        // ignore
	    }

    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public static List<TestRun> lookupAllBySubmissionPK(String submissionPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +TestRun.ATTRIBUTES+
            " FROM test_runs " +
            " WHERE test_runs.submission_pk = ? " +
            " ORDER BY test_timestamp DESC ";
        
        PreparedStatement stmt = null;
        try {
            stmt=conn.prepareStatement(query);
            stmt.setString(1, submissionPK);
            
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
        
    }
    
    private static List<TestRun> getAllFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        ResultSet rs = stmt.executeQuery();
        List<TestRun> testRunList = new LinkedList<TestRun>();
        while (rs.next())
        {
            TestRun testRun = new TestRun();
            testRun.fetchValues(rs, 1);
            testRunList.add(testRun);
        }
        return testRunList;
    }

    public static List<TestRun> lookupAllByProjectPK(String projectPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +TestRun.ATTRIBUTES+
            " FROM test_runs, projects " +
            " WHERE test_runs.project_jarfile_pk = projects.project_jarfile_pk " +
            " AND projects.project_pk = ? ";
        
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, projectPK);
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
}
