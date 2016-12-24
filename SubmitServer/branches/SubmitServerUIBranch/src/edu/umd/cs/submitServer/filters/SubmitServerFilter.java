/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 11, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.umd.cs.submitServer.BestSubmissionPolicy;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.SubmitServerDatabaseProperties;
import edu.umd.cs.submitServer.SubmitServerUtilities;

/**
 * @author jspacco
 *
 * Base class for all filters in the SubmitServer.  Provides utility methods for getting 
 * and releasing database connections, and establishes a general logger for all filters.
 * 
 */
public abstract class SubmitServerFilter implements Filter, SubmitServerConstants
{
	private  Logger authenticationLog;
	protected Logger getAuthenticationLog() {
		if (authenticationLog==null) {
			authenticationLog=Logger.getLogger(AUTHENTICATION_LOG);
		}
		return authenticationLog;
	}
    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
    	// Not sure what needs to be done in here...
    }

    private SubmitServerDatabaseProperties submitServerDatabaseProperties;
//    private  String dbServer;
//    private  String dbUser;
//    private  String dbPassword;
    
    protected ServletContext servletContext;
    private Logger submitServerFilterLog;
    protected Logger getSubmitServerFilterLog() {
    	if (submitServerFilterLog==null) {
    		submitServerFilterLog = Logger.getLogger(SubmitServerFilter.class);
    		if ("true".equals(servletContext.getInitParameter("DEBUG"))) {
    			submitServerFilterLog.setLevel((Level)Level.DEBUG);
    		}
    	}
    	return submitServerFilterLog;
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
    	servletContext = filterConfig.getServletContext();

        try {
            submitServerDatabaseProperties=new SubmitServerDatabaseProperties(servletContext);
        } catch (ClassNotFoundException e) {
            throw new ServletException(e);
        }
        /*
        // TODO allow app-specific over-rides
        String driver = servletContext.getInitParameter("database.driver");
        dbServer = servletContext.getInitParameter("database.server.jdbc.url");
	    dbUser = servletContext.getInitParameter("database.user");
	    dbPassword= servletContext.getInitParameter("database.password");
	    // Make sure we can load a database driver
	    try {
        	Class.forName(driver);
        } catch (ClassNotFoundException e) {
        	throw new ServletException(e);
        }
        */
        getSubmitServerFilterLog().debug("Initializing logger for " +getClass());
    }
    
    /**
     * Gets a connection to the database.
     * @return a connection to the database.
     * @throws SQLException
     */
    protected Connection getConnection() throws SQLException
	{	    
        getSubmitServerFilterLog().warn("database server is: " +submitServerDatabaseProperties.getDatabaseServer());
        return submitServerDatabaseProperties.getConnection();
	}
	
    /**
     * Releases a database connection.
     * Swallows (or handles) any SQLExceptions that happen since there's nothing
     * the web application can do if a database connection cannot be closed.
     * @param conn the connection to release
     */
    protected void releaseConnection(Connection conn)
	{
	    try {
	        submitServerDatabaseProperties.releaseConnection(conn);
        } catch (SQLException e) {
            getSubmitServerFilterLog().warn(e.getMessage(), e);
        }
	}
	
	protected void handleSQLException(SQLException e)
	{
	    // log SQLException
		getSubmitServerFilterLog().info(e.getMessage(), e);
	}
    
    private static Map<String,BestSubmissionPolicy> bestSubmissionPolicyMap=new HashMap<String,BestSubmissionPolicy>();
    /**
     * Returns an instance of the BestSubmissionPolicy class with the given className.
     * Returns an instance of DefaultBestSubmissionPolicy if className is null.
     * @param className The name of the BestSubmissionPolicy subclass to return. 
     * @return An instance of a subclass of BestSubmissionPolicy.
     * @throws ServletException If anything goes wrong the exception will be wrapped with a
     *      ServletException, which will be thrown.
     */
    public static BestSubmissionPolicy getBestSubmissionPolicy(String className)
    throws ServletException
    {
        if (className==null)
            className=DEFAULT_BEST_SUBMISSION_POLICY;
        if (bestSubmissionPolicyMap.containsKey(className))
            return bestSubmissionPolicyMap.get(className);
        BestSubmissionPolicy bestSubmissionPolicy=(BestSubmissionPolicy)SubmitServerUtilities.createNewInstance(className);
        bestSubmissionPolicyMap.put(className, bestSubmissionPolicy);
        return bestSubmissionPolicy;
    }
}
