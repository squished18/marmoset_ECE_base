/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 25, 2005
 */
package edu.umd.cs.buildServer;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

/**
 * Run PMD on a compiled submission.
 * 
 * @see <a href="http://pmd.sourceforge.net">PMD website</a>
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class PMDRunner implements ConfigurationKeys, ISubmissionInspectionStep {
	private ProjectSubmission projectSubmission;
	private TestOutcomeCollection testOutcomeCollection;

	/**
	 * Constructor.
	 * setProjectSubmission() must be called before this object can be executed.
	 */
	public PMDRunner() {
		this.testOutcomeCollection = new TestOutcomeCollection();
	}
	
	private static final int PMD_TIMEOUT_IN_SECONDS = 120;

	public void setProjectSubmission(ProjectSubmission projectSubmission) {
		this.projectSubmission = projectSubmission;
	}

	public TestOutcomeCollection getTestOutcomeCollection() {
		return testOutcomeCollection;
	}

	public void execute() {
	    projectSubmission.getLog().debug("Running PMD on the code");
	    // rulesets of pmd detectors
	    projectSubmission.getLog().debug("BuildServer ROOT env: " +System.getenv("BUILDSERVER_ROOT"));
	    projectSubmission.getLog().debug(projectSubmission.getConfig().getOptionalProperty(BUILDSERVER_ROOT));
	    String RULESETS =
			"rulesets/basic.xml,rulesets/braces.xml," +
			"rulesets/codesize.xml,rulesets/controversial.xml," +
			"rulesets/coupling.xml,rulesets/design.xml," +
			"rulesets/naming.xml,rulesets/strictexception.xml," +
			"rulesets/strings.xml,rulesets/unusedcode.xml";
		
		String buildServerRoot = projectSubmission.getConfig().getOptionalProperty(BUILDSERVER_ROOT);
		if (buildServerRoot != null) {
			// Our own PMD rules
			RULESETS += "," +buildServerRoot+ "/pmd/langfeatures.xml";
		}
	    
	    // first stry the pmd.sh on the path
	    String pmdExe = "pmd.sh";
		// if PMD_HOME is specified, use this instead
	    if (projectSubmission.getConfig().getOptionalProperty(PMD_HOME) != null)
		{
		    pmdExe = projectSubmission.getConfig().getOptionalProperty(PMD_HOME) + "/bin/pmd.sh";
		}
	    List<String> args = new LinkedList<String>();
		args.add(pmdExe);
		args.add(projectSubmission.getZipFile().getPath());
		args.add("xml");
		args.add(RULESETS);
		
		projectSubmission.getLog().debug("pmd command: " +MarmosetUtilities.commandToString(args));

		Process process = null;
		boolean exited = false;
		Alarm alarm = new Alarm(PMD_TIMEOUT_IN_SECONDS, Thread.currentThread());
		try {
			process = Runtime.getRuntime().exec(
					args.toArray(new String[args.size()]));
			alarm.start();
			
			XMLDocumentBuilder stdoutMonitor = new XMLDocumentBuilder(
					process.getInputStream(), projectSubmission.getLog());
			Thread stderrMonitor = IO.monitor(process.getErrorStream(), new DevNullOutputStream());
			stdoutMonitor.start();
			stderrMonitor.start();
			
			// Wait for process to exit
			process.waitFor();
			stdoutMonitor.join();
			stderrMonitor.join();
			exited = true;
			alarm.turnOff();
			
			readPMDTestOutcomes(stdoutMonitor);

		} catch (IOException e) {
			projectSubmission.getLog().warn("Could not run PMD", e);
		} catch (InterruptedException e) {
			projectSubmission.getLog().info("PMD process timed out", e);
		} finally {
			if (process != null && !exited) {
				process.destroy();
			}
		}
	}

	private void readPMDTestOutcomes(XMLDocumentBuilder stdoutMonitor) {
		Document document = stdoutMonitor.getDocument();
		if (document == null)
			return;
		
		int count = 0;
		Iterator fileNodeIter = document.selectNodes("//pmd/file").iterator();
		while (fileNodeIter.hasNext()) {
			Node fileElement = (Node) fileNodeIter.next();
			String fileName = fileElement.valueOf("@name");
			Iterator violationIter = fileElement.selectNodes("./violation").iterator();
			while (violationIter.hasNext()) {
				Node violationElement = (Node) violationIter.next();
				String line = violationElement.valueOf("@line");
				String rule = violationElement.valueOf("@rule");
				String description = violationElement.getText();
				String priority = violationElement.valueOf("@priority");

				// Turn the warning into a TestOutcome
				TestOutcome testOutcome = new TestOutcome();
				testOutcome.setTestType(TestOutcome.PMD_TEST);
				testOutcome.setTestName(rule);
				testOutcome.setOutcome(TestOutcome.PMD_TEST);
				testOutcome.setShortTestResult(fileName + ":" + line);
				testOutcome.setLongTestResult(description);
				testOutcome.setTestNumber(count++);
				testOutcome.setExceptionClassName(priority); // XXX: HACK!
				testOutcome.setDetails(null);
				
				testOutcomeCollection.add(testOutcome);
			}
		}
		
		projectSubmission.getLog().info("Recorded " + count + " PMD warnings as test outcomes");
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println(
					"Usage: " +
					PMDRunner.class.getName() +
					" <submission zipfile>");
			System.exit(1);
		}

		String submissionZipFileName = args[0];
		
		ProjectSubmission projectSubmission = ProjectSubmission.createFakeProjectSubmission(
				"java",
				null,
				submissionZipFileName,
				null,
				null,
				null
				);
		
		PMDRunner pmdRunner = new PMDRunner();
		pmdRunner.setProjectSubmission(projectSubmission);
		pmdRunner.execute();
		
		pmdRunner.getTestOutcomeCollection().dump(System.out);
	}
}
