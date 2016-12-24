/*
 * Created on Aug 30, 2004
 *
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jspacco
 *
 */
public class Course {
	/**
	 * List of all attributes for courses table.
	 */
     static final String[] ATTRIBUTE_NAME_LIST = {
            "course_pk",
            "semester",
            "courseName",
            "section",
            "description",
            "url"
	};

	/**
	 * Fully-qualified attributes for courses table.
	 */
	 public static final String ATTRIBUTES = Queries.getAttributeList("courses",
			ATTRIBUTE_NAME_LIST);
	
	private String coursePK;
	private String semester;
	private String courseName;
	private String section;
	private String description;
	private String url;

	/**
	 * @return Returns the courseName.
	 */
	public String getCourseName() {
		return courseName;
	}
	/**
	 * @param courseName The courseName to set.
	 */
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	/**
	 * @return Returns the coursePK.
	 */
	public String getCoursePK() {
		return coursePK;
	}
	/**
	 * @param coursePK The coursePK to set.
	 */
	public void setCoursePK(String coursePK) {
		this.coursePK = coursePK;
	}
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return Returns the section.
	 */
	public String getSection() {
		return section;
	}
	/**
	 * @param section The section to set.
	 */
	public void setSection(String section) {
		this.section = section;
	}
	/**
	 * @return Returns the semester.
	 */
	public String getSemester() {
		return semester;
	}
	/**
	 * @param semester The semester to set.
	 */
	public void setSemester(String semester) {
		this.semester = semester;
	}
	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void insert(Connection conn)
	throws SQLException
	{
	    String insert = 
	        " INSERT INTO courses " +
	        " VALUES " +
	        "	(DEFAULT," +
	        "	?," +
	        "	?," +
	        "	?," +
	        "	?," +
	        "	?) ";
	    
	    PreparedStatement stmt = null;
	    try {
	        stmt = conn.prepareStatement(insert);
	        stmt.setString(1, getSemester());
	        stmt.setString(2, getCourseName());
	        stmt.setString(3, getSection());
	        stmt.setString(4, getDescription());
	        stmt.setString(5, getUrl());
	        
	        stmt.executeUpdate();
	        
	        // set the coursePK to the PK of the last course inserted, which should
	        // have cached the previous auto-increment value
	        // TODO put this into a transaction
	        setCoursePK(Queries.lastInsertId(conn));
	    } finally {
	        Queries.closeStatement(stmt);
	    }
	}
	
	public int fetchValues(ResultSet resultSet, int startingFrom) throws SQLException
	{
		setCoursePK(resultSet.getString(startingFrom++));
		setSemester(resultSet.getString(startingFrom++));
		setCourseName(resultSet.getString(startingFrom++));
		setSection(resultSet.getString(startingFrom++));
		setDescription(resultSet.getString(startingFrom++));
		setUrl(resultSet.getString(startingFrom++));
		return startingFrom;
	}
	
	/**
	 * Finds a cousre in the database based in the name of the course.
	 * @param courseName
	 * @param conn
	 * @return the Course object representing the row found in the DB; null if it can't be found
	 * @throws SQLException
	 */
	public static Course lookupCourseByCourseNameSemester(String courseName, 
			String semester,
			Connection conn) throws SQLException
	{
		String query = "SELECT " +ATTRIBUTES+ " " +
				"FROM courses " +
				"WHERE courses.coursename = ? " +
				"AND courses.semester = ? ";
		
		PreparedStatement stmt = null;

		stmt = conn.prepareStatement(query);
		stmt.setString(1, courseName);
		stmt.setString(2, semester);
			
		return getCourseFromPreparedStatement(stmt);	
	}
	
	/**
	 * Finds a course based on its course PK.
	 *   
	 * @param coursePK the PK of the course
	 * @param conn the connection to the database
	 * @return the course if it's found.  If it's not found, SQLException will be 
	 * thrown.  This method never returns null.
	 * @throws SQLException if an error occurrs or the course is not found
	 */
	public static Course getByCoursePK(String coursePK, Connection conn)
	throws SQLException
	{
	    Course course = lookupByCoursePK(coursePK, conn);
	    if (course == null)
	        throw new SQLException("Course with PK " +coursePK+ " does not exist!");
	    return course;
	}
	
	/**
	 * Gets the course for which a particular project has been assigned.
	 * 
	 * @param projectPK the PK of the project
	 * @param conn the connection to the database
	 * @return the course if it's found; if not SQLException is thrown.  Will never
	 * return null.
	 * @throws SQLException
	 */
	public static Course getByProjectPK(String projectPK, Connection conn)
	throws SQLException
	{
	    String query = " SELECT " +ATTRIBUTES+ " " +
	    " FROM courses, projects " +
	    " WHERE coures.course_pk = projects.project_pk " +
	    " AND projects.project_pk = ? ";
	    
	    PreparedStatement stmt = conn.prepareStatement(query);
	    stmt.setString(1, projectPK);
	    
	    Course course = getCourseFromPreparedStatement(stmt);
	    if (course == null)
	        throw new SQLException("Cannot find course referenced by project with PK " +projectPK);
	    return course;
	}
	
	/**
	 * Looks up a a course based on the coursePK.
	 * 
	 * @param coursePK the course PK
	 * @param conn the connection to the database
	 * @return the Course if the coursePK is found; null otherwise
	 * @throws SQLException
	 */
	public static Course lookupByCoursePK(String coursePK, Connection conn)
	throws SQLException
	{
		String query = "SELECT " +ATTRIBUTES+ " " +
		"FROM courses " +
		"WHERE courses.course_pk = ?";
		
		PreparedStatement stmt = null;
		
		stmt = conn.prepareStatement(query);
		stmt.setString(1, coursePK);
		
		return getCourseFromPreparedStatement(stmt);
	}
	
	private static Course getCourseFromPreparedStatement(PreparedStatement stmt)
	throws SQLException
	{
		try {
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next())
			{
				Course course = new Course();
				course.fetchValues(rs, 1);
				return course;
			}
			return null;
		}
		finally {
			Queries.closeStatement(stmt);
		}
	}
    /**
     * Gets a course based on the studentRegistrationPK of someone registered for the course.
     * @param studentRegistrationPK
     * @param conn the connection to the database.
     * @return the Course if it is found; null if it doesn't exist.
     */
    public static Course lookupByStudentRegistrationPK(String studentRegistrationPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+
            " FROM courses, student_registration " +
            " WHERE student_registration.course_pk = courses.course_pk " +
            " AND student_registration.student_registration_pk = ? ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, studentRegistrationPK);
        return getCourseFromPreparedStatement(stmt);
    }
    /**
     * Gets a course from the databse by its courseName.
     * @param courseName The name of the course.
     * @param conn The connection to the database.
     * @return The course object; null if it can't be found.
     * @throws SQLException
     */
    public static Course lookupByCourseName(String courseName, Connection conn)
    throws SQLException
    {
        String query=
            " SELECT " +ATTRIBUTES+
            " FROM courses " +
            " WHERE coursename = ? ";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,courseName);
        return getCourseFromPreparedStatement(stmt);
    }

    public static List<Course> lookupAll(Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM courses ";
        PreparedStatement stmt = null;
        try {
            stmt=conn.prepareStatement(query);
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    private static List<Course> getAllFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        ResultSet rs = stmt.executeQuery();
        
        List<Course> list=new LinkedList<Course>();
        while (rs.next())
        {
            Course course = new Course();
            course.fetchValues(rs, 1);
            list.add(course);
        }
        return list;
    }

    public static List<Course> lookupAllByStudentPK(String studentPK, Connection conn)
    throws SQLException
    {
        String query = " SELECT " +ATTRIBUTES+ " "+
        " FROM courses, student_registration "+
        " WHERE student_registration.student_pk = ? "+
        " AND student_registration.course_pk = courses.course_pk ";
        
        PreparedStatement stmt = null;
        try {
            stmt=conn.prepareStatement(query);
            stmt.setString(1, studentPK);
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    public static List<Course> lookupallWithReadOnlyCapability(String studentPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +Course.ATTRIBUTES+ " " +
            " FROM courses, student_registration " +
            " WHERE student_registration.student_pk = ? " +
            " AND student_registration.course_pk = courses.course_pk " +
            " AND ( student_registration.instructor_capability = ? OR " +
            "       student_registration.instructor_capability = ?) ";
        
        PreparedStatement stmt = null;
        try {
            stmt=conn.prepareStatement(query);
            stmt.setString(1, studentPK);
            stmt.setString(2, StudentRegistration.READ_ONLY_CAPABILITY);
            stmt.setString(3, StudentRegistration.MODIFY_CAPABILITY);
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
}
