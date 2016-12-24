/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Sep 16, 2004
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;


/**
 * @deprecated Use the StudentRegistration.lookupAll...() methods that return a List
 * rather than this collection class.
 * @author jspacco
 * 
 */
public class StudentRegistrationCollection
{
    private TreeSet<StudentRegistration> studentRegistrations;
    private Map<String, StudentRegistration> studentRegistrationMap = new HashMap<String, StudentRegistration>();
     
    public StudentRegistrationCollection()
    {
        studentRegistrations = new TreeSet<StudentRegistration>();
    }
    
    public void add(StudentRegistration registration)
    {
        studentRegistrations.add(registration);
        studentRegistrationMap.put(registration.getStudentRegistrationPK(), registration);

    }
       
    public Iterator<StudentRegistration> iterator()
    {
        return studentRegistrations.iterator();
    }
    
    public TreeSet<StudentRegistration> getRegistrations() {
        return studentRegistrations;
    }
    
    public StudentRegistration getByStudentRegistrationPK(String studentRegistrationPK) {
        return studentRegistrationMap.get(studentRegistrationPK);
    }
    public int size()
    {
        return studentRegistrations.size();
    }

    public static StudentRegistrationCollection lookupAllByProjectPK(String projectPK, Connection conn)
    throws SQLException
    {
        String query = StudentRegistration.queryString(", student_submit_status") + " AND student_submit_status.project_pk = ? " +
        " AND student_registration.student_registration_pk = student_submit_status.student_registration_pk ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, projectPK);
        
        return getAllFromPreparedStatement(stmt);
    }

    public static StudentRegistrationCollection lookupAllByCoursePK(String coursePK, Connection conn)
    throws SQLException
    {
        String query = StudentRegistration.queryString("") + " AND student_registration.course_pk = ? ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, coursePK);
        
        return getAllFromPreparedStatement(stmt);
    }

    private static StudentRegistrationCollection getAllFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        try {
            ResultSet rs = stmt.executeQuery();
            
            StudentRegistrationCollection collection = new StudentRegistrationCollection();
            while (rs.next())
            {
                StudentRegistration registration = new StudentRegistration();
                registration.fetchValues(rs, 1);
                collection.add(registration);
            }
            return collection;
        }
        finally {
            Queries.closeStatement(stmt);
        }
    }

    /**
     * Returns a collection of studentRegistrations for the given studentPK.
     * @param studentPK the studentPK
     * @param conn the connection to the database
     * @return a collection of studentRegistration records for the given studentPK;
     * returns an empty collection if no records exist for the given studentPK.
     */
    public static StudentRegistrationCollection lookupAllByStudentPK(String studentPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +StudentRegistration.ATTRIBUTES+
            " FROM student_registration " +
            " WHERE student_pk = ? ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, studentPK);
        
        return getAllFromPreparedStatement(stmt);
    }
}
