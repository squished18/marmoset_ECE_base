/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on April 8, 2005
 */
package edu.umd.cs.marmoset.parser;

import java.io.IOException;
import java.io.Reader;

/**
 * Interface for token scanners for source code display and
 * highlighting.
 * 
 * @author David Hovemeyer
 */
public interface TokenScanner {
	/**
	 * Read a token from given Reader.
	 * 
	 * @param reader the Reader
	 * @return the Token, or null if the end of the input stream was reached
	 */
	public Token scan(Reader reader) throws IOException;
}
