/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Oct 19, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * BackgroundRetest
 * @author jspacco
 */
public class BackgroundRetest
{
	static final String[] ATTRIBUTE_NAME_LIST = {
		"submission_pk",
		"project_jarfile_pk",
		"num_successful_background_retests",
		"num_failed_background_retests"
	};

	static final String ATTRIBUTES =
		Queries.getAttributeList("background_retests", ATTRIBUTE_NAME_LIST);
	
	private String submissionPK;
	private String projectJarfilePK;
	private int numSuccessfulBackgroundRetests;
	private int numFailedBackgroundRetests;

	int fetchValues(ResultSet rs, int startingFrom)
    throws SQLException
    {
		setSubmissionPK(rs.getString(startingFrom++));
		setProjectJarfilePK(rs.getString(startingFrom++));
		setNumSuccessfulBackgroundRetests(rs.getInt(startingFrom++));
		setNumFailedBackgroundRetests(rs.getInt(startingFrom++));
		return startingFrom;
    }
	
	private int putValues(PreparedStatement stmt, int index)
	throws SQLException
	{
		stmt.setString(index++, getSubmissionPK());
		stmt.setString(index++, getProjectJarfilePK());
		stmt.setInt(index++, getNumSuccessfulBackgroundRetests());
		stmt.setInt(index++, getNumFailedBackgroundRetests());
		return index;
	}
	
	public void insert(Connection conn)
	throws SQLException
	{
		String insert = 
			" INSERT INTO background_retests " +
			" VALUES (?, ?, ?, ?) ";
		PreparedStatement stmt=null;
	    try {
	        stmt = conn.prepareStatement(insert);
	                
	        int index=1;
	        putValues(stmt, index);

	        stmt.executeUpdate();
	    } finally {
	    	Queries.closeStatement(stmt);
	    }
	}
	
	public void update(Connection conn)
	throws SQLException
	{
		String update =
			" UPDATE background_retests " +
			" SET " +
			" num_successful_background_retests = ?, " +
			" num_failed_background_retests = ? " +
			" WHERE submission_pk = ? " +
			" AND project_jarfile_pk = ? ";
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(update);
			int index=1;
			stmt.setInt(index++, getNumSuccessfulBackgroundRetests());
			stmt.setInt(index++, getNumFailedBackgroundRetests());
			stmt.setString(index++, getSubmissionPK());
			stmt.setString(index++, getProjectJarfilePK());
			
			stmt.executeUpdate();
		} finally {
			Queries.closeStatement(stmt);
		}
			
	}
	
	public static Map<String,BackgroundRetest> lookupMapBySubmissionPK(
        String submissionPK,
        Connection conn)
    throws SQLException
    {
        Map<String,BackgroundRetest> projectJarfilePKMap = new HashMap<String,BackgroundRetest>();
        List<BackgroundRetest> list = lookupAllBySubmissionPK(submissionPK, conn);
        for (BackgroundRetest backgroundRetest : list) {
            projectJarfilePKMap.put(backgroundRetest.getProjectJarfilePK(), backgroundRetest);
        }
        return projectJarfilePKMap;
    }
    
    public static List<BackgroundRetest> lookupAllBySubmissionPK(String submissionPK, Connection conn)
    throws SQLException
    {
        String query =
            " SELECT * " +
            " FROM background_retests " +
            " WHERE submission_pk = ? ";
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(query);
            stmt.setString(1,submissionPK);
            List<BackgroundRetest> resultList = new LinkedList<BackgroundRetest>();
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BackgroundRetest backgroundRetest = new BackgroundRetest();
                backgroundRetest.fetchValues(rs, 1);
                resultList.add(backgroundRetest);
            }
            return resultList;
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    public static BackgroundRetest lookupBySubmissionPKAndProjectJarfilePK(
		String submissionPK,
		String projectJarfilePK,
		Connection conn)
	throws SQLException
	{
		String sql =
			" SELECT * " +
			" FROM background_retests " +
			" WHERE submission_pk = ? " +
			" AND project_jarfile_pk = ? ";
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, submissionPK);
			stmt.setString(2, projectJarfilePK);
			
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next()) {
				BackgroundRetest backgroundRetest = new BackgroundRetest();
				backgroundRetest.fetchValues(rs, 1);
				return backgroundRetest;
			}
			return null;
		} finally {
			Queries.closeStatement(stmt);
		}
	}

	/**
	 * @return Returns the numFailedBackgroundRetests.
	 */
	public int getNumFailedBackgroundRetests() {
		return numFailedBackgroundRetests;
	}
	/**
	 * @param numFailedBackgroundRetests The numFailedBackgroundRetests to set.
	 */
	public void setNumFailedBackgroundRetests(int numFailedBackgroundRetests) {
		this.numFailedBackgroundRetests = numFailedBackgroundRetests;
	}
	/**
	 * @return Returns the numSuccessfulBackgroundRetests.
	 */
	public int getNumSuccessfulBackgroundRetests() {
		return numSuccessfulBackgroundRetests;
	}
	/**
	 * @param numSuccessfulBackgroundRetests The numSuccessfulBackgroundRetests to set.
	 */
	public void setNumSuccessfulBackgroundRetests(int numSuccessfulBackgroundRetests) {
		this.numSuccessfulBackgroundRetests = numSuccessfulBackgroundRetests;
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
}
