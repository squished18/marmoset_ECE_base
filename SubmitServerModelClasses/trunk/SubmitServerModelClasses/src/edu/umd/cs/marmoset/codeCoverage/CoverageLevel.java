package edu.umd.cs.marmoset.codeCoverage;

public enum CoverageLevel {
	METHOD, STATEMENT, BRANCH, NONE;
	public CoverageLevel max(CoverageLevel other) {
		if (this.compareTo(other) < 0)
			return other;
		return this;
	}

    public static CoverageLevel fromString(String s) {
        if ("method".equals(s))
            return METHOD;
        if ("statement".equals(s))
            return STATEMENT;
        if ("branch".equals(s))
            return BRANCH;
        return NONE;
    }

	public CoverageLevel min(CoverageLevel other) {
		if (this.compareTo(other) > 0)
			return other;
		return this;
	}

}
