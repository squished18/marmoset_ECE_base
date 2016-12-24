/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Sep 22, 2004
 */
package edu.umd.cs.buildServer;

/**
 * @author David Hovemeyer
 */
public class TextOutputSink {
	private StringBuffer buf;
	
	public TextOutputSink() {
		buf = new StringBuffer();
	}
	
	public synchronized void appendLine(String line) {
		buf.append(line);
		buf.append('\n');
	}
	
	public synchronized void append(String msg) {
	    buf.append(msg);
	}
	
	public synchronized String getOutput() {
		return buf.toString();
	}
}
