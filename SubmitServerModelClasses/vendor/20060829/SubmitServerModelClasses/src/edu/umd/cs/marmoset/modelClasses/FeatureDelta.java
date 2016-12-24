/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 28, 2005
 *
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

/**
 * @author jspacco
 *
 */
public class FeatureDelta
{
    private String submissionPK;
    private String testType;
    private String testName;
    private String priority;
    private int num;
    private int delta;
    
    final static String[] ATTRIBUTES_NAME_LIST = {
            "submission_pk",
            "test_type",
            "test_name",
            "priority",
            "num",
            "delta"
    };
    
    public final static String ATTRIBUTES = Queries.getAttributeList("feature_deltas_3", ATTRIBUTES_NAME_LIST);
    
    public FeatureDelta(String submissionPK, String testType, String testName, String priority)
    {
        this.submissionPK = submissionPK;
        this.testType = testType;
        this.testName = testName;
        this.priority = priority;
    }
    public FeatureDelta()
    {
        priority="-1";
    }
    
    public int hashCode()
    {
        return MarmosetUtilities.hashString(submissionPK) +
        	MarmosetUtilities.hashString(testType) +
        	MarmosetUtilities.hashString(testName);
    }
    
    public boolean equals(Object o)
    {
    		if (!(o instanceof FeatureDelta)) return false;
        FeatureDelta other = (FeatureDelta)o;
        return MarmosetUtilities.stringEquals(submissionPK, other.submissionPK) &&
        MarmosetUtilities.stringEquals(testType, other.testType) &&
        MarmosetUtilities.stringEquals(testName, other.testName);
    }
    
    public String toString()
    {
        return submissionPK +":"+ testType +":"+ testName +" = "+ num +", "+ delta;
    }
    
    private int putValues(PreparedStatement stmt, int index)
    throws SQLException
    {
        stmt.setString(index++, getSubmissionPK());
        stmt.setString(index++, getTestType());
        stmt.setString(index++, getTestName());
        stmt.setString(index++, getPriority());
        stmt.setInt(index++, getNum());
        stmt.setInt(index++, getDelta());
        return index;
    }
    
    public int fetchValues(ResultSet rs, int startingFrom)
    throws SQLException
    {
        setSubmissionPK(rs.getString(startingFrom++));
        setTestType(rs.getString(startingFrom++));
        setTestName(rs.getString(startingFrom++));
        setPriority(rs.getString(startingFrom++));
        setNum(rs.getInt(startingFrom++));
        setDelta(rs.getInt(startingFrom++));
        return startingFrom;
    }
    
    public void insert(Connection conn)
    throws SQLException
    {
        String insert = 
            " INSERT INTO feature_deltas_3 " +
            " VALUES (?, ?, ?, ?, ?, ?) ";
            
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(insert);
            int index = putValues(stmt, 1);
            stmt.executeUpdate();
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    public static void delete(String submissionPK, Connection conn)
    throws SQLException
    {
        String delete = 
            " DELETE feature_deltas_3 FROM feature_deltas_3 " +
            " WHERE submission_pk = ? ";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(delete);
            stmt.setString(1, submissionPK);
            
            stmt.executeUpdate();
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    public void update(Connection conn)
    throws SQLException
    {
        String update = 
            " UPDATE feature_deltas_3 " +
            " SET " +
            " num = ?, " +
            " delta = ? " +
            " WHERE submission_pk = ? " +
            " AND test_type = ? " +
            " AND test_name = ? " +
            " AND priority = ? ";
        
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            int index=1;
            stmt.setInt(index++, getNum());
            stmt.setInt(index++, getDelta());
            stmt.setString(index++, getSubmissionPK());
            stmt.setString(index++, getTestType());
            stmt.setString(index++, getTestName());
            stmt.setString(index++, getPriority());
            
            stmt.executeUpdate();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
    }
    
    /**
     * Inserts a collection of features into the DB.  If any of the features already exists 
     * (based on the composite primary key) then the data fields (num and delta) are updated 
     * instead.
     * @param featureCollection the list of features to be inserted (or updated)
     * @param conn the connection to the database
     * @throws SQLException
     */
    public static int insert(Collection featureCollection, Connection conn)
    throws SQLException
    {
        String insert =
            " INSERT INTO feature_deltas_3 " +
            " VALUES (?, ?, ?, ?, ?, ?) " +
            " ON DUPLICATE KEY UPDATE " +
            " num = ?, delta = ? ";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(insert);
            for (Iterator ii=featureCollection.iterator(); ii.hasNext();)
            {
                FeatureDelta feature = (FeatureDelta)ii.next();
                int index = feature.putValues(stmt, 1);
                stmt.setInt(index++, feature.getNum());
                stmt.setInt(index++, feature.getDelta());
                stmt.addBatch();
            }
            int[] updates = stmt.executeBatch();
            return updates.length;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
    }
    
    /**
     * @return Returns the delta.
     */
    public int getDelta()
    {
        return delta;
    }
    /**
     * @param delta The delta to set.
     */
    public void setDelta(int delta)
    {
        this.delta = delta;
    }
    /**
     * @return Returns the num.
     */
    public int getNum()
    {
        return num;
    }
    /**
     * @param num The num to set.
     */
    public void setNum(int num)
    {
        this.num = num;
    }
    /**
     * @return Returns the testName.
     */
    public String getTestName()
    {
        return testName;
    }
    /**
     * @param testName The testName to set.
     */
    public void setTestName(String testName)
    {
        this.testName = testName;
    }
    /**
     * @return Returns the testRunPK.
     */
    public String getSubmissionPK()
    {
        return submissionPK;
    }
    /**
     * @param testRunPK The testRunPK to set.
     */
    public void setSubmissionPK(String testRunPK)
    {
        this.submissionPK = testRunPK;
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
     * @return Returns the priority.
     */
    public String getPriority()
    {
        return priority;
    }
    /**
     * @param priority The priority to set.
     */
    public void setPriority(String priority)
    {
        this.priority = priority;
    }
}
