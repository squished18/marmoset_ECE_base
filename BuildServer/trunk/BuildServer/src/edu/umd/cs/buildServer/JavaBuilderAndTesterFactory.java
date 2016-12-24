/*
 * Created on Jan 20, 2005
 */
package edu.umd.cs.buildServer;

import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.utilities.ZipExtractorException;

/**
 * Factory for creating Builder and Tester objects
 * for Java ProjectSubmissions.
 * 
 * @author David Hovemeyer
 */
public class JavaBuilderAndTesterFactory implements BuilderAndTesterFactory {
	
	private TestProperties testProperties;
	private DirectoryFinder directoryFinder;
	
	/**
	 * Constructor.
	 * 
	 * @param config         the build server Configuration
	 * @param testProperties TestProperties loaded from test jarfile's test.properties
	 * @throws MissingConfigurationPropertyException 
	 */
	public JavaBuilderAndTesterFactory(Configuration config, TestProperties testProperties) throws MissingConfigurationPropertyException {
		this.testProperties = testProperties;
		this.directoryFinder = new JavaDirectoryFinder(config);
	}
	
	public DirectoryFinder getDirectoryFinder() {
		return directoryFinder;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuilderAndTesterFactory#createBuilder(edu.umd.cs.buildServer.ProjectSubmission)
	 */
	public Builder createBuilder(ProjectSubmission projectSubmission)
	throws MissingConfigurationPropertyException, BuilderException, ZipExtractorException
    {
		JavaSubmissionExtractor submissionExtractor=new JavaSubmissionExtractor(
				projectSubmission.getZipFile(),
				directoryFinder.getBuildDirectory(),
				projectSubmission.getLog()
				);
		// If the buildserver configuration specifies source files to exclude,
		// add them to the SubmissionExtractor
		String excludedSourceFileList =
			projectSubmission.getConfig().getOptionalProperty(ConfigurationKeys.EXCLUDED_SOURCE_FILE_LIST);
		if (excludedSourceFileList != null) {
			ArgumentParser parser = new ArgumentParser(excludedSourceFileList);
			while (parser.hasNext()) {
				submissionExtractor.addExcludedSourceFilePattern(parser.next());
			}
		}
		
		Builder builder = new JavaBuilder(
				testProperties,
				projectSubmission,
				directoryFinder,
				submissionExtractor
				);
		
		builder.addExpectedFile(projectSubmission.getZipFile().getName());
		builder.addExpectedFile(projectSubmission.getProjectJarFile().getName());
		builder.addExpectedFile("test.properties");
		builder.addExpectedFile("security.policy");
		
		return builder;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuilderAndTesterFactory#createTester(boolean, edu.umd.cs.buildServer.ProjectSubmission)
	 */
	public Tester createTester(boolean haveSecurityPolicyFile, ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException {
		return new JavaTester(
				testProperties,
				haveSecurityPolicyFile,
				projectSubmission,
				directoryFinder);
	}
}
