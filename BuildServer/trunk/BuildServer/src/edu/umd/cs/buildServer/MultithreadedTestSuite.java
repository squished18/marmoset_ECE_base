/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Sep 21, 2004
 */
package edu.umd.cs.buildServer;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * We run each test case in it own process which gives us more control
 * over killing infinite loops and deadlocks.  This class was always problematic
 * because it required Thread.stop().
 * <p>
 * A custom TestSuite class that runs each Test in 
 * a separate thread.  If the thread does not finish
 * before a timeout expires, the thread is killed
 * and the test is marked as a failure.
 * 
 * @author David Hovemeyer
 * @deprecated  We run each test case in it own process which gives us more control
 * over killing infinite loops and deadlocks.  This class was always problematic
 * because it required Thread.stop().
 */
public class MultithreadedTestSuite extends TestSuite {
	static final boolean DEBUG = Boolean.getBoolean("buildserver.debug.mttestsuite");
	
	/**
	 * Thread class to run a single Test.
	 */
	static private class RunTestThread extends Thread {
		private final TestResult testResult;
		private final Test test;
		private  boolean done;

		private RunTestThread(TestResult tempTestResult, Test test) {
			this.testResult = tempTestResult;
			this.test = test;
			this.done = false;
		}

		public void run() {
			test.run(testResult);
			if (DEBUG) System.out.println("Test finished, dude!");
			synchronized (this) {
				this.done = true;
				notifyAll();
			}
		}
		
		/**
		 * Return whether or not the Test has finished.
		 * @return true if the test finished, false otherwise
		 */
		public synchronized boolean isDone() {
			return this.done;
		}
	}

	/**
	 * TestListener that captures an error or failure
	 * for a single Test so that they can be replayed later
	 * into a TestResult.
	 */
	private static class ReplayTestListener implements TestListener {
		private Test test;
		private Throwable error;
		private AssertionFailedError failure;

		/**
		 * Constructor.
		 * @param test the Test whose execution we are listening to and recording
		 */
		public ReplayTestListener(Test test) {
			this.test = test;
		}
		
		public void addError(Test test, Throwable error) {
			this.error = error;
		}

		public void addFailure(Test test, AssertionFailedError failure) {
			this.failure = failure;
		}

		public void endTest(Test test) { }

		public void startTest(Test test) { }
		
		/**
		 * Replay the recorded test into given TestResult.
		 * @param testResult the TestResult
		 */
		public void replay(TestResult testResult) {
			testResult.startTest(test);
			
			if (error != null)
				testResult.addError(test, error);
			else if (failure != null)
				testResult.addFailure(test, failure);
			
			testResult.endTest(test);
		}
	}

	// Fields
	private int testTimeoutInSeconds;
	private String methodName;
	//private String expectedTestCaseName;
	
	/**
	 * Constructor.
	 * @param suiteClass           test suite Class
	 * @param testTimeoutInSeconds number of seconds to allow each Test before
	 *                             killing the test thread and recording the
	 *                             Test as a failure
	 */
	public MultithreadedTestSuite(Class suiteClass, int testTimeoutInSeconds) {
		this(suiteClass, testTimeoutInSeconds, null);
	}

	/**
	 * Constructor for testing a single test method.
	 * 
	 * @param suiteClass           test suite Class
	 * @param testTimeoutInSeconds number of seconds to allow the single Test before
	 *                             killing the test thread and recording the
	 *                             Test as a failure
	 * @param methodName           name of test method to run
	 */
	public MultithreadedTestSuite(Class suiteClass, int testTimeoutInSeconds, String methodName) {
		super(suiteClass);
		this.testTimeoutInSeconds = testTimeoutInSeconds;
		this.methodName = methodName;
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestSuite#runTest(junit.framework.Test, junit.framework.TestResult)
	 */
	public void runTest(final Test test, TestResult testResult) {

		// If a testMethod name was specified, make sure that's what is
		// being run.
		
		// Note that we really only support TestCase objects (since instances of a
		// suite class where a single test method will be executed dynamically),
		// not arbitrary Test objects (since we can't find the test method name
		// in that case).
		
		if (methodName != null && (test instanceof TestCase)) {
			TestCase testCase = (TestCase) test;
			if (DEBUG) System.err.println("Running test " + testCase.getName());
			if (!testCase.getName().equals(methodName)) {
				if (DEBUG) System.err.println("Skipping this test");
				return;
			}
		}
		
		// Create a ReplayTestListener to save the result of
		// the test, and a dummy TestResult to call it.
		ReplayTestListener listener = new ReplayTestListener(test);
		final TestResult oneShotTestResult = new TestResult();
		oneShotTestResult.addListener(listener);

		// Create thread to run the test
		RunTestThread runTestThread = new RunTestThread(oneShotTestResult, test);
		
		runTestThread.start();
		long finishTime = System.currentTimeMillis() + testTimeoutInSeconds * 1000L;
		synchronized(runTestThread) {
			while (!runTestThread.isDone() ) {
				long additionalTime = finishTime - System.currentTimeMillis();
				if (additionalTime <= 0) break;
				try {
					runTestThread.wait(additionalTime);
				} catch (InterruptedException e1) {
					// TODO: Log it, but ignore it
					// OK, the BuildServer monitors our stderr
					e1.printStackTrace(); 
				}
			}
		}
		// either isDone, or out of time
		if (!runTestThread.isDone() ) {
			if (DEBUG) System.out.println("Killing test thread, returning failure");
			runTestThread.interrupt();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO: Log it, but ignore it
			}
			
			if (!runTestThread.isDone()) {
				runTestThread.stop();
				testResult.startTest(test);
				testResult.addFailure(test, new TestTimeoutError("Test exceeded timeout of " +
						testTimeoutInSeconds + " seconds"));
				testResult.endTest(test);
				return;
			}
		}

		// Groovy, the test ran to completion and recorded its result.
		// The ReplayTestListener can add the outcome to the
		// real TestResult.
		listener.replay(testResult);
	
	}
}
