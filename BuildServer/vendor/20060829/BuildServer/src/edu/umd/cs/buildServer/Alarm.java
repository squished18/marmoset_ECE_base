/*
 * Copyright (C) 2004-2005 University of Maryland
 * All Rights Reserved
 * Created on Sep 1, 2004
 */
package edu.umd.cs.buildServer;

/**
 * Alarm object to interrupt a recipient thread after
 * a timeout expires.
 * 
 * @author David Hovemeyer
 */
public class Alarm {
	static int nextIdent;
	
	private int numSeconds;
	private Thread recipient;
	private Thread alarmThread;
	private boolean enabled;
	private boolean fired;
	private int myIdent;

	/**
	 * Constructor.
	 * 
	 * @param numSeconds number of seconds until the alarm goes off
	 * @param recipient the thread that should be signaled
	 *   after the alarm goes off
	 */
	public Alarm(int numSeconds, Thread recipient) {
		this.numSeconds = numSeconds;
		this.recipient = recipient;
		this.enabled = false;
		this.fired = false;

		synchronized (this.getClass()) {
			myIdent = nextIdent++;
		}
	}
	
	/**
	 * Start the alarm.
	 */
	public void start() {
		alarmThread = new Thread() {
			public void run() {
				try {
					synchronized (Alarm.this) {
						enabled = true;
					}
					
					Thread.sleep(numSeconds  * 1000);
					boolean fireAlarm = false;
					synchronized (Alarm.this) {
						if (enabled) {
							fired = true;
							fireAlarm = true;
						}
					}
					// NOTE: we are signaling the recipient outside
					// the synchronization block so it is guaranteed
					// to see the write fired = true. (?)
					if (fireAlarm)
						signalRecipient(recipient);
				} catch (InterruptedException ignore) {
					// This should never happen
				}
			}
		};
		
		// Don't keep the process alive just because an alarm hasn't fired
		alarmThread.setDaemon(true);
		
		// Start the alarm
		alarmThread.start();
	}

	/**
	 * Return whether or not the alarm has fired.
	 * @return true if the alarm has fired, false otherwise
	 */
	public synchronized boolean fired() {
		return fired;
	}
	
	/**
	 * Turn off the alarm.
	 * Note that the alarm can go off at anytime,
	 * so it is always possible for the recipient to
	 * be signaled (and for Alarm.fired() to return true)
	 * after this method is called.
	 * 
	 * <p> This method may only be called by the recipient thread.
	 */
	public synchronized void turnOff() {
		if (Thread.currentThread() != recipient)
			throw new IllegalStateException();
		enabled = false;

		// Shut the alarm thread down
		alarmThread.interrupt();
		int attempts = 4;
		boolean alarmDied = false;
	loop:
		while (attempts-- > 0) {
			//System.err.println("Waiting for alarm thread to die...");
			try {
				alarmThread.join(1000);
				alarmDied = true;
				break loop;
			} catch (InterruptedException ignore) {
				// Shouldn't happen (hopefully)
			}
		}
		if (!alarmDied) {
			System.err.println("ERROR: Alarm thread did not die!");
		}

		if (fired) {
			// The alarm notification must have happened after
			// recipient woke up, but before the alarm could
			// be turned off. 
			clearSignaledStatus();
		}
	}
	
	public String toString() {
		return "ALARM " + myIdent;
	}

	/**
	 * Signal to the recipient thread that the alarm has expired.
	 * 
	 * @param recipient the recipient thread
	 */
	private void signalRecipient(Thread recipient) {
		//System.err.println("FIRING " + toString() + "!");
		recipient.interrupt();
	}

	/**
	 * Clear whatever status is associated with an alarm notification.
	 */
	private void clearSignaledStatus() {
		Thread.interrupted(); // this will clear the interruped status
	}
}
