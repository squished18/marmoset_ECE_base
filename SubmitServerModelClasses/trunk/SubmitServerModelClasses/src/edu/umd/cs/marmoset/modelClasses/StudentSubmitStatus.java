/*
 * Created on Aug 31, 2004
 *
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

/**
 * @author jspacco
 *
 */
 public class StudentSubmitStatus {

	//private String pk;
	private String projectPK;
	private String studentRegistrationPK;
	private String oneTimePassword;
	private int numberSubmissions;
	private int numberCommits;
	private int extension = 0;
	
	// [NAT P002] removed random number generation
	
	/**
	 * List of all attributes of student_submit_status table. 
	 */
	  static final String[] ATTRIBUTES_LIST = {
			"project_pk",
			"student_registration_pk",
			"one_time_password",
			"number_submissions",
			"number_commits",
			"extension"
	};
	
	public static final String TABLE_NAME = "student_submit_status";
	 
	 /**
	 * Fully-qualified attributes for student_submit_status table.
	 */
	public static final String ATTRIBUTES =
		Queries.getAttributeList(TABLE_NAME, ATTRIBUTES_LIST);

	
    /**
     * @return Returns the numberSubmissions.
     */
    public int getNumberSubmissions()
    {
        return numberSubmissions;
    }
    /**
     * @param numberSubmissions The numberSubmissions to set.
     */
    public void setNumberSubmissions(int numberSubmissions)
    {
        this.numberSubmissions = numberSubmissions;
    }
	/**
	 * @return Returns the oneTimePassword.
	 */
	public String getOneTimePassword() {
		return oneTimePassword;
	}
	/**
	 * @param oneTimePassword The oneTimePassword to set.
	 */
	public void setOneTimePassword(String oneTimePassword) {
		this.oneTimePassword = oneTimePassword;
	}
	
	/**
	 * @return Returns the projectPK.
	 */
	public String getProjectPK() {
		return projectPK;
	}
	/**
	 * @param projectPK The projectPK to set.
	 */
	public void setProjectPK(String projectPK) {
		this.projectPK = projectPK;
	}
	/**
	 * @return Returns the studentRegistrationPK.
	 */
	public String getStudentRegistrationPK() {
		return studentRegistrationPK;
	}
	/**
	 * @param studentRegistrationPK The studentRegistrationPK to set.
	 */
	public void setStudentRegistrationPK(String studentRegistrationPK) {
		this.studentRegistrationPK = studentRegistrationPK;
	}
	
    /**
     * @return Returns the extension.
     */
    public int getExtension() {
        return extension;
    }
    /**
     * @param extension The extension to set.
     */
    public void setExtension(int extension) {
        this.extension = extension;
    }
    /**
     * @return Returns the numberCommits.
     */
    public int getNumberCommits()
    {
        return numberCommits;
    }
    /**
     * @param numberCommits The numberCommits to set.
     */
    public void setNumberCommits(int numberCommits)
    {
        this.numberCommits = numberCommits;
    }
	public void fetchValues(ResultSet resultSet, int startingFrom) throws SQLException
	{
		setProjectPK(resultSet.getString(startingFrom++));
		setStudentRegistrationPK(resultSet.getString(startingFrom++));
		setOneTimePassword(resultSet.getString(startingFrom++));
		setNumberSubmissions(resultSet.getInt(startingFrom++));
		setNumberCommits(resultSet.getInt(startingFrom++));
		setExtension(resultSet.getInt(startingFrom++));
	}
	
	/**
	 * Inserts a new student registration row into the database.
	 * <p>
	 * We first check for duplicates.  The primary key field of StudentSubmitStatus
	 * is obsolete and will be removed in future versions.  StudentSubmitStatus has
	 * a compound primary key consisting of studentRegistrationPK and projectPK
	 * since that uniquely identifies a StudentSubmitStatus.
	 * 
	 * @param conn the connection to the database
	 * @return true if a new row was added to the database, false if the row
	 * already existed and no new row was added
	 * @throws SQLException
	 */
	public boolean insert(Connection conn)
		throws SQLException
	{
		StudentSubmitStatus studentSubmitStatus = StudentSubmitStatus.lookupByStudentRegistrationPKAndProjectPK(
		        getStudentRegistrationPK(),
		        getProjectPK(),
		        conn);
		
		// if the row already exists, simply return false
		if (studentSubmitStatus != null)
		    return false;
	    
	    String insertSQL = 
	        " INSERT INTO student_submit_status " +
			" VALUES " +
			" (?, " +
			" ?, " +
			" ?, " +
			" ?, " +
			" ?, " +
			" ?) ";
		
		PreparedStatement stmt = conn.prepareStatement(insertSQL);
		
		int index=1;
		stmt.setString(index++, projectPK);
		stmt.setString(index++, studentRegistrationPK);
		stmt.setString(index++, oneTimePassword);
		stmt.setInt(index++, numberSubmissions);
		stmt.setInt(index++, numberCommits);
		stmt.setInt(index++, extension);
		
		executeUpdate(stmt);

		return true;
	}
	
	public void update(Connection conn)
	throws SQLException
	{
	    String update = 
	        " UPDATE " + TABLE_NAME +
	        " SET " +
	        " one_time_password = ?, " +
	        " number_submissions = ?, " +
	        " number_commits = ?, " +
	        " extension = ? " +
	        " WHERE student_registration_pk = ? " +
	        " AND project_pk = ? ";
	    PreparedStatement stmt=null;
	    try {
	        // update statement
	        int index=1;
	        stmt = conn.prepareStatement(update);
	        stmt.setString(index++, getOneTimePassword());
	        stmt.setInt(index++, getNumberSubmissions());
	        stmt.setInt(index++, getNumberCommits());
	        stmt.setInt(index++, getExtension());
	        // where clause
	        stmt.setString(index++, getStudentRegistrationPK());
	        stmt.setString(index++, getProjectPK());
	        stmt.executeUpdate();
	    } finally {
			    if (stmt != null) stmt.close();
	    }
	}
		
	public StudentSubmitStatus() {}
	
	public static StudentSubmitStatus createAndInsert(
	        String projectPK,
	        String studentRegistrationPK,
	        Connection conn)
	throws SQLException
	{
	    StudentSubmitStatus studentSubmitStatus = new StudentSubmitStatus();
	    studentSubmitStatus.projectPK = projectPK;
	    studentSubmitStatus.studentRegistrationPK = studentRegistrationPK;
	    studentSubmitStatus.oneTimePassword = MarmosetUtilities.nextRandomPassword(); // [NAT P002]
	    studentSubmitStatus.insert(conn);
	    return studentSubmitStatus;
	}
	
	private void executeUpdate(PreparedStatement stmt)
	throws SQLException
	{
	    try {
	        stmt.executeUpdate();
	    } finally {
	        try {
	            if (stmt != null) stmt.close();
	        } catch (SQLException ignore) {
	            // ignore
	        }
	        stmt = null;
	    }
	}
	
	public static StudentSubmitStatus lookupByStudentRegistrationPKAndProjectPK(
			String studentRegistrationPK,
			String projectPK,
			Connection conn)
			throws SQLException
	{
		String query =
			"SELECT " + ATTRIBUTES +
			" FROM " + TABLE_NAME +
			" WHERE student_registration_pk = ? " +
			" AND project_pk = ?";
		
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, studentRegistrationPK);
		stmt.setString(2, projectPK);
		
		return getFromPreparedStatement(stmt);
	}
	
    public static Map<String, StudentSubmitStatus> lookupAllByProjectPK(
            String projectPK,
            Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " + ATTRIBUTES + 
            " FROM " + TABLE_NAME + 
            " WHERE project_pk = ?";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, projectPK);
        
        return getAllFromPreparedStatement(stmt);
    }
    
    
	private static StudentSubmitStatus getFromPreparedStatement(PreparedStatement stmt)
	throws SQLException
	{
	    try {
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next())
			{
				StudentSubmitStatus studentSubmitStatus = new StudentSubmitStatus();
				studentSubmitStatus.fetchValues(rs, 1);
				return studentSubmitStatus;
			}
			return null;
		}
		finally
		{
			if (stmt != null)
			{
				try {
					stmt.close();
				} catch (SQLException ignore) {
					// Ignore
				}
			}
		}
	}
     private static Map<String, StudentSubmitStatus> getAllFromPreparedStatement(PreparedStatement stmt)
        throws SQLException
        {
            try {
                ResultSet rs = stmt.executeQuery();
                
                Map<String, StudentSubmitStatus> map = new LinkedHashMap<String, StudentSubmitStatus>();
                while (rs.next())
                {
                    StudentSubmitStatus studentSubmitStatus = new StudentSubmitStatus();
                    studentSubmitStatus.fetchValues(rs, 1);
                    map.put(studentSubmitStatus.getStudentRegistrationPK(), studentSubmitStatus);
                }
                return map;
            }
            finally {
                Queries.closeStatement(stmt);
            }
        }
     
	
}
