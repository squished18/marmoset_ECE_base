/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 9, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.umd.cs.submitServer.BestSubmissionPolicy;
import edu.umd.cs.submitServer.IAuthenticationService;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.SubmitServerDatabaseProperties;
import edu.umd.cs.submitServer.SubmitServerUtilities;
import edu.umd.cs.submitServer.shared.BuildServerMonitor;

/**
 * @author jspacco
 *
 * Base class for all servlets in the SubmitServer.  Provides methods for getting and releasing
 * database connections as well as access to certain well-defined loggers.
 * 
 * XXX Note that static methods in a subclass of HttpServlet are not useful because
 * most web containers will use a separate classloader for each servlet, so there's not as
 * much sharing happening.
 */
public abstract class SubmitServerServlet extends HttpServlet implements SubmitServerConstants
{
    private BuildServerMonitor buildServerMonitor;
    /** XXX We have to use the getInstance() method to make sure there is one shared
     * buildServerMonitor because most webapp containers load each servlet
     * with its own classloader, so marking the field static doesn't help.
     * BuildServerMonitor is being placed into a separate
     * jarfile and placed in tomcat/shared/lib so that it's loaded by a classloader
     * that's shared by all the webapps (and therefore all servlets within a single webapp).
     * For more details about where jarfiles/warfiles are being installed,
     * see the "generic.install" target in build.xml.  Also note that this works for
     * tomcat but I don't know the conventions for other servlet-container providers.
     * @return The BuildServerMonitor singleton.
     */
    protected BuildServerMonitor getBuildServerMonitor() {
        if (buildServerMonitor==null) {
            buildServerMonitor=BuildServerMonitor.getInstance();
        }
        return buildServerMonitor;
    }
	
	/**
	 * Logger for authentication information.
	 */
	private static Logger authenticationLog;
	protected Logger getAuthenticationLog() {
		if (authenticationLog==null) {
			authenticationLog=Logger.getLogger(AUTHENTICATION_LOG);
		}
		return authenticationLog;
	}
	/**
	 * Generic logger object for all servlets to use.
	 */
	private Logger submitServerServletLog;
	protected Logger getSubmitServerServletLog() {
		if (submitServerServletLog==null) {
			submitServerServletLog = Logger.getLogger(SubmitServerServlet.class);
			if ("true".equals(getServletContext().getInitParameter("DEBUG"))) {
				submitServerServletLog.setLevel((Level)Level.DEBUG);
    		}
		}
		return submitServerServletLog;
	}
	
	private SubmitServerDatabaseProperties submitServerDatabaseProperties;
	
    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        super.init();
        
        ServletContext servletContext = getServletContext();
        try {
            submitServerDatabaseProperties=new SubmitServerDatabaseProperties(servletContext);
        } catch (ClassNotFoundException e) {
            throw new ServletException(e);
        }
        getSubmitServerServletLog().debug("Initializing logger for " +getClass());
    }
    
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#log(java.lang.String, java.lang.Throwable)
	 */
	public void log(String msg, Throwable throwable) {
		getSubmitServerServletLog().info(msg, throwable);
	}
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#log(java.lang.String)
	 */
	public void log(String msg) {
		getSubmitServerServletLog().info(msg);
	}
    /**
     * Gets a connection to the database.
     * @return a connection to the database.
     * @throws SQLException
     */
    protected Connection getConnection() throws SQLException
	{
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
				getSubmitServerServletLog().warn("Unable to close connection", e);
        }
	}
	
	protected void handleSQLException(SQLException e)
	{
		// TODO Get rid of this method or make it throw a ServletException
	    // This method could in theory be used for logging and checking out SQLExceptions
        // but this method is *NOT* called everywhere where an SQLException is thrown
        // so it would probably be better to get rid of this method altogether.
	}
	
	protected void rollbackIfUnsuccessfulAndAlwaysReleaseConnection(boolean transactionSuccess, Connection conn)
	{
	    try {
	        if (!transactionSuccess)
	            if (conn != null) {
	                // TODO Log a stack trace as well!
                    getSubmitServerServletLog().warn("Rolling back a transaction");
                    conn.rollback();
                }
	    } catch (SQLException ignore) {
	    	getSubmitServerServletLog().warn("Unable to rollback connection", ignore);
	        // ignore
	    }
	    releaseConnection(conn);
	}
    
    private IAuthenticationService authenticationService;
    /**
     * Gets the implementatino of IAuthenticationService used to authenticate people for
     * to the submitServer.  The idea is that other institutions can write their own
     * implementations of IAuthenticationService to authenticate however they want.
     * <p>
     * The IAuthenticationService object is lazily initialized and so it's possible that
     * some errors that might be caught at init() time 
     * 
     * @return The concrete implementation of IAuthenticationService used by this
     * web application for authentication.
     * @throws ServletException
     */
    protected IAuthenticationService getIAuthenticationService()
    throws ServletException
    {
        // Return cached copy if we've already loaded it
        if (authenticationService!=null)
            return authenticationService;
        //Otherwise we *MUST* be able to load the class used for authentication 
        // or we're screwed.
        String authenticationServiceClassname = 
            getServletContext().getInitParameter(AUTHENTICATION_SERVICE);
        getSubmitServerServletLog().debug("authenticationServiceClass: " +authenticationServiceClassname);
        return (IAuthenticationService)SubmitServerUtilities.createNewInstance(authenticationServiceClassname);
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
        if (bestSubmissionPolicyMap.containsKey(className))
            return bestSubmissionPolicyMap.get(className);
        BestSubmissionPolicy bestSubmissionPolicy=(BestSubmissionPolicy)SubmitServerUtilities.createNewInstance(className);
        bestSubmissionPolicyMap.put(className, bestSubmissionPolicy);
        return bestSubmissionPolicy;
    }
}
