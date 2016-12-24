/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Dec 19, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Student;

/**
 * GenericStudentPasswordAuthenticationService
 * @author jspacco
 */
public class GenericStudentPasswordAuthenticationService implements IAuthenticationService 
{

    public Student authenticateLDAP(String campusUID, String uidPassword, Connection conn, boolean skipLDAP)
    throws SQLException, ClientRequestException
    {
        Student student=Student.lookupByCampusUID(campusUID,conn);
        if (student==null)
            throw new ClientRequestException("Cannot find user " +campusUID);
        if (skipLDAP)
            return student;
	//ON: disable password checking as this is done by Apache web-server
	/*
        if (uidPassword.equals(student.getPassword()))
            return student;
	*/
	return student;

	//this line is never reached
        //throw new BadPasswordException(HttpServletResponse.SC_UNAUTHORIZED, "Wrong password for user " +campusUID);
    }
    
}
