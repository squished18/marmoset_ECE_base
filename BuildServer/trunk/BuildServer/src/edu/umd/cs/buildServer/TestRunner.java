/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Aug 24, 2004
 */
package edu.umd.cs.buildServer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;

import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.ExceptionData;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * Run some JUnit tests and record the outcomes.
 * 
 * @author Bill Pugh
 * @author David Hovemeyer
 */
public class TestRunner extends BaseTestRunner {
	/**
	 * An input stream that does nothing but return EOF.
	 */
	private static class DevNullInputStream extends InputStream {
		/* (non-Javadoc)
		 * @see java.io.InputStream#read()
		 */
		public int read() throws IOException {
			return -1;
		}
	}
	
	private static final int DEFAULT_TEST_TIMEOUT_IN_SECONDS = 30;


	// XXX: this field seems to be unused
	private String submissionPK;
	
	
	private String testType;
	private int testTimeoutInSeconds;
	private TestOutcomeCollection outcomeCollection;
	/** If nonnull, the named test method will be the only test case executed. */
	private String testMethod;
	/** Where to start numbering recorded test outcomes. */
	private int nextTestNumber;
	
	// Transient state
	private Class suiteClass;
	private TestOutcome currentTestOutcome;
	private int failCount, passCount;
	private static Logger log;
    private static Logger getLog() {
    	if (log==null) {
    		log=Logger.getLogger(BuildServer.class);
    	}
    	return log;
    }
	

	/**
	 * Constructor
	 * @param submissionPK PK of the submission being tested
	 * @param testType type of test being performed
	 */
	public TestRunner(String submissionPK, String testType, int testTimeoutInSeconds) {
		this.submissionPK = submissionPK;
		this.testType = testType;
		this.testTimeoutInSeconds = testTimeoutInSeconds;
		this.outcomeCollection = new TestOutcomeCollection();
		
		this.nextTestNumber = 0;
		this.currentTestOutcome = null;
		this.failCount = this.passCount = 0;
	}
	
	/**
	 * Set the single test method to execute.
	 * By default, all test methods in the test suite class
	 * will be executed.
	 * 
	 * @param testMethod the name of the single test method to execute
	 */
	public void setTestMethod(String testMethod) {
		this.testMethod = testMethod;
	}
	
	/**
	 * Set number of first test case to be recorded.
	 * 
	 * @param nextTestNumber
	 */
	public void setNextTestNumber(int nextTestNumber) {
		this.nextTestNumber = nextTestNumber;
	}
	
	/**
	 * Get Collection containing all TestOutcomes.
	 * @return the Collection of TestOutcomes
	 */
	public Collection getTestOutcomes() {
		return outcomeCollection.getAllOutcomes();
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see junit.runner.BaseTestRunner#testStarted(java.lang.String)
	 */
	public void testStarted(String testName) {
		// Create a new (incomplete) TestOutcome to represent
		// the outcome of this test.

		currentTestOutcome = new TestOutcome();
		currentTestOutcome.setTestType(testType);
		currentTestOutcome.setTestName(testName);
		currentTestOutcome.setTestNumber(nextTestNumber++);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.runner.BaseTestRunner#testEnded(java.lang.String)
	 */
	public void testEnded(String testName) {
		if (currentTestOutcome.getOutcome() == null) {
			++passCount;
			
			// The test didn't fail, so it must have succeeded.
			currentTestOutcome.setOutcome(TestOutcome.PASSED);
			currentTestOutcome.setShortTestResult("PASSED");
			currentTestOutcome.setLongTestResult("");
			// since this didn't fail, these can be empty
			currentTestOutcome.setExceptionClassName("");
			currentTestOutcome.setDetails(null);
		}
		outcomeCollection.add(currentTestOutcome);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.runner.BaseTestRunner#testFailed(int, junit.framework.Test,
	 *      java.lang.Throwable)
	 */
	public void testFailed(int status, Test test, Throwable t) {
		++failCount;
		
		// determine finer-grained cause of failure
		if (notYetImplemented(t)) {
		    currentTestOutcome.setOutcome(TestOutcome.NOT_IMPLEMENTED);
		} else if (t instanceof TestTimeoutError) {
		    currentTestOutcome.setOutcome(TestOutcome.TIMEOUT);
		} else if (t instanceof SecurityException) {
		    currentTestOutcome.setOutcome(TestOutcome.HUH);
		} else if (t instanceof AssertionFailedError) {
		    currentTestOutcome.setOutcome(TestOutcome.FAILED);
		} else if (isThrownFromTestCode(t)) {
			// We assume that any exception thrown from test code
			// is the student's fault.  E.g., a method which was
			// supposed to return a non-null value returned null,
			// and the test code dereferenced it.
			currentTestOutcome.setOutcome(TestOutcome.FAILED);
		} else {
		    currentTestOutcome.setOutcome(TestOutcome.ERROR);
		}
        currentTestOutcome.setShortTestResult( t.toString() + formatShortExceptionMessage(t) );
		currentTestOutcome.setLongTestResult(formatExceptionText(t));
		ExceptionData exceptionData = new ExceptionData(t);
		currentTestOutcome.setExceptionClassName(exceptionData.getClassName());
		// No longer storing exception object in the details field
		// I'm now putting the code coverage data
		//currentTestOutcome.setDetails(exceptionData);
	}
	
	/**
	 * Return whether or not the given exception was thrown
	 * from test code.
	 * 
	 * @param t the exception
	 * @return true if the exception was thrown from test code, false otherwise
	 */
	private boolean isThrownFromTestCode(Throwable t) {
		StackTraceElement[] trace = t.getStackTrace();
		
		if (trace.length < 1)
			return false;

        return trace[0].getClassName().contains(suiteClass.getName());
	}

	/**
	 * Checks if the functionality this test case exercises has not been implemented.  This allows us
	 * to distinguish between a method throwing UnsupportedOperationException because it hasn't been 
	 * implemented from a test cause that fails because of another type of exception (such as
	 * AssertionFailedException).
	 * @param t the throwable
	 * @return true if this test case failed because the necessary functionality has not yet been
	 * implemented; false otherwise
	 */
	private static boolean notYetImplemented(Throwable t)
	{
	    if (t instanceof UnsupportedOperationException || t instanceof NoSuchMethodException || t instanceof ClassNotFoundException)
	        return true;
	    if (t.getCause() instanceof UnsupportedOperationException)
	    	return true;
	    return false;
	}

	/**
	 * Format exception to describe (briefly) where the exception occurred.
	 * @param t the exception
	 * @return where the exception occurred
	 */
	private static String formatShortExceptionMessage(Throwable t) {
		StackTraceElement[] trace = t.getStackTrace();
		if (trace.length == 0)
			return " at unknown source line";
		else
 			return " at " + trace[0].toString() + "...";
	}

	/**
	 * Format an exception object to store in the long_test_result
	 * field of the test_outcomes table.
	 * @param t the exception
	 * @return the long description string for the exception
	 */
	private static String formatExceptionText(Throwable t) {
		StringBuffer buf = new StringBuffer();
		buf.append(t.toString());
		StackTraceElement[] stack = t.getStackTrace();
		for (int i = 0; i < stack.length; ++i) {
			StackTraceElement element = stack[i];
			buf.append("\n\t" + element.toString());
		}
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.runner.BaseTestRunner#runFailed(java.lang.String)
	 */
	protected void runFailed(String message) {
		getLog().debug("Run failed: " +message);
	}

	public void runTests(String name)
	throws BuilderException
	{
		Thread t = new Thread("suicideThread") {
		    public void run() {
                try {
                    Thread.sleep(testTimeoutInSeconds * 1000 + 15*60*1000);
                    System.exit(1);
                } catch (InterruptedException ignore) {
                    // ignore
                }
            }
        };
        t.setDaemon(true);
        t.start();
        
        getBuildServerLog().trace("Running tests for class " + name);
		
		Test suite = getTest(name);
		// TODO Throw an exception here!
		if (suite == null) {
			getBuildServerLog().fatal("Could not load test " + name);
			throw new BuilderException("Could not load test " +name);
		}
		TestResult result = new TestResult();
		result.addListener(this);
		suite.run(result);
	}

	/* (non-Javadoc)
	 * @see junit.runner.BaseTestRunner#getTest(java.lang.String)
	 */
	public Test getTest(String testClassName) {
		// Return a TestSuite for the class that executes each
		// Test in its own thread, killing threads for tests
		// that exceed the timeout value.
		
		// As a side-effect, store the test suite Class

		try {
			this.suiteClass = loadSuiteClass(testClassName);
		} catch (ClassNotFoundException e) {
			runFailed("Could not load test class " + testClassName + ": " + e.toString());
			return null;
		}
	
		// TODO replace MultithreadedTestSuite with something that doesn't call Thread.stop()
        TestSuite ts = new TestSuite();
        ts.addTest(TestSuite.createTest(suiteClass, testMethod));
        return ts;
		//return new MultithreadedTestSuite(suiteClass, testTimeoutInSeconds, testMethod);
	}
	
	private static Logger buildServerLog;
	private static Logger getBuildServerLog() {
		if (buildServerLog==null) {
			buildServerLog = Logger.getLogger("edu.umd.cs.buildServer.BuildServer");
		}
		return buildServerLog;
	}

	public static void main(String[] args) {
		int startTestNumber = -1;
		
		int argCount = 0;
		while (argCount < args.length) {
			String opt = args[argCount];
			if (!opt.startsWith("-"))
				break;
			if (opt.equals("-startTestNumber")) {
				++argCount;
				if (argCount >= args.length)
					throw new IllegalArgumentException("-startTestNumber option requires argument");
				startTestNumber = Integer.parseInt(args[argCount]);
			} else {
				throw new IllegalArgumentException("Unknown option " + opt);
			}
			
			++argCount;
		}
		if (argCount > 0) {
			String[] remainingArgs = new String[args.length - argCount];
			System.arraycopy(args, argCount, remainingArgs, 0, remainingArgs.length);
			args = remainingArgs;
		}

		if (args.length < 4 || args.length > 6) {
			getBuildServerLog().fatal("Usage: " + TestRunner.class.getName() +
					" [options] <submission_pk> <test_type> <test classname> <output file> " +
					"[<test timeout in seconds>] [<test method>]");
			getBuildServerLog().fatal("Options:");
			getBuildServerLog().fatal("-startTestNumber <n>   Start numbering test outcomes at <n>");
			System.exit(1);
		}
		
		String submissionPK = args[0]; 
		String testType = args[1];
		String testClassname = args[2];
		String outputFile = args[3];
		
		int testTimeoutInSeconds = DEFAULT_TEST_TIMEOUT_IN_SECONDS;
		if (args.length >= 5) {
			// The JavaTester will pass -1 if the test timeout was not explicitly
			// specified.
			int argVal = Integer.parseInt(args[4]);
			if (argVal > 0)
				testTimeoutInSeconds = argVal;
		}
		
		String testMethod = null;
		if (args.length >= 6) {
			testMethod = args[5];
		}
		
		// Redirect reads from System.in so that they always return EOF
		System.setIn(new DevNullInputStream());
		
		// Execute the tests
		TestRunner r = new TestRunner(submissionPK, testType, testTimeoutInSeconds);
		if (testMethod != null) {
			r.setTestMethod(testMethod);
		}
		if (startTestNumber >= 0) {
			r.setNextTestNumber(startTestNumber);
		}
		
		// Save test results to a file
		try {
			r.runTests(testClassname);
			
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(outputFile)));
			
			out.writeObject(r.getTestOutcomes());
			out.close();
			
			// Shutdown the process.
			// There may be non-daemon threads running which would
			// keep the process alive if we just fell off the
			// end of main().
			System.exit(0);
		} catch (BuilderException e) {
			getBuildServerLog().fatal("runTests() failed", e);
			System.exit(1);
		} catch (IOException e) {
			getBuildServerLog().fatal("TestRunner raised an IOException", e);
			System.exit(1);
		}
	}
}
