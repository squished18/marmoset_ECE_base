/*
 * Copyright (C) 2004-2005 University of Maryland
 * All Rights Reserved
 * Created on Oct 6, 2004
 */
package edu.umd.cs.buildServer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.utilities.ZipExtractor;
import edu.umd.cs.marmoset.utilities.ZipExtractorException;

/**
 * Extract a project submission into the build directory.
 * 
 * @author David Hovemeyer
 */
public abstract class SubmissionExtractor extends ZipExtractor {

	// Fields
	private String projectRoot;
	private List<String> sourceFileList;
	private Logger log;
	private boolean prunedSourceFileList;

	/**
	 * Constructor.
	 * 
	 * @param zipFile        the submission zipfile
	 * @param directory      directory to extract the submission into
	 * @param buildServerLog BuildServer's Log
	 * @throws BuilderException
	 */
	public SubmissionExtractor(File zipFile,
			File directory,
			Logger buildServerLog)
	throws ZipExtractorException
    {
		super(zipFile, directory);
		this.projectRoot = "";
		this.log = buildServerLog;
		this.sourceFileList = new LinkedList<String>();
		this.prunedSourceFileList = false;
	}
	
	/**
	 * Set the project root directory inside the submission zipfile.
	 * Only files inside this directory will be extracted from the zipfile.
	 * 
	 * @param projectRoot the project root directory
	 */
	public void setProjectRoot(String projectRoot) {
		this.projectRoot = projectRoot;
	}
	
	/**
	 * Get the list of source files that should be compiled.
	 * 
	 * @return List of source files (Strings)
	 */
	public List<String> getSourceFileList() {
		if (!prunedSourceFileList) {
			pruneSourceFileList(sourceFileList);
			this.prunedSourceFileList = true;
		}
		
		return sourceFileList;
	}
	
	/**
	 * @return Returns the log.
	 */
	protected Logger getLog() {
		return log;
	}

	protected boolean shouldExtract(String entryName) {
		// FIXME: we really should report an error if
		// an entry doesn't begin with the project root
		return entryName.startsWith(projectRoot);
	}

	protected String transformFileName(String entryName) {
		return entryName.substring(projectRoot.length());
	}

	protected void successfulFileExtraction(String entryName, String fileName) {
		if (isSourceFile(fileName)) {
			this.sourceFileList.add(fileName);
		}
	}

	/**
	 * Return whether or not the file whose name is given
	 * is a source file.
	 * 
	 * @param fileName the file name
	 * @return true if the file is a source file, false if not
	 */
	protected abstract boolean isSourceFile(String fileName);
	
	/**
	 * Prune the source file list by removing those that should not
	 * be compiled.  A no-op implementation of this method
	 * is acceptable.
	 * 
	 * @param sourceFileList List of source file names (Strings)
	 */
	protected abstract void pruneSourceFileList(List<String> sourceFileList);
}