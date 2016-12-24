/*
 * Copyright (C) 2005 University of Maryland
 * All Rights Reserved
 * Created on Jan 22, 2005
 */
package edu.umd.cs.buildServer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;

import edu.umd.cs.marmoset.modelClasses.HttpHeaders;

/**
 * Request projects to build from the SubmitServer,
 * build them, perform quick and release tests, and
 * send the results back to the SubmitServer.
 * This class contains the main() method which runs the build
 * server.
 * 
 * @author David Hovemeyer
 */
public class BuildServerDaemon extends BuildServer implements ConfigurationKeys {
	private String configFile;
	
	/** Our HttpClient instance. */
	private HttpClient client;

	/**
	 * Constructor.
	 */
	public BuildServerDaemon() {
	}
	
	/**
	 * Set the name of the BuildServer configuration file.
	 * 
	 * @param configFile
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuildServer#prepareToExecute()
	 */
	protected void prepareToExecute() {
		this.client = new HttpClient();
		client.setConnectionTimeout(5000);
		//client.setTimeout(50000);
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuildServer#initConfig()
	 */
	public void initConfig() throws IOException {
		// TODO: Verify that all the important parameters are set, and FAIL EARLY is necessary
        // TODO: Make getter methods in Config for the required params (build.directory, test.files.directory, etc)
        getConfig().load(new BufferedInputStream(new FileInputStream(configFile)));
		
		// I'm setting the clover binary database in this method rather than in
		// the config.properties file because it always goes into /tmp and I need
		// a unique name in case there are multiple buildServers on the same host
		
		// TODO move the location of the Clover DB to the build directory.
		// NOTE: This requires changing the security.policy since Clover needs to be able
		// to read, write and create files in the directory.
		//
        //String cloverDBPath = getConfig().getRequiredProperty(BUILD_DIRECTORY) +"/myclover.db";
        String cloverDBPath = "/tmp/myclover.db." + Long.toHexString(nextRandomLong());
        getConfig().setProperty(CLOVER_DB, cloverDBPath);
	}

	private String getServletURL(String key) throws MissingConfigurationPropertyException {
		String protocol = getConfig().getRequiredProperty(SUBMIT_SERVER_PROTOCOL);
		String host = getConfig().getRequiredProperty(SUBMIT_SERVER_HOST);
		String port = getConfig().getRequiredProperty(SUBMIT_SERVER_PORT);
		String path = getConfig().getRequiredProperty(key);
		return protocol + "://" + host + ":" + port + path;
	}
	
	private String getRequestProjectURL() throws MissingConfigurationPropertyException {
		return getServletURL(SUBMIT_SERVER_REQUESTPROJECT_PATH);
	}
	
	private String getGetProjectJarURL() throws MissingConfigurationPropertyException {
		return getServletURL(SUBMIT_SERVER_GETPROJECTJAR_PATH);
	}
	
	private String getReportTestResultsURL() throws MissingConfigurationPropertyException {
		return getServletURL(SUBMIT_SERVER_REPORTTESTRESULTS_PATH);
	}

	/**
	 * Get a required header value.
	 * If the header value isn't specified in the server response,
	 * returns null.
	 * 
	 * @param method     the HttpMethod representing the request/response
	 * @param headerName the name of the header
	 * @return the value of the header, or null if the header isn't present
	 * @throws HttpException
	 */
	private String getRequiredHeaderValue(HttpMethod method, String headerName) throws HttpException {
		Header header = method.getResponseHeader(headerName);
		if (header == null || header.getValues().length != 1) {
			getLog().error("Internal error: Missing header " + headerName +
					" in submit server response");
			return null;
		}
		return header.getValue();
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuildServer#getProjectSubmission()
	 */
	protected ProjectSubmission getProjectSubmission()
			throws MissingConfigurationPropertyException, IOException {

		MultipartPostMethod method = new MultipartPostMethod(getRequestProjectURL());
	
		String supportedCoursePKList = getConfig().getRequiredProperty(SUPPORTED_COURSE_LIST);
		String semester = getConfig().getOptionalProperty(SEMESTER);
		if (semester != null)
		    method.addParameter("semester", semester);
		
		method.addParameter("password", getConfig().getRequiredProperty(SUBMIT_SERVER_PASSWORD));
		method.addParameter("hostname", getConfig().getRequiredProperty(HOSTNAME));
		method.addParameter("courses", supportedCoursePKList);
		        
		BuildServer.printURI(getLog(), method);
		
                getLog().debug("executing method");
		int responseCode = client.executeMethod(method);
                getLog().debug("done executing method");
		if (responseCode != HttpStatus.SC_OK) {
			if (responseCode == 503) {
				getLog().trace("Server returned 503 (no work)");
			} else {
				getLog().error("HTTP server returned non-OK response: " +
						responseCode + ": " + method.getStatusText());
				getLog().error("Full error message: " +method.getResponseBodyAsString());
			}
			return null;
		}
		
		getLog().debug("content-type: " + method.getResponseHeader("Content-type"));
		getLog().debug("content-length: " + method.getResponseHeader("content-length"));
		
		// Ensure we have a submission PK.
		String submissionPK = getRequiredHeaderValue(method, HttpHeaders.HTTP_SUBMISSION_PK_HEADER);
		if (submissionPK == null)
			return null;
		
		// Ensure we have a project PK.
		String projectJarfilePK = getRequiredHeaderValue(method, HttpHeaders.HTTP_PROJECT_JARFILE_PK_HEADER);
		if (projectJarfilePK == null)
			return null;
		
		// This is a boolean value specifying whether the project jar file
		// is NEW, meaning that it needs to be tested against the
		// canonical project solution.  The build server doesn't need
		// to do anything with this value except pass it back to
		// the submit server when reporting test outcomes.
		String isNewProjectJarfile = getRequiredHeaderValue(method, HttpHeaders.HTTP_NEW_PROJECT_JARFILE);
		if (isNewProjectJarfile == null)
			return null;
        
        // Opaque boolean value representing whether this was a "background retest".
        // The BuildServer doesn't need to do anything with this except pass it
        // back to the SubmitServer.
        String isBackgroundRetest = getRequiredHeaderValue(method, HttpHeaders.HTTP_BACKGROUND_RETEST);
        if (isBackgroundRetest==null)
            isBackgroundRetest="no";
        
        getLog().debug("Background Retest: " +isBackgroundRetest);
        
        ServletAppender servletAppender = (ServletAppender)getLog().getAppender("servletAppender");
        if (isBackgroundRetest.equals("yes"))
            servletAppender.setThreshold(Level.FATAL);
        else
            servletAppender.setThreshold(Level.INFO);

		
		ProjectSubmission projectSubmission = new ProjectSubmission(
				getConfig(),
				getLog(),
				submissionPK,
				projectJarfilePK,
				isNewProjectJarfile,
                isBackgroundRetest);
		
		projectSubmission.setMethod(method);
		
		return projectSubmission;
	}
	
	protected void downloadSubmissionZipFile(ProjectSubmission projectSubmission)
			throws IOException {
		IO.download(projectSubmission.getZipFile(), projectSubmission.getMethod());
	}
	
	protected void downloadProjectJarFile(ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException, HttpException, IOException, BuilderException {
		// FIXME: We should cache these
		
		MultipartPostMethod method = new MultipartPostMethod(getGetProjectJarURL());
		method.addParameter("password", getConfig().getRequiredProperty(SUBMIT_SERVER_PASSWORD));
		method.addParameter("projectJarfilePK", projectSubmission.getProjectJarfilePK());
		
		
		BuildServer.printURI(getLog(), method);
		
		try {
			int responseCode = client.executeMethod(method);
			if (responseCode != HttpStatus.SC_OK) {
				throw new BuilderException("Could not download project jar file: " + responseCode
						+ ": " + method.getStatusText());
			}
			
			getLog().trace("Downloading project jar file");
			IO.download(projectSubmission.getProjectJarFile(), method);
			
			// We're passing the project_jarfile_pk so we don't need to read it from
			// the headers
			
			// wait for a while in case the files have not "settled"
            // TODO: Verify that this is still necessary; should be OK unless run on NFS
			pause(3000);
					
			getLog().trace("Done.");
		} finally {
			method.releaseConnection();
		}

	}
	
	protected void releaseConnection(ProjectSubmission projectSubmission) {
		projectSubmission.getMethod().releaseConnection();
	}
	
    private void dumpOutcomes(ProjectSubmission projectSubmission) {
        // Can't dump outcomes if we don't have a test.properties file.
        if (projectSubmission.getTestProperties()==null)
            return;
        ObjectOutputStream out = null;
        File outputFile = new File(projectSubmission.getBuilderAndTesterFactory().getDirectoryFinder().getBuildDirectory(),
            "daemonresults.out");
        try {
            out = new ObjectOutputStream(new FileOutputStream(outputFile));
            projectSubmission.getTestOutcomeCollection().write(out);
            System.out.println("Test outcomes saved in " + outputFile.getPath());
            //out.flush();
            //DumpTestOutcomes.dump(projectSubmission.getTestOutcomeCollection(), System.out);
            //if (projectSubmission.getCodeMetrics() != null) {
              //  System.out.println("Code Metrics: " +projectSubmission.getCodeMetrics());
            //}
        } catch (IOException e) {
            System.err.println("Could not save test outcome collection in " + outputFile.getPath());
            e.printStackTrace(); // OK, this is a command line app
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
    
	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuildServer#reportTestResults(edu.umd.cs.buildServer.ProjectSubmission)
	 */
	protected void reportTestResults(ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException {
		
		dumpOutcomes(projectSubmission);
        
        getLog().info("Test outcome collection for " +projectSubmission.getSubmissionPK()+
		        " for test setup " +projectSubmission.getProjectJarfilePK() +
		        " contains " +projectSubmission.getTestOutcomeCollection().size() + 
		        " entries");
		
		// Format the test outcome collection as bytes in memory
		ByteArrayOutputStream sink = new ByteArrayOutputStream();
		try {
			ObjectOutputStream out = new ObjectOutputStream(sink);
			
			projectSubmission.getTestOutcomeCollection().write(out);
            out.close();
		} catch (IOException ignore) {
			// Can't happen
		}
		byte[] testOutcomeData = sink.toByteArray();
		getLog().info("Test data for submission " +projectSubmission.getSubmissionPK()+
		        " for test setup " +projectSubmission.getProjectJarfilePK()+
		        " contains " + testOutcomeData.length + " bytes from " +
		        projectSubmission.getTestOutcomeCollection().size()+ " test outcomes");
		
		String hostname = getConfig().getOptionalProperty(HOSTNAME);
		if (hostname == null)
			hostname = "unknown";
		
		MultipartPostMethod method = new MultipartPostMethod(getReportTestResultsURL());
		method.addParameter("password", getConfig().getRequiredProperty(SUBMIT_SERVER_PASSWORD));
		method.addParameter("submissionPK", projectSubmission.getSubmissionPK());
		method.addParameter("projectJarfilePK", projectSubmission.getProjectJarfilePK());
		method.addParameter("newProjectJarfile", projectSubmission.getIsNewProjectJarfile());
        method.addParameter("isBackgroundRetest", projectSubmission.getIsBackgroundRetest());
		method.addParameter("testMachine", hostname);
		// CodeMetrics
        if (projectSubmission.getCodeMetrics() != null) {
		    getLog().debug("Code Metrics: " +projectSubmission.getCodeMetrics());
            projectSubmission.getCodeMetrics().mapIntoHttpHeader(method);
		}
		method.addPart(new FilePart("testResults",
				new ByteArrayPartSource("testresults.out", testOutcomeData)));
		printURI(method);
		
		try {
			getLog().debug("Submitting test results for " +projectSubmission.getSubmissionPK() +"...");
			
			int statusCode = client.executeMethod(method);
			if (statusCode == HttpStatus.SC_OK) 
				getLog().debug("Done submitting test results for submissionPK " +projectSubmission.getSubmissionPK()+ 
						"; statusCode=" + statusCode);
			else {
				
				getLog().error("Error submitting test results for submissionPK " +projectSubmission.getSubmissionPK()+": " + statusCode
						+ ": " + method.getStatusText());
				// TODO: Should we do anything else in case of an error?
			}
		} catch (HttpException e) {
			getLog().error("Internal error: HttpException submitting test results", e);
			return;
		} catch (IOException e) {
			getLog().error("Internal error: IOException submitting test results", e);
			return;
		} finally {
			getLog().trace("Releasing connection...");
			method.releaseConnection();
			getLog().trace("Done releasing connection");
		}

	}
	
	
	
	/**
	 * Command-line interface.
	 */
	public static void main(String[] args) {
		if (args.length < 1 || args.length > 2) {
			System.err.println("Usage: " + BuildServerDaemon.class.getName() + " <config properties> [ once ]");
			System.exit(1);
		}

		Protocol easyhttps = new Protocol("https",
				new EasySSLProtocolSocketFactory(), 443);
		Protocol.registerProtocol("easyhttps", easyhttps);

		final String configFile = args[0];
		BuildServerDaemon buildServer = new BuildServerDaemon();
		buildServer.setConfigFile(configFile);
		
		try {
			buildServer.initConfig();
            // Setting "once" on the command line forces the BuildServer to 
            // run once and print all output to standard out.
            // Equivalent to setting "debug.donotloop=true" and "log.directory=console"
            // in the config.properties file, without actually having to edit the file.
            if (args.length > 1 && args[1].equalsIgnoreCase("once")) {
                buildServer.getConfig().setProperty(LOG_DIRECTORY, "console");
                buildServer.getConfig().setProperty(DEBUG_DO_NOT_LOOP, "true");
            }
			buildServer.executeServerLoop();
			buildServer.getLog().info("Shutting down");
		} catch (Exception e) {
			getBuildServerLog().fatal("BuildServerDaemon got fatal exception; waiting for cron to restart me: ", e);
		}
	}


	private static SecureRandom rng = new SecureRandom();

	private static long nextRandomLong() {
		synchronized (rng) {
			return rng.nextLong();
		}
	}
}
