/*
 * Copyright (C) 2005 University of Maryland
 * All Rights Reserved
 * Created on April 18, 2005
 */
package edu.umd.cs.buildServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpException;

/**
 * A variation of BuildServer which runs FindBugs on
 * submissions, and saves the complete XML output to
 * a directory.  Submissions and test setup files
 * are read from specified directories in the filesystem.
 * A text file (the submission list) is used to find the
 * submission pks and test setup pks.
 * 
 * @author David Hovemeyer
 */
public class FindBugsBuildServer extends BuildServer {
	private String configFile;
	private BufferedReader submissionListReader;
	private File jarCacheDir;
	private File submissionCacheDir;
	private File findBugsOutputDir;
	
	private int currentSubmissionIndex;
	private String currentSubmissionPK;
	private String currentTestSetupPK;
	
	// These are used if we're just checking a subset of the submissions.
	// This is useful for dividing up work for cluster nodes.
	private int startSubmission = -1;
	private int numSubmissions;

	/**
	 * Constructor.
	 * 
	 * @param configFile              BuildServer configuration file
	 * @param submissionListFile      file specifying list of (submission_pk,test_setup_pk) pairs,
	 *                                comma or tab separated 
	 * @throws FileNotFoundException
	 */
	public FindBugsBuildServer(
			String configFile,
			String submissionListFile)
			throws FileNotFoundException {
		this.configFile = configFile;
		this.submissionListReader = new BufferedReader(new FileReader(submissionListFile));
	}

	public void initConfig() throws IOException {
		getConfig().load(new FileInputStream(configFile));
		
		// Hard-code tools.java to specify our custom FindBugsRunner
		getConfig().setProperty(ConfigurationKeys.INSPECTION_TOOLS_PFX + "java", "XMLProducingFindBugsRunner");
	
		// Don't run any tests
		getConfig().setProperty(ConfigurationKeys.SKIP_TESTS, "true");
		
		// Don't get build info
		getConfig().setProperty(ConfigurationKeys.SKIP_BUILD_INFO, "true");
		
		// We may just be doing a subset of submissions
		startSubmission = getConfig().getOptionalIntegerProperty("submissions.start", -1);
		numSubmissions = getConfig().getOptionalIntegerProperty("submissions.num", 0);
	}
	
	protected boolean useServletAppender() {
		return false;
	}

	protected void prepareToExecute() throws MissingConfigurationPropertyException {
		jarCacheDir = new File(getConfig().getRequiredProperty(ConfigurationKeys.JAR_CACHE_DIRECTORY));
		submissionCacheDir = new File(
				getConfig().getRequiredProperty(ConfigurationKeys.SUBMISSION_DIRECTORY));
		findBugsOutputDir = new File(
				getConfig().getRequiredProperty(ConfigurationKeys.FINDBUGS_OUTPUT_DIRECTORY));
		
		if (startSubmission >= 0) {
			getLog().info("Testing submission range " + startSubmission + ".." + (startSubmission + numSubmissions));
		}
	}

	protected boolean continueServerLoop() {
		try {
			return getNextSubmissionToTest();
		} catch (IOException e) {
			return false;
		}
	}

	private boolean getNextSubmissionToTest() throws IOException {
		if (startSubmission >= 0) {
			// If needed, skip submissions at the beginning of the submission list
			if (currentSubmissionIndex == 0) {
				getLog().info("Skipping initial submissions...");
				// Skip all submissions until we get to the start submission
				while (currentSubmissionIndex < startSubmission) {
					if (!getNextSubmissionTuple())
						return false;
					++currentSubmissionIndex;
				}
				getLog().info("Initial submissions skipped, now positioned at " + currentSubmissionIndex);
			}
			
			// Did we go past the end of our submission range?
			if (currentSubmissionIndex >= startSubmission + numSubmissions) {
				getLog().info("At the end of the submission range (" + currentSubmissionIndex + ")");
				return false;
			}
		}
		
		if (!getNextSubmissionTuple())
			return false;
		
		++currentSubmissionIndex;
		return true;
	}

	/**
	 * Get the next (submission pk, project pk) tuple from the submission list.
	 * 
	 * @return true if we got a valid tuple, false if not or if we reached EOF
	 * @throws IOException
	 */
	private boolean getNextSubmissionTuple() throws IOException {
		String line = submissionListReader.readLine();
		if (line == null)
			return false;
		ArrayList<String> arr = new ArrayList<String>();
		StringTokenizer t = new StringTokenizer(line, "\t,");
		while (t.hasMoreTokens()) {
			String token = t.nextToken();
			arr.add(token);
		}
		if (arr.size() != 2) {
			return false;
		}
		currentSubmissionPK = arr.get(0);
		currentTestSetupPK = arr.get(1);
		
		return true;
	}

	protected ProjectSubmission getProjectSubmission()
			throws MissingConfigurationPropertyException, IOException {
		ProjectSubmission projectSubmission = new ProjectSubmission(
				getConfig(),
				getLog(),
				currentSubmissionPK,
				currentTestSetupPK,
				"false",
                "false");
		return projectSubmission;
	}

	protected void downloadSubmissionZipFile(ProjectSubmission projectSubmission)
			throws IOException {
		IO.copyFile(
				new File(submissionCacheDir, projectSubmission.getSubmissionPK() + ".zip"),
				projectSubmission.getZipFile()
				);
	}

	protected void releaseConnection(ProjectSubmission projectSubmission) {
		// Nothing to do
	}

	protected void downloadProjectJarFile(ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException, HttpException,
			IOException, BuilderException {
		// We can use the existing project jarfile in place by simply modifying
		// the ProjectSubmission.
		projectSubmission.projectJarFile = new File(jarCacheDir, projectSubmission.getProjectJarfilePK() + ".jar");
	}

	protected void reportTestResults(ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException {
		// Nothing to do
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: " + FindBugsBuildServer.class.getName() +
					" <config file> <submission list file>");
			System.exit(1);
		}
		
		String configFile = args[0];
		String submissionList = args[1];
		
		FindBugsBuildServer buildServer = new FindBugsBuildServer(configFile, submissionList);
		buildServer.initConfig();
		buildServer.executeServerLoop();
	}

}
