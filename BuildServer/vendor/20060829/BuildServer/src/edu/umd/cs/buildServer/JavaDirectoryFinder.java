/*
 * Copyright (C) 2005 University of Maryland
 * All Rights Reserved
 * Created on Jan 21, 2005
 */
package edu.umd.cs.buildServer;

import java.io.File;

/**
 * DirectoryFinder for building and testing Java submissions.
 * 
 * @author David Hovemeyer
 */
public class JavaDirectoryFinder extends DirectoryFinder {
	private File buildDirectory;
	private File testFilesDirectory;

	/**
	 * Constructor.
	 * 
	 * @param config the buildserver Configuration
	 * @throws MissingConfigurationPropertyException
	 */
	public JavaDirectoryFinder(Configuration config) throws MissingConfigurationPropertyException {
		super(config);
	    this.buildDirectory = new File(config.getRequiredProperty(BUILD_DIRECTORY));
		this.testFilesDirectory = new File(config.getRequiredProperty(TEST_FILES_DIRECTORY));
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
		return testFilesDirectory;
	}
}
