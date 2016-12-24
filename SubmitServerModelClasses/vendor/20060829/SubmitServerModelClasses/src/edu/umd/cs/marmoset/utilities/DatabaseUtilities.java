/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 16, 2005
 *
 */
package edu.umd.cs.marmoset.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author jspacco
 * TODO Write a version of getConnection() that uses dbunit to read an xml file.
 */
public final class DatabaseUtilities
{
    private static final boolean DEBUG=Boolean.getBoolean("DEBUG");
    private static final String SERVER=System.getProperty("database");
    
    private static final String DRIVER = System.getProperty("database.driver", "org.gjt.mm.mysql.Driver");
    private static Properties dbProps = null;
    private static final String MY_CNF_FILE=System.getProperty("my.cnf.file");
    

    private DatabaseUtilities() {}
    
    static {
        try {
            // Load the database driver
            Class.forName(DRIVER);
            if (DEBUG)
                System.out.println("Loaded db driver " +DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't find DB Driver: " +e.getMessage(),e);
        }
    }
    
    public static Properties getDbProps()
    throws SQLException
    {
        // Very hack-ish method
        // This will read all the properties out of the .my.cnf file, but doesn't do
        // very intelligently.  For example, it doesn't distinguish between the various
        // different categories like [mysql] vs. [mysqladmin] vs [client], and will
        // use the last user/password that show up in the file.
        // Thus the primary use of this method is to get the password out of the
        // .my.cnf file where it needs to be stored anyway.
        if (dbProps==null) {
            try {
                dbProps = new Properties();
                File myCnfFile = new File(System.getenv("HOME") +"/.my.cnf");
                if (MY_CNF_FILE != null)
                    myCnfFile = new File(MY_CNF_FILE);
                dbProps.load(new FileInputStream(myCnfFile));
            } catch (IOException e) {
                throw new SQLException(e.getMessage());
            }
        }
        return dbProps;
    }
    /**
     * This is the preferred method of connecting to the database.
     * Will use the $HOME/.my.cnf file (if one is avaialable) unless
     * over-ridden by setting -Dmy.cnf.file=&lt;filename&gt;
     * @param dbServer A string representing the protocol, database driver,
     *      URL, port and name of the database. 
     * @return A connection to the database.
     * @throws SQLException
     */
    public static Connection getConnection(String dbServer)
    throws SQLException
    {
        return getConnection(dbServer, getDbProps());
    }
    
    public static Connection getConnection(String dbServer, Properties props)
    throws SQLException
    {
        if (DEBUG) {
            System.out.println("dbServer: " +dbServer);
            System.out.println("properties: " +props);
        }
        return DriverManager.getConnection(dbServer, props);
    }
    
    public static Connection getConnection()
    throws SQLException
    {
        if (SERVER!=null) {
            return getConnection(SERVER);
        }
        return getConnection(Integer.parseInt(getDbProps().getProperty("port","3306")));
    }
    
    public static Connection getConnection(int port)
    throws SQLException
    {
        String host=getDbProps().getProperty("host");
        if (host==null) {
            host="localhost";
        }
        String dbServer="jdbc:mysql://" +
            host+ ":" +port+ 
            "/"+ getDbProps().getProperty("database");
        return getConnection(dbServer);
    }
    
    public static void releaseConnection(Connection conn)
    {
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignore) {
            // ignore
        }
    }
}
