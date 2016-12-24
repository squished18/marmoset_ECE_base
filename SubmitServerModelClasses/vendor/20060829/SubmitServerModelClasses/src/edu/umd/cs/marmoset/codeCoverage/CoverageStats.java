/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Jun 29, 2005
 *
 */
package edu.umd.cs.marmoset.codeCoverage;

/**
 * High-level code coverage metrics and results.
 * 
 * @author jspacco
 */
public class CoverageStats {
	private int coveredStmt = 0;

	private int totalStmt = 0;

	private int coveredMethods = 0;

	private int totalMethods = 0;

	private int coveredCond = 0;

	private int totalCond = 0;

	/**
	 * Merges the current coverageStats object with another coverageStats
	 * object. Updates the current coverageStats object (i.e. this method has
	 * destructive side-effects) Does not change the other coverageStats object.
	 * 
	 * @param other
	 *            The other coverageStats object to merge with.
	 */
	public void merge(CoverageStats other) {
		this.coveredCond += other.coveredCond;
		this.totalCond += other.totalCond;

		this.coveredMethods += other.coveredMethods;
		this.totalMethods += other.totalMethods;

		this.coveredStmt += other.coveredStmt;
		this.totalStmt += other.totalStmt;
	}

	public void addNewLine(Line line) {
		if (line instanceof Method) {
			totalMethods++;
			if (line.isCovered())
				coveredMethods++;
		} else if (line instanceof Cond) {
			totalCond++;
			// XXX When computing percent coverage, a conditional must fire both
			// its
			// true and false branches, whereas for bug finding we just care
			// whether
			// the line was executed or not.
			// I'm using the "percent coverage" meaning here looking for whether
			// the line executed both its branches.
			if (line.isCovered())
				coveredCond++;
		} else if (line instanceof Stmt) {
			totalStmt++;
			if (line.isCovered())
				coveredStmt++;
		} else {
			throw new IllegalStateException("Unexpected subclass of line: " + line.getClass());
		}
	}

	private static int convertToPercent(int stuff, int total) {
		if (total == 0) return 0;
		return (100*stuff)/total;
	}

	public int getCoveredCondPercentage() {
		return convertToPercent(getCoveredCond(), getTotalCond());
	}

	public int getCoveredMethodsPercentage() {
		return convertToPercent(getCoveredMethods(), getTotalMethods());
	}

	public int getCoveredStmtPercentage() {
		return convertToPercent(getCoveredStmt(), getTotalStmt());
	}

	public int getCoveredElementsPercentage() {
		return convertToPercent(getCoveredElements(), getTotalElements());
	}

	public int getCoveredCond() {
		return coveredCond;
	}

	public int getCoveredMethods() {
		return coveredMethods;
	}

	public int getCoveredStmt() {
		return coveredStmt;
	}

	public int getTotalCond() {
		return totalCond;
	}

	public int getTotalMethods() {
		return totalMethods;
	}

	public int getTotalStmt() {
		return totalStmt;
	}

	public int getCoveredElements() {
		return coveredMethods + coveredStmt;
	}

	public int getTotalElements() {
		return totalMethods + totalStmt;
	}

	/**
	 * Returns the necessary HTML to insert these coverage stats into a table.
	 * 
	 * @return The HTML to put these coverage stats into a row of an HTML table.
	 */
	public String getHTMLTableRow() {
		if (getCoveredMethods() == 0) 
			return "<td colspan=3>none</td>\n";
		
		return "<td>" + getCoveredStmt() + "/" + getTotalStmt() + "</td>\n" + 
            "<td>" + getCoveredCond() + "/" + getTotalCond() + "</td>\n" +
            "<td>" + getCoveredMethods() + "/" + getTotalMethods() + "</td>\n";
	}

	public String toString() {
		if (getTotalMethods() == 0) 
			return "none";
		return getCoveredStmt() + "/" + getTotalStmt() + DELIMITER + getCoveredCond() + "/"
				+ getTotalCond() + DELIMITER + getCoveredMethods() + "/" + getTotalMethods();
	}

	public static final String DELIMITER = "\t";

	public String getCSVValues() {
		return getCoveredStmt() + DELIMITER + getCoveredCond() + DELIMITER + getCoveredMethods();
	}

	public String getCSVTotals() {
		return getTotalStmt() + DELIMITER + getTotalCond() + DELIMITER + getTotalMethods();
	}
}