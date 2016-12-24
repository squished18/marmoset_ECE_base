/*
 * Copyright (C) 2004-2005, University of Maryland
 * All Rights Reserved
 * Created on Sep 25, 2004
 */
package edu.umd.cs.buildServer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Node;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;

/**
 * Run FindBugs on a project and report the generated warnings
 * as TestOutcomes.
 * 
 * @author David Hovemeyer
 */
public class FindBugsRunner extends AbstractFindBugsRunner {
	private static final boolean ALL_DETECTORS = false;
	
	/**
	 * Delta from default detector set.
	 * Omit FindOpenStream, DroppedException, SerializableIdiom, FindHEmismatch,
	 * and IteratorIdioms, add FindDeadLocalStores and InfiniteRecursiveLoop.
	 */
	private static final String VISITORS_DELTA =
		"-FindOpenStream,-DroppedException,-SerializableIdiom,-FindHEmismatch," +
		"-IteratorIdioms,-CloneIdiom,-ComparatorIdiom"
		;

	/**
	 * Constructor.
	 * setProjectSubmission() must be called before the object
	 * can be used.
	 */
	public FindBugsRunner() {
	}

	protected String[] getExtraFindBugsOptions() {
		// TODO Refactor to read options from test.properties file.
        if (ALL_DETECTORS) {
			return new String[]{"-low"};
		} else {
			return new String[]{
					"-chooseVisitors",
					VISITORS_DELTA,
					"-bugCategories",
					"CORRECTNESS",
			};
		}
	}

	protected Thread createStdoutMonitor(InputStream in) {
		return new FindBugsDocumentBuilder(in, projectSubmission.getLog());
	}

	protected Thread createStderrMonitor(InputStream err) {
		return IO.monitor(err, new DevNullOutputStream());
	}
	
	protected void inspectFindBugsResults(Thread stdoutMonitor, Thread stderrMonitor) {
		Document document = ((FindBugsDocumentBuilder) stdoutMonitor).getDocument();
		if (document == null) {
			getProjectSubmission().getLog().warn("No document read from findbugs process");
			return;
		}
		
		SortedBugCollection bugCollection = ((FindBugsDocumentBuilder) stdoutMonitor).getBugCollection();
		if (bugCollection == null) {
			getProjectSubmission().getLog().warn("Could not get BugCollection from findbugs process");
			return;
		}

		// Convert the Document to TestOutcomes, keeping track of
		// the mapping of BugInstance uids to TestOutcomes.
		final HashMap<Integer, TestOutcome> uidToTestOutcomeMap = new HashMap<Integer, TestOutcome>();
		ConvertFindBugsDocumentToTestOutcomeCollection converter =
			new ConvertFindBugsDocumentToTestOutcomeCollection(document, getTestOutcomeCollection()) {
			protected void nodeToTestOutcomeHook(Node bugInstanceNode,TestOutcome testOutcome) {
				try {
					Integer uid = Integer.decode(bugInstanceNode.valueOf("@uid"));
					uidToTestOutcomeMap.put(uid, testOutcome);
				} catch (NumberFormatException e) {
					// Ignore
				}
			}
		};
		projectSubmission.getLog().trace("Reading XML document...");
		converter.convert();

		// Try to add actual BugInstance objects to the TestOutcomes.
		// They will be serialized and stored in the database.
		for (Iterator i = bugCollection.iterator(); i.hasNext(); ) {
			BugInstance bugInstance =(BugInstance) i.next();
			String uid = bugInstance.getUniqueId();
			if (uid == null)
				continue;
			
			try {
				TestOutcome testOutcome = uidToTestOutcomeMap.get(Integer.decode(uid));
				if (testOutcome != null) {
					testOutcome.setDetails(bugInstance);
				}
			} catch (NumberFormatException e) {
				projectSubmission.getLog().warn("Bad uid in BugInstance", e);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println(
					"Usage: " +
					FindBugsRunner.class.getName() +
					" <build_directory>" +
					" <project jar file>");
			System.exit(1);
		}
		
		String buildDirectoryName = args[0];
		String projectJarFileName = args[1];

		ProjectSubmission projectSubmission = ProjectSubmission.createFakeProjectSubmission(
				"java",
				buildDirectoryName,
				null,
				null,
				projectJarFileName,
				null
				);

		if (System.getProperty("findbugs.options") != null) {
			projectSubmission.getTestProperties().setProperty("findbugs.options", System.getProperty("findbugs.options"));
		}
		
		FindBugsRunner findBugsRunner = new FindBugsRunner();
		findBugsRunner.setProjectSubmission(projectSubmission);
		findBugsRunner.execute();
		
		findBugsRunner.getTestOutcomeCollection().dump(System.out);
	}
}

// vim:ts=4
