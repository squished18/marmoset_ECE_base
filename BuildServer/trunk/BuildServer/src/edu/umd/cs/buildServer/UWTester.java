package edu.umd.cs.buildServer;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;


import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

public class UWTester extends CTester {
// these are from cwrapper.c
static final int correct_ret = 0;
static final int runtime_ret = 1;
static final int timelimit_ret = 2;
static final int wrong_ret = 3;


	public UWTester(TestProperties testProperties, boolean haveSecurityPolicyFile, ProjectSubmission projectSubmission, DirectoryFinder directoryFinder) {
		super(testProperties, haveSecurityPolicyFile, projectSubmission, directoryFinder);
	}

	/**
	 * Execute a single test executable.
	 * 
	 * @param exeName    name of the test executable
	 * @param testType   test type (public, release, secret, etc.)
	 * @param testNumber test number (among other tests of the same type)
	 */
	protected void executeTest(String exeName, String testType, int testNumber) throws BuilderException {

		Process process = null;
		boolean finished = false;
		
		CombinedStreamMonitor streamMonitor = null;
		
		try {
			// Hopefully the test executable is really there.
			checkTestExe(exeName);
			
			// Run the test executable in the build directory.
			getLog().debug("Running C test number " +testNumber+ ": " +exeName+
			        " process in directory " + getDirectoryFinder().getBuildDirectory());
			// Add LD_LIBRARY_PATH according to the environment, if requested
            String[] environment=null;
            if (getTestProperties().getLdLibraryPath()!=null) {
                environment = new String[] {
                    getTestProperties().getLdLibraryPath(),
                    "MARM_SUBPK="+getProjectSubmission().getSubmissionPK(),
                    "MARM_SETUPPK="+getProjectSubmission().getProjectJarfilePK()
                };
                getLog().debug(getTestProperties().getLdLibraryPath());
            } else {
                environment = new String[] {
                    "MARM_SUBPK="+getProjectSubmission().getSubmissionPK(),
                    "MARM_SETUPPK="+getProjectSubmission().getProjectJarfilePK()
                };
            }
			
            int maxTime = getTestProperties().getTestTimeoutInSeconds();
            maxTime += 1;
            String shell = getProjectSubmission().getConfig().getStringProperty(SHELL, "bash");
            // XXX Will only work on a *nix machine.  Actually the entire process of testing
            // C, Ruby or OCaml will only work on Linux (and maybe Solaris but it hasn't been tested).
            String[] command = null;
            
            // Using an unprivileged account:
            // Note that this requires the instructor's Makefile to give o+x permissions
            // to anything it needs to execute, such as utility perl scripts for diffing files
            // and whatnot.
            
            // ulimits are in bytes
            // TODO Make the parameters passed to ulimit configurable by the test.properties file
            // either globally (i.e. for all test cases) or per-test case
            // XXX The virtual memory limit is really high because Java allocates
            // a huge amount of virtual memory that it never uses, even if you set
            // something like -Xmx=128m .  While this code is the for CTester
            // and ostensibly runs C code (or other code launched from the command line)
            // this mechanism is used for example in CMSC 420 to call Java, 
            // which will fail if it cannot allocate a huge amount of virtual memory.
            // Thus, we're limiting the actual memory
            // to 128 MB and the virtual memory to 756 MB, and hoping this is
            // well enough balanced to let Java allocate all the virtual memory it
            // wants, but will kill student C code that's calling malloc in a loop
            // before it takes down the BuildServer.
            int virtualMemoryLimit=750*(1024*1024);
            int memoryLimit=128*(1024*1024);
            String unprivilegedAccount=getProjectSubmission().getConfig().getOptionalProperty(UNPRIVILEGED_ACCOUNT);
            if (unprivilegedAccount!=null && !unprivilegedAccount.trim().equals("")) {
                command=new String[] {shell, "-c", "ulimit -t " +maxTime+ 
                " -v " +virtualMemoryLimit+ " "+
                //" -m " +memoryLimit + " "+
                " ; "+
                "cd " +getDirectoryFinder().getBuildDirectory().getAbsolutePath()+
                " ; "+
                "sudo -u " +unprivilegedAccount.trim()+ " "+
                "./" +exeName
                };
            } else {
                /*
                command=new String[] {shell, "-c", "ulimit -t " +maxTime+ 
                " -v  " +virtualMemoryLimit+ " "+
                //" -m " +memoryLimit+ " "+
                " ; "+
                new File(getDirectoryFinder().getBuildDirectory(), exeName).getAbsolutePath()};
                */
                command=new String[] {shell, "-c", 
                //"cwrapper "+maxTime+" "+
                new File(getDirectoryFinder().getBuildDirectory(), exeName).getAbsolutePath()+" "+maxTime};
            }

            getLog().debug("Test command: " +MarmosetUtilities.commandToString(command));
            process = Runtime.getRuntime().exec(
                    command,
					environment,
					getDirectoryFinder().getBuildDirectory());
			
			// Read the stdout/stderr from the test executable.
			streamMonitor = new CombinedStreamMonitor(
					process.getInputStream(), process.getErrorStream());
			streamMonitor.start();

			// Start a thread which will wait for the process to exit.
			// The issue here is that Java has timed monitor waits,
			// but not timed process waits.  We emulate the latter
			// using the former.
	//		ProcessExitMonitor exitMonitor = new ProcessExitMonitor(process);
	//		exitMonitor.start();

			// Record a test outcome.
			TestOutcome testOutcome = new TestOutcome();
			testOutcome.setTestNumber(testNumber);
			testOutcome.setTestName(exeName);
			testOutcome.setTestType(testType);
			long processTimeoutMillis = getTestProperties().getTestTimeoutInSeconds() * 1000L;
            

			// Wait for the process to exit.
				int exitCode = process.waitFor();
				finished = true;
				streamMonitor.join();
				getLog().debug("Process exited with exit code " + exitCode);
				
                                switch(exitCode) {
                                    case correct_ret:
                                        testOutcome.setOutcome(TestOutcome.PASSED);
				testOutcome.setShortTestResult("Test " + exeName + " " + testOutcome.getOutcome());
				// XXX We're storing the output from the streamMonitor in the 
				// testOutcome record whether it passes or fails
				testOutcome.setLongTestResult(streamMonitor.getCombinedOutput());
                                        break;
                                    case timelimit_ret:
			    testOutcome.setOutcome(TestOutcome.TIMEOUT);
                testOutcome.setShortTestResult("Test " + exeName + " did not complete before the timeout of " +
                						getTestProperties().getTestTimeoutInSeconds() + " seconds)");
                testOutcome.setLongTestResult(streamMonitor != null ? streamMonitor.getCombinedOutput() : "");

                                        break;
                                    case runtime_ret:
                                    case wrong_ret:
                                        testOutcome.setOutcome(exitCode==wrong_ret ? TestOutcome.FAILED : TestOutcome.ERROR);
				testOutcome.setShortTestResult("Test " + exeName + " " + testOutcome.getOutcome());
				// XXX We're storing the output from the streamMonitor in the 
				// testOutcome record whether it passes or fails
				testOutcome.setLongTestResult(streamMonitor.getCombinedOutput());
                                        break;
                                    default:
			throw new BuilderException("test script returned unknown exit code: "+ exitCode);
                                }
                                testOutcome.setShortTestResult(testOutcome.getShortTestResult());
				
				
			
			getTestOutcomeCollection().add(testOutcome);
		} catch (IOException e) {
			// Possible reasons this could happen are:
			//   - the Makefile is buggy and didn't create the exes it should have
			//   - a temporary resource exhaustion prevented the process from running 
			// In any case, we can't trust the test results at this point,
			// so we'll abort all testing of this submission.
			throw new BuilderException("Could not run test process", e);
		} catch (InterruptedException e) {
			throw new BuilderException("Test process wait interrupted unexpectedly", e);	
		} finally {
			// Whatever happens, make sure we don't leave the process running
			if (process != null && !finished) {
				MarmosetUtilities.destroyProcessGroup(process, getLog());
			}
		}
	}
}
