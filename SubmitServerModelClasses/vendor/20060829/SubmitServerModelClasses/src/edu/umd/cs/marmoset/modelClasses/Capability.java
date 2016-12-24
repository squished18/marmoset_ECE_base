/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Sep 28, 2004
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Capability class.  Capabilities currently are 'read-only' and 'modify', and having
 * 'modify' capability implies that you also have 'read-only' capability.  It's not
 * clear how, if at all, this class will expand and if each capability will have to
 * be a subset of some other capabilities.
 * 
 * @author jspacco
 * @deprecated
  */
public class Capability
{
    private String studentRegistrationPK;
    private String capability;
    
    public static final String NO_CAPABILITY = "none";
    /**
	 * List of all attributes for capabilities table
	 */
    final static String[] ATTRIBUTE_NAME_LIST = {
            "student_registration_pk",
            "capability"
    };
    
    public static final String TABLE_NAME = "capabilities";
    
    /**
     * fully-qualified attributes for the capabilities table.
     */
    public final static String ATTRIBUTES = Queries.getAttributeList(TABLE_NAME, ATTRIBUTE_NAME_LIST);
    
    /**
     * @return Returns the capability.
     */
    public String getCapability() {
        return capability;
    }
    /**
     * @param capability The capability to set.
     */
    public void setCapability(String capability) {
        this.capability = capability;
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
    
    public void fetchValues(ResultSet rs, int startingFrom)
    throws SQLException
    {
        setStudentRegistrationPK(rs.getString(startingFrom+0));
        setCapability(rs.getString(startingFrom+1));
    }
    
    /**
     * Looks up the capability for a given studentPK and coursePK.  Will return
     * null if no capability is found.
     * 
     * @param studentPK the PK of the student
     * @param coursePK the PK of the course
     * @param conn the connection to the database
     * @return the Capability if it's found; null otherwise
     * @throws SQLException
     */
    public static Capability lookupByStudentPKAndCoursePK(
            String studentPK,
            String coursePK,
            Connection conn)
    throws SQLException
    {
        String query = "SELECT " +ATTRIBUTES+ " " +
        " FROM " +TABLE_NAME+ ", student_registration, students " +
        " WHERE " +TABLE_NAME+ ".student_registration_pk = student_registration.student_registration_pk " +
        " AND student_registration.student_pk = students.student_pk " +
        " AND students.student_pk = ? " +
        " AND student_registration.course_pk = ? ";
        
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
        
            stmt.setString(1, studentPK);
            stmt.setString(2, coursePK);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Capability capability = new Capability();
                capability.fetchValues(rs, 1);
                return capability;
            }
            return null;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
    }
    
    public static String getByStudentPKAndCoursePK(
            String studentPK,
            String coursePK,
            Connection conn)
    throws SQLException
    {
        Capability capability = lookupByStudentPKAndCoursePK(studentPK, coursePK, conn);
        if (capability == null)
            return NO_CAPABILITY;
        // returning the singleton strings so that reference equality will work
        // I think this would work anyway but this seems easier
        String cap = capability.getCapability();
        if (cap.equals(StudentRegistration.READ_ONLY_CAPABILITY))
            return StudentRegistration.READ_ONLY_CAPABILITY;
        else if (cap.equals(StudentRegistration.MODIFY_CAPABILITY))
            return StudentRegistration.MODIFY_CAPABILITY;
        throw new IllegalStateException("Illegal capability in the database: " +cap);
    }
    public static Map<String, Capability> lookupAllByCoursePK(
            String coursePK,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +Capability.ATTRIBUTES+ 
            " FROM capabilities, student_registration " +
            " WHERE student_registration.course_pk = ? " +
            " AND capabilities.student_registration_pk = student_registration.student_registration_pk ";
        Map<String, Capability> result = new HashMap<String, Capability>();
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, coursePK);

        ResultSet rs = stmt.executeQuery();

        while (rs.next())
        {
            Capability capability = new Capability();
            capability.fetchValues(rs, 1);
            result.put(capability.getStudentRegistrationPK(), capability);
        }
        return result;
    }
}
