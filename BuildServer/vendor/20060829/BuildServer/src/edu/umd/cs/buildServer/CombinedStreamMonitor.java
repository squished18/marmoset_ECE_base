/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Sep 1, 2004
 */
package edu.umd.cs.buildServer;

import java.io.InputStream;

/**
 * Monitor two input streams asynchronously. 
 * @author David Hovemeyer
 */
public class CombinedStreamMonitor {
	static final int MAX_NUM_BYTES_INPUT = 24 * 1024;
	
	// Fields
	private TextOutputSink outputSink;
	private MonitorThread stdoutMonitor, stderrMonitor;
	
	/**
	 * Constructor.
	 * @param in an input stream (usually the stdout from a Process)
	 * @param err another input stream (usually the stderr from a Process)
	 */
	public CombinedStreamMonitor(InputStream in, InputStream err) {
		this.outputSink = new TextOutputSink();
		this.stdoutMonitor = new MonitorThread(in, outputSink);
		this.stderrMonitor = new MonitorThread(err, outputSink);
	}

	public void setDrainLimit(int maxBytes) {
		// FIXME: this should really be an overall limit, not just per-stream
		stdoutMonitor.setDrainLimit(maxBytes);
		stderrMonitor.setDrainLimit(maxBytes);
	}
	
	/**
	 * Start asynchronously reading data from the streams.
	 */
	public void start() {
		stdoutMonitor.start();
		stderrMonitor.start();
	}

	/**
	 * Wait for all data to be read.
	 * @throws InterruptedException if either reading threads is interrupted
	 */
	public void join() throws InterruptedException {
		stdoutMonitor.join();
		stderrMonitor.join();
	}
	
	/**
	 * Get the combined output from both streams.
	 * @return a String containing the combined output
	 */
	public String getCombinedOutput() {
		return outputSink.getOutput();
	}
}
