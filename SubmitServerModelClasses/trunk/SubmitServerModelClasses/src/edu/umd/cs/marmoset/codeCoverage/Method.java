/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Jun 23, 2005
 *
 */
package edu.umd.cs.marmoset.codeCoverage;

public class Method extends Line<Method> {
	public Method(int lineNumber, int count) {
		super(lineNumber, count);
	}

	public Method union(Method other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Method(getLineNumber(), getCount() + other.getCount());
	}

	@Override
	public Method intersect(Method other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Method(getLineNumber(), Math.min(getCount(), other.getCount()));
	}

	@Override
	public Method excluding(Method other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Method(getLineNumber(), excluding(getCount(), other.getCount()));
	}
}