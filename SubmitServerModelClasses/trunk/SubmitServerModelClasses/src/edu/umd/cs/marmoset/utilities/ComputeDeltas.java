/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 28, 2005
 *
 */
package edu.umd.cs.marmoset.utilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Snapshot;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;

/**
 * @author jspacco
 *
 */
public class ComputeDeltas
{
    private static final boolean DEBUG=false;
    private static int couldNotRunSkipped=0;
//    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static int totalProcessed=0;
    
    private static boolean COMPUTE_COUNT_DELTAS=true;
    private static boolean DIFF_WITH_PREVIOUS=true;
    
    public static void main(String[] args)
    throws Exception
    {
        // These are true by default and can only but shut off my being explicitly set to false
        if ("false".equalsIgnoreCase(System.getProperty("COMPUTE_COUNT_DELTAS"))) {
            COMPUTE_COUNT_DELTAS=false;
        }
        if ("false".equalsIgnoreCase(System.getProperty("DIFF_WITH_PREVIOUS"))) {
            DIFF_WITH_PREVIOUS=false;
        }
        
        if (args.length < 5) {
            System.err.println("Usage: java ComputeDeltas \n " +
                    "<participants file> \n" +
                    "<coureName> \n" +
                    "<semester> \n" +
                    "<database url> \n" +
                    "<p1> [ <p2> ... <pN> ]");
            System.exit(1);
        }
        
        String cvsAccountFile = args[0];
        String courseName=args[1];
        String semester=args[2];
        String dbUrl=args[3];
        
        if (DEBUG) {
            System.err.println("cvsAccountsFile: " +cvsAccountFile);
        }
            
        List<String> cvsAccountList = new ArrayList<String>();
        
        TextFileReader reader = new TextFileReader(cvsAccountFile);
        for (String line : reader) {
            // TODO move comment skipping functionality into a subclass of TextFileReader
            line=line.replaceAll("#.*", "");
            if (line.equals("")) continue;
            cvsAccountList.add(line);
        }

        Connection conn=null;
        try {
            conn=DatabaseUtilities.getConnection(dbUrl);

		    for (String cvsAccount : cvsAccountList) {
                
		        for (int ii=4; ii < args.length; ii++)
		        {
		            String projectNumber = args[ii];
		            System.err.println(cvsAccount +" " +projectNumber);
		            computeDeltas(semester, courseName, projectNumber, cvsAccount, conn);
    		    }
    		}
       } finally {
            DatabaseUtilities.releaseConnection(conn);
        }
        System.out.println("Done; skipped " +couldNotRunSkipped);
    }
    
    public static void computeDeltas(
        String semester,
        String courseName,
        String projectNumber,
        String cvsAccount,
        Connection conn)
    throws SQLException, IOException
    {
        System.out.println("looking up project " +courseName+ ", " +projectNumber+ ", " +semester);
        
        Project project = Project.lookupByCourseProjectSemester(
            courseName,
            projectNumber,
            semester,
            conn);
        
        System.out.println("looking up registration: " +cvsAccount);
        
        StudentRegistration registration = StudentRegistration.lookupByCvsAccountAndCoursePK(
                cvsAccount,
                project.getCoursePK(),
                conn);
        if (registration==null) {
            System.out.println("No registration for " +cvsAccount+ " for " + courseName+ " in " +semester);
            return;
        }
        
        List<Snapshot> snapshotList = new LinkedList<Snapshot>();
        Map<String,TestRun> testRunMap = new HashMap<String,TestRun>();
        
        Snapshot.lookupSnapshotsAndTestRunsByProjectPKAndStudentRegistrationPK(
            snapshotList,
            testRunMap,
            project.getProjectPK(),
            registration.getStudentRegistrationPK(),
            conn);

        System.out.println("numSnapshots: " +snapshotList.size());
        
        Map<String,Snapshot> visitedClassfileMap = new HashMap<String,Snapshot>();
        
        // We need at least 1 snapshot to continue
        if (snapshotList.size() < 1)
            return;

        // First set the raw commit timestamps (useful for computing work sessions)
        // and raw previous snapshot pointers
        for (int ii=1; ii < snapshotList.size(); ii++) {
            Snapshot current=snapshotList.get(ii);
            Snapshot previous=snapshotList.get(ii-1);
            current.setTimeSinceLastCommit(convertToHours(current.getCommitTimestamp().getTime()-
                    previous.getCommitTimestamp().getTime()));
            current.setPreviousSubmissionPK(previous.getSubmissionPK());
            current.update(conn);
        }
        
        
        // Track the previous compilable snapshot for each snapshot so we can put a pointer to it
        Snapshot previousCompilable=null;
        for (int ii=1; ii < snapshotList.size(); ii++) {
            Snapshot current=snapshotList.get(ii);

            // skip submissions that don't compile
            // also skip pathological submissions that are stuck pending or marked broken 
            // (these submissions are pathologically broken and cannot be easily tested)
            // there should be very few or zero of these
            if (!current.isCompileSuccessful() ||
                    !current.getBuildStatus().equals(Submission.COMPLETE))
            {
                continue;
            }
            
            // At this point, we're guaranteed that current is complete and compilable
            
            // Get our current md5sum classfile from the map
            
            String currentClassfileMd5sum=testRunMap.get(current.getSubmissionPK()).getMd5sumClassfiles();
            if (currentClassfileMd5sum==null) {
                // We created this map with ALL of the md5sums in it; the get() method
                // should NEVER return null!
                throw new RuntimeException(cvsAccount +"\t"+ projectNumber+"\t"+courseName+"\t"+
                    semester+"\t: cannot find the md5sum classfile!");
            }
                    
            // If we've seen this md5sum classfile before, then skip it
            if (visitedClassfileMap.containsKey(currentClassfileMd5sum)) {
                System.out.println("Duplicate!");
                current.setPreviousMd5sumClassfiles(visitedClassfileMap.get(currentClassfileMd5sum).getSubmissionPK());
                current.update(conn);
                continue;
            }
            visitedClassfileMap.put(currentClassfileMd5sum,current);
            
            // At this point, we have a compilable, unique snapshot!
            // Make sure that we could run the tests...
                        
            // get current test outcomes
            TestOutcomeCollection currentOutcomeCollection = TestOutcomeCollection.lookupByTestRunPK(
                    current.getCurrentTestRunPK(),
                    conn);

            // Skip if we could not run any set of tests.  This should not happen,
            // except maybe with student tests, which we shouldn't care about
            if (currentOutcomeCollection.countOutcomes(TestOutcome.COULD_NOT_RUN) > 0)
            {
                couldNotRunSkipped++;
                continue;
            }
            
            // If we don't have a previous compilable snapshot, then it's this one
            // and continue
            if (previousCompilable==null) {
                previousCompilable=current;
                continue;
            }
            
            // at this point we have a valid, non-null current and previous submission
            current.setPreviousSubmissionPK(previousCompilable.getSubmissionPK());
            
            // XXX Set the time since the last compilable commit
            current.setTimeSinceLastCompilableCommit(
                convertToHours(current.getCommitTimestamp().getTime()-
                    previousCompilable.getCommitTimestamp().getTime()));
            
            
            // XXX testDelta / findBugsDelta
            if (COMPUTE_COUNT_DELTAS)
            {
                // Get the previous collection of testOutcomes
                TestOutcomeCollection previousOutcomeCollection = TestOutcomeCollection.lookupByTestRunPK(
                    previousCompilable.getCurrentTestRunPK(),
                    conn);
                computeTestAndFindBugsDeltas(
                    previousCompilable,
                    current,
                    previousOutcomeCollection,
                    currentOutcomeCollection);
                
                //System.out.println("updated submission deltas for submissionPK " +current.getSubmissionPK());
            }
            
            //XXX compute the diffs 
            if (DIFF_WITH_PREVIOUS) {
                current.diffWithPrevious(previousCompilable, conn);
            }
            
            current.update(conn);
            
            previousCompilable = current;
            System.out.println("Processed " +(totalProcessed++)+ " at submission " +current.getSubmissionPK());
        }
    }
    
    private static int MILLIS_PER_HOUR=1000*60*60;
    private static int MILLIS_PER_MIN=1000*60;
    
    private static NumberFormat format=new DecimalFormat("00");
    
    public static String convertToHours(long time)
    {
        int hours=(int)time / MILLIS_PER_HOUR;
        time -= hours * MILLIS_PER_HOUR;
        int minutes=(int)time / MILLIS_PER_MIN;
        time -= minutes * MILLIS_PER_MIN;
        int seconds=(int)time / 1000;
        
        return hours +":"+ format.format(minutes) +":"+ format.format(seconds);
    }
    
//    /**
//     * @param currentOutcomeCollection
//     */
//    private static void postProcessExceptionSource(
//            Snapshot submission, 
//            TestOutcomeCollection outcomeCollection,
//            PrintWriter out,
//            Connection conn)
//    throws SQLException
//    {
//        for (Iterator ii=outcomeCollection.cardinalTestTypesIterator(); ii.hasNext();) {
//            TestOutcome outcome = (TestOutcome)ii.next();
//            // redundant
//            if (! (outcome.getTestType().equals(TestOutcome.PUBLIC_TEST) ||
//                    outcome.getTestType().equals(TestOutcome.RELEASE_TEST) ||
//                    outcome.getTestType().equals(TestOutcome.SECRET_TEST)))
//                continue;
//            if (outcome.getOutcome().equals(TestOutcome.PASSED))
//                continue;
//            
//            // security exceptions, timeouts, unimplemented methods,
//            // and normal failures (due to AssertionFailedError) are *NOT* interesting
//            if (outcome.getOutcome().equals(TestOutcome.FAILED) ||
//                    outcome.getOutcome().equals(TestOutcome.HUH) ||
//                    outcome.getOutcome().equals(TestOutcome.TIMEOUT) ||
//                    outcome.getOutcome().equals(TestOutcome.NOT_IMPLEMENTED))
//                continue;
//            
//            // at this point, we have a test that failed due to an error
//            if (outcome.isExceptionSourceInTestDriver()) {
//                out.println("source is in the driver for submissionPK: " +
//                        submission.getSubmissionPK() + " testRunPK: "+
//                        outcome.getTestRunPK() +", "+
//                        outcome.getTestName() +", "+
//                        outcome.getTestNumber() +", "+
//                        outcome.getLongTrimmedTestResult());
//                out.flush();
//                outcome.setOutcome(TestOutcome.FAILED);
//                outcome.update(conn);
//            }
//        }
//        
//    }

    
    private static void computeTestAndFindBugsDeltas(
        Snapshot previous,
        Snapshot current,
        TestOutcomeCollection previousOutcomeCollection,
        TestOutcomeCollection currentOutcomeCollection)
    {
        // compute:
        // test delta
        // FB delta
        // fault delta
        // md5sum duplicates for source/classfiles
        
        // compute delta of test cases passed: 
        // +N means N more test cases passed; 
        // -N means N fewer test cases passed (or N more test cases failed)
        current.setTestDelta(new Integer(Math.max(current.getValuePassedOverall(), 0) - 
                Math.max(previous.getValuePassedOverall(), 0)));
        // compute delta of findbugs
        // +N means N more findbugs warnings issued
        // -N means N fewer findbugs warnings issued
        current.setFindbugsDelta(new Integer(current.getNumFindBugsWarnings() - 
                previous.getNumFindBugsWarnings()));
         
        // figure out faults delta
        // +N means N more faults
        // -N means N fewer faults (absence of a fault does not necessarily mean that
        // the test case failed; it's possible that the necessary code has not been
        // implemented)
        int deltaFaults = computeDeltaFaults(currentOutcomeCollection, previousOutcomeCollection);
        current.setFaultsDelta(new Integer(deltaFaults));
        
        Pair faults = computeNewFaults(currentOutcomeCollection, previousOutcomeCollection);
        Integer totalFaults= new Integer(currentOutcomeCollection.countFaults());
//        System.out.println("SubmissionPK " +current.getSubmissionPK()+
//                " has " +totalFaults+ " total faults, " +faults.newFaults+
//                " new faults and " +faults.removedFaults+
//                " removed faults.");
                
        // total faults
        current.setTotalFaults(totalFaults);
        // compute new and removed faults
        current.setNewFaults(faults.newFaults);
        current.setRemovedFaults(faults.removedFaults);
    }
    
    private static class Pair
    {
        final Integer newFaults, removedFaults;
        public Pair(Integer newFaults, Integer removedFaults)
        {
            this.newFaults = newFaults;
            this.removedFaults = removedFaults;
        }
    }
    
    /**
     * @param currentOutcomeCollection
     * @param previousOutcomeCollection
     * @return
     */
    private static Pair computeNewFaults(TestOutcomeCollection currentOutcomeCollection, TestOutcomeCollection previousOutcomeCollection)
    {
        int newFaults=0;
        int removedFaults=0;
        //Map currentMap = new HashMap();
        Map<String,TestOutcome> previousMap = new HashMap<String,TestOutcome>();
        
        for (Iterator jj=previousOutcomeCollection.cardinalTestTypesIterator(); jj.hasNext();)
        {
            TestOutcome previousOutcome = (TestOutcome)jj.next();
            // NOTE: I'm assuming here that no two PUBLIC and RELEASE tests have the same name!
            previousMap.put(previousOutcome.getTestName(), previousOutcome);
        }
        
        for (Iterator ii=currentOutcomeCollection.cardinalTestTypesIterator(); ii.hasNext();)
        {
            TestOutcome current = (TestOutcome)ii.next();
            TestOutcome previous = (TestOutcome)previousMap.get(current.getTestName());
            // they are the same, no change
            if (current.getOutcome().equals(previous.getOutcome()))
                continue;
            // they are both faults, no change
            if (current.isFault() && previous.isFault())
                continue;
            if (!current.isFault() &&
                    previous.isFault()) {
                // current PASSED, previous was a FAULT
                removedFaults++;
            } else if (current.isFault() && !previous.isFault()) {
                // current FAULT, previus must have been passed or not implemented
                newFaults++;
            }
        }
        return new Pair(new Integer(newFaults), new Integer(removedFaults));
    }

    private static int computeDeltaFaults(TestOutcomeCollection currentOutcomeCollection,
            TestOutcomeCollection previousOutcomeCollection)
    {
        int currentFaults = currentOutcomeCollection.countFaults();
        int previousFaults = previousOutcomeCollection.countFaults();
        
        return currentFaults - previousFaults;
    }
    
    /**
     * TODO Use the BugHistory code for this
     * TODO Modify the submissions table so that we can track snapshots that both add
     * AND remove warnings
     * @param current
     * @param previous
     * @param currentCollection
     * @param previousCollection
     * @param allBugs
     */
    private static void compareFindBugsWarningSets(Snapshot current, Snapshot previous,
            TestOutcomeCollection currentCollection, TestOutcomeCollection previousCollection,
            SortedBugCollection allBugs)
    {
        System.out.println("previous: " +previous.getSubmissionPK() +", current: " +current.getSubmissionPK());
        SortedBugCollection currentBugCollection=new SortedBugCollection();
        for (Iterator ii=currentCollection.findBugsIterator(); ii.hasNext();) {
            TestOutcome outcome = (TestOutcome)ii.next();
            BugInstance bug = (BugInstance)outcome.getDetails();
            if (bug != null)
                currentBugCollection.add(bug);
        }
        
        SortedBugCollection previousBugCollection=new SortedBugCollection();
        for (Iterator ii=previousCollection.findBugsIterator();
        	ii.hasNext();)
        {
            TestOutcome outcome = (TestOutcome)ii.next();
            if (!(outcome.getDetails() instanceof edu.umd.cs.findbugs.BugInstance))
                System.err.println(outcome.getDetails().getClass());
            BugInstance bug = (BugInstance)outcome.getDetails();
            if (bug != null)
                previousBugCollection.add(bug);
        }
        
        // TODO what to do with the bug information?
        SortedBugCollection result = null;
        //BugHistory bugHistory = new BugHistory(currentBugCollection, previousBugCollection);

        //result = bugHistory.performSetOperation(BugHistory.ADDED_WARNINGS);
        
        int size=0;
        for (Iterator ii=result.iterator(); ii.hasNext(); size++) {
            BugInstance bug = (BugInstance)ii.next();
            
            // only add the new warnings
            allBugs.add(bug);
            BugPattern pattern = bug.getBugPattern();
            if (pattern != null)
                System.err.println("Bug Pattern: " +pattern);
            System.out.println(bug.toString() +" unique id: "+ bug.getUniqueId());
        }
        
        if (size > 0)
            System.out.println("submission " +current.getSubmissionPK()+ " has " +size+ " bug warnings");
    }
}
