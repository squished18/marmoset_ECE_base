/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Jun 23, 2005
 *
 */
package edu.umd.cs.marmoset.codeCoverage;

public class Stmt extends Line<Stmt> {
	public Stmt(int lineNumber, int count) {
		super(lineNumber, count);
	}

	public Stmt union(Stmt other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Stmt(getLineNumber(), getCount() + other.getCount());
	}

	@Override
	public Stmt intersect(Stmt other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Stmt(getLineNumber(), Math.min(getCount(), other.getCount()));
	}

	@Override
	public Stmt excluding(Stmt other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Stmt(getLineNumber(), excluding(getCount(), other.getCount()));
	}
}