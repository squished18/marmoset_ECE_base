/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on May 9, 2006
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;

/**
 * SubmitServerDatabaseProperties
 * @author jspacco
 */
public class SubmitServerDatabaseProperties
{
    private String databaseUser;
    private String databasePassword;
    private String databaseServer;
    private String databaseDriver;
    
    /**
     * Retrieve either an initParameter or its over-ridden value from the given servletContext.
     * <p>
     * Tomcat allows you to set initParameters in two different places (tomcat/conf/web.xml for
     * the entire tomcat server, or in webapp/WEB-INF/web.xml of the web-app's warfile, 
     * which will be limited to only that web-app).  You
     * unfortunately <b>cannot</b> set initParameters in both files, or the server will
     * fail when it tries to load.  This is very, very annoying.
     * <p>
     * This method (a hack that fixes this limitation) first looks for an "override" parameter,
     * which is an initParameter in
     * webapp/WEB-INF/web.xml with the suffix "__override" that has the same name
     * as an initParameter in tomcat/conf/web.xml.  For example, tomcat/conf/web.xml may
     * have "database.user" set to "root", while webapp/WEB-INF/web.xml will have
     * "database.user__override" set to "normal_user" instead.
     * <p>
     * This method returns the override parameter, if any; otherwise if an override parameter 
     * is not available, then the method instead returns the regular initParameter.
     * 
     * @param key The key of the initParameter.
     * @param servletContext The servletContext.
     * @return The value that the given initParameter key is bound to in the given 
     * servletContext.
     */
    private static String getInitParameterOrOverride(String key, ServletContext servletContext)
    {
        String value=servletContext.getInitParameter(key + "__override");
        if (value!=null)
            return value;
        return servletContext.getInitParameter(key);
    }
    
    public SubmitServerDatabaseProperties(ServletContext servletContext)
    throws ClassNotFoundException
    {
        databaseDriver = getInitParameterOrOverride("database.driver",servletContext);
        databaseServer = getInitParameterOrOverride("database.server.jdbc.url",servletContext);
        databaseUser= getInitParameterOrOverride("database.user",servletContext);
        databasePassword= getInitParameterOrOverride("database.password",servletContext);
        // Allow over-rides of the database name
        // Used for access to archival data where we're not using the default "submitserver"
        // database name; for example jdbc:mysql://localhost:4306/fall2005 on marmoset6.
        // I think we can assume that there will always be a database name in the databaseServer
        // variable or else we won't know what DB to access anyway.
        String databaseName=getInitParameterOrOverride("database.name",servletContext);
        if (databaseName != null) {
            int index=databaseServer.lastIndexOf('/');
            if (index >= 0) {
                databaseServer=databaseServer.substring(0,index+1);
                databaseServer += databaseName;
            }
        }
        // Try to load the database Driver; if no suitable driver is found we want to fail 
        // as early as possible!
        Class.forName(databaseDriver);
    }
    
    public Connection getConnection() throws SQLException
    {       
        Connection conn = DriverManager.getConnection(databaseServer +
                "?user=" +databaseUser+ 
                "&password=" +databasePassword);
        return conn;
    }
    
    public void releaseConnection(Connection conn)
    throws SQLException
    {
        if (conn != null)
            conn.close();
    }
    
    /**
     * @return Returns the databaseDriver.
     */
    public String getDatabaseDriver()
    {
        return databaseDriver;
    }
    /**
     * @param databaseDriver The databaseDriver to set.
     */
    public void setDatabaseDriver(String databaseDriver)
    {
        this.databaseDriver = databaseDriver;
    }
    /**
     * @return Returns the databasePassword.
     */
    public String getDatabasePassword()
    {
        return databasePassword;
    }
    /**
     * @param databasePassword The databasePassword to set.
     */
    public void setDatabasePassword(String databasePassword)
    {
        this.databasePassword = databasePassword;
    }
    /**
     * @return Returns the databaseServer.
     */
    public String getDatabaseServer()
    {
        return databaseServer;
    }
    /**
     * @param databaseServer The databaseServer to set.
     */
    public void setDatabaseServer(String databaseServer)
    {
        this.databaseServer = databaseServer;
    }
    /**
     * @return Returns the databaseUser.
     */
    public String getDatabaseUser()
    {
        return databaseUser;
    }
    /**
     * @param databaseUser The databaseUser to set.
     */
    public void setDatabaseUser(String databaseUser)
    {
        this.databaseUser = databaseUser;
    }
    
    
}
