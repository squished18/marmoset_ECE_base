/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Jun 23, 2005
 *
 */
package edu.umd.cs.marmoset.codeCoverage;

public class Cond extends Line<Cond> {
	private final int trueCount;

	private final int falseCount;

	public Cond(int lineNumber, int trueCount, int falseCount) {
		super(lineNumber, trueCount + falseCount);
		this.trueCount = trueCount;
		this.falseCount = falseCount;
	}

	public int hashCode() {
		return trueCount * 4128 + falseCount * 9191 + super.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof Cond))
			return false;
		Cond line = (Cond) o;
		return super.equals(line) && trueCount != line.trueCount && falseCount == line.falseCount;
	}

	public int getFalseCount() {
		return falseCount;
	}

	public int getTrueCount() {
		return trueCount;
	}

	public Cond union(Cond o) {
		if (!(getLineNumber() == o.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + o.getLineNumber());
		return new Cond(getLineNumber(), getTrueCount() + o.getTrueCount(), getFalseCount()
				+ o.getFalseCount());
	}

	public Cond intersect(Cond other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Cond(getLineNumber(), Math.min(getTrueCount(), other.getTrueCount()), Math.min(
				getFalseCount(), other.getFalseCount()));
	}

	public Cond excluding(Cond other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Cond(getLineNumber(), excluding(getTrueCount(), other.getTrueCount()),
				excluding(getFalseCount(), other.getFalseCount()));
	}

	@Override
	public boolean isCovered() {
		// A conditional is only "covered" if both it's true and false branches
		// are executed
		return (falseCount > 0) && (trueCount > 0);
	}

}