/*
 * Copyright (C) 2004-2005, University of Maryland
 * All Rights Reserved
 * Created on March 25, 2005
 */
package edu.umd.cs.buildServer;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * Output monitor thread which parses an XML document and
 * creates a dom4j Document from it.
 * 
 * @author David Hovemeyer
 */
public class XMLDocumentBuilder extends Thread {
	private InputStream in;
	private Document document;
	private Logger buildServerLog;

	/**
	 * Constructor.
	 * 
	 * @param in             the InputStream to read the XML from
	 * @param buildServerLog Log where diagnostic messages should be sent
	 */
	public XMLDocumentBuilder(InputStream in, Logger buildServerLog) {
		this.in = in;
		this.buildServerLog = buildServerLog;
	}

	/**
	 * Get the finished XML Document.
	 * 
	 * @return the Document, or null if the document could not be read
	 */
	public Document getDocument() {
		return document;
	}
	
	/**
	 * Get the Log.
	 * 
	 * @return the Log
	 */
	public Logger getLog() {
		return buildServerLog;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		SAXReader reader = new SAXReader();
		try {
			// Read the FindBugs output as a dom4j Document
			this.document = reader.read(in);
			buildServerLog.debug("Read xml document!");
			
			documentFinished();
		} catch (DocumentException e) {
			// Ignore.
			buildServerLog.info("Couldn't parse XML results", e);
		}
	}
	
	/**
	 * Hook which subclasses may override to do additional processing
	 * when the Document has been successfully read.
	 * By default, does nothing.
	 */
	protected void documentFinished() {
		// Nothing - subclasses may override to post-process the XML document
	}
}
