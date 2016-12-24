/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on April 8, 2005
 */
package edu.umd.cs.submitServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.codeCoverage.FileWithCoverage;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.parser.JavaTokenScanner;

/**
 * Render a sourcefile from a Submission as HTML.
 * 
 * @author David Hovemeyer
 */
public class DisplaySubmissionSourceCode {
	
	private static final Set<String> sourceExtensionSet = new HashSet<String>();
	static {
		sourceExtensionSet.add(".java");
		sourceExtensionSet.add(".c");
		sourceExtensionSet.add(".h");
		sourceExtensionSet.add(".ocaml");
		sourceExtensionSet.add(".ml");
		sourceExtensionSet.add(".mli");
		sourceExtensionSet.add(".ruby");
		sourceExtensionSet.add(".rb");
		sourceExtensionSet.add(".hex");
		sourceExtensionSet.add(".asm");
		sourceExtensionSet.add(".dfa");
		sourceExtensionSet.add(".wl");
	}
	
	/**
	 * Fetch the Collection of source files for a given Submission.
	 * 
	 * @param conn       the database connection
	 * @param submission the Submission
	 * @return List containing source file names (Strings) for the Submission
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<String> getSourceFilesForSubmission(
			Connection conn, Submission submission) throws IOException, SQLException {
		TreeSet<String> result = new TreeSet<String>();

		byte[] archive = submission.downloadArchive(conn);
		
		ZipInputStream zipInput = null;
		try {
			zipInput = new ZipInputStream(new ByteArrayInputStream(archive));
			ZipEntry zipEntry;
			
			while ((zipEntry = zipInput.getNextEntry()) != null) {
				String entryName = zipEntry.getName(); 
				int lastDot = entryName.lastIndexOf('.');
				if (lastDot >= 0) {
					String ext = entryName.substring(lastDot);
					if (sourceExtensionSet.contains(ext))
						result.add(entryName);
				}
			}
			
			List<String> list = new LinkedList<String>();
			list.addAll(result);
			
			return list;
		} finally {
			if (zipInput != null) zipInput.close();
		}
	}
	
	/**
	 * Render the given sourcefile from the given Submission as HTML.
	 * 
	 * @param conn               the database connection
	 * @param submission         the Submission
	 * @param sourceFile         the filename of the sourcefile within the Submission
	 * @param highlightStartLine first line to highlight
	 * @param numHighlightLines  number of lines to highlight
	 * @param numContextLines    number of lines of context before and after highlight to display
	 * @param codeCoverageResults the code coverage information for all of the files.
	 * 		If null, then we assume no code coverage information is available and none will
	 * 		be displayed.
	 * @return formatted HTML table displaying the source code
	 * @throws IOException
	 * @throws SQLException
	 */
	public static String displaySourceCode(
			Connection conn,
			Submission submission,
			String sourceFile,
			Integer highlightStartLine,
			Integer numHighlightLines,
			Integer numContextLines,
			CodeCoverageResults codeCoverageResults) throws IOException, SQLException {
		sourceFile = sourceFile.replace('\\', '/');

		byte[] archive = submission.downloadArchive(conn);
		//Project project = Project.getByProjectPK(submission.getProjectPK(), conn);
		
		ZipInputStream zipInput = null;
		try {
			zipInput = new ZipInputStream(new ByteArrayInputStream(archive));
			
			ZipEntry zipEntry;
			
			while ((zipEntry = zipInput.getNextEntry()) != null) {
				String entryName = zipEntry.getName().replace('\\', '/');
				
				if (sourceFile.equals(entryName) ||
						sourceFile.equals(trimSourceFileName(entryName)))
				{

					return displaySourceCode(
							zipInput,
							entryName,
							highlightStartLine,
							numHighlightLines,
							numContextLines,
							codeCoverageResults);

				}
			}
			
			return null;
		} finally {
			if (zipInput != null) zipInput.close();
		}
	}

	private static String trimSourceFileName(String entryName) {
		int lastSlash = getLastSlash(entryName);
		if (lastSlash >= 0) {
			entryName = entryName.substring(lastSlash + 1);
		}
		return entryName;
	}

	private static int getLastSlash(String entryName) {
		int forwardSlashPos = entryName.lastIndexOf('/');
		int backwardSlashPos = entryName.lastIndexOf('\\');
		return Math.max(forwardSlashPos, backwardSlashPos);
	}

	private static String displaySourceCode(
			InputStream zipInput,
			String sourceFile,
			Integer highlightStartLine,
			Integer numHighlightLines,
			Integer numContextLines,
			CodeCoverageResults codeCoverageResults) throws IOException {
		// FIXME: are there character set issues here?
		
		ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outputBytes);
		
		// TODO Instantiate subclasses of DisplaySourceCodeAsHTML 
        // based on the source code extension
        DisplaySourceCodeAsHTML src2html = new DisplaySourceCodeAsHTML();
		src2html.setInputStream(zipInput);
		src2html.setOutputStream(out);
		src2html.setDefaultTokenStyles();
		if (sourceFile.endsWith(".java")) {
			src2html.setTokenScanner(new JavaTokenScanner());
		}

		// Highlight and context support
		if (highlightStartLine != null && numHighlightLines != null) {
			if (highlightStartLine.intValue() > 0 && numHighlightLines.intValue() >= 0) {
				src2html.addHighlightRange(
						highlightStartLine.intValue(),
						numHighlightLines.intValue(),
						"codehighlight");

				// Number of context lines around the code highlight to display
				if (numContextLines != null && numContextLines.intValue() > 0) {
					int startLine = Math.max(1, highlightStartLine.intValue() - numContextLines.intValue());
					int endLine =
						highlightStartLine.intValue() +
						numHighlightLines.intValue() +
						numContextLines.intValue();
					src2html.setDisplayRange(startLine, endLine);
				}
			}
		}
		
		if (codeCoverageResults != null) {
		    // XXX I'm using the fact that the File class will properly
		    // strip off leading paths (without me using lastIndexOf() or somesuch).
		    // Sort of a hack because the sourceFile is not a path to a real file and
		    // the file created should never be used as a file.
		    File f = new File(sourceFile);
		    FileWithCoverage fileWithCoverage = codeCoverageResults.getFileWithCoverage(f.getName());
		    src2html.setFileWithCoverage(fileWithCoverage);
		    try {
		    src2html.setExcludingCodeCoverage(codeCoverageResults.isExcludingResult());
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		}
		src2html.convert();
		
		return outputBytes.toString();
	}
}
