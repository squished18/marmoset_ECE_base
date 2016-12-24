/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Apr 26, 2005
 *
 */
package edu.umd.cs.buildServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpException;

import edu.umd.cs.marmoset.modelClasses.Debug;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.marmoset.modelClasses.Snapshot;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * @author jspacco
 *
 */
public class StudentTestsBuildServer extends BuildServer
{
	private File testSetupFile;
	private File submissionZipfile;
	private String submissionPK;
	private File tmpDir;
	private Iterator snapshotListIterator;
	private Connection conn;
	
	StudentTestsBuildServer(File tmpDir, Iterator snapshotListIterator, File testSetupFile, Connection conn)
	{
	    this.tmpDir = tmpDir;
	    this.snapshotListIterator = snapshotListIterator;
	    this.testSetupFile = testSetupFile;
	    this.conn = conn;
	}
	
    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#initConfig()
     */
    public void initConfig() throws IOException
    {
        // Hard-code tools.java to specify our custom test case finder
		getConfig().setProperty(ConfigurationKeys.INSPECTION_TOOLS_PFX + "java", "StudentTestFinderRunner");
        
        // Don't run any tests
		getConfig().setProperty(ConfigurationKeys.SKIP_TESTS, "true");
		
		getConfig().setProperty(BUILD_DIRECTORY, tmpDir + "/build");
		getConfig().setProperty(TEST_FILES_DIRECTORY, tmpDir+ "/testfiles");
		getConfig().setProperty(JAR_CACHE_DIRECTORY, tmpDir+ "/jarcache");
		getConfig().setProperty(LOG_DIRECTORY, "console");
		
		getConfig().setProperty(DEBUG_PRESERVE_SUBMISSION_ZIPFILES, "true");
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#prepareToExecute()
     */
    protected void prepareToExecute()
            throws MissingConfigurationPropertyException
    {
        // nothing to do
        getLog().info("prepareToExecute()");
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#getProjectSubmission()
     */
    protected ProjectSubmission getProjectSubmission()
            throws MissingConfigurationPropertyException, IOException
    {
        ProjectSubmission projectSubmission = new ProjectSubmission(
				getConfig(),
				getLog(),
				submissionPK,
				"99",
				"false",
                "false");
		return projectSubmission;
    }
    
    int count=0;
    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#continueServerLoop()
     * This is basically an interator
     */
    protected boolean continueServerLoop()
    {
        if (snapshotListIterator.hasNext())
        {
            try {
                Snapshot snapshot = (Snapshot)snapshotListIterator.next();
                this.submissionPK = snapshot.getSubmissionPK();
                String submissionZipFileName = submissionPK+".zip";
                byte[] bytes = snapshot.downloadArchive(conn);
                File submissionZipfile= new File(tmpDir, submissionZipFileName);
                FileOutputStream fout = new FileOutputStream(submissionZipfile);
                fout.write(bytes, 0, bytes.length);
                fout.close();
                this.submissionZipfile = submissionZipfile;
                return true;
            } catch (SQLException e) {
                getLog().error(e);
            } catch (IOException e) {
                getLog().error(e);
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#downloadSubmissionZipFile(edu.umd.cs.buildServer.ProjectSubmission)
     */
    protected void downloadSubmissionZipFile(ProjectSubmission projectSubmission)
            throws IOException
    {
        projectSubmission.zipFile = submissionZipfile;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#releaseConnection(edu.umd.cs.buildServer.ProjectSubmission)
     */
    protected void releaseConnection(ProjectSubmission projectSubmission)
    {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#downloadProjectJarFile(edu.umd.cs.buildServer.ProjectSubmission)
     */
    protected void downloadProjectJarFile(ProjectSubmission projectSubmission)
            throws MissingConfigurationPropertyException, HttpException,
            IOException, BuilderException
    {
        projectSubmission.projectJarFile = testSetupFile;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#reportTestResults(edu.umd.cs.buildServer.ProjectSubmission)
     */
    protected void reportTestResults(ProjectSubmission projectSubmission)
            throws MissingConfigurationPropertyException
    {
        // nothing to do
    }
    
    protected boolean useServletAppender() {
		return false;
	}

    public static void main(String[] args)
    throws Exception
    {
        String projects = "p1 p2 p3 p4 p5 p7 p8 p9";
        String[] projectNumberList = projects.split("\\s+");
        
        Connection conn=null;
        try {
            conn=getConnection();
            
            String projectNumber = projectNumberList[6];
            Project project = Project.lookupByCourseProjectSemester(
                    "CMSC132",
                    projectNumber,
                    "Fall2004",
                    conn);
            
            ProjectJarfile projectJarfile = ProjectJarfile.lookupByProjectJarfilePK(
                    project.getProjectJarfilePK(),
                    conn);
            
            List snapshotList = Snapshot.lookupAllUniqueSnapshotsByProjectNumber(
                    project.getProjectPK(),
                    conn);
            
            File tmpDir = BuildServerUtilities.createTempDirectory();
            
            File testSetup=null;
            try {
                String testSetupName = "testSetup.zip";
                byte[] bytes = projectJarfile.downloadArchive(conn);
                testSetup = new File(tmpDir, testSetupName);
                FileOutputStream fout = new FileOutputStream(testSetup);
                fout.write(bytes, 0, bytes.length);
                fout.close();
            } finally {}
            
            Iterator snapshotListIterator = snapshotList.iterator();
            StudentTestsBuildServer buildServer = new StudentTestsBuildServer(tmpDir, snapshotListIterator, testSetup, conn);
            
            System.out.println(tmpDir.getAbsolutePath());
            
            buildServer.initConfig();
    		buildServer.executeServerLoop();
    		TestOutcomeCollection collection =
    		    buildServer.getProjectSubmission().getTestOutcomeCollection();
    		System.out.println("I found a total of " +collection.size()+ " student written outcomes");
    		
    		for (Iterator ii=collection.iterator(); ii.hasNext();)
    		{
    		    TestOutcome outcome = (TestOutcome)ii.next();
    		    System.out.println(outcome.getTestRunPK() +": " +outcome.getTestType() +": "+
    		            outcome.getTestName());
    		}
    		
        } finally {
            releaseConnection(conn);
        }
    }
    private static final String DRIVER = "org.gjt.mm.mysql.Driver";
    private static final String HOSTNAME = System.getProperty("hostname");

    static Connection getConnection()
    throws SQLException
    {
        try {
            // Load the database driver
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            Debug.error("Cannot find DB Driver " + DRIVER);
            e.printStackTrace();
        }
        
        String dbServer = "jdbc:mysql://marmoset2.umiacs.umd.edu/submitserver";
        if (HOSTNAME != null)
            dbServer = "jdbc:mysql://" +HOSTNAME+ "/submitserver";
            
        String dbUser = "root";
        String dbPassword= "blondie1980";
        
    	return DriverManager.getConnection(dbServer +
    	        "?user=" +dbUser+ 
    	        "&password=" +dbPassword);
    }
    
    static void releaseConnection(Connection conn)
    {
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignore) {
            // ignore
        }
    }
}
