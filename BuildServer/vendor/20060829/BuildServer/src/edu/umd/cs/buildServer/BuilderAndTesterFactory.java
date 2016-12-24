/*
 * Copyright (C) 2004-2005 University of Maryland
 * All Rights Reserved
 * Created on Jan 20, 2005
 */
package edu.umd.cs.buildServer;

import edu.umd.cs.marmoset.utilities.ZipExtractorException;

/**
 * Factory for creating Builder and Tester objects
 * for a ProjectSubmission.
 * 
 * @author David Hovemeyer
 */
public interface BuilderAndTesterFactory {
	public DirectoryFinder getDirectoryFinder();
	
	/**
	 * Create a Builder for a ProjectSubmission.
	 * 
	 * @param projectSubmission the ProjectSubmission to build
	 * @return a Builder which can build the ProjectSubmission
	 * @throws BuilderException
	 * @throws MissingConfigurationPropertyException
	 */
	public Builder createBuilder(ProjectSubmission projectSubmission)
			throws BuilderException, MissingConfigurationPropertyException, ZipExtractorException;
	
	/**
	 * Create a Tester for a ProjectSubmission.
	 * 
	 * @param haveSecurityPolicyFile true if there is a security.policy file
	 * @param projectSubmission      the ProjectSubmission to test
	 * @return a Tester which can test the ProjectSubmission
	 * @throws MissingConfigurationPropertyException
	 */
	public Tester createTester(boolean haveSecurityPolicyFile, ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException;
}
