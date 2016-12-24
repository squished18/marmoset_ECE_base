/*
 * Created on Sep 25, 2004
 */
package edu.umd.cs.buildServer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author David Hovemeyer
 */
public class DevNullOutputStream extends OutputStream {

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException {
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[])
	 */
	public void write(byte[] b) throws IOException {
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
	}

}
