/*
 * Created on Aug 31, 2004
 *
 */
package edu.umd.cs.marmoset.modelClasses;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jspacco
 * TODO refactor getAllFromPreparedStatement() and getFromPreparedStatement() so that they 
 * don't try to close the statement themselves but rather allow their callers to do so.  The
 * way the code is currently structured could leak statements if any of the setString() methods
 * fail (though I don't think matters in practice since the connections all get closed anyway).
 */
public class Student  implements Comparable {
	private String studentPK;
	private String campusUID;
	private String employeeNum;
	private String firstname;
	private String lastname;
	private String superUser;
	private String givenConsent;
	private String accountType=NORMAL_ACCOUNT;
	private String latestSubmissionPK;
    private String password;
	
	public static final String DEMO_ACCOUNT = "demo";
	public static final String NORMAL_ACCOUNT = "normal";
	
	public static final String TABLE_NAME="students";
	
	/**
	 *  List of all attributes of students table.
	 */
	  static final String[] ATTRIBUTE_LIST = {
			"student_pk","campus_uid","employee_num",
			"firstname","lastname","superuser","given_consent",
			"latest_submission_pk", "password"
	 };
	
	/**
	 * Fully-qualified attributes for students table.
	 */
	 public static final String ATTRIBUTES =
		Queries.getAttributeList("students", ATTRIBUTE_LIST);
	 
	 // constants related to consent forms
	 public static final String CONSENTED = "yes";
	 public static final String NOT_CONSENTED = "no";
	 public static final String UNDER_18 = "under 18";
	 public static final String PENDING = "pending";
	
	/**
	 * @param superUser The superUser to set.
	 */
	public void setSuperUser(String superUser) {
		this.superUser = superUser;
	}
	/**
	 * @return Returns the campusUID.
	 */
	public String getCampusUID() {
		return campusUID;
	}
	/**
	 * @param campusUID The campusUID to set.
	 */
	public void setCampusUID(String campusUID) {
		this.campusUID = campusUID;
	}
	/**
	 * @return Returns the employeeNum.
	 */
	public String getEmployeeNum() {
		return employeeNum;
	}
	/**
	 * @param employeeNum The employeeNum to set.
	 */
	public void setEmployeeNum(String employeeNum) {
		this.employeeNum = employeeNum;
	}
	/**
	 * @return Returns the firstname.
	 */
	public String getFirstname() {
		return firstname;
	}
	/**
	 * @param firstname The firstname to set.
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	/**
	 * @return Returns the lastname.
	 */
	public String getLastname() {
		return lastname;
	}
	/**
	 * @param lastname The lastname to set.
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	/**
	 * @return Returns the studentPK.
	 */
	public String getStudentPK() {
		return studentPK;
	}
	/**
	 * @param studentPK The studentPK to set.
	 */
	public void setStudentPK(String studentPK) {
		this.studentPK = studentPK;
	}
    /**
     * @return Returns the givenConsent.
     */
    public String getGivenConsent() {
        return givenConsent;
    }
    /**
     * @param givenConsent The givenConsent to set.
     */
    public void setGivenConsent(String givenConsent) {
        this.givenConsent = givenConsent;
    }
    /**
     * @return Returns the latestSubmissionPK.
     */
    public String getLatestSubmissionPK()
    {
        return latestSubmissionPK;
    }
    public String getAccountType() {
        return accountType;
    }
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    /**
     * @param latestSubmissionPK The latestSubmissionPK to set.
     */
    public void setLatestSubmissionPK(String latestSubmissionPK)
    {
        this.latestSubmissionPK = latestSubmissionPK;
    }
	public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }
    public int fetchValues(ResultSet resultSet, int startingFrom) throws SQLException {
		setStudentPK(resultSet.getString(startingFrom++));
		setCampusUID(resultSet.getString(startingFrom++));
		setEmployeeNum(resultSet.getString(startingFrom++));
		setFirstname(resultSet.getString(startingFrom++));
		setLastname(resultSet.getString(startingFrom++));
		setSuperUser(resultSet.getString(startingFrom++));
		setGivenConsent(resultSet.getString(startingFrom++));
		setLatestSubmissionPK(resultSet.getString(startingFrom++));
        setPassword(resultSet.getString(startingFrom++));
		return startingFrom;
	}
	
	/**
	 * Inserts a student into the database (using conn) with a fresh
	 * primary key.
	 * 
	 * Checks for duplicates and on a duplicate, throws an exception
	 * 
	 * @param conn the connection to the database
	 * @throws SQLException if something goes wrong or a duplicate is found
	 */
	public void insert(Connection conn)
	throws SQLException
	{
	    Student student = lookupByCampusUID(getCampusUID(), conn);
		if (student != null)
		{
		    // record does not exist so insert it
		    throw new SQLException("A record with directory id " +getCampusUID()+ 
		            " already exists");
		}
		executeInsert(conn);
	}
	
	private void executeInsert(Connection conn)
	throws SQLException
	{
	    String query = "INSERT INTO " +
		" students "+
		" VALUES (DEFAULT, ?, ?, ?, ?, DEFAULT, DEFAULT, ?, DEFAULT, ?) ";

		PreparedStatement stmt = conn.prepareStatement(query);

		stmt.setString(1, campusUID);
		stmt.setString(2, employeeNum);
		stmt.setString(3, firstname);
		stmt.setString(4, lastname);
		stmt.setString(5, accountType);
        stmt.setString(6, password);
				
		stmt.executeUpdate();
		
		// set PK to the value of the last autoincrement for this connection
        // this will be the PK used for the inset we just performed
		setStudentPK(Queries.lastInsertId(conn));

		Queries.closeStatement(stmt);
	}
	
	/**
	 * If the record doesn't exist, then we insert it.  Otherwise we update the mutable fields.
	 * 
	 * TODO handle givenConsent and superUser fields.
	 * 
	 * @param conn the connection to the database
	 * @throws SQLException
	 */
	public void insertOrUpdate(Connection conn)
	throws SQLException
	{
	    Student student = lookupByCampusUID(campusUID, conn);
	    if (student == null)
	    {
	        executeInsert(conn);
	    }
	    else
	    {
	        update(conn);
            setStudentPK(student.getStudentPK());
	    }
	}
	
	public void update(Connection conn)
	throws SQLException
	{
	    String update = Queries.makeUpdateStatementWithWhereClause(
	            ATTRIBUTE_LIST,
	            "students",
	            " WHERE student_pk = ? ");
	    
	    PreparedStatement stmt = null;
	    try {
	        stmt = conn.prepareStatement(update);

	        int index = putValues(stmt, 1);
	        stmt.setString(index, getStudentPK());
	        
	        stmt.executeUpdate();
	        
	    } finally {
	        Queries.closeStatement(stmt);
	    }
	}
	
	private int putValues(PreparedStatement stmt, int index)
	throws SQLException
	{
	    stmt.setString(index++, getCampusUID());
	    stmt.setString(index++, getEmployeeNum());
	    stmt.setString(index++, getFirstname());
	    stmt.setString(index++, getLastname());
	    stmt.setString(index++, superUser);
	    stmt.setString(index++, getGivenConsent());
	    stmt.setString(index++, getLatestSubmissionPK());
        stmt.setString(index++, getPassword());
	    return index;
	}
	
	/**
	 * Returns true if this student (person, really) is a superuser, false otherwise.
	 * 
	 * Currently the only way to 
	 */
	public boolean isSuperUser()
	{
		if (superUser.equals("yes"))
			return true;
		return false;
	}
	
	/**
	 * Gets a student based on the studentPK.  This method will never return null; if the
	 * a student with the given studentPK cannot be found, an exception is thrown.
	 * 
	 * @param studentPK the PK of the student
	 * @param conn the connection to the database
	 * @return the student record if it's found.  This method never returns null; we throw
	 * an exception if no student exists with the given PK.
	 * @throws SQLException
	 */
	public static Student getByStudentPK(String studentPK, Connection conn)
	throws SQLException
	{
	    Student student = lookupByStudentPK(studentPK, conn);
	    if (student == null)
	        throw new SQLException("Unable to find student with PK: " +studentPK);
	    return student;
	}
	
	/**
	 * Looks up a student record based on its PK.
	 * 
	 * @param studentPK the PK of the student record
	 * @param connection the connection to the database
	 * @return the student record if it is found, null otherwise
	 * @throws SQLException
	 */
	public static Student lookupByStudentPK(String studentPK, Connection connection)
	throws SQLException
	{
	    String query = " SELECT " +ATTRIBUTES+
	    " FROM students " +
	    " WHERE students.student_pk = ? ";
	    
	    PreparedStatement stmt = connection.prepareStatement(query);
	    stmt.setString(1, studentPK);
	    
	    return getFromPreparedStatement(stmt);
	}
    /**
     * Looks up a student record based on its PK.
     * 
     * @param studentPK the PK of the student record
     * @param connection the connection to the database
     * @return the student record if it is found, null otherwise
     * @throws SQLException
     */
    public static Map<String, Student> lookupAllByCoursePK(String coursePK, Connection connection)
    throws SQLException
    {
        String query = " SELECT " +ATTRIBUTES+
        " FROM students,student_registration  " +
        " WHERE students.student_pk = student_registration.student_pk "   +  
        " AND student_registration.course_pk = ?";
        
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, coursePK);
        
        return getAllFromPreparedStatement(stmt);
    }
    
    /**
     * Looks up a student record based on its PK.
     * 
     * @param studentPK the PK of the student record
     * @param connection the connection to the database
     * @return the student record if it is found, null otherwise
     * @throws SQLException
     */
    public static Map<String, Student> lookupAll(Connection connection)
    throws SQLException
    {
        String query = " SELECT " +ATTRIBUTES+
        " FROM students";
        
        PreparedStatement stmt = connection.prepareStatement(query);
        
        return getAllFromPreparedStatement(stmt);
    }
    
 	
	public static Student lookupByCampusUID(String campusUID, Connection connection)
	throws SQLException
	{
		String query = "SELECT " +ATTRIBUTES+
		" FROM "+
		"students "+
		"WHERE students.campus_uid = ?";
		
		PreparedStatement stmt = connection.prepareStatement(query);
			
		stmt.setString(1, campusUID);
			
		return getFromPreparedStatement(stmt);
	}
	
	private static Student getFromPreparedStatement(PreparedStatement stmt)
            throws SQLException {
        try {
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Student student = new Student();
                student.fetchValues(rs, 1);
                return student;
            }
            return null;
        } finally {
            stmt.close();

        }
    }
    private static Map<String, Student> getAllFromPreparedStatement(PreparedStatement stmt)
            throws SQLException {
        Map<String, Student> result = new HashMap<String, Student>();
        try {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Student student = new Student();
                student.fetchValues(rs, 1);
                result.put(student.getStudentPK(), student);
            }
            return result;
        } finally {
            stmt.close();
        }
    }
	
	public static StatementAndResultSet lookupByCampusUID(String campusUID, 
			Student student, Connection connection)
	throws SQLException
	{
		String query = "SELECT " +ATTRIBUTES+
		" FROM "+
		" students "+
		" WHERE students.campus_uid = ?";
		
		boolean success = false;
		StatementAndResultSet stmt = null;
		
		try {
			stmt = new StatementAndResultSet(connection.prepareStatement(query,
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE));
			
			stmt.getStatement().setString(1, campusUID);
			
			stmt.execute();
			
			if (stmt.getResultSet().next()) {
				student.fetchValues(stmt.getResultSet(), 1);
				
				success = true;
			}
		} finally {
			if (!success && stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
		
		return stmt;
	}
    /**
     * Looks up a student record based on the student's employeeNum (these are unique IDs
     * supplied by the university, much like a Social Security Number (SSN).  Returns null
     * if no student record with that employeeNumber exists.
     *  
     * @param employeeNum the employee number
     * @return the student record if it exists, else null.
     */
    public static Student lookupByEmployeeNum(String employeeNum, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+ 
            " FROM " +TABLE_NAME+
            " WHERE employee_num = ? ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, employeeNum);
        return getFromPreparedStatement(stmt);
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object that) {
        Student thatStudent = (Student) that;
        int result = this.lastname.compareTo(thatStudent.lastname);
        if (result != 0) return result;
        result = this.firstname.compareTo(thatStudent.firstname);
        return result;
    }
    
    /**
     * @param emailAddress
     * @return
     */
    public static Student lookupByEmailAddress(String emailAddress,
            Connection conn)
    throws SQLException
    {
        // For demo accounts, we are storing:
        // email address in the campus_uid
        // password in the password field
        // random garbage in the employee_num field to start with that will be replaced with the
        // database primary key that will be generated
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM " +TABLE_NAME+
            " WHERE campus_uid = ? ";
        
        PreparedStatement stmt= conn.prepareStatement(query);
        stmt.setString(1, emailAddress);
        return getFromPreparedStatement(stmt);
    }
    
    /**
     * Static factory method that creates a student record with account_type='demo' that is
     * suitable for use with the demo server to be located at marmosetdemo.cs.umd.edu
     * @param emailAddress The email address of the demo user.
     * @param firstname The demo user's firstname.
     * @param lastname The demo user's lastname.
     * @param conn The connection to the database.
     * @return A fresh student record for this account.
     * @throws SQLException
     * 
     * TODO check for duplicates!
     */
    public static Student createNewDemoAccount(
            String emailAddress,
            String firstname,
            String lastname,
            Connection conn)
    throws SQLException
    {
        Student student = new Student();
        // we're overloading campusUID with the email address for demo accounts
        student.setCampusUID(emailAddress);
        // we're also overloading employeeNum with the password for demo accounts
        student.setPassword(nextRandomPassword());
        // Put something random in here; we're going to change this to the autoupdate
        // value as soon as the insert succeeds
        student.setEmployeeNum(nextRandomPassword());
        student.setFirstname(firstname);
        student.setLastname(lastname);
        //student.setAccountType(DEMO_ACCOUNT);
        student.insert(conn);
        // Reset the employeeNum so that it matches the studentPK
        // Demo accounts don't really need an employeeNum.
        student.setEmployeeNum(student.getStudentPK());
        student.update(conn);
        return student;
    }
    
    /**
     * Sets the account_type for this student record to 'demo' for use with marmosetdemo.cs.umd.edu
     * @param conn The connection to the database.
     * @throws SQLException
     */
    private void makeDemoAccount(Connection conn)
    throws SQLException
    {
        // Make sure that we don't try to change the status of a student record with no
        // studentPK.  This usually means that we forgot to insert this record into the DB.
        if (studentPK == null)
            throw new IllegalStateException("You cannot update a student record with null " +
            		" for a studentPK.  Did you forget to insert this record into the DB?");

        String update =
            " UPDATE " +TABLE_NAME+
            " SET account_type = " +DEMO_ACCOUNT+
            " WHERE student_pk = ? ";
        
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(update);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    private static SecureRandom rng = new SecureRandom();
    private static int MAX_PASSWORD_LENGTH = 10;
	private static String nextRandomPassword() {
		synchronized (rng) {
			long l = rng.nextLong();
			return Long.toHexString(l).substring(0, MAX_PASSWORD_LENGTH);
		}
	}
}
