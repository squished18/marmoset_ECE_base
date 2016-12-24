/*
 * Copyright (C) 2004,2005 University of Maryland
 * All Rights Reserved
 * Created on May 1, 2005
 */
package edu.umd.cs.buildServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Thread to monitor the stdout or stderr of a process
 * and append its output to a TextOutputSink.
 * The amount of output read is limited to MAX_NUM_BYTES_OUTPUT.
 */
public class MonitorThread extends Thread {
	private LimitedInputStream in;
	private BufferedReader reader;
	private TextOutputSink outputSink;
	
	/**
	 * Constructor.
	 * @param in the InputStream to monitor
	 */
	public MonitorThread(InputStream in, TextOutputSink outputSink) {
		this.in = new LimitedInputStream(in, CombinedStreamMonitor.MAX_NUM_BYTES_INPUT);
		this.reader = new BufferedReader(new InputStreamReader(this.in));
		this.outputSink = outputSink;
	}
	
	public TextOutputSink getOutputSink() {
		return outputSink;
	}

	public void setDrainLimit(int maxBytes) {
		in.setDrainLimit(maxBytes);
	}
	
	public void run() {
		try {
			// Read lines of output from the stream
			String line;
			while ((line = reader.readLine()) != null) {
				outputSink.appendLine(line);
			}
		} catch (IOException e) {
			// Ignore
		} finally {
			// Attempt to drain all output from the stream,
			// and close it.
			try {
				in.drain();
			} catch (IOException e) {
				// Ignore
			}
			try {
				in.close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}
}