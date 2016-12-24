package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * @author jspacco
 *
 */
public class EclipseLaunchEvent
{
	public static final String[] ATTRIBUTE_NAME_LIST = {
        "eclipse_launch_event_pk",
        "student_registration_pk",
        "project_number",
        "event",
        "md5sum",
        "timestamp",
        "skew"
	};
	/**
	 * Fully-qualified attributes for courses table.
	 */
	public static final String ATTRIBUTES = Queries.getAttributeList("eclipse_launch_events",
		ATTRIBUTE_NAME_LIST);
  
	private String eclipseLaunchEventPK;
	private String studentRegistrationPK;
	private String projectNumber;
	private String md5sum;
	private String event;
	private Timestamp timestamp;
	private int skew;
	
	public EclipseLaunchEvent() {}
	
	public void insert(Connection conn)
	throws SQLException
	{
		// TODO Check for duplicates!
		String query =
			" INSERT INTO eclipse_launch_events " +
			" VALUES (DEFAULT, ?, ?, ?, ?, ?, ?) ";
		PreparedStatement stmt=null;
		try {
			stmt = conn.prepareStatement(query);
			putValues(stmt, 1);
			stmt.execute();
			setEclipseLaunchEventPK(Queries.lastInsertId(conn));
		} finally {
			Queries.closeStatement(stmt);
		}
	}
	
	public int fetchValues(ResultSet resultSet, int startingFrom) throws SQLException
	{
		setEclipseLaunchEventPK(resultSet.getString(startingFrom++));
		setStudentRegistrationPK(resultSet.getString(startingFrom++));
		setProjectNumber(resultSet.getString(startingFrom++));
		setMd5sum(resultSet.getString(startingFrom++));
		setEvent(resultSet.getString(startingFrom++));
		setTimestamp(resultSet.getTimestamp(startingFrom++));
		setSkew(resultSet.getInt(startingFrom++));
		return startingFrom;
	}
	
	private int putValues(PreparedStatement stmt, int index)
	throws SQLException
	{
	    stmt.setString(index++, getStudentRegistrationPK());
	    stmt.setString(index++, getProjectNumber());
	    stmt.setString(index++, getMd5sum());
	    stmt.setString(index++, getEvent());
	    stmt.setTimestamp(index++, getTimestamp());
	    stmt.setInt(index++, getSkew());
	    return index;
	}
	
	/**
	 * @return Returns the date.
	 */
	public String getMd5sum() {
		return md5sum;
	}
	/**
	 * @param date The date to set.
	 */
	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}
	/**
	 * @return Returns the eclipseLaunchEventPK.
	 */
	public String getEclipseLaunchEventPK() {
		return eclipseLaunchEventPK;
	}
	/**
	 * @param eclipseLaunchEventPK The eclipseLaunchEventPK to set.
	 */
	public void setEclipseLaunchEventPK(String eclipseLaunchEventPK) {
		this.eclipseLaunchEventPK = eclipseLaunchEventPK;
	}
	/**
	 * @return Returns the event.
	 */
	public String getEvent() {
		return event;
	}
	/**
	 * @param event The event to set.
	 */
	public void setEvent(String event) {
		this.event = event;
	}
	/**
	 * @return Returns the projectNumber.
	 */
	public String getProjectNumber() {
		return projectNumber;
	}
	/**
	 * @param projectNumber The projectNumber to set.
	 */
	public void setProjectNumber(String projectNumber) {
		this.projectNumber = projectNumber;
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
	 * @return Returns the timestamp.
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return Returns the skew.
	 */
	public int getSkew() {
		return skew;
	}
	/**
	 * @param skew The skew to set.
	 */
	public void setSkew(int skew) {
		this.skew = skew;
	}
	
	
}
