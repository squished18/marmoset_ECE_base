/*
 * Copyright (C) 2005 University of Maryland
 * All Rights Reserved
 * Created on Jan 21, 2005
 */
package edu.umd.cs.buildServer;

import java.io.File;

/**
 * Interface which can locate the build and testfiles directories.
 * Depending on what kind of submission we're building and testing,
 * these might be different directories, or they might be the
 * same directory.
 * 
 * @author David Hovemeyer
 */
public abstract class DirectoryFinder implements ConfigurationKeys {
    protected File buildServerRoot;
    
    protected DirectoryFinder(Configuration config) throws MissingConfigurationPropertyException {
        this.buildServerRoot = new File(config.getRequiredProperty(BUILDSERVER_ROOT));
    }
    
    /**
	 * Get the build directory.
	 * 
	 * @return the build directory
	 */
	public abstract File getBuildDirectory();
	
	/**
	 * Get the testfiles directory.
	 * 
	 * @return the testfiles directory
	 */
	public abstract File getTestFilesDirectory();
	
	/**
	 * Get the buildServer root directory.
	 * 
	 * @return the buildServer root directory
	 */
	public File getBuildServerRoot() {
	    return buildServerRoot;
	}
}
