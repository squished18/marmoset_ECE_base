/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 10, 2005
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
 * @deprecated
 */
public class CapabilityCollection
{
    private List<Capability> collection = new ArrayList<Capability>();
    
    public void add(Capability capability)
    {
        collection.add(capability);
    }
    
    public Iterator<Capability> iterator()
    {
        return collection.iterator();
    }
    
    public static CapabilityCollection lookupAllByStudentPK(
            String studentPK,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +Capability.ATTRIBUTES+ 
            " FROM capabilities, student_registration " +
            " WHERE student_registration.student_pk = ? " +
            " AND capabilities.student_registration_pk = student_registration.student_registration_pk ";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, studentPK);

        ResultSet rs = stmt.executeQuery();

        CapabilityCollection collection = new CapabilityCollection();
        while (rs.next())
        {
            Capability capability = new Capability();
            capability.fetchValues(rs, 1);
            collection.add(capability);
        }
        return collection;
    }
}
