/*
 * Copyright (C) 2005 University of Maryland
 * All Rights Reserved
 * Created on Jan 21, 2005
 */
package edu.umd.cs.buildServer;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.utilities.ZipExtractorException;

/**
 * SubmissionExtractor for C, OCaml and Ruby submissions.
 * <p>
 * <b>NOTE:</b> "CSubmissionExtractor" is a legacy name.  
 * We use the same infrastructure for for building and testing C, OCaml and Ruby code 
 * because the process is exactly the same.  For more details see {@see CBuilder}.
 * 
 * @author David Hovemeyer
 */
public class CSubmissionExtractor extends SubmissionExtractor {

	/**
	 * Constructor.
	 * 
	 * @param zipFile        the submission zipfile
	 * @param directory      directory to extract submission into
	 * @param buildServerLog the buildserver's Log
	 * @throws BuilderException
	 */
	public CSubmissionExtractor(File zipFile, File directory, Logger buildServerLog)
    throws ZipExtractorException
    {
		super(zipFile, directory, buildServerLog);
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.SubmissionExtractor#isSourceFile(java.lang.String)
	 */
	protected boolean isSourceFile(String fileName) {
		// Note: it really isn't necessary to keep track of source filenames,
		// because a Makefile is actually used to build the submission.
		return fileName.endsWith(".c");
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.SubmissionExtractor#pruneSourceFileList(java.util.List)
	 */
	protected void pruneSourceFileList(List<String> sourceFileList) {
		// We don't do any source file pruning
	}

}
