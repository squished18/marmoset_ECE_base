/*
 * Copyright (C) 2004-2005, University of Maryland
 * All Rights Reserved
 * Created on May 2, 2005
 */
package edu.umd.cs.buildServer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to extract arguments from a string.  Arguments
 * can be separated using whitespace, or may be quoted using double
 * quote characters.
 * 
 * @author Bill Pugh
 * @author David Hovemeyer
 */
class ArgumentParser {
	static final Pattern ARG_REGEX_PATTERN =
		Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"|\\S+");
	
	// Fields
	private Matcher m;
	private String next;

	/**
	 * Constructor.
	 *
	 * @param value the String to be parsed
	 */
	public ArgumentParser(String value) {
		m = ARG_REGEX_PATTERN.matcher(value);
	}

	/**
	 * Return whether or not the string contains another argument.
	 */
	public boolean hasNext() {
		fetchNext();
		return next != null;
	}

	/**
	 * Get the next argument.
	 * Must call hasNext() first to ensure that there
	 * actually is another argument.
	 */
	public String next() {
		fetchNext();
		if (next == null)
			throw new IllegalStateException();
		String result = next;
		next = null;
		return result;
	}

	private void fetchNext() {
		if (next == null) {
			if (m.find())
				next = m.group(0);
		}
	}
}