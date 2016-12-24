/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 28, 2005
 */
package edu.umd.cs.buildServer;

import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * An analysis step for examining a compiled submission and
 * reporting test outcomes on it.
 * 
 * @author David Hovemeyer
 */
public interface ISubmissionInspectionStep {
	/**
	 * Set the ProjectSubmission to inspect.
	 * 
	 * @param projectSubmission the ProjectSubmission to inspect
	 */
	public void setProjectSubmission(ProjectSubmission projectSubmission);
	
	/**
	 * Execute the analysis.
	 * 
	 * @throws BuilderException
	 */
	public void execute() throws BuilderException;
	
	/**
	 * Get the TestOutcomeCollection containing TestOutcomes
	 * reported by this inspection step.
	 *
	 * @return the TestOutcomeCollection
	 */
	public TestOutcomeCollection getTestOutcomeCollection();
}
