/*
 * Copyright (C) 2004-2005, University of Maryland
 * All Rights Reserved
 * Created on April 18, 2005
 */
package edu.umd.cs.buildServer;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.dom4j.io.SAXWriter;
import org.xml.sax.SAXException;

import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SAXBugCollectionHandler;
import edu.umd.cs.findbugs.SortedBugCollection;

/**
 * Output monitor thread which builds a FindBugs BugCollection
 * from an XML document.
 * 
 * @author David Hovemeyer
 */
class FindBugsDocumentBuilder extends XMLDocumentBuilder {
	private SortedBugCollection bugCollection;
	private Project project;

	/**
	 * Constructor.
	 * 
	 * @param in  InputStream to read XML document from
	 * @param log Log where diagnostic messages are sent
	 */
	public FindBugsDocumentBuilder(InputStream in, Logger log) {
		super(in, log);
	}
	
	protected void documentFinished() {
		try {
			// Generate a BugCollection from the dom4j tree
			SortedBugCollection bugCollection = new SortedBugCollection();
			Project project = new Project();
			SAXBugCollectionHandler handler =
				new SAXBugCollectionHandler(bugCollection, project);
			SAXWriter saxWriter = new SAXWriter(handler);
			saxWriter.write(getDocument());
			
			this.bugCollection = bugCollection;
			this.project = project;
		} catch (SAXException e) {
			getLog().info("Couldn't generate BugCollection from findbugs XML output", e);
		}
	}
	
	public SortedBugCollection getBugCollection() {
		return bugCollection;
	}
	
	public Project getProject() {
		return project;
	}
}
