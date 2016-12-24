package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;

import edu.umd.cs.marmoset.utilities.DatabaseUtilities;

import junit.framework.TestCase;

/**
 * TestOutcomeTest
 * @author jspacco
 */
public class TestOutcomeTest extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestOutcomeTest.class);
    }

    private Connection conn;
    protected void setUp() throws Exception
    {
        // TODO Find some way so that the location of the DB is NOT hardcoded
        super.setUp();
        conn=DatabaseUtilities.getConnection("jdbc:mysql://submit.cs.umd.edu/submitserver");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        DatabaseUtilities.releaseConnection(conn);
    }
    
    private TestOutcome lookupByTestRunPK(String testRunPK, String testType, int testNumber)
    throws Exception
    {
        TestOutcomeCollection collection=TestOutcomeCollection.lookupByTestRunPK(testRunPK, conn);
        return collection.getOutcomeByTestTypeAndTestNumber(testType, String.valueOf(testNumber));
    }
    
    public void testCoversLineOrPreviousThreeLines()
    throws Exception
    {
        String testRunPK="99988";
        TestOutcomeCollection collection=TestOutcomeCollection.lookupByTestRunPK(testRunPK,conn);
        TestOutcome outcome=collection.getOutcomeByTestTypeAndTestNumber(TestOutcome.RELEASE_TEST, "10");
        System.out.println(outcome.getExceptionSourceFromLongTestResult());
        System.out.println(collection.isExceptionSourceApproximatelyCovered(outcome, 3));
        
        assertEquals(true, collection.isExceptionSourceApproximatelyCovered(outcome, 3));
        
    }

}
