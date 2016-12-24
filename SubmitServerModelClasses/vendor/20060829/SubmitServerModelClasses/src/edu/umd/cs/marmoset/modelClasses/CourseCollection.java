/*
 * Created on Sep 13, 2004
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author jspacco
 * TODO Factor out this class; it's useless!  The static lookup methods in here
 * should just return a List.
 */
public class CourseCollection
{
	private List<Course> courseList;
	
	public CourseCollection()
	{
		courseList = new ArrayList<Course>();
	}
	
	public Iterator<Course> iterator()
	{
	    return courseList.iterator();
	}
	
	public boolean isEmpty()
	{
	    return courseList.isEmpty();
	}
	
	public void add(Course course)
	{
		courseList.add(course);
	}
	
	public List<Course> getCollection()
	{
		return courseList;
	}

    /**
     * Looks up all the courses a student is taking.
     * 
     * @param studentPK the student PK
     * @param conn the connection to the database
     * @return The collection of courses the student is taking.
     * This may be an empty collection.  
     * @throws SQLException
     */
    public static CourseCollection lookupCoursesByStudentPK(String studentPK, 
    		Connection conn) throws SQLException
    {
    	String query = "SELECT " +Course.ATTRIBUTES+ " "+
    	"FROM courses, student_registration "+
    	"WHERE student_registration.student_pk = ? "+
    	"AND student_registration.course_pk = courses.course_pk ";
    	
    	PreparedStatement stmt = conn.prepareStatement(query);
    	stmt.setString(1, studentPK);
    	
    	return getAllFromPreparedStatement(stmt);
    }
    
    /**
     * Looks up all the courses currently in the database.
     * @param conn The connection to the database.
     * @return
     * @throws SQLException
     */
    public static CourseCollection lookupAll(Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +Course.ATTRIBUTES+
            " FROM courses ";
        PreparedStatement stmt = conn.prepareStatement(query);
        return getAllFromPreparedStatement(stmt);
    }
    
    private static CourseCollection getAllFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        try {
            ResultSet rs = stmt.executeQuery();
		
            CourseCollection courses = new CourseCollection();
            while (rs.next())
            {
                Course course = new Course();
                course.fetchValues(rs, 1);
                courses.add(course);
            }
            return courses;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) {
                    // Ignore
                }
            }
        }
    }
    
    public static CourseCollection lookupAllWithModifyCapability(
            String studentPK,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +Course.ATTRIBUTES+ " " +
            " FROM courses, student_registration " +
            " WHERE student_registration.student_pk = ? " +
            " AND student_registration.course_pk = courses.course_pk " +
            " AND student_registration.instructor_capability = ? ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, studentPK);
        stmt.setString(2, StudentRegistration.MODIFY_CAPABILITY);
        
        return getAllFromPreparedStatement(stmt);
    }

    
    public static CourseCollection lookupAllWithReadOnlyCapability(
            String studentPK,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +Course.ATTRIBUTES+ " " +
            " FROM courses, student_registration " +
            " WHERE student_registration.student_pk = ? " +
            " AND student_registration.course_pk = courses.course_pk " +
            " AND ( student_registration.instructor_capability = ? OR " +
            "	    student_registration.instructor_capability = ?) ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, studentPK);
        stmt.setString(2, StudentRegistration.READ_ONLY_CAPABILITY);
        stmt.setString(3, StudentRegistration.MODIFY_CAPABILITY);
        
        return getAllFromPreparedStatement(stmt);
    }
}
