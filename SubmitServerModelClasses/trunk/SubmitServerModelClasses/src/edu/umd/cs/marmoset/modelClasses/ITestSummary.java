/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 20, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

/**
 * @author jspacco
 *
 */
public interface ITestSummary extends Comparable 
{
    
    /**
     * Computes the total number of points received.
     * <p><b>NOTE</b>Does not subtract points for test categories that were 
     * marked COULD_NOT_RUN and therefore returned -1.
     * @return total number of points received for this submission;
     * -1 if the submission did not compile.
     */
    public int getValuePassedOverall();

    /**
     * @return true if the TestSummary compiled, false otherwise
     */
    public boolean isCompileSuccessful();

    /**
     * Gets the total number of points received for public tests.  
     * @return total number of points received for public tests; 
     * return -1 if the public tests are marked COULD_NOT_RUN; 
     * returns 0 if there are no public tests.
     */
    public int getValuePublicTestsPassed();

    /**
     * Gets the total number of points received for release tests.  
     * @return total number of points received for release tests; 
     * return -1 if the release tests are marked COULD_NOT_RUN; 
     * returns 0 if there are no release tests.
     */
    public int getValueReleaseTestsPassed();

    /**
     * Gets the total number of points received for secret tests.  
     * @return total number of points received for secret tests; 
     * return -1 if the secret tests are marked COULD_NOT_RUN; 
     * returns 0 if there are no secret tests.
     */
    public int getValueSecretTestsPassed();

    /**
     * Gets the number of FindBugs warnings.  
     * This is a raw count of the number of warnings.
     * @return the number of FindBugs warnings.
     */
    public int getNumFindBugsWarnings();
}
