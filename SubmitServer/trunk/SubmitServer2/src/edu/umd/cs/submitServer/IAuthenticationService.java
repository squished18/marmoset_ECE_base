/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Nov 21, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.marmoset.modelClasses.Student;

/**
 * IAuthentication
 * @author jspacco
 */
public interface IAuthenticationService
{
    @NonNull public Student authenticateLDAP(
        String campusUID,
        String uidPassword,
        Connection conn,
        boolean skipLDAP)
    throws SQLException, NamingException, ClientRequestException;
    
    
       
}
