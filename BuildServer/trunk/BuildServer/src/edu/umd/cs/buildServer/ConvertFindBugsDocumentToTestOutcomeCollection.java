/*
 * Copyright (C) 2004-2005, University of Maryland
 * All Rights Reserved
 * Created on May 6, 2005
 */
package edu.umd.cs.buildServer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SAXBugCollectionHandler;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * Convert a Document containing FindBugs results into
 * TestOutcomes, and add them to a TestOutcomeCollection.
 * 
 * @author David Hovemeyer
 */
public class ConvertFindBugsDocumentToTestOutcomeCollection {
	private Document document;
	private TestOutcomeCollection testOutcomeCollection;
	
	/**
	 * Constructor.
	 * 
	 * @param document              the Document containing FindBugs results
	 * @param testOutcomeCollection the TestOutcomeCollection to add FindBugs TestOutcomes to
	 */
	public ConvertFindBugsDocumentToTestOutcomeCollection(
			Document document,
			TestOutcomeCollection testOutcomeCollection) {
		this.document = document;
		this.testOutcomeCollection = testOutcomeCollection;
	}
	
	/**
	 * Perform the conversion from FindBugs XML to TestOutcomes.
	 */
	public void convert() {
		int count = 0;
		for (Iterator i = document.selectNodes("/BugCollection/BugInstance").iterator(); i.hasNext();) {
			Node bugInstanceNode = (Node) i.next();
			
			String type = bugInstanceNode.valueOf("@type");
			String warningText = bugInstanceNode.valueOf("./LongMessage");
			String location = bugInstanceNode.valueOf("./SourceLine/Message");
			String priority = bugInstanceNode.valueOf("@priority");

			// Turn the warning into a TestOutcome
			TestOutcome testOutcome = new TestOutcome();
			testOutcome.setTestType(TestOutcome.FINDBUGS_TEST);
			testOutcome.setTestName(type);
			testOutcome.setOutcome(TestOutcome.WARNING);
			testOutcome.setShortTestResult(location);
			testOutcome.setLongTestResult(warningText);
			testOutcome.setTestNumber(count++);
			testOutcome.setExceptionClassName(priority); // XXX: Hack!
			testOutcome.setDetails(null);
			
			testOutcomeCollection.add(testOutcome);
			nodeToTestOutcomeHook(bugInstanceNode, testOutcome);
		}
		
	}

	/**
	 * Hook called when a BugInstance Node is converted into a TestOutcome.
	 * By default, this does nothing.  Subclasses may override.
	 * 
	 * @param bugInstanceNode the BugInstance Node in the XML document
	 * @param testOutcome     the TestOutcome created to represent the BugInstance
	 */
	protected void nodeToTestOutcomeHook(Node bugInstanceNode, TestOutcome testOutcome) {
	}

	private static String inputFile;

	public static void main(String[] argv) {
		try {
			runMain(argv);
		} catch (Exception e) {
			if (inputFile != null) {
				System.err.println("While converting " + inputFile);
			}
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void runMain(String[] argv) throws Exception {
		if (argv.length < 1) {
			System.err.println("Usage: " + ConvertFindBugsDocumentToTestOutcomeCollection.class.getName() +
					" <findbugs xml file> [<findbugs xml file> ...]");
			System.exit(1);
		}
		
		for (int i = 0; i < argv.length; ++i) {
			String docFile = argv[i];
			if (!docFile.endsWith(".xml")) {
				System.err.println("Skipping invalid input filename " + docFile);
				continue;
			}
			String outputFile = docFile.substring(0, docFile.length() - ".xml".length()) + ".out";

			inputFile = docFile;
			
			// XXX make sure we have uids
			int nextUID = 0;
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(docFile));
			for (Iterator j = document.selectNodes("/BugCollection/BugInstance").iterator(); j.hasNext();) {
				Element element = (Element) j.next();
				Attribute attr = element.attribute("uid");
				if (attr == null) {
					element.addAttribute("uid", "");
					attr=element.attribute("uid");
				}
				attr.setValue("" + nextUID++);
			}
			
			// Convert to BugCollection
			SortedBugCollection bugCollection = new SortedBugCollection();
			SAXBugCollectionHandler saxHandler =
				new SAXBugCollectionHandler(bugCollection, new Project());
			SAXWriter writer = new SAXWriter(saxHandler);
			writer.write(document);

			// XXX code duplication w/ FindBugsRunner 
			TestOutcomeCollection testOutcomeCollection = new TestOutcomeCollection();
			final HashMap<Integer, TestOutcome> uidToTestOutcomeMap = new HashMap<Integer, TestOutcome>();
			ConvertFindBugsDocumentToTestOutcomeCollection converter =
				new ConvertFindBugsDocumentToTestOutcomeCollection(document, testOutcomeCollection) {
				protected void nodeToTestOutcomeHook(Node bugInstanceNode,TestOutcome testOutcome) {
					try {
						Integer uid = Integer.decode(bugInstanceNode.valueOf("@uid"));
						uidToTestOutcomeMap.put(uid, testOutcome);
					} catch (NumberFormatException e) {
						// Ignore
					}
				}
			};
			converter.convert();

			// Try to add actual BugInstance objects to the TestOutcomes.
			// XXX code duplication w/ FindBugsRunner 
			for (Iterator j = bugCollection.iterator(); j.hasNext(); ) {
				BugInstance bugInstance =(BugInstance) j.next();
				String uid = bugInstance.getUniqueId();
				if (uid == null)
					continue;
				
				try {
					TestOutcome testOutcome = uidToTestOutcomeMap.get(Integer.decode(uid));
					if (testOutcome != null) {
						// Something is messing up SourceLineAnnotations getting Messages 
						SourceLineAnnotation sourceLine = bugInstance.getPrimarySourceLineAnnotation();
						if (sourceLine != null) {
							testOutcome.setShortTestResult(sourceLine.toString());
						}
						
						testOutcome.setDetails(bugInstance);
					}
				} catch (NumberFormatException e) {
					System.err.println("Bad uid in BugInstance:"+ e);
				}
			}
			
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
				testOutcomeCollection.write(out);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}

// vim:ts=4
