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
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Student;

/**
 * MarylandAuthenticationService
 * @author jspacco
 */
public class MarylandAuthenticationService implements IAuthenticationService
{
    private boolean useSSL=false;

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.IAuthentication#authenticateLDAP(java.lang.String, java.lang.String, java.sql.Connection, boolean)
     */
    public Student authenticateLDAP(String campusUID, String uidPassword,
        Connection conn, boolean skipLDAP)
    throws SQLException, NamingException, ClientRequestException
    {
        //
        // LDAP Authentication
        //
        if (campusUID == null) {
            String msg = "campusUID or uidPassword null";
            throw new CanNotFindDirectoryIDException(
                    HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        //
        // Perform reverse-LDAP lookup
        //
        Student student = Student.lookupByCampusUID(campusUID, conn);
        if (student == null) {
            String msg = "Cannot find directoryID: " + campusUID;
            throw new CanNotFindDirectoryIDException(
                    HttpServletResponse.SC_UNAUTHORIZED, msg);
        }
        if (skipLDAP || authenticateViaLDAP(student.getEmployeeNum(), uidPassword)) {
            return student;
        }
        String msg = "Password incorrect for directoryID: " + campusUID;
        throw new BadPasswordException(HttpServletResponse.SC_UNAUTHORIZED, msg);
    }
    
    private boolean authenticateViaLDAP(String employeeNumber,
        String password) throws NamingException {
    //          if (employeeNumber.equals("104450831") && password.equals("qaz"))
    //          return true;
    try {
        Hashtable<String, String> env = new Hashtable<String, String>(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL,
                "ldap://directory.umd.edu/dc=people,dc=ldap,dc=umd,dc=edu");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        String principle = "employeeNumber=" + employeeNumber
                + ",dc=people,dc=ldap,dc=umd,dc=edu";
        env.put(Context.SECURITY_PRINCIPAL, principle);
        env.put(Context.SECURITY_CREDENTIALS, password);
        if (useSSL)
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        DirContext ctx = new InitialDirContext(env);
        ctx.close();
    } catch (AuthenticationException e) {
        return false;
    }
    return true;
    }
    
    private void useSSL(String cacertsFile, String cacertsPassword) {
        System.setProperty("javax.net.ssl.trustStore", cacertsFile);
        System.setProperty("javax.net.ssl.trustStorePassword", cacertsPassword);
        useSSL = false;
    }

}
