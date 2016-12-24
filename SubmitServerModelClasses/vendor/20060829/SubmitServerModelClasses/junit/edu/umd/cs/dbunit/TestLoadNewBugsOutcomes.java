/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on May 5, 2005
 *
 */
package edu.umd.cs.dbunit;

import java.sql.Connection;
import java.util.Random;

import junit.framework.TestCase;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;
import edu.umd.cs.marmoset.utilities.DatabaseUtilities;

/**
 * @author jspacco
 *
 */
public class TestLoadNewBugsOutcomes extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestLoadNewBugsOutcomes.class);
    }
    
    public void testLoadNewFindBugsOutcomes()
    throws Exception
    {
        Connection conn=null;
        try {
            conn = DatabaseUtilities.getConnection();
            
            String submissionPK = "56107";
            
            TestOutcomeCollection newBugCollection = new TestOutcomeCollection();
            
            Random r = new Random();
            int totalNewFindBugsOutcomes = r.nextInt(5) + 1;
            
            for (int testNumber=0; testNumber < totalNewFindBugsOutcomes; testNumber++)
            {
                TestOutcome outcome = new TestOutcome();
                
                outcome.setTestType(TestOutcome.FINDBUGS_TEST);
                outcome.setTestNumber(testNumber);
                outcome.setOutcome(TestOutcome.WARNING);
                outcome.setPointValue(0);
                outcome.setTestName("FAKE_WARNING_TESTING_PURPOSES");
                outcome.setShortTestResult("Foo.java:[line " +testNumber+ "]");
                outcome.setLongTestResult("This is actually a fake warning for testing purposes");
                outcome.setExceptionClassName(null);
                outcome.setDetails(null);
                
                newBugCollection.add(outcome);
            }
            
            System.out.println("loading " +totalNewFindBugsOutcomes+ " new findbugs outcomes");
            
            Submission oldSubmission = Submission.lookupBySubmissionPK(submissionPK, conn);
            System.err.println("oldSubmission.getCurrentTestRunPK() = " +oldSubmission.getCurrentTestRunPK());
            
            Submission.loadNewFindBugsOutcomes(newBugCollection, submissionPK, conn);
            
            Submission newSubmission = Submission.lookupBySubmissionPK(submissionPK, conn);
            System.err.println("newSubmission.getCurrentTestRunPK() = " +newSubmission.getCurrentTestRunPK());
            
            assertNotSame(oldSubmission.getCurrentTestRunPK(), newSubmission.getCurrentTestRunPK());
            
            assertEquals(newBugCollection.size(), newSubmission.getNumFindBugsWarnings());
            
            TestRun newTestRun = TestRun.lookupByTestRunPK(newSubmission.getCurrentTestRunPK(), conn);
            assertEquals(newBugCollection.size(), newTestRun.getNumFindBugsWarnings());
            
        } finally {
            DatabaseUtilities.releaseConnection(conn);
        }
    }

}
