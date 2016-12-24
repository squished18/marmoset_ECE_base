/*
 * Copyright (C) 2005 University of Maryland
 * All Rights Reserved
 * Created on Jan 22, 2005
 */
package edu.umd.cs.buildServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.IOUtils;

/**
 * A test harness BuildServer that just needs a project jarfile,
 * submission zipfile, and an empty directory in order to run.
 * This is useful for realistic testing of submissions
 * and newly-developed projects without the need to
 * run a SubmitServer instance.
 * 
 * @author David Hovemeyer
 */
public class BuildServerTestHarness extends BuildServer {
	// Fields
	private String submissionZipFile;
	private String projectJarFile;
	private String testingBaseDirectory;
	
	/**
	 * Constructor.
	 * 
	 * @param submissionZipFile    filename of the submission zipfile
	 * @param projectJarFile       filename of the project jarfile
	 * @param testingBaseDirectory name of an empty directory to use for testing
	 */
	public BuildServerTestHarness(
			String submissionZipFile,
			String projectJarFile,
			String testingBaseDirectory) {
		this.submissionZipFile = submissionZipFile;
		this.projectJarFile = projectJarFile;
		this.testingBaseDirectory = testingBaseDirectory;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuildServer#initConfig()
	 */
	public void initConfig() throws IOException {
	    if (System.getenv("PMD_HOME") != null)
	        getConfig().setProperty(PMD_HOME, System.getenv("PMD_HOME"));
	    getConfig().setProperty(
				BUILD_DIRECTORY, new File(testingBaseDirectory, "build").getPath());
		getConfig().setProperty(
				TEST_FILES_DIRECTORY, new File(testingBaseDirectory, "testfiles").getPath());
		getConfig().setProperty(
				JAR_CACHE_DIRECTORY, new File(testingBaseDirectory, "jarcache").getPath());
		getConfig().setProperty(
				LOG_DIRECTORY, "console");
		getConfig().setProperty(
		        BUILDSERVER_ROOT, System.getenv("BUILDSERVER_ROOT"));
		
		// FIXME:  this is almost certainly wrong
		getConfig().setProperty(
				SUPPORTED_COURSE_LIST, "testSubmission.sh");
		// FIXME:  this is also wrong in general, but might allow me to do some testing
		getConfig().setProperty(
				SUPPORTED_COURSE_LIST, "CMSC433");
		
		getConfig().setProperty(DEBUG_VERBOSE, "true");
		getConfig().setProperty(DEBUG_DO_NOT_LOOP, "true");
		getConfig().setProperty(DEBUG_PRESERVE_SUBMISSION_ZIPFILES, "true");
		
		if (Boolean.getBoolean("debug.security"))
			getConfig().setProperty(DEBUG_SECURITY, "true");
		
		if (Boolean.getBoolean("runStudentTests"))
			getConfig().setProperty(RUN_STUDENT_TESTS, "true");
		
		String tools = System.getenv("tools.java");
		if (tools != null)
		    getConfig().setProperty(
		            ConfigurationKeys.INSPECTION_TOOLS_PFX + "java",
		            tools);
		
		String performCodeCoverage = System.getenv("PERFORM_CODE_COVERAGE");
		if ("true".equals(performCodeCoverage))
		    getConfig().setProperty(CODE_COVERAGE, "true");
		
		// TODO move the location of the Clover DB to the build directory.
		// NOTE: This requires changing the security.policy since Clover needs to be able
		// to read, write and create files in the directory.
		String cloverDBPath = "/tmp/myclover.db." + Long.toHexString(nextRandomLong());
		getConfig().setProperty(CLOVER_DB, cloverDBPath);
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuildServer#prepareToExecute()
	 */
	protected void prepareToExecute() {
		// Nothing to do here
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuildServer#getProjectSubmission()
	 */
	protected ProjectSubmission getProjectSubmission()
			throws MissingConfigurationPropertyException, IOException {
		
		ProjectSubmission projectSubmission = new ProjectSubmission(
				getConfig(),
				getLog(),
				"42", // submissionPK
				"17", // projectJarfilePK
				"false",
                "false");
		
		// Override the Files for the submission zipfile and project jarfile
		projectSubmission.zipFile = new File(submissionZipFile).getAbsoluteFile();
		projectSubmission.projectJarFile = new File(projectJarFile).getAbsoluteFile();
		
//		System.err.println("submission zipfile is " + projectSubmission.getZipFile());
//		System.err.println("project jarfile is " + projectSubmission.getProjectJarFile());
		
		return projectSubmission;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuildServer#downloadSubmissionZipFile(edu.umd.cs.buildServer.ProjectSubmission)
	 */
	protected void downloadSubmissionZipFile(ProjectSubmission projectSubmission)
			throws IOException {
		// We don't need to actually download the submission zipfile
		// (since it already exists).
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuildServer#releaseConnection(edu.umd.cs.buildServer.ProjectSubmission)
	 */
	protected void releaseConnection(ProjectSubmission projectSubmission) {
		// Nothing to do, no network connection
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuildServer#downloadProjectJarFile(edu.umd.cs.buildServer.ProjectSubmission)
	 */
	protected void downloadProjectJarFile(ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException, HttpException,
			IOException, BuilderException {
		// Nothing to do, project jarfile should already exist
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuildServer#reportTestResults(edu.umd.cs.buildServer.ProjectSubmission)
	 */
	protected void reportTestResults(ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException {
		ObjectOutputStream out = null;
		File outputFile = new File(testingBaseDirectory, "alltests.out");
		try {
			out = new ObjectOutputStream(new FileOutputStream(outputFile));
			projectSubmission.getTestOutcomeCollection().write(out);
			System.out.println("Test outcomes saved in " + outputFile.getPath());
			DumpTestOutcomes.dump(projectSubmission.getTestOutcomeCollection(), System.out);
			if (projectSubmission.getCodeMetrics() != null) {
                System.out.println("Code Metrics: " +projectSubmission.getCodeMetrics());
			}
		} catch (IOException e) {
			System.err.println("Could not save test outcome collection in " + outputFile.getPath());
			e.printStackTrace(); // OK, this is a command line app
		} finally {
            IOUtils.closeQuietly(out);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("Usage: " + BuildServerTestHarness.class.getName() +
					" <submission zipfile> <project jarfile> <work directory>");
			System.exit(1);
		}
		
		String submissionZipFile = args[0];
		String projectJarFile = args[1];
		String workDirectory = args[2];
		
		BuildServerTestHarness buildServer = new BuildServerTestHarness(
				submissionZipFile,
				projectJarFile,
				workDirectory);
		buildServer.initConfig();
		
		System.out.println("Testing submission " + submissionZipFile + " using test jar file " +
				projectJarFile);
		
		buildServer.executeServerLoop();
		
		getBuildServerLog().trace("Done.");
	}
	private static SecureRandom rng = new SecureRandom();

	private static long nextRandomLong() {
		synchronized (rng) {
			return rng.nextLong();
		}
	}

    @Override
    public int getNumServerLoopIterations()
    {
        return numServerLoopIterations;
    }
}
