/*
 * Copyright (C) 2005 University of Maryland
 * All Rights Reserved
 * Created on Feb 10, 2005
 */
package edu.umd.cs.buildServer;

/**
 * Wait a fixed amount of time for a process to exit.
 * 
 * @author David Hovemeyer
 */
public class ProcessExitMonitor extends Thread {
	private Process process;
	private boolean exited;
	private volatile int exitCode;

	/**
	 * Constructor.
	 * 
	 * @param process the process to monitor for exit
	 */
	public ProcessExitMonitor(Process process) {
		this.setDaemon(true);
		this.process = process;
		this.exited = false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			this.exitCode = process.waitFor();
			synchronized (this) {
				this.exited = true;
				notifyAll();
			}
		} catch (InterruptedException e) {
			// Interrupted!
		}
	}

	/**
	 * Wait given number of milliseconds for the process to exit.
	 * 
	 * @param millis number of milliseconds to wait for the process to exit
	 * @return true if the process exited in the given amount of time,
	 *         false otherwise
	 * @throws InterruptedException
	 */
	public synchronized boolean waitForProcessToExit(long millis) throws InterruptedException {
		if (!exited) {
			wait(millis);
		}
		return exited;
	}

	/**
	 * @return Returns the exitCode.
	 */
	public int getExitCode() {
		return exitCode;
	}
}
