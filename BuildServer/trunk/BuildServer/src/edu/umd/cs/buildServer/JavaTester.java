/*
 * Copyright (C) 2004-2005, University of Maryland
 * All Rights Reserved
 * Created on Jan 20, 2005
 */
package edu.umd.cs.buildServer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.commons.io.CopyUtils;
import org.apache.log4j.Logger;

import com.cenqua.clover.reporters.xml.XMLReporter;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.codeCoverage.CoverageLevel;
import edu.umd.cs.marmoset.codeCoverage.FileWithCoverage;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestProperties;

/**
 * Tester for Java ProjectSubmissions.
 * 
 * @author David Hovemeyer
 */
public class JavaTester extends Tester {
	private static final String CLOVER_BUILD_FILE_PATH = "edu/umd/cs/buildServer/clover-clean.xml";

	// Fields
	private TrustedCodeBaseFinder trustedCodeBaseFinder;

	private boolean debugJavaSecurity;

	private CloverCleanRunner cloverCleanRunner;

	/**
	 * Constructor.
	 * 
	 * @param testProperties         TestProperties loaded from the project jarfile's test.properties
	 * @param haveSecurityPolicyFile true if there is a security.policy file in the project jarfile
	 * @param projectSubmission      the ProjectSubmission
	 * @param directoryFinder        DirectoryFinder to locate build and testfiles directories
	 */
	public JavaTester(TestProperties testProperties,
			boolean haveSecurityPolicyFile,
			ProjectSubmission projectSubmission, DirectoryFinder directoryFinder) {
		super(testProperties, haveSecurityPolicyFile, projectSubmission,
				directoryFinder);

		this.trustedCodeBaseFinder = new TrustedCodeBaseFinder(this);
		this.trustedCodeBaseFinder.execute();

		this.debugJavaSecurity = projectSubmission.getConfig()
				.getOptionalBooleanProperty(DEBUG_SECURITY)
				|| projectSubmission.getConfig().getOptionalBooleanProperty(
						DEBUG_SECURITY_ALT);

		if (debugJavaSecurity) {
			getLog().debug("Enabling debugging of java security");
		}
	}

	/**
	 * Get the TrustedCodeBaseFinder.
	 * 
	 * @return the TrustedCodeBaseFinder
	 */
	public TrustedCodeBaseFinder getTrustedCodeBaseFinder() {
		return trustedCodeBaseFinder;
	}

	public boolean getDebugJavaSecurity() {
		return debugJavaSecurity;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.Tester#execute()
	 */
	protected void executeTests() {
		// Test the submission
		getLog().debug(
				"java.class.path=" + System.getProperty("java.class.path"));

		doExecuteInstructorTests();

		cleanUpCloverFiles();

		testsCompleted();

	}

	private void cleanUpCloverFiles() {
		try {
			String cloverDBPath = getProjectSubmission().getConfig()
					.getRequiredProperty(CLOVER_DB);
			File file = new File(cloverDBPath);
			if (!file.delete()) {
				getLog().warn(
						"Unable to delete leftover clover file: "
								+ cloverDBPath);
			}
		} catch (MissingConfigurationPropertyException e) {
			getLog().error("Cannot find clover DB Path", e);
		}

	}

	private void doExecuteInstructorTests() {
		// Execute all of the kinds of dynamic tests (public, release, secret, etc.)
		// specified in test.properties.
		String[] dynamicTestTypes = TestOutcome.DYNAMIC_TEST_TYPES;
		for (String testType : dynamicTestTypes) {
			String testClassName = getTestProperties().getTestClass(testType);
			// TODO if classname is empty then return COULD_NOT_RUN
			if (testClassName != null) {
				String classpath = buildInstructorTestClasspath();
				// Classpath for student tests should be the student directory to make sure
				// we get the correct classfile.
				if (isStudentTestType(testType))
					classpath = buildStudentTestClasspath();

				// Execute the tests, and load the outcomes into the test outcome collection
				JavaTestProcessExecutor executor = new JavaTestProcessExecutor(
						this, testClassName, testType, classpath);

				JUnitTestCaseFinder finder = new JUnitTestCaseFinder();

				if (getProjectSubmission().useInstrumentedSrcDir()) {
					// Clean up the clover DB to prepare it to store new coverage results
					runCloverClean();
				}

				// Find all the junit tests in the given class.
				// Note that for student written tests, we look in the ${build}/obj
				// directory where classfiles are generated, not the testfiles directory,
				// because the instructor's student-written tests classfile will probably 
				// be empty or have 1 or 2 tests.
				File classFilesDir = null;
				if (isStudentTestType(testType)) {
					classFilesDir = getProjectSubmission()
							.getBuilderAndTesterFactory().getDirectoryFinder()
							.getBuildDirectory();
					classFilesDir = new File(classFilesDir, "obj");
					getLog().debug(
							"Will look for student-written tests in "
									+ classFilesDir.getAbsolutePath());
				} else {
					classFilesDir = getProjectSubmission()
							.getBuilderAndTesterFactory().getDirectoryFinder()
							.getTestFilesDirectory();
					getLog().debug(
							"Will look for " + testType + " in "
									+ classFilesDir.getAbsolutePath());
				}

				getLog().debug(
						"classFilesDir: " + classFilesDir + " for testType: "
								+ testType);
				getLog().debug(
						"build output directory: "
								+ getProjectSubmission()
										.getBuildOutputDirectory()
										.getAbsolutePath());

				// add the directory to the classpath of the unit test finder
				// so that we can find the classfiles with the public, release, secret
				// and student tests.  We find student tests in the ${build}/obj directory;
				// the other test types are in the testfiles directory.
				try {
					//finder.addClassPathEntry(classFilesDir.getAbsolutePath());
					finder.addClassPathEntry(classpath);
					finder.setInspectedClass(testClassName);
					finder.findTestCases();
				} catch (ClassNotFoundException e) {
					// Don't fail if we can't find any test cases due to a classNotFoundException.
					// Students can submit files in a variety of bad formats with web submissions.
					// Just note that we couldn't run a particular class because we couldn't 
					// find the required classfiles.
					String shortTestResult = e.getMessage();
					getLog().error(
							"Unable to find any student tests: "
									+ shortTestResult);
					TestOutcome outcome = Tester.createCouldNotRunOutcome(
							testType, shortTestResult, e.toString());
					getTestOutcomeCollection().add(outcome);
					continue;
				}

				getLog().debug("className: " + testClassName);
				getLog().debug(
						"testfilesDir: " + classFilesDir.getAbsolutePath());

				// XXX Don't refactor this into a foreach loop!  
				// Note that we need increment testNumber as we go along.
				int testNumber = 0;
				for (Iterator<JUnitTestCase> ii = finder
						.getTestCaseCollection().iterator(); ii.hasNext(); testNumber++) {
					JUnitTestCase junitTestCase = ii.next();
					if (getProjectSubmission().useInstrumentedSrcDir()) {
						// clean up the results in the clover database
						// so that we get fresh coverage for each unit test
						// set the title as the name of the test method
						try {
							cloverCleanRunner.execute();
						} catch (IOException e) {
							getLog().info(
									"Unable to run 'ant clean' for clover", e);
							// FIXME Mark that we've received a clover exception and then
							// start ignoring clover operations.  Need a durable place to set
							// this since it will be detected by subprocesses.
						}
					}

					getLog().debug(
							"test case: " + junitTestCase.getMethodName());
					// run each junit test individually
					executor.setTestMethod(junitTestCase);
					executor.setStartTestNumber(testNumber);
					if (getTestProperties().getLdLibraryPath() != null) {
						getLog().debug(getTestProperties().getLdLibraryPath());
					}

					// execute the test in a separate process
					// XXX What are the reasons for 
					TestOutcome outcome = executor.executeTests();

					// TODO Figure out why submissionPK=45 is causing
					// arrayOOB exception to happen
					// XXX NOT SAFE in general to return null here
					if (outcome == null) {
						getLog().error("executor.executeTests() returned null!");
						continue;
					}

					// TODO move the addCodeCoverageToTestOutcome functionality
					// into the executeTests() method
					if (getProjectSubmission().useInstrumentedSrcDir()) {
						addCodeCoverageToTestOutcome(outcome);
					}
					getTestOutcomeCollection().add(outcome);
				}
				// TODO probably should delete the myclover.db file
				getLog().debug("End of Tester process output\n");
			}
		}
		// Make sure we clean up the final remaining coverage files
		if (getProjectSubmission().useInstrumentedSrcDir()) {
			runCloverClean();
		}
	}

	/**
	 * @throws BuilderException
	 * @throws IOException
	 */
	private void runCloverClean() {
		try {
			// CloverCleanRunner is an ant task that cleans the clover database file
			// There is no programmatic interface to the clover functionality for
			// cleaning out the database and we need to do this after running each
			// unit test because we need coverage for individual unit tests rather
			// than global coverage.
			cloverCleanRunner = new CloverCleanRunner();
			// The buildserver first copies the clover-clean.xml file to a
			// well-known location in /tmp
			File cloverBuildFile = new File(CLOVER_BUILD_FILE);
			// only create the clover-clean.xml file if it doesn't already exist
			if (!cloverBuildFile.exists()) {
				getLog().info("Creating " + cloverBuildFile.getAbsolutePath());
				InputStream in = getClass().getClassLoader()
						.getResourceAsStream(CLOVER_BUILD_FILE_PATH);
				if (in == null)
					throw new BuilderException(
							"Could not find clover-clean build file path");
				OutputStream out = null;
				try {
					out = new BufferedOutputStream(new FileOutputStream(
							cloverBuildFile));
					CopyUtils.copy(in, out);
				} catch (IOException e) {
					throw new BuilderException(
							"Could not create clover.xml file", e);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						// Ignore
					}
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							// Ignore
						}
					}
				}
			}
			// TODO Maybe put the cloverDB file into the student's build directory?
			String cloverDBPath = null;
			try {
				cloverDBPath = getProjectSubmission().getConfig()
						.getRequiredProperty(CLOVER_DB);
			} catch (MissingConfigurationPropertyException e) {
				getLog().fatal(e.getMessage(), e);
				throw new BuilderException("Missing CLOVER_DB: " + e);
			}
			File cloverDB = new File(cloverDBPath);
			cloverCleanRunner.setBuildXml(cloverBuildFile);
			cloverCleanRunner.setCloverDB(cloverDB);

			// Clean-up any lingering information in the database from a previous run.
			cloverCleanRunner.execute();
		} catch (IOException e) {
			getLog().error("Unable to clean the existing clover results!", e);
			// Signal error
		} catch (BuilderException e) {
			getLog().error("Unable to clean the existing clover results!", e);
			// Signal error
		}
	}

	/**
	 * @param testType
	 * @return
	 */
	private boolean isStudentTestType(String testType) {
		return testType.equals(TestOutcome.STUDENT_TEST);
	}

	/**
	 * Load required information from the test properties.
	 * 
	 * @throws BuilderException
	 */
	protected void loadTestProperties() throws BuilderException {
		super.loadTestProperties();
	}


	/**
	 * Build the classpath to be used for the offical instructor test cases
	 * (from the test setup jarfile). This is composed of:
	 *
	 * <ul>
	 * <li> the current classpath (allowing all classes from this application to be executed,
	 *      e.g., TestRunner)</li>
	 * <li> the test setup jar file (allowing tests and project classes to be found)</li>
	 * <li> the build output directory (allowing the student application to be found)</li>
	 * </ul>
	 *
	 * <p>The order is important, since we don't want to allow students
	 * to spoof tests or project classes.</p>
	 * 
	 * @return complete classpath to use when executing instructor tests
	 */
	private String buildInstructorTestClasspath() {
		StringBuffer cp = new StringBuffer();
		cp.append(System.getProperty("java.class.path"));
		cp.append(File.pathSeparator);
		cp.append(getProjectSubmission().getProjectJarFile().getAbsolutePath());
		cp.append(File.pathSeparator);
		cp.append(getProjectSubmission().getBuildOutputDirectory()
				.getAbsolutePath());
		return cp.toString();
	}

	/**
	 * Build the classpath to be used for unofficial student test cases.
	 * These are run on a best-effort basis.  Components are:
	 * 
	 * <ul>
	 * <li> the current classpath (allowing all classes from this application to be executed,
	 *      e.g., TestRunner)</li>
	 * <li>build output directory (student code)</li>
	 * <li>project test-setup (instructor's test cases and aux. code)</li>
	 * </ul>
	 * <p>
	 * <b>Note </b> If students change any auxiliary .java files, the modified files
	 * will be loaded instead of the unmodified versions in the test setup jarfile.  This
	 * is because we place the build output directory first onto the classpath in order 
	 * to load the student-written student-tests classfile first.
	 * <p>
	 * Also note that if students don't include a classfile for their own tests, then we
	 * will execute the instructor's skeleton placeholder class for student-written tests.
	 * 
	 * @return complete classpath to use when executing student tests
	 */
	private String buildStudentTestClasspath() {
		StringBuffer cp = new StringBuffer();
		cp.append(System.getProperty("java.class.path"));
		cp.append(File.pathSeparator);
		cp.append(getProjectSubmission().getBuildOutputDirectory()
				.getAbsolutePath());
		cp.append(File.pathSeparator);
		cp.append(getProjectSubmission().getProjectJarFile().getAbsolutePath());
		return cp.toString();
	}

	/**
	 * Open test outcome file.
	 * 
	 * @param testType       test_type of tests stored in the file
	 * @param outputFilename name of the output file
	 * @return a FileInputStream to read the test outcome file
	 * @throws BuilderException if the test outcome file couldn't be opened
	 */
	public FileInputStream openTestOutcomeFile(String testType,
			String outputFilename) throws FileNotFoundException {
		// Try several times to open the test outcomes file.
		// It seems that on Linux, files created by subprocesses
		// don't become visible right away, even if you successfully
		// wait for the child process that created them to exit.
		// So, we try to open the file in a loop, sleeping between
		// attempts.
		File outputFile = new File(outputFilename);
		FileInputStream fis = null;
		int numAttempts = 0;
		final int MAX_ATTEMPTS = 3;
		IOException openException = null;
		while (fis == null && numAttempts < MAX_ATTEMPTS) {
			try {
				++numAttempts;
				fis = new FileInputStream(outputFile);
			} catch (FileNotFoundException e) {
				openException = e;
				getLog().warn(
						"Could not open test outcome file " + outputFilename
								+ " (attempt " + numAttempts + ")", e);

				// Wait for a bit
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignore) {
					// Ignore
				}
			}
		}
		if (fis == null) {
			throw new FileNotFoundException(
					"Could not open test outcome file for " + testType
							+ " tests after " + MAX_ATTEMPTS + " attempts: "
							+ openException.toString());
		}
		try {
			return fis;
		} finally {
			// Delete the file if we return successfully.
			// If the next test case doesn't produce an output file, we don't
			// want to accidentally read this output file.
			//if (outputFile != null) outputFile.delete();
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 5 || args.length > 6) {
			System.err.println("Usage: " + JavaTester.class.getName()
					+ " [-debugSecurity=(true|false)]" + " <build directory>"
					+ " <testfiles directory>" + " <test.properties file>"
					+ " <security.policy file>" + " <project test jarfile>");
			System.exit(1);
		}

		int argCount = 0;

		boolean debugSecurity = false;
		if (args[0].startsWith("-debugSecurity=")) {
			debugSecurity = Boolean.valueOf(
					args[0].substring("-debugSecurity=".length()))
					.booleanValue();
			argCount++;
		}

		String buildDirectoryFilename = args[argCount++];
		String testfilesDirectoryFilename = args[argCount++];
		String testPropertiesFilename = args[argCount++];
		String securityPolicyFilename = args[argCount++];
		String projectJarFilename = args[argCount++];

		Configuration config = new Configuration();
		config.setProperty(BUILD_DIRECTORY, new File(buildDirectoryFilename)
				.getAbsolutePath());
		config.setProperty(TEST_FILES_DIRECTORY, new File(
				testfilesDirectoryFilename).getAbsolutePath());
		config.setProperty(DEBUG_SECURITY, debugSecurity ? "true" : "false");

		Logger log = Logger.getLogger(Tester.class);

		TestProperties testProperties = new TestProperties();
		testProperties.load(testPropertiesFilename);

		boolean haveSecurityPolicyFile = new File(securityPolicyFilename)
				.exists();

		File projectJarFile = new File(projectJarFilename).getAbsoluteFile();

		ProjectSubmission projectSubmission = new ProjectSubmission(config,
				log, "42", "69", "false", "false");
		projectSubmission.projectJarFile = projectJarFile;

		Tester tester = new JavaTester(testProperties, haveSecurityPolicyFile,
				projectSubmission, new JavaDirectoryFinder(config));

		tester.execute();

		tester.getTestOutcomeCollection().dump(System.out);
	}

	private void addCodeCoverageToTestOutcome(TestOutcome outcome) {
		String cloverDBPath;
		try {
			cloverDBPath = getProjectSubmission().getConfig()
					.getRequiredProperty(CLOVER_DB);

			// the name of xml file where we'll put the results
			String xmlOutputFile = getDirectoryFinder().getBuildDirectory()
					.getAbsolutePath()
					+ "/"
					+ outcome.getTestType()
					+ "-"
					+ outcome.getTestNumber()
					+ "-"
					+ outcome.getTestName()
					+ ".xml";
			// Java interface to clover's binary database.  We need to
			// get the binary coverage data into an XML file that we can store
			// in the database.
			String[] cliArgs = {
					"-i",
					cloverDBPath,
					"-o",
					xmlOutputFile,
					"-t",
					outcome.getTestType() + "-" + outcome.getTestNumber() + "-"
							+ outcome.getTestName(), "-l" };
			String coverageMarkupCmd = " ";
			for (int ii = 0; ii < cliArgs.length; ii++) {
				coverageMarkupCmd += cliArgs[ii] + " ";
			}
			getLog().debug("Clover args for dumping data: " + coverageMarkupCmd);

			StringBuffer cloverReportArgs = new StringBuffer();
			for (int ii = 0; ii < cliArgs.length; ii++) {
				cloverReportArgs.append(cliArgs[ii] + " ");
			}
			getLog().debug("Args for clover report: " + cloverReportArgs);
			int result = XMLReporter.mainImpl(cliArgs);
			if (result != 0) {
				getLog().error("Unable to generate Clover XML report");
			}
			outcome.setCodeCoveralXMLResults(new File(xmlOutputFile));

			// Track overall code coverage information here, to be
			// used later when we compute if a release test covers code that
			// is uncovered by anything else.
			if (outcome.getTestType().equals(TestOutcome.PUBLIC_TEST)) {
				publicStudentCoverage.union(outcome.getCodeCoverageResults());
			} else if (outcome.getTestType().equals(TestOutcome.STUDENT_TEST)) {
				publicStudentCoverage.union(outcome.getCodeCoverageResults());
			} else if (outcome.getTestType().equals(TestOutcome.RELEASE_TEST)) {
				releaseCoverage.union(outcome.getCodeCoverageResults());
			}

		} catch (MissingConfigurationPropertyException e) {
			getLog().error(
					"Not causing the build process to fail due to clover, "
							+ "but there was an error: " + e.getMessage(), e);
		} catch (IOException e) {
			getLog().error(
					"Not causing the build process to fail due to clover, "
							+ "but there was an error: " + e.getMessage(), e);
		}
	}

	/**
	 * @deprecated
	 * @return
	 */
	private Set<JUnitTestCase> buildPublicTestSet() {
		Set<JUnitTestCase> publicTestSet = new HashSet<JUnitTestCase>();
		String publicTestClass = getTestProperties().getTestClass(
				TestOutcome.PUBLIC_TEST);
		if (publicTestClass != null) {
			JUnitTestCaseFinder publicTestFinder = new JUnitTestCaseFinder();
			publicTestFinder.addClassPathEntry(getProjectSubmission()
					.getProjectJarFile().getAbsolutePath());
			try {
				publicTestFinder.setInspectedClass(publicTestClass);
				publicTestFinder.findTestCases();

				publicTestSet.addAll(publicTestFinder.getTestCaseCollection());
			} catch (ClassNotFoundException e) {
				getLog().warn("Could not inspect public tests", e);
			}
		}
		return publicTestSet;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.Tester#javaCallback()
	 */
	@Override
	protected void javaCallback() {
		// Post-processing:
		// 1) which release-test exceptions were covered by some other student/public test cases
		// 2) if a release-test covers any methods that were not covered by student/public tests
		// 3) what methods weren't covered by any student/public test cases at all
		if (getTestProperties().getLanguage().equalsIgnoreCase(JAVA)
				&& getTestProperties().isPerformCodeCoverage()) {
			try {
				//releaseCoverage.excluding(publicStudentCoverage);
				//publicStudentCoverage=getTestOutcomeCollection().getOverallCoverageResultsForPublicAndStudentTests();
				for (TestOutcome outcome : getTestOutcomeCollection()
						.getReleaseAndSecretOutcomes()) {
					if (!outcome.getTestType().equals(TestOutcome.RELEASE_TEST) &&
                        !outcome.getTestType().equals(TestOutcome.SECRET_TEST))
						throw new IllegalStateException(
								"TestOutcomeCollection.getReleaseAndSecretOutcomes() "
										+ "is badly broken and returning non-release, non-secret test outcomes!");
					getLog().trace("Relase test #" + outcome.getTestNumber());
					// XXX Figure out if this release test covers any METHODS
					// that are uncovered by student/public tests
					CodeCoverageResults testCoverage = new CodeCoverageResults(
							outcome.getCodeCoverageResults());
					// If for some reason we didn't get any coverage, skip this one
					if (testCoverage == null) {
						getLog().trace("Cannot find coverage for " + outcome);
						continue;
					}
					getLog().trace(
							"this single test's overall coverage: "
									+ testCoverage.getOverallCoverageStats());
					getLog().trace(
							"public-student coverage: "
									+ publicStudentCoverage
											.getOverallCoverageStats());
					testCoverage.excluding(publicStudentCoverage);
					getLog().trace(
							"coarsest stats after exclusion: "
									+ testCoverage.getOverallCoverageStats());
					CoverageLevel level = testCoverage.coarsestCoverageLevel();
					getLog().trace("coarsest coverage level: " + level);
					outcome.setCoarsestCoverageLevel(level);

					// XXX Is the exception in this release test covered by 
					// some other public/student tests?
					if (outcome.getOutcome().equals(TestOutcome.ERROR)) {
						getLog().trace(
								"release test #" + outcome.getTestNumber()
										+ " threw an exception!");
						StackTraceElement stackTrace = outcome
								.getExceptionSourceFromLongTestResult();
						// Figure out if exception is covered by student/public tests?
						// TODO refactor and put a getCoverageByFileLine(String filename, int lineNumber)
						getLog().trace("public student coverage for error case: " +publicStudentCoverage);
                        getLog().trace("stack trace = " +stackTrace);
                        if (publicStudentCoverage!=null && stackTrace != null) {
                            FileWithCoverage fileWithCoverage = publicStudentCoverage
                            .getFileWithCoverage(stackTrace.getFileName());
                            if (fileWithCoverage != null) {
                                if (fileWithCoverage
                                .getStmtCoverageCount(stackTrace
                                .getLineNumber()) > 0) {
                                    outcome
                                    .setExceptionSourceCoveredElsewhere(true);
                                }
                            }
                        }
					}
				}
				// XXX Find the methods that are NOT covered by any public/student test 
				// First, find all the classfiles, excluding the test cases.
				List<File> classFiles = BuildServerUtilities
						.listNonCloverClassFilesInDirectory(
								getProjectSubmission().getBuildOutputDirectory(),
								getTestProperties());

				UncoveredMethodFinder finder = new UncoveredMethodFinder();
				finder.setCodeCoverageResults(publicStudentCoverage);
				finder.addClassPathEntry(buildInstructorTestClasspath());
				finder.setLog(getLog());
				finder.initRepository();
                for (File file : classFiles) {
                    try {
                        finder.inspectClass(file);
                    } catch (ClassFormatException e) {
                        getLog().trace("Unable to parse classfile " +file.getAbsolutePath());
                    }
                }

				int methodNum = 0;
				for (StackTraceElement trace : finder.findUncoveredMethods()) {
					getTestOutcomeCollection().add(
							createUncoveredMethodOutcome(trace, methodNum++));
				}
			} catch (IOException e) {
				getLog().warn(
						"Unable to process code coverage results for "
								+ projectSubmission.getSubmissionPK()
								+ " tested against "
								+ projectSubmission.getProjectJarfilePK()
								+ " where code coverage " + " was set to "
								+ getTestProperties().isPerformCodeCoverage());
			}
		}
	}

	private TestOutcome createUncoveredMethodOutcome(StackTraceElement trace,
			int methodNum) {
		TestOutcome outcome = new TestOutcome();

		outcome.setTestType(TestOutcome.UNCOVERED_METHOD);
		outcome.setTestNumber(methodNum);
		outcome.setOutcome(TestOutcome.UNCOVERED_METHOD);
		outcome.setTestName(trace.getMethodName());
		outcome.setPointValue(trace.getLineNumber());
		outcome.setShortTestResult(trace.getFileName());
		outcome.setExceptionClassName(trace.getClassName());
		outcome.setLongTestResult(trace.toString());

		return outcome;
	}

}
