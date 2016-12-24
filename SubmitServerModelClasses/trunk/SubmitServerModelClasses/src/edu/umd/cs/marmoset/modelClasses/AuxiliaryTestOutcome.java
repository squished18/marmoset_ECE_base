/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 8, 2006
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import edu.umd.cs.marmoset.codeCoverage.CoverageLevel;

/**
 * AuxiliaryTestOutcomes
 * @author jspacco
 */
public class AuxiliaryTestOutcome
{
    private String testRunPK="0";
    private String testType;
    private int testNumber;
    private String outcome;
    private CoverageLevel failingOnlyCoarsestCoverageLevel;
    
    public static final String[] ATTRIBUTE_NAME_LIST = {
        "test_run_pk",
        "test_type",
        "test_number",
        "outcome",
        "failing_only_coarsest_coverage_level"
    };
    
    /** Name of this table in the database. */
     public static final String TABLE_NAME =  "auxiliary_test_outcomes";
     
    /**
     * Fully-qualified attributes for test_outcomes table.
     */
    public static final String ATTRIBUTES = 
        Queries.getAttributeList(TABLE_NAME, ATTRIBUTE_NAME_LIST);

    public AuxiliaryTestOutcome(TestOutcome outcome)
    {
        this.testRunPK=outcome.getTestRunPK();
        this.testType=outcome.getTestType();
        this.testNumber=outcome.getTestNumber();
        this.outcome=outcome.getOutcome();
    }
    
    int putValues(PreparedStatement stmt, int index)
    throws SQLException
    {
        stmt.setString(index++, getTestRunPK());
        stmt.setString(index++, getTestType());
        stmt.setInt(index++, getTestNumber());
        stmt.setString(index++, getOutcome());
        stmt.setString(index++, (getFailingOnlyCoarsestCoverageLevel()!=null)?getFailingOnlyCoarsestCoverageLevel().toString():CoverageLevel.NONE.toString());
        return index;
    }
    
    public int fetchValues(ResultSet rs, int startingFrom)
    throws SQLException
    {
        setTestRunPK(rs.getString(startingFrom++));
        setTestType(rs.getString(startingFrom++));
        setTestNumber(rs.getInt(startingFrom++));
        setOutcome(rs.getString(startingFrom++));
        setFailingOnlyCoarsestCoverageLevel(CoverageLevel.fromString(rs.getString(startingFrom++)));
        return startingFrom;
    }
    
    public void insert(Connection conn)
    throws SQLException
    {
        String query=Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME);

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);

            putValues(stmt, 1);

            // Insert the values!
            stmt.executeUpdate();
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    public static void insertOrUpdateBatch(List<AuxiliaryTestOutcome> list, Connection conn)
    throws SQLException
    {
        String query=Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME);
        query += " ON DUPLICATE KEY UPDATE  failing_only_coarsest_coverage_level = ? ";
        
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            
            for (AuxiliaryTestOutcome outcome : list) {
                int index=outcome.putValues(stmt, 1);
                stmt.setString(index,outcome.getFailingOnlyCoarsestCoverageLevel().toString());
                stmt.addBatch();
            }
            
            // Insert the values!
            stmt.executeBatch();
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    public static void insertBatch(List<AuxiliaryTestOutcome> list, Connection conn)
    throws SQLException
    {
        String query=Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME);
//        String query = " INSERT INTO " + TABLE_NAME + 
//        " SET "+
//        " test_run_pk = ?, " +
//        " test_type = ?," +
//        " test_number = ?, " +
//        " outcome = ?, " +
//        " failing_only_coarsest_coverage_level = ? ";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);

            for (AuxiliaryTestOutcome outcome : list) {
                outcome.putValues(stmt, 1);
                stmt.addBatch();
            }

            // Insert the values!
            stmt.executeBatch();
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    /**
     * @return Returns the outcome.
     */
    public String getOutcome()
    {
        return outcome;
    }
    /**
     * @param outcome The outcome to set.
     */
    public void setOutcome(String outcome)
    {
        this.outcome = outcome;
    }
    /**
     * @return Returns the testNumber.
     */
    public int getTestNumber()
    {
        return testNumber;
    }
    /**
     * @param testNumber The testNumber to set.
     */
    public void setTestNumber(int testNumber)
    {
        this.testNumber = testNumber;
    }
    /**
     * @return Returns the testRunPK.
     */
    public String getTestRunPK()
    {
        return testRunPK;
    }
    /**
     * @param testRunPK The testRunPK to set.
     */
    public void setTestRunPK(String testRunPK)
    {
        this.testRunPK = testRunPK;
    }
    /**
     * @return Returns the testType.
     */
    public String getTestType()
    {
        return testType;
    }
    /**
     * @param testType The testType to set.
     */
    public void setTestType(String testType)
    {
        this.testType = testType;
    }
    /**
     * @return Returns the coversFailingOnly.
     */
    public CoverageLevel getFailingOnlyCoarsestCoverageLevel()
    {
        return failingOnlyCoarsestCoverageLevel;
    }
    /**
     * @param coversFailingOnly The coversFailingOnly to set.
     */
    public void setFailingOnlyCoarsestCoverageLevel(CoverageLevel coversFailingOnly)
    {
        this.failingOnlyCoarsestCoverageLevel = coversFailingOnly;
    }

    /**
     * Insert each test outcome one at a time; if it's a duplicate then just ignore it.
     * @param auxOutcomeList
     * @param conn
     * @throws SQLException
     */
    public static void insertSingly(List<AuxiliaryTestOutcome> auxOutcomeList, Connection conn)
    throws SQLException
    {
        for (AuxiliaryTestOutcome auxOutcome : auxOutcomeList) {
            try {
                auxOutcome.insert(conn);
            } catch (SQLException ignore) {
                // ignore
            }
        }
        
    }
    
    
}
