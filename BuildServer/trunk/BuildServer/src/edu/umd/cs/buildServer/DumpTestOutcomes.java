/*
 * Copyright (C) 2005 University of Maryland
 * All Rights Reserved
 * Created on Jan 23, 2005
 */
package edu.umd.cs.buildServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.Iterator;

import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * Dump a saved TestOutcomeCollection to stdout.
 * 
 * @author David Hovemeyer
 */
public class DumpTestOutcomes {
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage: " + DumpTestOutcomes.class.getName() +
					" <test outcome file>");
			System.exit(1);
		}
		
		String filename = args[0];
		
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(filename));
			TestOutcomeCollection testOutcomeCollection= new TestOutcomeCollection();
			testOutcomeCollection.read(in);
			
			dump(testOutcomeCollection, System.out);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

	/**
	 * @param testOutcomeCollection
	 * @param out
	 */
	public static void dump(TestOutcomeCollection testOutcomeCollection, PrintStream out) {
		for (Iterator i = testOutcomeCollection.iterator();i.hasNext();) {
			TestOutcome testOutcome = (TestOutcome) i.next();
			
			out.println(testOutcome.getTestType() + "(" + testOutcome.getTestNumber() +
					")," + testOutcome.getTestName() + "," + testOutcome.getOutcome() +
					(testOutcome.getTestType().equals(TestOutcome.RELEASE_TEST) || 
                    testOutcome.getTestType().equals(TestOutcome.SECRET_TEST) ?
                        ","+testOutcome.getExceptionSourceCoveredElsewhere() +","+
                        testOutcome.getCoarsestCoverageLevel() : "")
            );
            if (testOutcome.getOutcome().equals(TestOutcome.FAILED) ||
			        testOutcome.getOutcome().equals(TestOutcome.ERROR) ||
			        testOutcome.getOutcome().equals(TestOutcome.HUH) ||
			        testOutcome.getOutcome().equals(TestOutcome.TIMEOUT)) {
				out.println("Long test result for failed test:");
				out.println(testOutcome.getLongTestResult());
			}
            if (testOutcome.getOutcome().equals(TestOutcome.UNCOVERED_METHOD)) {
                out.println("uncovered method: " +testOutcome.getLongTestResult());
            }
            
			if (testOutcome.getTestType().equals(TestOutcome.FINDBUGS_TEST)) {
				out.println("findbugs: " + testOutcome.getLongTestResult());
				if (testOutcome.getShortTestResult().length() > 0) {
					out.println("\t" + testOutcome.getShortTestResult());
				}
			}
		}
	}
}
