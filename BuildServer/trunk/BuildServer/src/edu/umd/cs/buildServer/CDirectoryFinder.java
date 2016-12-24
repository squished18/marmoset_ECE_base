/*
 * Copyright (C) 2005 University of Maryland
 * All Rights Reserved
 * Created on Jan 21, 2005
 */
package edu.umd.cs.buildServer;

import java.io.File;

/**
 * DirectoryFinder for C, OCaml and Ruby projects.
 * <p>
 * <b>NOTE:</b> "CSubmissionExtractor" is a legacy name.  
 * We use the same infrastructure for for building and testing C, OCaml and Ruby code 
 * because the process is exactly the same.  For more details see {@see CBuilder}.
 * 
 * @author David Hovemeyer
 */
public class CDirectoryFinder extends DirectoryFinder implements ConfigurationKeys {
	
	private File buildDirectory;
	
	/**
	 * Constructor.
	 * 
	 * @param config the BuildServer Configuration
	 * @throws MissingConfigurationPropertyException
	 */
	public CDirectoryFinder(Configuration config) throws MissingConfigurationPropertyException {
		super(config);
	    this.buildDirectory = new File(config.getRequiredProperty(BUILD_DIRECTORY));
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.DirectoryFinder#getBuildDirectory()
	 */
	public File getBuildDirectory() {
		return buildDirectory;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.DirectoryFinder#getTestFilesDirectory()
	 */
	public File getTestFilesDirectory() {
		// For C submissions, the test files are extracted into the
		// build directory, not the testfiles directory.
		return buildDirectory;
	}

	
}
