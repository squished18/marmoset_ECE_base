/*
 * Copyright (C) 2004-2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 10, 2005
 */
package edu.umd.cs.buildServer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

/**
 * Execute our TestRunner class in a subprocess, in order to
 * run one or more JUnit tests.
 * 
 * @see edu.umd.cs.buildServer.JavaTester
 * @see edu.umd.cs.buildServer.TestRunner
 * @author David Hovemeyer
 */
public class JavaTestProcessExecutor implements ConfigurationKeys{
	private final JavaTester tester;
	private  final String testClass;
	private  final String testType;
	private   String outputFilename;
	private  final String classPath;
	private String testMethod;
	private JUnitTestCase testCase;
	private int nextTestNumber;
	@Deprecated private boolean doNotReportProcessTimeout;
    private String[] environment=null;

	/**
	 * Constructor.
	 * 
	 * @param tester         the JavaTester
	 * @param testClass      name of test class
	 * @param testType       type of test (public, release, secret of student)
	 * @param classPath      classpath to use when executing the tests
	 */
	public JavaTestProcessExecutor(
			JavaTester tester,
			String testClass,
			String testType,
			String classPath) {
		this.tester = tester;
		this.testClass = testClass;
		this.testType = testType;
		this.classPath = classPath;
		this.nextTestNumber = 0;
		this.doNotReportProcessTimeout = false;
	}
	
	/**
	 * Set the method name of the single test case to execute.
	 * This is useful for running a single test case out of a
	 * test suite class.  Will also set the outputFilename based
	 * on the name of the test case we're running.
	 * 
	 * @param testMethod name of the single test method to run
	 */
	public void setTestMethod(JUnitTestCase testCase) {
		this.testCase = testCase;
		this.testMethod = testCase.getMethodName();
		this.outputFilename = new File(getDirectoryFinder().getBuildDirectory(), testType+ "." +testMethod +".out").getAbsolutePath();
	}
    
    public void setEnvironment(String[] environment) {
        this.environment = environment;
    }
	
	/**
	 * Set the starting test number to be used when collecting
	 * TestOutcomes.
	 * 
	 * @param startTestNumber
	 */
	public void setStartTestNumber(int startTestNumber) {
		this.nextTestNumber = startTestNumber;
	}

	/**
	 * @deprecated
	 * Specify whether or not process timeout should be ignored.
	 * By default, process timeout is <em>not</em> ignored, and results
	 * in the entire test class being marked as "could not run".
	 * If process timeout is ignored, we will not generate <em>any</em>
	 * TestOutcome(s).
	 * 
	 * @param doNotReportProcessTimeout true if process timeout should be ignored,
	 *                                  false if not
	 */
	public void setDoNotReportProcessTimeout(boolean doNotReportProcessTimeout) {
		this.doNotReportProcessTimeout = doNotReportProcessTimeout;
	}
	
	public Logger getLog() {
		return tester.getLog();
	}
	
	public TestOutcomeCollection getTestOutcomeCollection() {
		return tester.getTestOutcomeCollection();
	}
	
	public DirectoryFinder getDirectoryFinder() {
		return tester.getDirectoryFinder();
	}
	
	public ProjectSubmission getProjectSubmission() {
		return tester.getProjectSubmission();
	}
	
	public TrustedCodeBaseFinder getTrustedCodeBaseFinder() {
		return tester.getTrustedCodeBaseFinder();
	}
	
	public boolean getDebugJavaSecurity() {
		return tester.getDebugJavaSecurity();
	}
	
	/**
	 * Execute test(s) in test suite class.
	 * 
	 * @throws IOException
	 * @throws BuilderException
	 */
	public TestOutcome executeTests()
	//throws InternalBuildServerException//, IOException 
	{
		String buildServerTestFilesDir = getDirectoryFinder().getTestFilesDirectory().getAbsolutePath() + File.separator;
		
		// Build arguments to java process
		List<String> javaArgs = new LinkedList<String>();
		javaArgs.add("java");
        //TODO Factor the amount of memory and the extra -D parameters into config.properties
        javaArgs.add("-Xmx256m");
        javaArgs.add("-Dcom.sun.management.jmxremote");
        String vmArgs = tester.getTestProperties().getVmArgs();
        if (vmArgs != null) {
            // Break up into separate tokens if necessary
            StringTokenizer tokenizer = new StringTokenizer(vmArgs);
            while (tokenizer.hasMoreElements()) {
                String nextArg = tokenizer.nextToken();
                nextArg.replace("${buildserver.test.files.dir}", buildServerTestFilesDir);
                 // nextArg = nextArg.replace("${buildserver.test.files.dir}", buildServerTestFilesDir);
				javaArgs.add(nextArg);
            }
        }
		javaArgs.add("-classpath");
		javaArgs.add(classPath);
		// Tests must run headless, for obvious reasons
		javaArgs.add("-Djava.awt.headless=true");
		// Specify filename of project jar file
		javaArgs.add("-Dbuildserver.test.jar.file=" + getProjectSubmission().getProjectJarFile().getAbsolutePath() + "");
		// Specify the path of the build directory
		javaArgs.add("-Dbuildserver.build.dir=" + getDirectoryFinder().getBuildDirectory().getAbsolutePath());
		// Add trusted code bases
		for (Iterator<TrustedCodeBase> i = getTrustedCodeBaseFinder().getCollection().iterator(); i.hasNext(); ) {
			TrustedCodeBase trustedCodeBase = i.next();
			javaArgs.add("-D" + trustedCodeBase.getProperty() + "=" + trustedCodeBase.getValue());
		}
		// Let the test classes know where test files are.
		// Append a separator to the end, because this makes it
		// easier for the tests to determine how to access
		// the test files.
		javaArgs.add("-Dbuildserver.test.files.dir=" + buildServerTestFilesDir);
		if (getDebugJavaSecurity()) {
			javaArgs.add("-Djava.security.debug=access,failure");
		}
		if (tester.getHasSecurityPolicyFile()) {
			// Project jar file contained a security policy file
			javaArgs.add("-Djava.security.manager");
			javaArgs.add("-Djava.security.policy=file:" 
                    + new File(getDirectoryFinder().getTestFilesDirectory(), "security.policy").getAbsolutePath());
		}
		// XXX TestRunner
		javaArgs.add(TestRunner.class.getName());
		if (nextTestNumber > 0) {
			javaArgs.add("-startTestNumber");
			javaArgs.add(String.valueOf(nextTestNumber));
		}
		javaArgs.add(getProjectSubmission().getSubmissionPK());
		javaArgs.add(testType);
		javaArgs.add(testClass);
		javaArgs.add(outputFilename);
		int timeoutInSeconds = tester.getTestProperties().getTestTimeoutInSeconds();
		if (testCase != null && testCase.getMaxTimeInSeconds() != 0) {
			timeoutInSeconds =  testCase.getMaxTimeInSeconds();
            getLog().trace("Using @MaxTestTime(value=" +timeoutInSeconds+ ") annotation");
        }
		javaArgs.add(String.valueOf(timeoutInSeconds));
		if (testMethod != null) {
			javaArgs.add(testMethod);
		}
		
		// Which directory to execute the TestRunner in.
		// By default, this is the build directory, but the
		// cwd.testfiles.dir property may set it to
		// be the testfiles directory.
		File testRunnerCWD = getDirectoryFinder().getBuildDirectory();
		// Student-written tests must be run from the build directory
		// (where the student code is extracted) no matter what
		if (tester.getTestProperties().isTestRunnerInTestfileDir() && !testType.equals(TestOutcome.STUDENT_TEST))
			testRunnerCWD = getDirectoryFinder().getTestFilesDirectory();
		
		getLog().debug("TestRunner working directory: " +testRunnerCWD);
		
		// Execute the test!
		int exitCode;
		//XXX What is this timing?  This assumes we're timing the entire process, which
		// we're clearly not doing from here
		Alarm alarm = tester.getTestProcessAlarm();
		CombinedStreamMonitor monitor = null;

		Process testRunner = null;
		boolean isRunning = false;
		try {
			// Spawn the TestRunner process
			testRunner = Runtime.getRuntime().exec(
					javaArgs.toArray(new String[javaArgs.size()]),
					environment,
					testRunnerCWD
			);
            
            String cmd = MarmosetUtilities.commandToString(javaArgs);
            getLog().debug("TestRunner command: " + cmd);
            try {
                int pid=MarmosetUtilities.getPid(testRunner);
                getLog().debug("Subprocess for submission "  +getProjectSubmission().getSubmissionPK() +
                    " for testSetup " +getProjectSubmission().getProjectJarfilePK() +
                    " for "+testType +" "+nextTestNumber+
                    " " +testMethod+
                    " in testClass " +testClass+
                    " has pid = " +pid);
            } catch (IllegalAccessException e) {
                getLog().debug("Cannot get PID of process: " +e);
            } catch (NoSuchFieldException e) {
                getLog().debug("Cannot get PID of process: " +e);
            }
            
			isRunning = true;

			// Start the timeout alarm
			alarm.start();

			// Record the output
			monitor = tester.createStreamMonitor(testRunner.getInputStream(), testRunner.getErrorStream());
			monitor.start();
			
			// Wait for the test runner to finish.
			// This may be interrupted by the timeout alarm.
			monitor.join();
			exitCode = testRunner.waitFor();
			isRunning = false;
            // Groovy, we finished before the alarm went off.
            // Disable it (and clear our interrupted status)
            // in case it went off just after the process wait
            // finished.
            alarm.turnOff();
            
			// Just for debugging...
			getLog().debug("TestRunner process finished; captured to stdout/stderr output was: ");
			getLog().debug(monitor.getCombinedOutput());
            if (monitor.getCombinedOutput().contains("AccessControlException")) {
                getLog().warn("Clover could not be initialized due to an AccessControlException. " +
                        " Please check your security.policy file and make sure that student code " +
                        "has permission to read/write/delete /tmp and can install shutdown hooks");
            }
	
		} catch (IOException e) {
			String shortTestResult=getFullTestName() +" failed with IOException: " +e.getMessage();
			// TODO get a stack trace into here
			String longTestResult=e.toString();
			getLog().error(shortTestResult, e);
			return Tester.createUnableToRunOneTestOutcome(testType, testMethod, testClass, 
                nextTestNumber,	TestOutcome.FAILED, shortTestResult, longTestResult);
		} catch (InterruptedException e) {
			if (!alarm.fired())
				getLog().error("Someone unexpectedly interrupted the timer");

			String shortTestResult = "Timeout!";
			String longTestResult = monitor.getCombinedOutput();
			getLog().error(shortTestResult, e);
            getLog().trace("Timeout for " +testType+ " " +testMethod+ " " +nextTestNumber);
			return Tester.createUnableToRunOneTestOutcome(testType, testMethod, testClass, 
                nextTestNumber, TestOutcome.TIMEOUT, shortTestResult, longTestResult);
		} finally {
			// Make sure the process is cleaned up.
			if (isRunning)
				testRunner.destroy();
		}
		
		if (exitCode != 0) {
			// Test runner couldn't execute the tests for some reason.
			// This is probably not our fault.
			// Just add an outcome recording the output of
			// the test runner process.
			String shortTestResult = getFullTestName() +" subprocess failed to return with 0 status";
			String longTestResult = monitor.getCombinedOutput();
			getLog().error(shortTestResult);
			return Tester.createUnableToRunOneTestOutcome(testType, testMethod, testClass,
                nextTestNumber,	TestOutcome.FAILED, shortTestResult, longTestResult);
		}
		
		getLog().debug(getFullTestName()+ " test finished with " + new File(outputFilename).length() +
				" bytes of output");

		return readTestOutcomeFromFile();
	}
	
	private String getFullTestName() {
		return testType +"-"+ nextTestNumber +"-"+ testMethod;
	}

	/**
	 * Reads (and deserializes) a testOutcome from the file produced by the call
	 * in executeTests() that runs a test.  This call spawns a JVM in a separate process
	 * and so produces a file for us to read.
	 * TODO Delete the file when we're done with it.
	 * @return The deserialized testOutcome read from the file.
	 */
	private TestOutcome readTestOutcomeFromFile()
	{
		ObjectInputStream in=null;
		// TODO refactor and just read a single test outcome
		TestOutcomeCollection currentOutcomeCollection = new TestOutcomeCollection();
		try {
			// Open test outcome file.
			if (!currentOutcomeCollection.isEmpty()) {
				getLog().debug("Non-empty outcome collection and we're about to read " +testType+"."+outputFilename);
			}
			FileInputStream fis = tester.openTestOutcomeFile(testType, outputFilename);
			
			// XXX Legacy issue:  We used to run all of the test outcomes in separate threads,
			// producing a testOutcomeCollection.  Now we run each individual JUnit test case
			// in a separate JVM process (makes it easier to kill a test cleanly) and so we
			// are still reading/writing testOutcomeCollections rather than individual testOutcomes.
			in = new ObjectInputStream(new BufferedInputStream(fis));
			currentOutcomeCollection.read(in);
			
			// Find all the outcomes that running this test produced
			// See earlier legacy issue:
			// We are running each JUnit test in a separate process 
			// and should always have exactly one testOutcome
			if (currentOutcomeCollection.size() > 1) {
				for (TestOutcome outcome : currentOutcomeCollection) {
					getLog().debug("Multiple test outcomes coming back! " +outcome);
				}
				throw new IOException("JUnit tests must be run one at a time!");
			} else if (currentOutcomeCollection.isEmpty()) {
				// TODO put large messages into a messages.properties file
				String message = "Test " +testMethod+ " produced no outcome.  " +
				"This usually happens when a test case times out, but it " +
				"can happen for a variety of other reasons.  " +
				"For example, if a student calls System.exit(), this causes the test case to " +
				"stop suddenly and prevents the SubmitServer from figuring out what went wrong.\n" +
				"In general, you should not call System.exit(); if your program gets into a " +
				"situation where you want to halt immediately and signal an error, " +
				"then instead try throwing a RuntimeException, like this:\n" +
				"throw new RuntimeException(\"Halting program because...\")\n";
				throw new IOException(message);
			}
			return currentOutcomeCollection.getAllOutcomes().get(0);
		} catch (IOException e) {
			getLog().warn("IOException while reading currentOutcomeCollection from file", e);
			
			TestOutcome outcome = new TestOutcome();
			outcome.setTestType(testType);
			outcome.setTestName(testMethod);
			outcome.setOutcome(TestOutcome.FAILED);
			outcome.setShortTestResult("Unable to read test results for " +testMethod);
			outcome.setLongTestResult(e.getMessage());
			outcome.setTestNumber(nextTestNumber);
			outcome.setExceptionClassName("");
			outcome.setDetails(null);
			return outcome;
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ignore) {
				getLog().warn("Unable to close input Stream for " +outputFilename, ignore);
			}
		}
	}

	/**
	 * @return
	 */
	private boolean isStudentTestType() {
		return testType.equals(TestOutcome.STUDENT_TEST);
	}
}
