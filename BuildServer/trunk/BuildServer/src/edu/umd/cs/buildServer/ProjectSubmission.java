/*
 * Copyright (C) 2005 University of Maryland
 * All Rights Reserved
 * Created on Jan 19, 2005
 */
package edu.umd.cs.buildServer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.CodeMetrics;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.modelClasses.TestPropertyKeys;

/**
 * A project submission to be compiled and tested.
 * This object stores all of the state information
 * about a submission to avoid having to pass the state
 * as parameters to the various BuildServer subsystems.
 * 
 * @author David Hovemeyer
 */
public class ProjectSubmission implements ConfigurationKeys, TestPropertyKeys {
	private Configuration config;
	private Logger log;
	private String submissionPK;
	private String projectJarfilePK;
	private String isNewProjectJarfile;
    private String isBackgroundRetest;
    /** 
     * Auxiliary information about the source to be built, such as an md5sum of the
     * classfiles and/or the names of student-written tests.
     * So far we don't compute any auxiliary information for C builds.
     */
    private CodeMetrics codeMetrics;
	
	File zipFile;
	File projectJarFile;
	
	private TestOutcomeCollection testOutcomeCollection;

	private HttpMethod method;
	private TestProperties testProperties;
	private BuilderAndTesterFactory builderAndTesterFactory;
	
	/**
	 * Constructor.
	 * 
	 * @param config              the BuildServer's Configuration
	 * @param log                 the BuildServer's Log
	 * @param submissionPK        the submission PK
	 * @param projectJarfilePK    the project jarfile PK
	 * @param isNewProjectJarfile boolean: whether or not the submission is for a new project jarfile
	 *                            being tested with the canonical project solution
	 * @throws MissingConfigurationPropertyException 
	 */
	public ProjectSubmission(
			Configuration config,
			Logger log,
			String submissionPK,
			String projectJarfilePK,
			String isNewProjectJarfile,
            String isBackgroundRetest)
    throws MissingConfigurationPropertyException
    {
		this.config = config;
		this.log = log;
		this.submissionPK = submissionPK;
		this.projectJarfilePK = projectJarfilePK;
		this.isNewProjectJarfile = isNewProjectJarfile;
        this.isBackgroundRetest = isBackgroundRetest;
		
		// Choose a name for the zip file based on
		// the build directory and the submission PK.
		String zipFileName = config.getRequiredProperty(BUILD_DIRECTORY) +
				File.separator + "submission_" + getSubmissionPK() + ".zip";
		this.zipFile = new File(zipFileName);
		
		// Choose a name for the project jar file based
		// on the build directory and the project PK.
		String projectJarFileName = new File(
				config.getRequiredProperty(JAR_CACHE_DIRECTORY),
				"proj_" + getProjectJarfilePK() + ".jar").getAbsolutePath();
		this.projectJarFile = new File(projectJarFileName);
		
		this.testOutcomeCollection = new TestOutcomeCollection();
	}
	
	/**
	 * @return Returns the config.
	 */
	public Configuration getConfig() {
		return config;
	}
	
	/**
	 * @return Returns the log.
	 */
	public Logger getLog() {
		return log;
	}

	/**
	 * @return Returns the isNewProjectJarfile value.
	 */
	public String getIsNewProjectJarfile() {
		return isNewProjectJarfile;
	}

	/**
	 * @return Returns the projectJarfilePK.
	 */
	public String getProjectJarfilePK() {
		return projectJarfilePK;
	}

	/**
	 * @return Returns the submissionPK.
	 */
	public String getSubmissionPK() {
		return submissionPK;
	}
	
	/**
	 * Get File storing submission zip file.
	 * 
	 * @return the File storing the submission zip file
	 */
	public File getZipFile() {
		return zipFile;
	}
	
	/**
	 * Get the File storing the project jar file.
	 * 
	 * @return the File storing the project jar file
	 */
	public File getProjectJarFile() {
		return projectJarFile;
	}

	/**
	 * @return Returns the testOutcomeCollection.
	 */
	public TestOutcomeCollection getTestOutcomeCollection() {
		return testOutcomeCollection;
	}
	
	/**
	 * @param method The method to set.
	 */
	public void setMethod(HttpMethod method) {
		this.method = method;
	}
	
	/**
	 * @return Returns the method.
	 */
	public HttpMethod getMethod() {
		return method;
	}
	
	/**
	 * Set the TestProperties.
	 * @param testProperties the TestProperties
	 */
	public void setTestProperties(TestProperties testProperties) {
		this.testProperties = testProperties;
	}
	
	/**
	 * Get the TestProperties.
	 * @return the TestProperties
	 */
	public TestProperties getTestProperties() {
		return testProperties;
	}

	/**
	 * Based on the language specified in test.properties,
	 * create a BuilderAndTesterFactory.
     * 
     * @return a BuilderAndTesterFactory to be used to build and test the submission
	 * @throws BuilderException 
	 */
	public BuilderAndTesterFactory createBuilderAndTesterFactory()
	throws BuilderException
    {
		try {
            String language = testProperties.getLanguage();
            if (language.equals(JAVA)) {
				this.builderAndTesterFactory = new JavaBuilderAndTesterFactory(getConfig(), testProperties);
			} else if (language.equals(C) ||
					language.equals(OCAML) ||
					language.equals(RUBY))
			{
				// XXX The CBuilder and CTester are also used for OCaml and Ruby projects.
				// The CBuilder and CTester are flexible and only require a Makefile and
				// a list of test executables that return zero to signal passing and non-zero
				// to signal failure.
				this.builderAndTesterFactory = new CBuilderAndTesterFactory(getConfig(), testProperties);
                        }else if(language.equals(UW)) {
				this.builderAndTesterFactory = new UWBuilderAndTesterFactory(getConfig(), testProperties);
			} else {
				throw new BuilderException("Unknown language specified in test.properties: " + language);
			}
		} catch (MissingConfigurationPropertyException e) {
			throw new BuilderException("Could not create builder/tester factory for submission", e);
        }		
		return builderAndTesterFactory;
	}

	/**
	 * Get the BuilderAndTesterFactory.
	 * @return the BuilderAndTesterFactory
	 */
	public BuilderAndTesterFactory getBuilderAndTesterFactory() {
		return builderAndTesterFactory;
	}
	/**
	 * Should we use the directory with src code instrumented for code coverage?
	 * 
	 * TODO Instrumented source is specific to Clover; other code coverage tools 
	 * (such as Emma) don't have instrument the source and so make this step unnecessary.
	 * It's still not clear how to integrate everything together.
	 * @return True if we should use the src directory instrumented for code coverage;
	 * false otherwise.
	 * <p>
	 * TODO If the buildServer's configuration asks for code coverage,
	 * but we notice that we don't have permission to read and write the directory
	 * where the code coverage data is being written, then we need to either:
	 * <ul>
	 * <li> over-ride the code coverage setting or else all the test outcomes will fail.
	 * <li> add the necessary permissions to the security policy file.
	 * </ul>
	 * This would be easy if there were some way to ask a security.policy file what
	 * permissions it is granting.  I don't know if this is possible or how to do so.
	 * Future work.
	 * 
	 */
	public boolean useInstrumentedSrcDir() {
		// TODO Need to check that we have appropriate permissions in the security.policy
		return getTestProperties().isPerformCodeCoverage();
	}
	
	/**
	 * Get the build output directory.
	 * It is not legal to call this method until the BuilderAndTesterFactory
	 * has been created.
	 * 
	 * @return the File specifying the build output directory
	 */
	public File getBuildOutputDirectory() {
		return new File(
				getBuilderAndTesterFactory().getDirectoryFinder().getBuildDirectory(),
				BuildServer.BUILD_OUTPUT_DIR);
	}
	
	public File getSrcDirectory() {
	    return new File(
	            getBuilderAndTesterFactory().getDirectoryFinder().getBuildDirectory(),
	            BuildServer.SOURCE_DIR);
	}
	
	public File getInstSrcDirectory() {
	    return new File(
	            getBuilderAndTesterFactory().getDirectoryFinder().getBuildDirectory(),
	            BuildServer.INSTRUMENTED_SOURCE_DIR);
	}
	
	/**
	 * Create a fake ProjectSubmission object suitable for running
	 * a standalone Builder, Tester, or other analysis,
	 * using manually specified files and/or directories. 
	 * 
	 * @param language              project language; "c" or "java"
	 * @param buildDirName          name of build directory (optional)
	 * @param submissionZipFileName name of submission zip file (optional)
	 * @param jarcacheDirName       name of jar cache directory (optional)
	 * @param projectJarFileName    name of project jar file (optional)
	 * @param testFilesDirName      name of test files directory (optional)
	 * @return a configured ProjectSubmission
	 * @throws MissingConfigurationPropertyException
	 * @throws IOException
	 * @throws BuilderException 
	 */
	public static ProjectSubmission createFakeProjectSubmission(
			String language,
			String buildDirName,
			String submissionZipFileName,
			String jarcacheDirName,
			String projectJarFileName,
			String testFilesDirName) throws MissingConfigurationPropertyException, IOException, BuilderException {
		
		if (buildDirName == null)
			buildDirName = "NONE";
		if (jarcacheDirName == null)
			jarcacheDirName = "NONE";
		if (testFilesDirName == null)
			testFilesDirName = "NONE";
		
		Configuration config = new Configuration();
		config.setProperty(ConfigurationKeys.DEBUG_VERBOSE, "true");
		config.setProperty(ConfigurationKeys.JAR_CACHE_DIRECTORY, jarcacheDirName);
		config.setProperty(ConfigurationKeys.BUILD_DIRECTORY, buildDirName);
		config.setProperty(ConfigurationKeys.TEST_FILES_DIRECTORY, testFilesDirName);
		config.setProperty(ConfigurationKeys.LOG_DIRECTORY, "console");
		
		Logger log = BuildServer.createLog(config, false);

		ProjectSubmission projectSubmission = new ProjectSubmission(config, log, "17", "42", "false", "false");
		if (submissionZipFileName != null) {
			projectSubmission.zipFile = new File(submissionZipFileName);
		}
		if (projectJarFileName != null) {
			projectSubmission.projectJarFile = new File(projectJarFileName);
		}
		
		TestProperties testProperties = new TestProperties();
		testProperties.setProperty(TestPropertyKeys.BUILD_LANGUAGE, language);
		
		projectSubmission.setTestProperties(testProperties);
		
		projectSubmission.createBuilderAndTesterFactory();
		
		return projectSubmission;
	}

    public String getIsBackgroundRetest() {
        return isBackgroundRetest;
    }

    public void setCodeMetrics(CodeMetrics codeMetrics) {
        this.codeMetrics=codeMetrics;
    }
    public CodeMetrics getCodeMetrics() {
        return codeMetrics;
    }
}
