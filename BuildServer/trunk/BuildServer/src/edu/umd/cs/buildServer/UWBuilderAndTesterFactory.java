/*
 * Copyright (C) 2005 University of Maryland
 * All Rights Reserved
 * Created on Jan 21, 2005
 */
package edu.umd.cs.buildServer;

import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.utilities.ZipExtractorException;

/**
 * Factory for producing Builder and Tester objects
 * for C, OCaml and Ruby ProjectSubmissions.
 * <p>
 * <b>NOTE:</b> "CBuilderAndTester" is a legacy name.  
 * We use the same infrastructure for building and testing C, OCaml and Ruby code 
 * because the process is exactly the same.  For more details see {@see CBuilder}.
 * 
 * @author David Hovemeyer
 * @author jspacco
 */
public class UWBuilderAndTesterFactory extends CBuilderAndTesterFactory {
	
	/**
	 * Constructor.
	 * 
	 * @param config         the build server Configuration
	 * @param testProperties Properties loaded from test jarfile's test.properties
	 * @throws MissingConfigurationPropertyException 
	 */
	public UWBuilderAndTesterFactory(Configuration config, TestProperties testProperties) throws MissingConfigurationPropertyException {
            super(config, testProperties);
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.BuilderAndTesterFactory#createTester(boolean, edu.umd.cs.buildServer.ProjectSubmission)
	 */
	public Tester createTester(boolean haveSecurityPolicyFile,
			ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException {
		
		UWTester tester = new UWTester(
				testProperties,
				haveSecurityPolicyFile,
				projectSubmission,
				directoryFinder);
		
		return tester;
	}

}
